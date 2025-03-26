/**
 * 家庭物品管理系统统计报表JavaScript文件
 */

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化统计数据
    loadStatisticsData();
    
    // 搜索按钮点击事件
    document.getElementById('table-search-btn').addEventListener('click', function() {
        const keyword = document.getElementById('table-search').value.trim();
        if (keyword) {
            searchItems(keyword);
        } else {
            loadItemsTable();
        }
    });
    
    // 搜索框回车事件
    document.getElementById('table-search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const keyword = this.value.trim();
            if (keyword) {
                searchItems(keyword);
            } else {
                loadItemsTable();
            }
        }
    });
});

/**
 * 加载统计数据
 */
function loadStatisticsData() {
    // 获取家庭ID
    const familyId = getFamilyId();
    
    // 加载概览数据
    loadOverviewStats(familyId);
    
    // 加载分类统计
    loadCategoryStats(familyId);
    
    // 加载空间统计
    loadSpaceStats(familyId);
    
    // 加载使用频率统计
    loadFrequencyStats(familyId);
    
    // 加载状态统计
    loadStatusStats(familyId);
    
    // 加载价值趋势
    loadValueTrend(familyId);
    
    // 加载物品表格
    loadItemsTable(familyId);
}

/**
 * 加载概览统计数据
 */
function loadOverviewStats(familyId) {
    // 加载物品总数
    fetch(`/api/items/page?current=1&size=1&familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                document.getElementById('total-items').textContent = data.data.total;
            }
        })
        .catch(error => console.error('Error:', error));
    
    // 加载物品总价值
    fetch(`/api/items/value?familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                document.getElementById('total-value').textContent = '¥' + data.data.toFixed(2);
                
                // 计算平均价格
                fetch(`/api/items/page?current=1&size=1&familyId=${familyId}`)
                    .then(response => response.json())
                    .then(countData => {
                        if (countData.code === 200 && countData.data.total > 0) {
                            const avgPrice = data.data / countData.data.total;
                            document.getElementById('avg-price').textContent = '¥' + avgPrice.toFixed(2);
                        }
                    })
                    .catch(error => console.error('Error:', error));
            }
        })
        .catch(error => console.error('Error:', error));
    
    // 加载即将过期物品数量
    fetch(`/api/items/expiring?days=30&familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                document.getElementById('expiring-items').textContent = data.data.length;
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 加载分类统计
 */
function loadCategoryStats(familyId) {
    fetch(`/api/items/stat/category?familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderCategoryChart(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 渲染分类统计图表
 */
function renderCategoryChart(data) {
    const chartContainer = document.getElementById('category-chart');
    if (!chartContainer) return;
    
    // 处理数据
    const categories = [];
    const counts = [];
    const values = [];
    
    data.forEach(item => {
        categories.push(item.categoryName || '未分类');
        counts.push(item.count);
        values.push(parseFloat(item.value.toFixed(2)));
    });
    
    // 创建图表
    const chart = echarts.init(chartContainer);
    
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        legend: {
            data: ['数量', '价值']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [
            {
                type: 'category',
                data: categories,
                axisLabel: {
                    interval: 0,
                    rotate: 30
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                name: '数量',
                position: 'left'
            },
            {
                type: 'value',
                name: '价值',
                position: 'right',
                axisLabel: {
                    formatter: '¥{value}'
                }
            }
        ],
        series: [
            {
                name: '数量',
                type: 'bar',
                data: counts
            },
            {
                name: '价值',
                type: 'bar',
                yAxisIndex: 1,
                data: values
            }
        ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', function() {
        chart.resize();
    });
}

/**
 * 加载空间统计
 */
function loadSpaceStats(familyId) {
    fetch(`/api/items/stat/space?familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderSpaceChart(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 渲染空间统计图表
 */
function renderSpaceChart(data) {
    const chartContainer = document.getElementById('space-chart');
    if (!chartContainer) return;
    
    // 处理数据
    const spaces = [];
    const counts = [];
    
    data.forEach(item => {
        spaces.push(item.spaceName || '未指定');
        counts.push(item.count);
    });
    
    // 创建图表
    const chart = echarts.init(chartContainer);
    
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
            orient: 'vertical',
            left: 10,
            data: spaces
        },
        series: [
            {
                name: '空间分布',
                type: 'pie',
                radius: ['50%', '70%'],
                avoidLabelOverlap: false,
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '18',
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: false
                },
                data: spaces.map((space, index) => {
                    return {
                        value: counts[index],
                        name: space
                    };
                })
            }
        ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', function() {
        chart.resize();
    });
}

/**
 * 加载使用频率统计
 */
function loadFrequencyStats(familyId) {
    fetch(`/api/items/stat/frequency?familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderFrequencyChart(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 渲染使用频率统计图表
 */
function renderFrequencyChart(data) {
    const chartContainer = document.getElementById('frequency-chart');
    if (!chartContainer) return;
    
    // 处理数据
    const frequencies = [];
    const counts = [];
    
    // 定义频率顺序和显示名称
    const frequencyOrder = {
        'daily': '每天',
        'weekly': '每周',
        'monthly': '每月',
        'rarely': '很少'
    };
    
    // 确保所有频率都有数据
    Object.keys(frequencyOrder).forEach(key => {
        const item = data.find(d => d.usageFrequency === key);
        frequencies.push(frequencyOrder[key]);
        counts.push(item ? item.count : 0);
    });
    
    // 创建图表
    const chart = echarts.init(chartContainer);
    
    const option = {
        tooltip: {
            trigger: 'item'
        },
        legend: {
            top: '5%',
            left: 'center'
        },
        series: [
            {
                name: '使用频率',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: false,
                    position: 'center'
                },
                emphasis: {
                    label: {
                        show: true,
                        fontSize: '18',
                        fontWeight: 'bold'
                    }
                },
                labelLine: {
                    show: false
                },
                data: frequencies.map((freq, index) => {
                    return {
                        value: counts[index],
                        name: freq
                    };
                })
            }
        ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', function() {
        chart.resize();
    });
}

/**
 * 加载状态统计
 */
function loadStatusStats(familyId) {
    fetch(`/api/items/page?current=1&size=1000&familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const items = data.data.records;
                renderStatusChart(items);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 渲染状态统计图表
 */
function renderStatusChart(items) {
    const chartContainer = document.getElementById('status-chart');
    if (!chartContainer) return;
    
    // 统计各状态数量
    const statusCount = {
        'normal': 0,
        'damaged': 0,
        'discarded': 0,
        'lent': 0
    };
    
    items.forEach(item => {
        if (item.status in statusCount) {
            statusCount[item.status]++;
        }
    });
    
    // 定义状态显示名称
    const statusNames = {
        'normal': '正常',
        'damaged': '损坏',
        'discarded': '丢弃',
        'lent': '借出'
    };
    
    // 定义状态颜色
    const statusColors = {
        'normal': '#28a745',
        'damaged': '#ffc107',
        'discarded': '#6c757d',
        'lent': '#17a2b8'
    };
    
    // 准备图表数据
    const data = Object.keys(statusCount).map(key => {
        return {
            value: statusCount[key],
            name: statusNames[key],
            itemStyle: {
                color: statusColors[key]
            }
        };
    });
    
    // 创建图表
    const chart = echarts.init(chartContainer);
    
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
            orient: 'vertical',
            left: 10,
            data: Object.values(statusNames)
        },
        series: [
            {
                name: '物品状态',
                type: 'pie',
                radius: '50%',
                data: data,
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', function() {
        chart.resize();
    });
}

/**
 * 加载价值趋势
 */
function loadValueTrend(familyId) {
    // 获取当前日期
    const today = new Date();
    const endDate = formatDate(today);
    
    // 获取12个月前的日期
    const startDate = formatDate(new Date(today.getFullYear() - 1, today.getMonth(), today.getDate()));
    
    // 构建API URL
    const url = `/api/items/date?startDate=${startDate}&endDate=${endDate}&familyId=${familyId}`;
    
    // 发送请求获取物品列表
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderValueTrendChart(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 渲染价值趋势图表
 */
function renderValueTrendChart(items) {
    const chartContainer = document.getElementById('value-trend-chart');
    if (!chartContainer) return;
    
    // 按月份分组物品
    const monthlyData = {};
    
    // 获取当前日期
    const today = new Date();
    
    // 初始化过去12个月的数据
    for (let i = 0; i < 12; i++) {
        const date = new Date(today.getFullYear(), today.getMonth() - i, 1);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        monthlyData[monthKey] = {
            count: 0,
            value: 0
        };
    }
    
    // 统计每月物品数量和价值
    items.forEach(item => {
        if (item.purchaseDate) {
            const purchaseDate = new Date(item.purchaseDate);
            const monthKey = `${purchaseDate.getFullYear()}-${String(purchaseDate.getMonth() + 1).padStart(2, '0')}`;
            
            if (monthKey in monthlyData) {
                monthlyData[monthKey].count += 1;
                monthlyData[monthKey].value += parseFloat(item.price) * item.quantity;
            }
        }
    });
    
    // 准备图表数据
    const months = Object.keys(monthlyData).sort();
    const counts = months.map(month => monthlyData[month].count);
    const values = months.map(month => monthlyData[month].value.toFixed(2));
    
    // 格式化月份显示
    const formattedMonths = months.map(month => {
        const [year, monthNum] = month.split('-');
        return `${year}年${monthNum}月`;
    });
    
    // 创建图表
    const chart = echarts.init(chartContainer);
    
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                crossStyle: {
                    color: '#999'
                }
            }
        },
        toolbox: {
            feature: {
                dataView: { show: true, readOnly: false },
                magicType: { show: true, type: ['line', 'bar'] },
                restore: { show: true },
                saveAsImage: { show: true }
            }
        },
        legend: {
            data: ['物品数量', '物品价值']
        },
        xAxis: [
            {
                type: 'category',
                data: formattedMonths.reverse(),
                axisPointer: {
                    type: 'shadow'
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                name: '数量',
                position: 'left'
            },
            {
                type: 'value',
                name: '价值',
                position: 'right',
                axisLabel: {
                    formatter: '¥{value}'
                }
            }
        ],
        series: [
            {
                name: '物品数量',
                type: 'bar',
                data: counts.reverse()
            },
            {
                name: '物品价值',
                type: 'line',
                yAxisIndex: 1,
                data: values.reverse()
            }
        ]
    };
    
    chart.setOption(option);
    
    // 窗口大小变化时重绘图表
    window.addEventListener('resize', function() {
        chart.resize();
    });
}

/**
 * 加载物品表格
 */
function loadItemsTable(familyId) {
    if (!familyId) {
        familyId = getFamilyId();
    }
    
    // 构建API URL
    const url = `/api/items/page?current=1&size=100&familyId=${familyId}`;
    
    // 发送请求获取物品列表
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderItemsTable(data.data.records);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 渲染物品表格
 */
function renderItemsTable(items) {
    const tableBody = document.querySelector('#items-table tbody');
    if (!tableBody) return;
    
    // 清空表格
    tableBody.innerHTML = '';
    
    if (items.length === 0) {
        const row = document.createElement('tr');
        row.innerHTML = '<td colspan="8" class="text-center">暂无数据</td>';
        tableBody.appendChild(row);
        return;
    }
    
    // 加载分类和空间数据
    Promise.all([
        loadCategoryMap(),
        loadSpaceMap()
    ]).then(([categoryMap, spaceMap]) => {
        // 创建表格行
        items.forEach(item => {
            const row = document.createElement('tr');
            
            // 计算总价值
            const totalValue = parseFloat(item.price) * item.quantity;
            
            row.innerHTML = `
                <td>${item.name}</td>
                <td>${categoryMap[item.categoryId] || '未分类'}</td>
                <td>${spaceMap[item.spaceId] || '未指定'}</td>
                <td>${item.quantity}</td>
                <td>¥${parseFloat(item.price).toFixed(2)}</td>
                <td>¥${totalValue.toFixed(2)}</td>
                <td><span class="badge ${getStatusClass(item.status)}">${getStatusText(item.status)}</span></td>
                <td>${item.purchaseDate || '未知'}</td>
            `;
            
            tableBody.appendChild(row);
        });
    }).catch(error => console.error('Error:', error));
}

/**
 * 加载分类映射
 */
async function loadCategoryMap() {
    const familyId = getFamilyId();
    
    try {
        const response = await fetch(`/api/categories/tree?familyId=${familyId}`);
        const data = await response.json();
        
        if (data.code === 200) {
            const categoryMap = {};
            flattenCategories(data.data, categoryMap);
            return categoryMap;
        }
        
        return {};
    } catch (error) {
        console.error('Error:', error);
        return {};
    }
}

/**
 * 扁平化分类树
 */
function flattenCategories(categories, map) {
    categories.forEach(category => {
        map[category.id] = category.name;
        
        if (category.children && category.children.length > 0) {
            flattenCategories(category.children, map);
        }
    });
}

/**
 * 加载空间映射
 */
async function loadSpaceMap() {
    const familyId = getFamilyId();
    
    try {
        const response = await fetch(`/api/spaces/tree?familyId=${familyId}`);
        const data = await response.json();
        
        if (data.code === 200) {
            const spaceMap = {};
            flattenSpaces(data.data, spaceMap);
            return spaceMap;
        }
        
        return {};
    } catch (error) {
        console.error('Error:', error);
        return {};
    }
}

/**
 * 扁平化空间树
 */
function flattenSpaces(spaces, map) {
    spaces.forEach(space => {
        map[space.id] = space.name;
        
        if (space.children && space.children.length > 0) {
            flattenSpaces(space.children, map);
        }
    });
}

/**
 * 搜索物品
 */
function searchItems(keyword) {
    const familyId = getFamilyId();
    
    // 构建API URL
    const url = `/api/items/page?current=1&size=100&name=${encodeURIComponent(keyword)}&familyId=${familyId}`;
    
    // 发送请求搜索物品
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderItemsTable(data.data.records);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 设置日期范围
 */
function setDateRange(range) {
    const today = new Date();
    let startDate, endDate;
    
    switch (range) {
        case 'week':
            // 本周开始（周一）
            startDate = new Date(today);
            startDate.setDate(today.getDate() - today.getDay() + (today.getDay() === 0 ? -6 : 1));
            endDate = today;
            applyDateRangeFilter(startDate, endDate);
            break;
            
        case 'month':
            // 本月开始
            startDate = new Date(today.getFullYear(), today.getMonth(), 1);
            endDate = today;
            applyDateRangeFilter(startDate, endDate);
            break;
            
        case 'quarter':
            // 本季度开始
            const quarter = Math.floor(today.getMonth() / 3);
            startDate = new Date(today.getFullYear(), quarter * 3, 1);
            endDate = today;
            applyDateRangeFilter(startDate, endDate);
            break;
            
        case 'year':
            // 本年开始
            startDate = new Date(today.getFullYear(), 0, 1);
            endDate = today;
            applyDateRangeFilter(startDate, endDate);
            break;
            
        case 'custom':
            // 显示自定义日期范围模态框
            const modal = new bootstrap.Modal(document.getElementById('dateRangeModal'));
            modal.show();
            break;
    }
}

/**
 * 应用自定义日期范围
 */
function applyDateRange() {
    const startDate = new Date(document.getElementById('startDate').value);
    const endDate = new Date(document.getElementById('endDate').value);
    
    if (startDate > endDate) {
        alert('开始日期不能晚于结束日期');
        return;
    }
    
    // 关闭模态框
    const modal = bootstrap.Modal.getInstance(document.getElementById('dateRangeModal'));
    modal.hide();
    
    // 应用日期范围过滤
    applyDateRangeFilter(startDate, endDate);
}

/**
 * 应用日期范围过滤
 */
function applyDateRangeFilter(startDate, endDate) {
    const familyId = getFamilyId();
    
    // 格式化日期
    const formattedStartDate = formatDate(startDate);
    const formattedEndDate = formatDate(endDate);
    
    // 构建API URL
    const url = `/api/items/date?startDate=${formattedStartDate}&endDate=${formattedEndDate}&familyId=${familyId}`;
    
    // 发送请求获取物品列表
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                // 更新表格
                renderItemsTable(data.data);
                
                // 更新图表
                updateChartsWithDateRange(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

/**
 * 根据日期范围更新图表
 */
function updateChartsWithDateRange(items) {
    // 更新状态统计图表
    renderStatusChart(items);
    
    // 统计总数和总价值
    let totalValue = 0;
    items.forEach(item => {
        totalValue += parseFloat(item.price) * item.quantity;
    });
    
    // 更新概览数据
    document.getElementById('total-items').textContent = items.length;
    document.getElementById('total-value').textContent = '¥' + totalValue.toFixed(2);
    document.getElementById('avg-price').textContent = items.length > 0 ? '¥' + (totalValue / items.length).toFixed(2) : '¥0.00';
    
    // 统计分类数据
    const categoryStats = {};
    items.forEach(item => {
        const categoryId = item.categoryId || 'uncategorized';
        if (!categoryStats[categoryId]) {
            categoryStats[categoryId] = {
                count: 0,
                value: 0
            };
        }
        categoryStats[categoryId].count += 1;
        categoryStats[categoryId].value += parseFloat(item.price) * item.quantity;
    });
    
    // 加载分类名称
    loadCategoryMap().then(categoryMap => {
        const categoryData = Object.keys(categoryStats).map(categoryId => {
            return {
                categoryName: categoryMap[categoryId] || '未分类',
                count: categoryStats[categoryId].count,
                value: categoryStats[categoryId].value
            };
        });
        
        // 更新分类图表
        renderCategoryChart(categoryData);
    });
    
    // 统计空间数据
    const spaceStats = {};
    items.forEach(item => {
        const spaceId = item.spaceId || 'unspecified';
        if (!spaceStats[spaceId]) {
            spaceStats[spaceId] = {
                count: 0
            };
        }
        spaceStats[spaceId].count += 1;
    });
    
    // 加载空间名称
    loadSpaceMap().then(spaceMap => {
        const spaceData = Object.keys(spaceStats).map(spaceId => {
            return {
                spaceName: spaceMap[spaceId] || '未指定',
                count: spaceStats[spaceId].count
            };
        });
        
        // 更新空间图表
        renderSpaceChart(spaceData);
    });
    
    // 统计使用频率数据
    const frequencyStats = {
        'daily': 0,
        'weekly': 0,
        'monthly': 0,
        'rarely': 0
    };
    
    items.forEach(item => {
        if (item.usageFrequency in frequencyStats) {
            frequencyStats[item.usageFrequency]++;
        }
    });
    
    // 准备频率数据
    const frequencyData = Object.keys(frequencyStats).map(key => {
        return {
            usageFrequency: key,
            count: frequencyStats[key]
        };
    });
    
    // 更新频率图表
    renderFrequencyChart(frequencyData);
}

/**
 * 获取状态文本
 */
function getStatusText(status) {
    const statusMap = {
        'normal': '正常',
        'damaged': '损坏',
        'discarded': '丢弃',
        'lent': '借出'
    };
    
    return statusMap[status] || '未知';
}

/**
 * 获取状态类
 */
function getStatusClass(status) {
    const classMap = {
        'normal': 'bg-success',
        'damaged': 'bg-warning',
        'discarded': 'bg-secondary',
        'lent': 'bg-info'
    };
    
    return classMap[status] || 'bg-light';
}

/**
 * 格式化日期
 */
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
}

/**
 * 获取家庭ID
 */
function getFamilyId() {
    // 从URL中获取家庭ID参数
    const urlParams = new URLSearchParams(window.location.search);
    const familyId = urlParams.get('familyId');
    
    // 如果URL中没有家庭ID，则从localStorage中获取
    if (!familyId) {
        return localStorage.getItem('currentFamilyId') || '1';
    }
    
    return familyId;
}