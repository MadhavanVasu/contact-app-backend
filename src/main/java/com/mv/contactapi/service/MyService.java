package com.mv.contactapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MyService {

    @Autowired
    StorageService storageService;

    public String uploadPhoto(String id, MultipartFile file) {
        return storageService.uploadFile(file, id);
    }

    public byte[] downloadPhoto(String id) {
        return storageService.downloadFile(id);
    }

    public String deletePhoto(String id) {
        return storageService.deleteFile(id);
    }

}
