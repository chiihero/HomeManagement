package com.chii.homemanagement.service.impl;

import com.chii.homemanagement.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务实现类
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LogManager.getLogger(FileStorageServiceImpl.class);
    
    // 文件存储根目录
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    // 文件访问基础URL
    @Value("${file.base-url:/uploads}")
    private String baseUrl;
    
    private Path fileStorageLocation;
    
    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            logger.info("文件存储目录初始化成功: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("无法创建文件存储目录: {}", this.fileStorageLocation);
            throw new RuntimeException("无法创建文件存储目录", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "");
    }

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 获取文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 检查文件名是否包含非法字符
        if (originalFilename.contains("..")) {
            throw new IOException("文件名包含非法路径字符: " + originalFilename);
        }
        
        // 生成唯一文件名
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);
        
        // 按日期创建子目录
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 构建存储路径
        Path targetDirectory;
        if (StringUtils.hasText(directory)) {
            targetDirectory = this.fileStorageLocation.resolve(directory).resolve(dateDir);
        } else {
            targetDirectory = this.fileStorageLocation.resolve(dateDir);
        }
        
        // 确保目标目录存在
        Files.createDirectories(targetDirectory);
        
        // 存储文件
        Path targetPath = targetDirectory.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 构建文件访问URL
        String filePath;
        if (StringUtils.hasText(directory)) {
            filePath = baseUrl + "/" + directory + "/" + dateDir + "/" + uniqueFilename;
        } else {
            filePath = baseUrl + "/" + dateDir + "/" + uniqueFilename;
        }
        
        logger.info("文件存储成功: {} -> {}", originalFilename, filePath);
        return filePath;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        try {
            // 从URL中提取文件路径
            String filePath = fileUrl;
            if (fileUrl.startsWith(baseUrl)) {
                filePath = fileUrl.substring(baseUrl.length());
            }
            
            // 确保路径是规范化的
            filePath = StringUtils.cleanPath(filePath);
            
            // 构建文件的完整路径
            Path fileToDelete = this.fileStorageLocation.resolve(filePath.startsWith("/") ? filePath.substring(1) : filePath);
            
            // 检查文件是否存在
            if (Files.exists(fileToDelete)) {
                // 删除文件
                return Files.deleteIfExists(fileToDelete);
            }
            
            return false;
        } catch (IOException ex) {
            logger.error("删除文件失败: {}", fileUrl, ex);
            return false;
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
} 