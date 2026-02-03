package com.example.shop._common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    String save(MultipartFile file);
}
