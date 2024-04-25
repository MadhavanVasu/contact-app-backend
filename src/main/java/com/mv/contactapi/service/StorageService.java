package com.mv.contactapi.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    AmazonS3 s3Client;

    private final Function<MultipartFile, File> convertMultipartToFile = (multiPartFile) -> {
        File convertedFile = new File(multiPartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(multiPartFile.getBytes());
        } catch (IOException e) {
            log.info("Error converting multipart file to to file", e);
        }
        return convertedFile;
    };

    public String uploadFile(MultipartFile file, String id) {
        File fileObj = convertMultipartToFile.apply(file);
        s3Client.putObject(new PutObjectRequest(bucketName, id, fileObj));
        fileObj.delete();
        return "File uploaded";
    }

    public byte[] downloadFile(String id) {
        S3Object object = s3Client.getObject(bucketName, id);
        S3ObjectInputStream inputStream = object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFile(String id) {
        s3Client.deleteObject(bucketName, id);
        return "File deleted";
    }


}
