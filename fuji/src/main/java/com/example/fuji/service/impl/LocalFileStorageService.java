package com.example.fuji.service.impl;
import com.example.fuji.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path root = Paths.get("uploads");

    @Override
    public String save(MultipartFile file) {

        try {

            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            String filename = UUID.randomUUID() 
                    + "_" + file.getOriginalFilename();

            Path destination = root.resolve(filename);

            Files.copy(file.getInputStream(), destination,
                    StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
