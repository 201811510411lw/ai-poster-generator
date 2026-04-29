package com.aiposter.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file, String folder);

    void delete(String storagePath);
}
