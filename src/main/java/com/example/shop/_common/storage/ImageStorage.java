package com.example.shop._common.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

//사진파일 로컬저장
@Component
public class ImageStorage implements FileStorage {
    private final String uploadDir = "/uploads/products/";

    public String save(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return path.toString();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 저장 실패");
        }
    }
}
