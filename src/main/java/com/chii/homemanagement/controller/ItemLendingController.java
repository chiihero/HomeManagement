package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.ItemLending;
import com.chii.homemanagement.entity.ResponseInfo;
import com.chii.homemanagement.service.ItemLendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 物品借用控制器
 */
@RestController
@RequestMapping("/api/lendings")
public class ItemLendingController {

    @Autowired
    private ItemLendingService itemLendingService;

    /**
     * 创建借用记录
     */
    @PostMapping
    public ResponseInfo<ItemLending> createItemLending(@RequestBody ItemLending itemLending) {
        return ResponseInfo.successResponse(itemLendingService.createItemLending(itemLending));
    }

    /**
     * 更新借用记录
     */
    @PutMapping("/{id}")
    public ResponseInfo<ItemLending> updateItemLending(@PathVariable Long id, @RequestBody ItemLending itemLending) {
        itemLending.setId(id);
        return ResponseInfo.successResponse(itemLendingService.updateItemLending(itemLending));
    }

    /**
     * 删除借用记录
     */
    @DeleteMapping("/{id}")
    public ResponseInfo<Boolean> deleteItemLending(@PathVariable Long id) {
        return ResponseInfo.successResponse(itemLendingService.deleteItemLending(id));
    }

    /**
     * 获取借用记录详情
     */
    @GetMapping("/{id}")
    public ResponseInfo<ItemLending> getItemLending(@PathVariable Long id) {
        return ResponseInfo.successResponse(itemLendingService.getItemLending(id));
    }

    /**
     * 获取物品的借用记录
     */
    @GetMapping("/item/{itemId}")
    public ResponseInfo<List<ItemLending>> getItemLendingsByItemId(@PathVariable Long itemId) {
        return ResponseInfo.successResponse(itemLendingService.getItemLendingsByItemId(itemId));
    }

    /**
     * 获取指定状态的借用记录
     */
    @GetMapping("/status/{status}")
    public ResponseInfo<List<ItemLending>> getItemLendingsByStatus(@PathVariable String status) {
        return ResponseInfo.successResponse(itemLendingService.getItemLendingsByStatus(status));
    }

    /**
     * 获取未归还的借用记录
     */
    @GetMapping("/unreturned")
    public ResponseInfo<List<ItemLending>> getUnreturnedItemLendings() {
        return ResponseInfo.successResponse(itemLendingService.getUnreturnedItemLendings());
    }

    /**
     * 物品归还
     */
    @PutMapping("/{id}/return")
    public ResponseInfo<ItemLending> returnItem(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
        // 如果未指定归还日期，使用当前日期
        LocalDate actualReturnDate = returnDate != null ? returnDate : LocalDate.now();
        return ResponseInfo.successResponse(itemLendingService.returnItem(id, actualReturnDate));
    }

    /**
     * 检查并更新借用状态（标记逾期等）
     */
    @PostMapping("/check-status")
    public ResponseInfo<Void> checkAndUpdateLendingStatus() {
        itemLendingService.checkAndUpdateLendingStatus();
        return ResponseInfo.successResponse();
    }
} 