package com.aiposter.storage;

public class StoredFile {
    private final String filename;
    private final String storagePath;
    private final String accessUrl;

    public StoredFile(String filename, String storagePath, String accessUrl) {
        this.filename = filename;
        this.storagePath = storagePath;
        this.accessUrl = accessUrl;
    }

    public String getFilename() {
        return filename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getAccessUrl() {
        return accessUrl;
    }
}
