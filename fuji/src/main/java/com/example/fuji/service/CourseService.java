package com.example.fuji.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.request.CourseDTO;
import com.example.fuji.entity.Course;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public Page<CourseDTO> getAllCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> getPublishedCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.findByIsPublishedTrue(pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> getCoursesByInstructor(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CourseDTO> searchCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = courseRepository.searchByTitle(keyword, pageable);

        return courses.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khóa học không tồn tại với id: " + id));
        return convertToDTO(course);
    }

    private CourseDTO convertToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorId(course.getInstructor().getId())
                .instructorName(course.getInstructor().getFullName())
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
