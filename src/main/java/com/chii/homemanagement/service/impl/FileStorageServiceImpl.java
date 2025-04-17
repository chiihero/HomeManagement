package com.chii.homemanagement.service.impl;

import com.chii.homemanagement.service.FileStorageService;
import com.chii.homemanagement.util.LibAvifUtil;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private LibAvifUtil libAvifUtil;
    
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
        return storeFile(file, "","");

    }

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        return storeFile(file, directory,"");
    }

    @Override
    public String storeFile(MultipartFile file, String directory, String fileName ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        // 获取文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = fileName;
        //不存在文件名时候创建文件名
        if (fileName == null || fileName.isEmpty()){
            // 检查文件名是否包含非法字符
            if (originalFilename.contains("..")) {
                throw new IOException("文件名包含非法路径字符: " + originalFilename);
            }

            // 生成唯一文件名
            String fileExtension = getFileExtension(originalFilename);
            uniqueFilename = generateUniqueFilename(fileExtension);
        }


        // 按日期创建子目录
        //String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 构建存储路径
        Path targetDirectory = this.fileStorageLocation;
        if (StringUtils.hasText(directory)) {
            targetDirectory = this.fileStorageLocation.resolve(directory);
        }
        // 确保目标目录存在
        Files.createDirectories(targetDirectory);

        // 存储文件
        Path targetPath = targetDirectory.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 构建文件访问URL
        String filePath;
        if (StringUtils.hasText(directory)) {
            filePath = baseUrl + "/" + directory + "/"  + uniqueFilename;
        } else {
            filePath = baseUrl + "/"  + uniqueFilename;
        }

        logger.info("文件存储成功: {} -> {}", originalFilename, filePath);
        return filePath;
    }

    @Override
    public String storeImageAsAvif(MultipartFile file) throws IOException {
        return storeImageAsAvif(file, "", -1);
    }

    @Override
    public String storeImageAsAvif(MultipartFile file, String directory) throws IOException {
        return storeImageAsAvif(file, directory, -1);
    }

    @Override
    public String storeImageAsAvif(MultipartFile file, String directory, int quality) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 获取原始文件名和检查其合法性
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IOException("文件名包含非法路径字符: " + originalFilename);
        }
        
        // 如果未指定质量，则根据文件大小动态计算压缩质量
        int compressionQuality = quality;
        if (quality <= 0) {
            compressionQuality = calculateQualityByFileSize(file.getSize());
            logger.info("根据文件大小({} KB)动态计算AVIF压缩质量: {}", file.getSize() / 1024, compressionQuality);
        }
        
        // 首先存储原始图片到临时文件
        Path tempDirectory = this.fileStorageLocation.resolve("temp");
        Files.createDirectories(tempDirectory);
        String tempFilename = generateUniqueFilename(getFileExtension(originalFilename));
        Path tempFilePath = tempDirectory.resolve(tempFilename);
        Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        try {
            // 构建目标AVIF文件路径
            Path targetDirectory = this.fileStorageLocation;
            if (StringUtils.hasText(directory)) {
                targetDirectory = this.fileStorageLocation.resolve(directory);
            }
            Files.createDirectories(targetDirectory);
            
            // 生成AVIF文件名
            String avifFilename = generateUniqueFilename(".avif");
            Path avifFilePath = targetDirectory.resolve(avifFilename);
            
            // 使用LibAvif转换为AVIF格式
            boolean converted = libAvifUtil.convertToAvif(tempFilePath, avifFilePath, compressionQuality);
            
            // 删除临时文件
            Files.deleteIfExists(tempFilePath);
            
            // 如果转换失败，抛出异常
            if (!converted) {
                throw new IOException("图片转换为AVIF格式失败");
            }
            
            // 检查转换后的文件大小，如果仍然超过目标大小且质量还可以降低，则尝试重新压缩
            long avifFileSize = Files.size(avifFilePath);
            if (avifFileSize > 204800 && compressionQuality > 15) { // 如果超过200KB且质量可继续降低
                int newQuality = Math.max(15, compressionQuality - 15); // 再降低质量，但不低于15
                logger.info("AVIF文件仍然过大({} KB)，尝试以更低质量{}重新压缩", avifFileSize / 1024, newQuality);
                
                // 重新压缩
                boolean reconverted = libAvifUtil.convertToAvif(avifFilePath, avifFilePath, newQuality);
                if (!reconverted) {
                    logger.warn("重新压缩AVIF文件失败，保留原压缩结果");
                }
            }
            
            // 构建文件访问URL
            String filePath;
            if (StringUtils.hasText(directory)) {
                filePath = baseUrl + "/" + directory + "/" + avifFilename;
            } else {
                filePath = baseUrl + "/" + avifFilename;
            }
            
            logger.info("图片成功转换为AVIF并存储: {} -> {}，大小: {} KB", originalFilename, filePath, Files.size(avifFilePath) / 1024);
            return filePath;
        } catch (Exception e) {
            // 出现异常，确保删除临时文件
            try {
                Files.deleteIfExists(tempFilePath);
            } catch (IOException ignored) {
                // 忽略删除临时文件时的异常
            }
            throw e;
        }
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
    
    /**
     * 根据文件大小动态计算AVIF压缩质量
     * 目标是将图片压缩到约200KB以下
     * 
     * @param fileSize 原始文件大小（字节）
     * @return 计算得出的压缩质量(1-100)
     */
    private int calculateQualityByFileSize(long fileSize) {
        // 文件大小转换为KB
        long fileSizeKB = fileSize / 1024;
        
        // 1MB = 1024KB
        if (fileSizeKB <= 200) {
            // 小图片使用较高质量
            return 70;
        } else if (fileSizeKB <= 500) {
            // 中等图片适中质量
            return 55;
        } else if (fileSizeKB <= 1024) {
            // 较大图片较低质量
            return 40;
        } else if (fileSizeKB <= 2048) {
            // 大图片低质量
            return 30;
        } else if (fileSizeKB <= 5120) {
            // 超大图片很低质量
            return 20;
        } else {
            // 巨大图片最低质量
            return 15;
        }
    }
} 