package com.example.fuji.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_category_mappings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_course_category", columnNames = {"course_id", "category_id"})
})
@Data
public class CourseCategoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_category_mapping_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_category_mapping_category",
            foreignKeyDefinition = "FOREIGN KEY (category_id) REFERENCES course_categories(id) ON DELETE CASCADE"
        )
    )
    private CourseCategory category;
}
