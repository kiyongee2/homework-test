package com.sudo.blog.service;

import com.sudo.blog.dto.UploadResponse;

public interface FileStorageService {
    UploadResponse store(byte[] bytes, String filename);
}
