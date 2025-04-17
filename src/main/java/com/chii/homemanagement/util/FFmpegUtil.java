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
 * FFmpeg工具类，用于处理图片和视频转换
 */
@Component
public class FFmpegUtil {

    private static final Logger logger = LogManager.getLogger(FFmpegUtil.class);

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;
    
    @Value("${ffmpeg.avif.quality:50}")
    private int avifQuality;

    /**
     * 将图片转换为AVIF格式
     *
     * @param inputPath  输入图片路径
     * @param outputPath 输出AVIF图片路径
     * @return 是否转换成功
     */
    public boolean convertToAvif(Path inputPath, Path outputPath) {
        try {
            // 检查目标文件所在目录是否存在，不存在则创建
            File outputDir = outputPath.getParent().toFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 构建FFmpeg命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-i", inputPath.toString(),
                    "-c:v", "libaom-av1",
                    "-crf", String.valueOf(avifQuality),
                    "-still-picture", "1",
                    "-strict", "experimental",
                    outputPath.toString()
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
                logger.error("FFmpeg转换超时: {}", inputPath);
                return false;
            }
            
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                logger.info("图片成功转换为AVIF格式: {} -> {}", inputPath, outputPath);
                return true;
            } else {
                logger.error("FFmpeg转换失败: {}，退出码: {}", inputPath, exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("FFmpeg转换图片时发生错误", e);
            return false;
        }
    }
    
    /**
     * 将图片转换为AVIF格式并指定质量
     *
     * @param inputPath  输入图片路径
     * @param outputPath 输出AVIF图片路径
     * @param quality    质量 (0-63)，数值越低质量越高，默认为50
     * @return 是否转换成功
     */
    public boolean convertToAvif(Path inputPath, Path outputPath, int quality) {
        try {
            // 检查目标文件所在目录是否存在，不存在则创建
            File outputDir = outputPath.getParent().toFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 构建FFmpeg命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-i", inputPath.toString(),
                    "-c:v", "libaom-av1",
                    "-crf", String.valueOf(quality),
                    "-still-picture", "1",
                    "-strict", "experimental",
                    outputPath.toString()
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
                logger.error("FFmpeg转换超时: {}", inputPath);
                return false;
            }
            
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                logger.info("图片成功转换为AVIF格式: {} -> {}", inputPath, outputPath);
                return true;
            } else {
                logger.error("FFmpeg转换失败: {}，退出码: {}", inputPath, exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("FFmpeg转换图片时发生错误", e);
            return false;
        }
    }
} 