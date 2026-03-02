package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/enquiry-attachments/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB

    @Value("${app.upload.dir:uploads}")
    private String baseUploadDir;

    public String storeFile(MultipartFile file, Long enquiryId, Long conversationId) throws IOException {
        validateFile(file);

        String uploadPath = createUploadPath(enquiryId, conversationId);
        createDirectoriesIfNotExist(uploadPath);

        String storedFilename = generateUniqueFilename(file.getOriginalFilename());
        Path targetLocation = Paths.get(uploadPath).resolve(storedFilename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uploadPath + "/" + storedFilename;
    }

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        validateFile(file);

        String uploadPath = baseUploadDir + "/" + subDirectory;
        createDirectoriesIfNotExist(uploadPath);

        String storedFilename = generateUniqueFilename(file.getOriginalFilename());
        Path targetLocation = Paths.get(uploadPath).resolve(storedFilename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uploadPath + "/" + storedFilename;
    }

    public byte[] loadFileAsBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    public boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE;
    }

    public String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    public static String formatFileSize(long fileSize) {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 5MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IOException("File name is invalid");
        }

        // Check for potentially dangerous file extensions
        String extension = getFileExtension(filename).toLowerCase();
        if (extension.equals(".exe") || extension.equals(".bat") || extension.equals(".sh") ||
            extension.equals(".cmd") || extension.equals(".scr")) {
            throw new IOException("File type not allowed for security reasons");
        }
    }

    private String createUploadPath(Long enquiryId, Long conversationId) {
        return baseUploadDir + "/" + UPLOAD_DIR + enquiryId + "/" + conversationId;
    }

    private void createDirectoriesIfNotExist(String uploadPath) throws IOException {
        Path path = Paths.get(uploadPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);
        String baseName = originalFilename.substring(0,
            originalFilename.length() - extension.length()).replaceAll("[^a-zA-Z0-9.-]", "_");

        return timestamp + "_" + uuid + "_" + baseName + extension;
    }
}
