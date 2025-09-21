    package com.c1se_01.roomiego.service.impl;
    import jakarta.annotation.PostConstruct;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.util.StringUtils;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.nio.file.StandardCopyOption;
    import java.util.UUID;

    @Service
    public class FileStorageService {

        @Value("${file.upload-dir:uploads/images}")
        private String uploadDir;

        private Path fileStoragePath;

        @PostConstruct
        public void init() {
            try {
                this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(this.fileStoragePath);
            } catch (Exception ex) {
                throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
            }
        }


        public String storeFile(MultipartFile file) {
            // Normalize file name
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

            // Generate unique file name to prevent duplicates
            String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

            try {
                // Check if the file's name contains invalid characters
                if (fileName.contains("..")) {
                    throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
                }

                // Copy file to the target location
                Path targetLocation = this.fileStoragePath.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // Return the relative path that will be stored in the database
                return fileName;
            } catch (IOException ex) {
                throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
            }
        }
    }