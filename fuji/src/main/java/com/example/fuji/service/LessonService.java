package com.example.fuji.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.LessonRequestDTO;
import com.example.fuji.dto.request.LessonUpdateDTO;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.response.LessonResponseDTO;
import com.example.fuji.entity.Course;
import com.example.fuji.entity.Lesson;
import com.example.fuji.enums.LessonType;
import com.example.fuji.enums.VideoType;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CourseRepository;
import com.example.fuji.repository.LessonRepository;
import com.example.fuji.repository.UserLessonCompletionRepository;
import com.example.fuji.utils.AuthUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserLessonCompletionRepository userLessonCompletionRepository;
    private final MediaService mediaService;
    private final AuthUtils authUtils;

    // ==================== API 7: createLesson ====================

    @Transactional
    public LessonResponseDTO createLesson(LessonRequestDTO dto, MultipartFile videoFile) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khóa học không tồn tại với id: " + dto.getCourseId()));

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(dto.getTitle());
        lesson.setLessonType(dto.getLessonType());
        lesson.setContent(dto.getContent());

        // Set lesson order (auto-increment if not provided)
        if (dto.getLessonOrder() != null) {
            lesson.setLessonOrder(dto.getLessonOrder());
        } else {
            int maxOrder = lessonRepository.findMaxLessonOrderByCourseId(dto.getCourseId());
            lesson.setLessonOrder(maxOrder + 1);
        }

        // Handle video type lessons
        if (dto.getLessonType() == LessonType.video) {
            handleVideoLesson(lesson, dto, videoFile);
        }

        // Handle task type lessons
        if (dto.getLessonType() == LessonType.task) {
            if (dto.getTaskType() == null) {
                throw new BadRequestException("Task type không được để trống cho bài học dạng task");
            }
            lesson.setTaskType(dto.getTaskType());
            lesson.setTaskData(dto.getTaskData());
        }

        Lesson savedLesson = lessonRepository.save(lesson);

        // Update lesson count in course
        int lessonCount = lessonRepository.countByCourseId(dto.getCourseId());
        course.setLessonCount(lessonCount);
        courseRepository.save(course);

        return convertToDTO(savedLesson, false);
    }

    // ==================== API 8: getLessonById ====================

    @Transactional(readOnly = true)
    public LessonResponseDTO getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bài học không tồn tại với id: " + lessonId));

        boolean userCompleted = false;
        try {
            Long userId = authUtils.getCurrentUserId();
            userCompleted = userLessonCompletionRepository.existsByUserIdAndLessonId(userId, lessonId);
        } catch (Exception e) {
            // User not authenticated, skip completion check
        }

        return convertToDTO(lesson, userCompleted);
    }

    // ==================== API 9: updateLesson ====================

    @Transactional
    public LessonResponseDTO updateLesson(Long lessonId, LessonUpdateDTO dto, MultipartFile videoFile) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bài học không tồn tại với id: " + lessonId));

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            lesson.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            lesson.setContent(dto.getContent());
        }
        if (dto.getLessonOrder() != null) {
            lesson.setLessonOrder(dto.getLessonOrder());
        }

        // Update lesson type if changed
        if (dto.getLessonType() != null) {
            lesson.setLessonType(dto.getLessonType());

            if (dto.getLessonType() == LessonType.video) {
                handleVideoLessonUpdate(lesson, dto, videoFile);
                // Clear task fields
                lesson.setTaskType(null);
                lesson.setTaskData(null);
            } else if (dto.getLessonType() == LessonType.task) {
                // Clear video fields
                lesson.setVideoUrl(null);
                lesson.setVideoType(null);
                lesson.setDuration(0);
            }
        }

        // Update task fields if lesson is task type
        if (lesson.getLessonType() == LessonType.task) {
            if (dto.getTaskType() != null) {
                lesson.setTaskType(dto.getTaskType());
            }
            if (dto.getTaskData() != null) {
                lesson.setTaskData(dto.getTaskData());
            }
        }

        // Update video fields if lesson is video type
        if (lesson.getLessonType() == LessonType.video) {
            handleVideoLessonUpdate(lesson, dto, videoFile);
        }

        if (dto.getDuration() != null) {
            lesson.setDuration(dto.getDuration());
        }

        Lesson updatedLesson = lessonRepository.save(lesson);
        return convertToDTO(updatedLesson, false);
    }

    // ==================== API 10: deleteLesson ====================

    @Transactional
    public void deleteLesson(Long lessonId, Long courseId) {
        Lesson lesson = lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bài học không tồn tại với id: " + lessonId + " trong khóa học id: " + courseId));

        // Delete video from Cloudinary if uploaded
        if (lesson.getVideoUrl() != null && lesson.getVideoUrl().contains("cloudinary")) {
            try {
                String publicId = extractPublicId(lesson.getVideoUrl());
                mediaService.deleteMedia(publicId, "video");
            } catch (Exception e) {
                log.warn("Không thể xóa video từ Cloudinary: {}", e.getMessage());
            }
        }

        lessonRepository.delete(lesson);

        // Update lesson count in course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khóa học không tồn tại với id: " + courseId));
        int lessonCount = lessonRepository.countByCourseId(courseId);
        course.setLessonCount(lessonCount);
        courseRepository.save(course);
    }

    // ==================== Get all lessons by course ====================

    @Transactional(readOnly = true)
    public List<LessonResponseDTO> getLessonsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Khóa học không tồn tại với id: " + courseId);
        }

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrderAsc(courseId);

        Long userId = null;
        try {
            userId = authUtils.getCurrentUserId();
        } catch (Exception e) {
            // Not authenticated
        }

        final Long finalUserId = userId;
        return lessons.stream()
                .map(lesson -> {
                    boolean completed = false;
                    if (finalUserId != null) {
                        completed = userLessonCompletionRepository
                                .existsByUserIdAndLessonId(finalUserId, lesson.getId());
                    }
                    return convertToDTO(lesson, completed);
                })
                .collect(Collectors.toList());
    }

    // ==================== HELPERS ====================

    private void handleVideoLesson(Lesson lesson, LessonRequestDTO dto, MultipartFile videoFile) {
        if (videoFile != null && !videoFile.isEmpty()) {
            // Upload video to Cloudinary
            try {
                MediaDTO uploadResult = mediaService.uploadVideo(videoFile);
                lesson.setVideoUrl(uploadResult.getUrl());
                lesson.setVideoType(VideoType.upload);
            } catch (IOException e) {
                throw new RuntimeException("Upload video thất bại: " + e.getMessage(), e);
            }
        } else if (dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            lesson.setVideoUrl(dto.getVideoUrl());
            lesson.setVideoType(dto.getVideoType() != null ? dto.getVideoType() : VideoType.youtube);
        }

        if (dto.getDuration() != null) {
            lesson.setDuration(dto.getDuration());
        }
    }

    private void handleVideoLessonUpdate(Lesson lesson, LessonUpdateDTO dto, MultipartFile videoFile) {
        if (videoFile != null && !videoFile.isEmpty()) {
            // Delete old video from Cloudinary if exists
            if (lesson.getVideoUrl() != null && lesson.getVideoUrl().contains("cloudinary")) {
                try {
                    String publicId = extractPublicId(lesson.getVideoUrl());
                    mediaService.deleteMedia(publicId, "video");
                } catch (Exception e) {
                    log.warn("Không thể xóa video cũ từ Cloudinary: {}", e.getMessage());
                }
            }
            try {
                MediaDTO uploadResult = mediaService.uploadVideo(videoFile);
                lesson.setVideoUrl(uploadResult.getUrl());
                lesson.setVideoType(VideoType.upload);
            } catch (IOException e) {
                throw new RuntimeException("Upload video thất bại: " + e.getMessage(), e);
            }
        } else if (dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            lesson.setVideoUrl(dto.getVideoUrl());
            lesson.setVideoType(dto.getVideoType() != null ? dto.getVideoType() : VideoType.youtube);
        }
    }

    private String extractPublicId(String url) {
        String[] parts = url.split("/upload/");
        if (parts.length > 1) {
            String path = parts[1];
            if (path.matches("v\\d+/.*")) {
                path = path.substring(path.indexOf('/') + 1);
            }
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0) {
                path = path.substring(0, dotIndex);
            }
            return path;
        }
        return url;
    }

    private LessonResponseDTO convertToDTO(Lesson lesson, boolean userCompleted) {
        return LessonResponseDTO.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourse().getId())
                .courseTitle(lesson.getCourse().getTitle())
                .title(lesson.getTitle())
                .lessonOrder(lesson.getLessonOrder())
                .lessonType(lesson.getLessonType())
                .videoUrl(lesson.getVideoUrl())
                .videoType(lesson.getVideoType())
                .duration(lesson.getDuration())
                .taskType(lesson.getTaskType())
                .taskData(lesson.getTaskData())
                .content(lesson.getContent())
                .completionCount(lesson.getCompletionCount())
                .userCompleted(userCompleted)
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
