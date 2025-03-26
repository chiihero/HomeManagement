package com.chii.homemanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chii.homemanagement.entity.Item;
import com.chii.homemanagement.entity.ItemLending;
import com.chii.homemanagement.mapper.ItemLendingMapper;
import com.chii.homemanagement.mapper.ItemMapper;
import com.chii.homemanagement.service.ItemLendingService;
import com.chii.homemanagement.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品借用服务实现类
 */
@Service
public class ItemLendingServiceImpl extends ServiceImpl<ItemLendingMapper, ItemLending> implements ItemLendingService {

    @Autowired
    private ItemLendingMapper itemLendingMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ReminderService reminderService;

    @Override
    @Transactional
    public ItemLending createItemLending(ItemLending itemLending) {
        // 设置初始状态和时间
        itemLending.setStatus("lending");
        itemLending.setCreateTime(LocalDateTime.now());
        itemLending.setUpdateTime(LocalDateTime.now());

        // 保存借用记录
        save(itemLending);

        // 更新物品状态为借出
        Item item = itemMapper.selectById(itemLending.getItemId());
        if (item != null) {
            item.setStatus("lent");
            itemMapper.updateById(item);

            // 生成归还提醒
            reminderService.generateRemindersForItem(itemLending.getItemId());
        }

        return itemLending;
    }

    @Override
    public ItemLending updateItemLending(ItemLending itemLending) {
        itemLending.setUpdateTime(LocalDateTime.now());
        updateById(itemLending);

        // 如果修改了预计归还日期，更新相关提醒
        if (itemLending.getExpectedReturnDate() != null) {
            reminderService.generateRemindersForItem(itemLending.getItemId());
        }

        return itemLending;
    }

    @Override
    @Transactional
    public boolean deleteItemLending(Long id) {
        ItemLending lending = getById(id);
        if (lending != null) {
            // 如果物品状态是借出中，并且删除借用记录，需要更新物品状态为正常
            if ("lending".equals(lending.getStatus())) {
                Item item = itemMapper.selectById(lending.getItemId());
                if (item != null && "lent".equals(item.getStatus())) {
                    item.setStatus("normal");
                    itemMapper.updateById(item);
                }
            }
        }
        return removeById(id);
    }

    @Override
    public ItemLending getItemLending(Long id) {
        return getById(id);
    }

    @Override
    public List<ItemLending> getItemLendingsByItemId(Long itemId) {
        return itemLendingMapper.findByItemId(itemId);
    }

    @Override
    public List<ItemLending> getItemLendingsByStatus(String status) {
        return itemLendingMapper.findByStatus(status);
    }

    @Override
    public List<ItemLending> getUnreturnedItemLendings() {
        return itemLendingMapper.findUnreturnedItems();
    }

    @Override
    @Transactional
    public ItemLending returnItem(Long id, LocalDate actualReturnDate) {
        ItemLending lending = getById(id);
        if (lending == null) {
            return null;
        }

        // 更新借用记录状态为已归还
        lending.setStatus("returned");
        lending.setActualReturnDate(actualReturnDate);
        lending.setUpdateTime(LocalDateTime.now());
        updateById(lending);

        // 更新物品状态为正常
        Item item = itemMapper.selectById(lending.getItemId());
        if (item != null && "lent".equals(item.getStatus())) {
            item.setStatus("normal");
            itemMapper.updateById(item);
        }

        return lending;
    }

    @Override
    public void checkAndUpdateLendingStatus() {
        LocalDate today = LocalDate.now();

        // 查询所有借出中的记录
        List<ItemLending> lendingItems = itemLendingMapper.findByStatus("lending");

        for (ItemLending lending : lendingItems) {
            // 如果预计归还日期已过，更新状态为逾期
            if (lending.getExpectedReturnDate() != null && lending.getExpectedReturnDate().isBefore(today)) {
                lending.setStatus("overdue");
                lending.setUpdateTime(LocalDateTime.now());
                updateById(lending);
            }
        }
    }
} 