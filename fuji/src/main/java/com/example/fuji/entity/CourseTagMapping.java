package com.example.fuji.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_tag_mappings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_course_tag", columnNames = {"course_id", "tag_id"})
})
@Data
public class CourseTagMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_tag_mapping_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "tag_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_tag_mapping_tag",
            foreignKeyDefinition = "FOREIGN KEY (tag_id) REFERENCES course_tags(id) ON DELETE CASCADE"
        )
    )
    private CourseTag tag;
}
