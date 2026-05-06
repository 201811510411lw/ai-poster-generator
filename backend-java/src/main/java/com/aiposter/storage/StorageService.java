package com.aiposter.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file, String folder);

    StoredFile store(byte[] content, String originalFilename, String contentType, String folder);

    void delete(String storagePath);
}
