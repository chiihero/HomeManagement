package com.chii.homemanagement.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 存储文件并返回访问URL
     *
     * @param file 要存储的文件
     * @return 文件访问URL
     * @throws IOException 如果存储过程中发生IO异常
     */
    String storeFile(MultipartFile file) throws IOException;

    /**
     * 存储文件到指定目录并返回访问URL
     *
     * @param file 要存储的文件
     * @param directory 存储子目录，例如 "entities", "users"等
     * @return 文件访问URL
     * @throws IOException 如果存储过程中发生IO异常
     */
    String storeFile(MultipartFile file, String directory) throws IOException;


    /**
     * 存储文件到指定目录并返回访问URL
     *
     * @param file 要存储的文件
     * @param directory 存储子目录，例如 "entities", "users"等
     * @param fileName 存储文件名
     * @return 文件访问URL
     * @throws IOException 如果存储过程中发生IO异常
     */
    String storeFile(MultipartFile file, String directory, String fileName ) throws IOException;

    /**
     * 存储图片并转换为AVIF格式
     *
     * @param file 要存储的图片文件
     * @return 转换后的AVIF图片访问URL
     * @throws IOException 如果存储或转换过程中发生IO异常
     */
    String storeImageAsAvif(MultipartFile file) throws IOException;

    /**
     * 存储图片到指定目录并转换为AVIF格式
     *
     * @param file 要存储的图片文件
     * @param directory 存储子目录，例如 "entities", "users"等
     * @return 转换后的AVIF图片访问URL
     * @throws IOException 如果存储或转换过程中发生IO异常
     */
    String storeImageAsAvif(MultipartFile file, String directory) throws IOException;

    /**
     * 存储图片到指定目录并转换为AVIF格式，指定质量参数
     *
     * @param file 要存储的图片文件
     * @param directory 存储子目录，例如 "entities", "users"等
     * @param fileName 存储文件名
     * @param quality 压缩质量 (0-63)，数值越低质量越高
     * @return 转换后的AVIF图片访问URL
     * @throws IOException 如果存储或转换过程中发生IO异常
     */
    String storeImageAsAvif(MultipartFile file, String directory, String fileName, int quality) throws IOException;

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL或路径
     * @return 是否成功删除
     */
    boolean deleteFile(String fileUrl);
} 