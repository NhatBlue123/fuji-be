package com.example.fuji.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.CourseRequestDTO;
import com.example.fuji.dto.request.CourseUpdateDTO;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.request.RatingRequestDTO;
import com.example.fuji.dto.response.CourseResponseDTO;
import com.example.fuji.dto.response.RatingResponseDTO;
import com.example.fuji.dto.response.UserSummaryDTO;
import com.example.fuji.entity.Course;
import com.example.fuji.entity.CourseRating;
import com.example.fuji.entity.Lesson;
import com.example.fuji.entity.User;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CourseRatingRepository;
import com.example.fuji.repository.CourseRepository;
import com.example.fuji.repository.LessonRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.utils.AuthUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseRatingRepository courseRatingRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final AuthUtils authUtils;

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getAllCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.findAllWithUsers(pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getPublishedCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.findByIsPublishedTrueWithUsers(pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getCoursesByInstructor(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.findByInstructorIdWithUsers(instructorId, pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> searchCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.searchByTitleWithUsers(keyword, pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(Long id) {
        Course course = courseRepository.findByIdWithUsers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khóa học không tồn tại với id: " + id));
        return convertToDTO(course);
    }

    @Transactional
    public CourseResponseDTO createCourse(CourseRequestDTO courseDTO, MultipartFile thumbnail) {
        // Get current logged-in user as creator (tương tự req.user trong Node.js)
        User creator = authUtils.getCurrentUser();

        // Validate instructor exists
        User instructor = userRepository.findById(courseDTO.getInstructorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Giảng viên không tồn tại với id: " + courseDTO.getInstructorId()));

        // Upload thumbnail if provided
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                MediaDTO uploadResult = mediaService.uploadImage(thumbnail);
                thumbnailUrl = uploadResult.getUrl();
            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thumbnail thất bại: " + e.getMessage(), e);
            }
        }

        // Create and configure course entity
        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setPrice(courseDTO.getPrice());
        course.setInstructor(instructor);
        course.setCreatedBy(creator);
        course.setThumbnailUrl(thumbnailUrl);
        course.setIsPublished(courseDTO.getIsPublished() != null ? courseDTO.getIsPublished() : false);
        course.setStudentCount(0);
        course.setLessonCount(0);
        course.setTotalDuration(0);
        course.setAverageRating(BigDecimal.ZERO);
        course.setRatingCount(0);

        // Save to database
        Course savedCourse = courseRepository.save(course);

        // Convert to DTO and return
        return convertToDTO(savedCourse);
    }

    @Transactional
    public CourseResponseDTO updateCourse(Long id, CourseUpdateDTO updates, MultipartFile thumbnail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khóa học không tồn tại với id: " + id));

        // Update fields if present (PATCH logic)
        if (updates.getTitle() != null && !updates.getTitle().isBlank()) {
            course.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null && !updates.getDescription().isBlank()) {
            course.setDescription(updates.getDescription());
        }
        if (updates.getPrice() != null) {
            course.setPrice(updates.getPrice());
        }
        if (updates.getIsPublished() != null) {
            course.setIsPublished(updates.getIsPublished());
        }
        if (updates.getInstructorId() != null) {
            User newInstructor = userRepository.findById(updates.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Giảng viên không tồn tại với id: " + updates.getInstructorId()));
            course.setInstructor(newInstructor);
        }

        // Upload new thumbnail if provided
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                MediaDTO uploadResult = mediaService.uploadImage(thumbnail);
                course.setThumbnailUrl(uploadResult.getUrl());
            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thumbnail thất bại: " + e.getMessage(), e);
            }
        }

        Course updatedCourse = courseRepository.save(course);
        return convertToDTO(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khóa học không tồn tại với id: " + id));

        // Cascade delete: xóa tất cả lessons của course
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonOrderAsc(id);
        if (!lessons.isEmpty()) {
            // Delete media files from Cloudinary for video lessons
            for (Lesson lesson : lessons) {
                if (lesson.getVideoUrl() != null && lesson.getVideoUrl().contains("cloudinary")) {
                    try {
                        String publicId = extractPublicId(lesson.getVideoUrl());
                        mediaService.deleteMedia(publicId, "video");
                    } catch (Exception e) {
                        log.warn("Không thể xóa video từ Cloudinary: {}", e.getMessage());
                    }
                }
            }
            lessonRepository.deleteByCourseId(id);
        }

        // Delete thumbnail from Cloudinary
        if (course.getThumbnailUrl() != null && course.getThumbnailUrl().contains("cloudinary")) {
            try {
                String publicId = extractPublicId(course.getThumbnailUrl());
                mediaService.deleteMedia(publicId, "image");
            } catch (Exception e) {
                log.warn("Không thể xóa thumbnail từ Cloudinary: {}", e.getMessage());
            }
        }

        courseRepository.deleteById(id);
    }

    // ==================== RATING ====================

    @Transactional
    public RatingResponseDTO rateCourse(Long courseId, RatingRequestDTO ratingDTO) {
        User currentUser = authUtils.getCurrentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Khóa học không tồn tại với id: " + courseId));

        // Check if user already rated → update instead of create
        CourseRating rating = courseRatingRepository
                .findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElse(null);

        if (rating != null) {
            rating.setRating(ratingDTO.getRating());
        } else {
            rating = new CourseRating();
            rating.setUser(currentUser);
            rating.setCourse(course);
            rating.setRating(ratingDTO.getRating());
        }

        courseRatingRepository.save(rating);

        // Recalculate average rating
        Double avgRating = courseRatingRepository.getAverageRatingByCourseId(courseId);
        int ratingCount = courseRatingRepository.countByCourseId(courseId);

        course.setAverageRating(avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        course.setRatingCount(ratingCount);
        courseRepository.save(course);

        return convertToRatingDTO(rating);
    }

    @Transactional(readOnly = true)
    public List<RatingResponseDTO> getCourseRatings(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Khóa học không tồn tại với id: " + courseId);
        }
        return courseRatingRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::convertToRatingDTO)
                .collect(Collectors.toList());
    }

    // ==================== HELPERS ====================

    private String extractPublicId(String url) {
        // Extract Cloudinary public_id from URL
        // e.g. https://res.cloudinary.com/.../fuji/images/media_xxx → fuji/images/media_xxx
        String[] parts = url.split("/upload/");
        if (parts.length > 1) {
            String path = parts[1];
            // Remove version prefix (v1234567890/)
            if (path.matches("v\\d+/.*")) {
                path = path.substring(path.indexOf('/') + 1);
            }
            // Remove file extension
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0) {
                path = path.substring(0, dotIndex);
            }
            return path;
        }
        return url;
    }

    private RatingResponseDTO convertToRatingDTO(CourseRating rating) {
        return RatingResponseDTO.builder()
                .id(rating.getId())
                .courseId(rating.getCourse().getId())
                .rating(rating.getRating())
                .review(rating.getReview())
                .user(UserSummaryDTO.builder()
                        .id(rating.getUser().getId())
                        .username(rating.getUser().getUsername())
                        .fullName(rating.getUser().getFullName())
                        .avatarUrl(rating.getUser().getAvatarUrl())
                        .build())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    private CourseResponseDTO convertToDTO(Course course) {
        // Build instructor summary (null-safe)
        UserSummaryDTO instructorSummary = null;
        if (course.getInstructor() != null) {
            User instructor = course.getInstructor();
            instructorSummary = UserSummaryDTO.builder()
                    .id(instructor.getId())
                    .username(instructor.getUsername())
                    .fullName(instructor.getFullName())
                    .avatarUrl(instructor.getAvatarUrl())
                    .build();
        }

        // Build author summary (null-safe)
        UserSummaryDTO authorSummary = null;
        if (course.getCreatedBy() != null) {
            User author = course.getCreatedBy();
            authorSummary = UserSummaryDTO.builder()
                    .id(author.getId())
                    .username(author.getUsername())
                    .fullName(author.getFullName())
                    .avatarUrl(author.getAvatarUrl())
                    .build();
        }

        return CourseResponseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructor(instructorSummary)
                .author(authorSummary)
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .studentCount(course.getStudentCount())
                .lessonCount(course.getLessonCount())
                .totalDuration(course.getTotalDuration())
                .averageRating(course.getAverageRating())
                .ratingCount(course.getRatingCount())
                .isPublished(course.getIsPublished())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
