package com.c1se_01.roomiego.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

  @TempDir
  Path tempDir;

  @Mock
  private MultipartFile multipartFile;

  private FileStorageService fileStorageService;

  @BeforeEach
  void setUp() throws Exception {
    fileStorageService = new FileStorageService();

    // Use reflection to set the uploadDir and fileStoragePath
    Field uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
    uploadDirField.setAccessible(true);
    uploadDirField.set(fileStorageService, tempDir.toString());

    Field fileStoragePathField = FileStorageService.class.getDeclaredField("fileStoragePath");
    fileStoragePathField.setAccessible(true);
    fileStoragePathField.set(fileStorageService, tempDir);

    // Call init to create directories
    fileStorageService.getClass().getMethod("init").invoke(fileStorageService);
  }

  @Test
  void storeFile_SuccessfulStore() throws IOException {
    // Arrange
    String originalFilename = "test.jpg";
    byte[] fileContent = "test content".getBytes();
    when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
    when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

    // Act
    String result = fileStorageService.storeFile(multipartFile);

    // Assert
    assertNotNull(result);
    assertTrue(result.endsWith("_" + originalFilename));
    assertTrue(result.startsWith("")); // UUID part
    Path storedFile = tempDir.resolve(result);
    assertTrue(Files.exists(storedFile));
    assertArrayEquals(fileContent, Files.readAllBytes(storedFile));
  }

  @Test
  void storeFile_FilenameContainsInvalidPathSequence() {
    // Arrange
    String invalidFilename = "../test.jpg";
    when(multipartFile.getOriginalFilename()).thenReturn(invalidFilename);

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      fileStorageService.storeFile(multipartFile);
    });
    assertTrue(exception.getMessage().contains("Sorry! Filename contains invalid path sequence"));
  }

  @Test
  void storeFile_IOExceptionDuringCopy() throws IOException {
    // Arrange
    String originalFilename = "test.jpg";
    when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
    when(multipartFile.getInputStream()).thenThrow(new IOException("IO Error"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      fileStorageService.storeFile(multipartFile);
    });
    assertTrue(exception.getMessage().contains("Could not store file"));
    assertTrue(exception.getMessage().contains("test.jpg"));
  }
}