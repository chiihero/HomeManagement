package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.ItemImage;
import com.chii.homemanagement.exception.BusinessException;
import com.chii.homemanagement.mapper.ItemImageMapper;
import com.chii.homemanagement.service.ItemImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 物品图片服务实现类
 */
@Service
public class ItemImageServiceImpl extends ServiceImpl<ItemImageMapper, ItemImage> implements ItemImageService {

    @Autowired
    private ItemImageMapper itemImageMapper;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Override
    @Transactional
    public ItemImage uploadImage(Long itemId, MultipartFile file, Boolean isMain) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isImage(originalFilename)) {
            throw new BusinessException("只允许上传图片文件(jpg, jpeg, png, gif)");
        }

        try {
            // 创建上传目录
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dirPath = uploadPath + "/item/" + today;
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // 生成新文件名
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String newFilename = UUID.randomUUID() + "." + extension;
            String filePath = dirPath + "/" + newFilename;

            // 保存文件
            Path targetPath = Paths.get(filePath);
            Files.copy(file.getInputStream(), targetPath);


            // 保存图片信息到数据库
            ItemImage itemImage = new ItemImage();
            itemImage.setItemId(itemId);
            itemImage.setUrl("/item/" + today + "/" + newFilename);
            itemImage.setType(file.getContentType());
            itemImage.setCreateTime(LocalDateTime.now());

            save(itemImage);

            return itemImage;
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public boolean deleteImage(Long id) {
        ItemImage image = getById(id);
        if (image == null) {
            throw new BusinessException("图片不存在");
        }


        // 删除数据库记录
        boolean result = removeById(id);

        // 删除文件
        try {
            String filePath = uploadPath + image.getUrl();
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("删除文件失败: " + e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<ItemImage> getItemImages(Long itemId) {
        LambdaQueryWrapper<ItemImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ItemImage::getItemId, itemId);
        return list(queryWrapper);
    }


    /**
     * 判断是否是图片文件
     */
    private boolean isImage(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (extension == null) {
            return false;
        }
        extension = extension.toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("gif");
    }
} 