package com.example.fuji.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_prerequisites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_prerequisite_course_prereq", columnNames = {"course_id", "prerequisite_course_id"})
})
@Data
public class CoursePrerequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_prerequisites_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "prerequisite_course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_prerequisites_prereq_course",
            foreignKeyDefinition = "FOREIGN KEY (prerequisite_course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course prerequisiteCourse;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;
}
