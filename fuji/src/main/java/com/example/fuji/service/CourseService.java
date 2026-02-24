package com.example.fuji.service;

import java.io.IOException;
import java.math.BigDecimal;

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
import com.example.fuji.dto.response.CourseResponseDTO;
import com.example.fuji.dto.response.UserSummaryDTO;
import com.example.fuji.entity.Course;
import com.example.fuji.entity.User;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CourseRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final MediaService mediaService;
    private final AuthUtils authUtils;

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getAllCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getPublishedCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.findByIsPublishedTrue(pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> getCoursesByInstructor(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> searchCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.searchByTitle(keyword, pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
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
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Khóa học không tồn tại với id: " + id);
        }
        courseRepository.deleteById(id);
    }

    private CourseResponseDTO convertToDTO(Course course) {
        // Build instructor summary
        UserSummaryDTO instructorSummary = UserSummaryDTO.builder()
                .id(course.getInstructor().getId())
                .username(course.getInstructor().getUsername())
                .fullName(course.getInstructor().getFullName())
                .avatarUrl(course.getInstructor().getAvatarUrl())
                .build();

        // Build author summary
        UserSummaryDTO authorSummary = UserSummaryDTO.builder()
                .id(course.getCreatedBy().getId())
                .username(course.getCreatedBy().getUsername())
                .fullName(course.getCreatedBy().getFullName())
                .avatarUrl(course.getCreatedBy().getAvatarUrl())
                .build();

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
