package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Item;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.ItemLendingService;
import com.chii.homemanagement.service.ItemService;
import com.chii.homemanagement.service.ReminderService;
import com.chii.homemanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘控制器
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ItemLendingService itemLendingService;

    @Autowired
    private UserService userService;

    /**
     * 仪表盘页面
     */
    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // 获取当前家庭ID
        Long familyId = (Long) session.getAttribute("currentFamilyId");
        if (familyId == null) {
            // 如果未选择家庭，重定向到首页
            return "redirect:/";
        }

        model.addAttribute("user", user);

        // 物品总数
        List<Item> allItems = itemService.getItemsByFamilyId(familyId);
        model.addAttribute("totalItems", allItems.size());

        // 计算物品总价值
        BigDecimal totalValue = allItems.stream()
                .map(item -> item.getPrice() != null ? item.getPrice().multiply(new BigDecimal(item.getQuantity())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalValue", totalValue);

        // 借出物品数量
        long lentItemsCount = allItems.stream()
                .filter(item -> "lent".equals(item.getStatus()))
                .count();
        model.addAttribute("lentItemsCount", lentItemsCount);

        // 待处理提醒
        List<Reminder> pendingReminders = reminderService.getRemindersByStatus(familyId, "pending");
        model.addAttribute("pendingRemindersCount", pendingReminders.size());
        model.addAttribute("pendingReminders", pendingReminders.stream().limit(5).collect(Collectors.toList()));

        // 今日提醒
        List<Reminder> todayReminders = reminderService.getTodayReminders(familyId);
        model.addAttribute("todayRemindersCount", todayReminders.size());

        // 分类物品分布数据
        prepareCategoryChartData(allItems, model);

        // 空间物品分布数据
        prepareSpaceChartData(allItems, model);

        // 使用频率分析数据
        prepareUsageChartData(allItems, model);

        // 最近活动
        prepareRecentActivities(familyId, model);

        return "dashboard";
    }

    /**
     * 准备分类物品分布数据
     */
    private void prepareCategoryChartData(List<Item> items, Model model) {
        // 此处实际应该从分类关联表中聚合数据
        // 这里使用模拟数据
        List<Map<String, Object>> categoryData = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();

        Map<String, Integer> categoryCountMap = new HashMap<>();
        categoryCountMap.put("电子产品", 15);
        categoryCountMap.put("家具", 10);
        categoryCountMap.put("厨房用品", 8);
        categoryCountMap.put("书籍", 12);
        categoryCountMap.put("其他", 5);

        for (Map.Entry<String, Integer> entry : categoryCountMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", entry.getKey());
            data.put("value", entry.getValue());
            categoryData.add(data);
            categoryNames.add(entry.getKey());
        }

        model.addAttribute("categoryData", categoryData);
        model.addAttribute("categoryNames", categoryNames);
    }

    /**
     * 准备空间物品分布数据
     */
    private void prepareSpaceChartData(List<Item> items, Model model) {
        // 此处实际应该从空间表中聚合数据
        // 这里使用模拟数据
        List<Map<String, Object>> spaceData = new ArrayList<>();
        List<String> spaceNames = new ArrayList<>();

        Map<String, Integer> spaceCountMap = new HashMap<>();
        spaceCountMap.put("客厅", 12);
        spaceCountMap.put("主卧", 18);
        spaceCountMap.put("次卧", 8);
        spaceCountMap.put("厨房", 15);
        spaceCountMap.put("书房", 10);
        spaceCountMap.put("储物间", 7);

        for (Map.Entry<String, Integer> entry : spaceCountMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", entry.getKey());
            data.put("value", entry.getValue());
            spaceData.add(data);
            spaceNames.add(entry.getKey());
        }

        model.addAttribute("spaceData", spaceData);
        model.addAttribute("spaceNames", spaceNames);
    }

    /**
     * 准备使用频率分析数据
     */
    private void prepareUsageChartData(List<Item> items, Model model) {
        // 统计使用频率分布
        Map<String, Long> usageFrequencyMap = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getUsageFrequency() != null ? item.getUsageFrequency() : "unknown",
                        Collectors.counting()
                ));

        List<Integer> usageData = new ArrayList<>();
        usageData.add(usageFrequencyMap.getOrDefault("daily", 0L).intValue());
        usageData.add(usageFrequencyMap.getOrDefault("weekly", 0L).intValue());
        usageData.add(usageFrequencyMap.getOrDefault("monthly", 0L).intValue());
        usageData.add(usageFrequencyMap.getOrDefault("rarely", 0L).intValue());
        usageData.add(usageFrequencyMap.getOrDefault("never", 0L).intValue());

        model.addAttribute("usageData", usageData);
    }

    /**
     * 准备最近活动数据
     */
    private void prepareRecentActivities(Long familyId, Model model) {
        // 实际应用中，这里应该有一个活动日志表
        // 这里使用模拟数据
        List<Map<String, Object>> activities = new ArrayList<>();

        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("title", "新增物品");
        activity1.put("description", "张三添加了新物品：小米手机");
        activity1.put("time", LocalDate.now().atTime(9, 30));
        activity1.put("user", "张三");
        activities.add(activity1);

        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("title", "物品借出");
        activity2.put("description", "李四借出了物品：电动工具");
        activity2.put("time", LocalDate.now().atTime(11, 15));
        activity2.put("user", "李四");
        activities.add(activity2);

        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("title", "物品归还");
        activity3.put("description", "王五归还了物品：小型投影仪");
        activity3.put("time", LocalDate.now().minusDays(1).atTime(16, 45));
        activity3.put("user", "王五");
        activities.add(activity3);

        Map<String, Object> activity4 = new HashMap<>();
        activity4.put("title", "处理提醒");
        activity4.put("description", "张三处理了保修到期提醒：笔记本电脑");
        activity4.put("time", LocalDate.now().minusDays(1).atTime(10, 20));
        activity4.put("user", "张三");
        activities.add(activity4);

        Map<String, Object> activity5 = new HashMap<>();
        activity5.put("title", "更新物品");
        activity5.put("description", "李四更新了物品信息：洗衣机");
        activity5.put("time", LocalDate.now().minusDays(2).atTime(14, 30));
        activity5.put("user", "李四");
        activities.add(activity5);

        model.addAttribute("recentActivities", activities);
    }
} 