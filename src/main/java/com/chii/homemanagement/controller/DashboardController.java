package com.chii.homemanagement.controller;

import com.chii.homemanagement.entity.Entity;
import com.chii.homemanagement.entity.Reminder;
import com.chii.homemanagement.entity.Tag;
import com.chii.homemanagement.entity.User;
import com.chii.homemanagement.service.EntityService;
import com.chii.homemanagement.service.ReminderService;
import com.chii.homemanagement.service.TagService;
import com.chii.homemanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

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
    private EntityService entityService;

    @Autowired
    private ReminderService reminderService;


    @Autowired
    private UserService userService;
    
    @Autowired
    private TagService tagService;

    /**
     * 仪表盘页面
     */
    @GetMapping
    public String dashboard(@ModelAttribute("model") Model model, HttpSession session) {
        // 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // 获取当前所有者ID
        Long ownerId = (Long) session.getAttribute("currentownerId");
        if (ownerId == null) {
            // 如果未选择所有者，重定向到首页
            return "redirect:/";
        }

        model.addAttribute("user", user);

        // 实体总数（物品类型）
        List<Entity> allEntities = entityService.getEntitiesByType(ownerId, "item");
        model.addAttribute("totalItems", allEntities.size());

        // 计算实体总价值
        BigDecimal totalValue = allEntities.stream()
                .map(entity -> entity.getPrice() != null && entity.getQuantity() != null ? 
                     entity.getPrice().multiply(new BigDecimal(entity.getQuantity())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalValue", totalValue);

        // 借出实体数量
        long lentEntitiesCount = allEntities.stream()
                .filter(entity -> "lent".equals(entity.getStatus()))
                .count();
        model.addAttribute("lentItemsCount", lentEntitiesCount);

        // 待处理提醒
        List<Reminder> pendingReminders = reminderService.getRemindersByStatus(ownerId, "pending");
        model.addAttribute("pendingRemindersCount", pendingReminders.size());
        model.addAttribute("pendingReminders", pendingReminders.stream().limit(5).collect(Collectors.toList()));

        // 今日提醒
        List<Reminder> todayReminders = reminderService.getTodayReminders(ownerId);
        model.addAttribute("todayRemindersCount", todayReminders.size());

        // 标签实体分布数据
        prepareTagChartData(ownerId, model);

        // 实体层级分布数据
        prepareEntityChartData(allEntities, model);

        // 使用频率分析数据
        prepareUsageChartData(allEntities, model);

        // 最近活动
        prepareRecentActivities(ownerId, model);

        return "dashboard";
    }

    /**
     * 准备标签物品分布数据
     */
    private void prepareTagChartData(Long ownerId, Model model) {
        // 从标签服务中获取所有标签
        List<Tag> tags = tagService.getTagsByOwnerId(ownerId);
        List<Map<String, Object>> tagData = new ArrayList<>();
        List<String> tagNames = new ArrayList<>();

        // 如果没有可用的标签数据，创建模拟数据
        if (tags.isEmpty()) {
            Map<String, Integer> tagCountMap = new HashMap<>();
            tagCountMap.put("电子", 15);
            tagCountMap.put("家具", 10);
            tagCountMap.put("厨房", 8);
            tagCountMap.put("书籍", 12);
            tagCountMap.put("其他", 5);

            for (Map.Entry<String, Integer> entry : tagCountMap.entrySet()) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", entry.getKey());
                data.put("value", entry.getValue());
                tagData.add(data);
                tagNames.add(entry.getKey());
            }
        } else {
            // 实际应用中，应该有一个方法来统计每个标签关联的物品数量
            // 这里简单模拟随机数量
            for (Tag tag : tags) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", tag.getName());
                data.put("value", (int) (Math.random() * 20) + 1); // 随机1-20个物品
                tagData.add(data);
                tagNames.add(tag.getName());
            }
        }

        model.addAttribute("tagData", tagData);
        model.addAttribute("tagNames", tagNames);
    }

    /**
     * 准备实体分布数据
     */
    private void prepareEntityChartData(List<Entity> entities, Model model) {
        // 此处应该统计父实体内的子实体数量
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
    private void prepareUsageChartData(List<Entity> entities, Model model) {
        // 统计使用频率分布
        Map<String, Long> usageFrequencyMap = entities.stream()
                .collect(Collectors.groupingBy(
                        entity -> entity.getUsageFrequency() != null ? entity.getUsageFrequency() : "unknown",
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
    private void prepareRecentActivities(Long ownerId, Model model) {
        // 实际应用中，这里应该有一个活动日志表
        // 这里使用模拟数据
        List<Map<String, Object>> activities = new ArrayList<>();

        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("title", "新增实体");
        activity1.put("description", "张三添加了新实体：小米手机");
        activity1.put("time", LocalDate.now().atTime(9, 30));
        activity1.put("user", "张三");
        activities.add(activity1);

        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("title", "实体借出");
        activity2.put("description", "李四借出了实体：电动工具");
        activity2.put("time", LocalDate.now().atTime(11, 15));
        activity2.put("user", "李四");
        activities.add(activity2);

        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("title", "新增标签");
        activity3.put("description", "王五创建了新标签：电子设备");
        activity3.put("time", LocalDate.now().minusDays(1).atTime(15, 45));
        activity3.put("user", "王五");
        activities.add(activity3);

        Map<String, Object> activity4 = new HashMap<>();
        activity4.put("title", "添加新实体");
        activity4.put("description", "赵六添加了新实体：储物箱");
        activity4.put("time", LocalDate.now().minusDays(1).atTime(16, 30));
        activity4.put("user", "赵六");
        activities.add(activity4);

        model.addAttribute("recentActivities", activities);
    }
} 