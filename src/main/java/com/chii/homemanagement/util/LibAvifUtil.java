package com.chii.homemanagement.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * LibAvif工具类，用于使用libavif处理图片转换为AVIF格式
 */
@Component
public class LibAvifUtil {

    private static final Logger logger = LogManager.getLogger(LibAvifUtil.class);

    @Value("${libavif.path:cavif}")
    private String libavifPath;
    
    @Value("${libavif.quality:50}")
    private int avifQuality;

    /**
     * 将图片转换为AVIF格式并指定质量
     *
     * @param inputPath  输入图片路径
     * @param outputPath 输出AVIF图片路径
     * @param quality    质量 (0-100)，数值越高质量越高，默认为50
     * @return 是否转换成功
     */
    public boolean convertToAvif(Path inputPath, Path outputPath, int quality) {
        try {
            // 检查目标文件所在目录是否存在，不存在则创建
            File outputDir = outputPath.getParent().toFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 检查输入文件是否存在
            if (!inputPath.toFile().exists()) {
                logger.error("输入文件不存在: {}", inputPath);
                return false;
            }
            
            // 确保质量参数在有效范围内
            int validQuality = Math.max(1, Math.min(100, quality > 0 ? quality : avifQuality));
            
            // 获取输入文件大小
            long inputFileSize = inputPath.toFile().length();
            logger.info("开始转换图片为AVIF: 输入文件={}, 大小={} KB, 质量参数={}", 
                    inputPath.getFileName(), inputFileSize / 1024, validQuality);
            
            // 构建libavif命令 (cavif工具)
            ProcessBuilder processBuilder = new ProcessBuilder(
                    libavifPath,
                    "-q", String.valueOf(validQuality),
                    "-o", outputPath.toString(),
                    inputPath.toString()
            );
            
            // 设置错误输出和标准输出合并
            processBuilder.redirectErrorStream(true);
            
            // 执行命令
            Process process = processBuilder.start();
            
            // 等待命令执行完成，最多等待30秒
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);
            
            // 检查是否超时或命令执行失败
            if (!completed) {
                process.destroyForcibly();
                logger.error("LibAvif转换超时: {}", inputPath);
                return false;
            }
            
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                // 获取输出文件大小
                if (outputPath.toFile().exists()) {
                    long outputFileSize = outputPath.toFile().length();
                    double compressionRatio = 1.0 - (double)outputFileSize / inputFileSize;
                    logger.info("图片成功转换为AVIF格式: {} -> {}, 输出大小={} KB, 压缩率={}%", 
                            inputPath.getFileName(), outputPath.getFileName(), 
                            outputFileSize / 1024, Math.round(compressionRatio * 100));
                } else {
                    logger.warn("AVIF转换可能成功但输出文件不存在: {}", outputPath);
                }
                return true;
            } else {
                logger.error("LibAvif转换失败: {}，退出码: {}", inputPath, exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("LibAvif转换图片时发生错误", e);
            return false;
        }
    }
} 