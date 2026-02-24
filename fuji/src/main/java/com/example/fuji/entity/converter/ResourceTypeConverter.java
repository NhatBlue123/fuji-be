package com.example.fuji.entity.converter;

import com.example.fuji.entity.enums.ResourceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for ResourceType enum.
 * Handles case-insensitive reading from DB (e.g., "audio" → AUDIO, "AUDIO" →
 * AUDIO).
 * Always writes as uppercase string to DB.
 */
@Converter(autoApply = true)
public class ResourceTypeConverter implements AttributeConverter<ResourceType, String> {

    @Override
    public String convertToDatabaseColumn(ResourceType attribute) {
        return attribute == null ? null : attribute.name(); // Always store as UPPERCASE
    }

    @Override
    public ResourceType convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        try {
            return ResourceType.valueOf(dbData.toUpperCase()); // Case-insensitive: "audio" → AUDIO
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ResourceType in DB: '" + dbData + "'");
        }
    }
}
