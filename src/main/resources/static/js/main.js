/**
 * 家庭物品管理系统主要JavaScript文件
 */

// 定义LocalStorage的键常量
const LS_KEYS = {
    CURRENT_FAMILY_ID: 'currentFamilyId',
    USER_SETTINGS: 'userSettings',
    THEME: 'theme',
    LAST_VIEWED_ITEMS: 'lastViewedItems',
    FILTER_PREFERENCES: 'filterPreferences'
};

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化侧边栏切换功能
    initSidebar();
    
    // 初始化页面内容
    initPageContent();
    
    // 初始化表单验证
    initFormValidation();
    
    // 初始化移动端导航
    initMobileNavigation();
    
    // 初始化图表（如果页面中有图表容器）
    if (document.querySelector('.chart-container')) {
        initCharts();
    }
    
    // 初始化分类树（如果页面中有分类树容器）
    if (document.querySelector('.category-tree')) {
        initCategoryTree();
    }
});

/**
 * 初始化侧边栏
 */
function initSidebar() {
    // 桌面端侧边栏切换按钮
    const sidebarToggle = document.getElementById('sidebar-toggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function() {
            const sidebar = document.getElementById('sidebar');
            sidebar.classList.toggle('collapsed');
            
            // 调整主内容区域
            const content = document.getElementById('content');
            if (content) {
                content.classList.toggle('expanded');
            }
        });
    }
    
    // 移动端侧边栏切换按钮
    const mobileSidebarToggle = document.getElementById('mobile-sidebar-toggle');
    if (mobileSidebarToggle) {
        mobileSidebarToggle.addEventListener('click', function() {
            toggleMobileSidebar();
        });
    }
    
    // 点击背景遮罩关闭侧边栏
    const sidebarBackdrop = document.getElementById('sidebar-backdrop');
    if (sidebarBackdrop) {
        sidebarBackdrop.addEventListener('click', function() {
            toggleMobileSidebar(false);
        });
    }
    
    // 激活当前页面对应的导航项
    activateCurrentNavItem();
}

/**
 * 切换移动端侧边栏
 * @param {boolean} show - 是否显示侧边栏，不传则自动切换
 */
function toggleMobileSidebar(show) {
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('sidebar-backdrop');
    
    if (sidebar && backdrop) {
        if (show === undefined) {
            // 自动切换
            sidebar.classList.toggle('show');
            backdrop.classList.toggle('d-none');
        } else if (show) {
            // 显示侧边栏
            sidebar.classList.add('show');
            backdrop.classList.remove('d-none');
        } else {
            // 隐藏侧边栏
            sidebar.classList.remove('show');
            backdrop.classList.add('d-none');
        }
    }
}

/**
 * 激活当前页面对应的导航项
 */
function activateCurrentNavItem() {
    const currentPath = window.location.pathname;
    
    // 侧边栏导航
    const navLinks = document.querySelectorAll('.sidebar .nav-link');
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPath || 
            (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
    
    // 移动端底部导航
    const mobileNavLinks = document.querySelectorAll('.mobile-nav .nav-link');
    mobileNavLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href === currentPath || 
            (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

/**
 * 初始化移动端导航
 */
function initMobileNavigation() {
    // 监听窗口大小变化，自动隐藏侧边栏
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 768) {
            // 在大屏设备上隐藏移动端侧边栏
            toggleMobileSidebar(false);
        }
    });
    
    // 调整表格在移动端的响应式
    adjustTablesToMobile();
    
    // 添加触摸滑动支持
    addTouchSwipeSupport();
}

/**
 * 添加触摸滑动支持
 */
function addTouchSwipeSupport() {
    const content = document.getElementById('content');
    const sidebar = document.getElementById('sidebar');
    
    if (!content || !sidebar) return;
    
    let touchStartX = 0;
    let touchEndX = 0;
    
    // 处理触摸开始事件
    document.addEventListener('touchstart', function(e) {
        touchStartX = e.changedTouches[0].screenX;
    }, { passive: true });
    
    // 处理触摸结束事件
    document.addEventListener('touchend', function(e) {
        touchEndX = e.changedTouches[0].screenX;
        handleSwipe();
    }, { passive: true });
    
    // 处理滑动手势
    function handleSwipe() {
        const swipeDistance = touchEndX - touchStartX;
        const threshold = 100; // 滑动阈值
        
        // 只在手机屏幕上处理滑动
        if (window.innerWidth >= 768) return;
        
        if (swipeDistance > threshold) {
            // 从左向右滑动，打开侧边栏
            toggleMobileSidebar(true);
        } else if (swipeDistance < -threshold && sidebar.classList.contains('show')) {
            // 从右向左滑动，关闭侧边栏
            toggleMobileSidebar(false);
        }
    }
}

/**
 * 调整表格在移动端的显示方式
 */
function adjustTablesToMobile() {
    // 为大型表格添加水平滚动功能
    const tables = document.querySelectorAll('table.table');
    tables.forEach(table => {
        const wrapper = table.parentElement;
        if (!wrapper.classList.contains('table-responsive')) {
            const container = document.createElement('div');
            container.className = 'table-responsive';
            table.parentNode.insertBefore(container, table);
            container.appendChild(table);
        }
    });
}

/**
 * 初始化页面内容
 */
function initPageContent() {
    // 根据当前页面路径加载不同的内容
    const path = window.location.pathname;
    
    if (path.includes('/items')) {
        loadItems();
    } else if (path.includes('/categories')) {
        loadCategories();
    } else if (path.includes('/spaces')) {
        loadSpaces();
    } else if (path.includes('/statistics')) {
        loadStatistics();
    } else if (path.includes('/settings')) {
        loadSettings();
    }
}

/**
 * 加载物品列表
 */
function loadItems() {
    // 获取家庭ID（实际应用中可能从用户会话或本地存储中获取）
    const familyId = getFamilyId();
    
    // 获取查询参数
    const queryParams = new URLSearchParams(window.location.search);
    const current = queryParams.get('current') || 1;
    const size = queryParams.get('size') || 10;
    
    // 构建API URL
    const url = `/api/items/page?current=${current}&size=${size}&familyId=${familyId}`;
    
    // 发送请求获取物品列表
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderItemList(data.data);
            } else {
                showMessage('error', '加载物品列表失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载物品列表失败，请检查网络连接');
        });
}

/**
 * 渲染物品列表
 */
function renderItemList(pageData) {
    const itemsContainer = document.getElementById('items-container');
    if (!itemsContainer) return;
    
    // 清空容器
    itemsContainer.innerHTML = '';
    
    // 获取物品列表
    const items = pageData.records;
    
    if (items.length === 0) {
        itemsContainer.innerHTML = '<div class="alert alert-info">暂无物品数据</div>';
        return;
    }
    
    // 创建物品卡片
    items.forEach(item => {
        const itemCard = createItemCard(item);
        itemsContainer.appendChild(itemCard);
    });
    
    // 渲染分页
    renderPagination(pageData);
}

/**
 * 创建物品卡片
 */
function createItemCard(item) {
    const card = document.createElement('div');
    card.className = 'col-md-4 col-lg-3 mb-4';
    
    // 获取状态样式
    const statusClass = getStatusClass(item.status);
    
    card.innerHTML = `
        <div class="card item-card">
            <div class="card-body">
                <span class="item-status ${statusClass}">${getStatusText(item.status)}</span>
                <h5 class="card-title">${item.name}</h5>
                <p class="card-text">${item.specification || ''}</p>
                <p class="card-text"><small class="text-muted">数量: ${item.quantity}</small></p>
                <p class="card-text"><small class="text-muted">价格: ¥${item.price}</small></p>
            </div>
            <div class="card-footer bg-transparent">
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" onclick="viewItemDetail(${item.id})">详情</button>
                    <button type="button" class="btn btn-outline-secondary" onclick="editItem(${item.id})">编辑</button>
                    <button type="button" class="btn btn-outline-danger" onclick="deleteItem(${item.id})">删除</button>
                </div>
            </div>
        </div>
    `;
    
    return card;
}

/**
 * 获取状态样式类
 */
function getStatusClass(status) {
    switch (status) {
        case 'normal': return 'status-normal';
        case 'damaged': return 'status-damaged';
        case 'discarded': return 'status-discarded';
        case 'lent': return 'status-lent';
        default: return '';
    }
}

/**
 * 获取状态文本
 */
function getStatusText(status) {
    switch (status) {
        case 'normal': return '正常';
        case 'damaged': return '损坏';
        case 'discarded': return '丢弃';
        case 'lent': return '借出';
        default: return '未知';
    }
}

/**
 * 渲染分页
 */
function renderPagination(pageData) {
    const paginationContainer = document.getElementById('pagination-container');
    if (!paginationContainer) return;
    
    // 清空容器
    paginationContainer.innerHTML = '';
    
    // 创建分页元素
    const pagination = document.createElement('nav');
    pagination.setAttribute('aria-label', '物品列表分页');
    
    const ul = document.createElement('ul');
    ul.className = 'pagination justify-content-center';
    
    // 上一页
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${pageData.current <= 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `<a class="page-link" href="javascript:void(0)" onclick="goToPage(${pageData.current - 1})" tabindex="-1">上一页</a>`;
    ul.appendChild(prevLi);
    
    // 页码
    const startPage = Math.max(1, pageData.current - 2);
    const endPage = Math.min(pageData.pages, pageData.current + 2);
    
    for (let i = startPage; i <= endPage; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === pageData.current ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="javascript:void(0)" onclick="goToPage(${i})">${i}</a>`;
        ul.appendChild(li);
    }
    
    // 下一页
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${pageData.current >= pageData.pages ? 'disabled' : ''}`;
    nextLi.innerHTML = `<a class="page-link" href="javascript:void(0)" onclick="goToPage(${pageData.current + 1})">下一页</a>`;
    ul.appendChild(nextLi);
    
    pagination.appendChild(ul);
    paginationContainer.appendChild(pagination);
}

/**
 * 跳转到指定页
 */
function goToPage(page) {
    const url = new URL(window.location.href);
    url.searchParams.set('current', page);
    window.location.href = url.toString();
}

/**
 * 查看物品详情
 */
function viewItemDetail(itemId) {
    // 构建API URL
    const url = `/api/items/${itemId}`;
    
    // 发送请求获取物品详情
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showItemDetailModal(data.data);
            } else {
                showMessage('error', '加载物品详情失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载物品详情失败，请检查网络连接');
        });
}

/**
 * 显示物品详情模态框
 */
function showItemDetailModal(item) {
    // 创建模态框
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = 'itemDetailModal';
    modal.tabIndex = '-1';
    modal.setAttribute('aria-labelledby', 'itemDetailModalLabel');
    modal.setAttribute('aria-hidden', 'true');
    
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="itemDetailModalLabel">${item.name} - 详情</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>规格：</strong> ${item.specification || '无'}</p>
                            <p><strong>数量：</strong> ${item.quantity}</p>
                            <p><strong>价格：</strong> ¥${item.price}</p>
                            <p><strong>购买日期：</strong> ${item.purchaseDate || '未知'}</p>
                            <p><strong>保修期：</strong> ${item.warrantyPeriod ? item.warrantyPeriod + '个月' : '无'}</p>
                            <p><strong>保修截止日期：</strong> ${item.warrantyEndDate || '无'}</p>
                        </div>
                        <div class="col-md-6">
                            <p><strong>使用频率：</strong> ${getUsageFrequencyText(item.usageFrequency)}</p>
                            <p><strong>使用年限：</strong> ${item.usageYears ? item.usageYears + '年' : '未知'}</p>
                            <p><strong>状态：</strong> <span class="badge ${getStatusClass(item.status)}">${getStatusText(item.status)}</span></p>
                            <p><strong>备注：</strong> ${item.remark || '无'}</p>
                            <p><strong>创建时间：</strong> ${formatDateTime(item.createTime)}</p>
                            <p><strong>更新时间：</strong> ${formatDateTime(item.updateTime)}</p>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                    <button type="button" class="btn btn-primary" onclick="editItem(${item.id})">编辑</button>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // 显示模态框
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
    
    // 模态框关闭后移除DOM
    modal.addEventListener('hidden.bs.modal', function() {
        document.body.removeChild(modal);
    });
}

/**
 * 获取使用频率文本
 */
function getUsageFrequencyText(frequency) {
    switch (frequency) {
        case 'daily': return '每天';
        case 'weekly': return '每周';
        case 'monthly': return '每月';
        case 'rarely': return '很少';
        default: return '未知';
    }
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '未知';
    
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * 编辑物品
 */
function editItem(itemId) {
    // 构建API URL
    const url = `/api/items/${itemId}`;
    
    // 发送请求获取物品详情
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showItemEditModal(data.data);
            } else {
                showMessage('error', '加载物品详情失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载物品详情失败，请检查网络连接');
        });
}

/**
 * 显示物品编辑模态框
 */
function showItemEditModal(item) {
    // 创建模态框
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.id = 'itemEditModal';
    modal.tabIndex = '-1';
    modal.setAttribute('aria-labelledby', 'itemEditModalLabel');
    modal.setAttribute('aria-hidden', 'true');
    
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="itemEditModalLabel">编辑物品</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="itemEditForm">
                        <input type="hidden" id="itemId" value="${item.id}">
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="name" class="form-label">物品名称</label>
                                <input type="text" class="form-control" id="name" value="${item.name}" required>
                            </div>
                            <div class="col-md-6">
                                <label for="specification" class="form-label">规格</label>
                                <input type="text" class="form-control" id="specification" value="${item.specification || ''}">
                            </div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="quantity" class="form-label">数量</label>
                                <input type="number" class="form-control" id="quantity" value="${item.quantity}" min="1" required>
                            </div>
                            <div class="col-md-6">
                                <label for="price" class="form-label">价格</label>
                                <input type="number" class="form-control" id="price" value="${item.price}" min="0" step="0.01" required>
                            </div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="purchaseDate" class="form-label">购买日期</label>
                                <input type="date" class="form-control" id="purchaseDate" value="${item.purchaseDate || ''}">
                            </div>
                            <div class="col-md-6">
                                <label for="warrantyPeriod" class="form-label">保修期（月）</label>
                                <input type="number" class="form-control" id="warrantyPeriod" value="${item.warrantyPeriod || ''}" min="0">
                            </div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="usageFrequency" class="form-label">使用频率</label>
                                <select class="form-select" id="usageFrequency">
                                    <option value="daily" ${item.usageFrequency === 'daily' ? 'selected' : ''}>每天</option>
                                    <option value="weekly" ${item.usageFrequency === 'weekly' ? 'selected' : ''}>每周</option>
                                    <option value="monthly" ${item.usageFrequency === 'monthly' ? 'selected' : ''}>每月</option>
                                    <option value="rarely" ${item.usageFrequency === 'rarely' ? 'selected' : ''}>很少</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="usageYears" class="form-label">使用年限</label>
                                <input type="number" class="form-control" id="usageYears" value="${item.usageYears || ''}" min="0">
                            </div>
                        </div>
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="status" class="form-label">状态</label>
                                <select class="form-select" id="status">
                                    <option value="normal" ${item.status === 'normal' ? 'selected' : ''}>正常</option>
                                    <option value="damaged" ${item.status === 'damaged' ? 'selected' : ''}>损坏</option>
                                    <option value="discarded" ${item.status === 'discarded' ? 'selected' : ''}>丢弃</option>
                                    <option value="lent" ${item.status === 'lent' ? 'selected' : ''}>借出</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="spaceId" class="form-label">存放空间</label>
                                <select class="form-select" id="spaceId">
                                    <!-- 空间选项将通过AJAX加载 -->
                                </select>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label for="remark" class="form-label">备注</label>
                            <textarea class="form-control" id="remark" rows="3">${item.remark || ''}</textarea>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-primary" onclick="saveItem()">保存</button>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // 加载空间选项
    loadSpaceOptions(item.spaceId);
    
    // 显示模态框
    const modalInstance = new bootstrap.Modal(modal);
    modalInstance.show();
    
    // 模态框关闭后移除DOM
    modal.addEventListener('hidden.bs.modal', function() {
        document.body.removeChild(modal);
    });
}

/**
 * 加载空间选项
 */
function loadSpaceOptions(selectedSpaceId) {
    // 获取家庭ID
    const familyId = getFamilyId();
    
    // 构建API URL
    const url = `/api/spaces/tree?familyId=${familyId}`;
    
    // 发送请求获取空间树
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderSpaceOptions(data.data, selectedSpaceId);
            } else {
                showMessage('error', '加载空间列表失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载空间列表失败，请检查网络连接');
        });
}

/**
 * 渲染空间选项
 */
function renderSpaceOptions(spaces, selectedSpaceId, level = 0) {
    const spaceSelect = document.getElementById('spaceId');
    if (!spaceSelect) return;
    
    // 清空选项（仅在顶层调用时）
    if (level === 0) {
        spaceSelect.innerHTML = '<option value="">请选择</option>';
    }
    
    // 递归渲染空间选项
    spaces.forEach(space => {
        const option = document.createElement('option');
        option.value = space.id;
        option.text = '　'.repeat(level) + space.name;
        option.selected = space.id === selectedSpaceId;
        spaceSelect.appendChild(option);
        
        // 递归处理子空间
        if (space.children && space.children.length > 0) {
            renderSpaceOptions(space.children, selectedSpaceId, level + 1);
        }
    });
}

/**
 * 保存物品
 */
function saveItem() {
    // 获取表单数据
    const itemId = document.getElementById('itemId').value;
    const name = document.getElementById('name').value;
    const specification = document.getElementById('specification').value;
    const quantity = document.getElementById('quantity').value;
    const price = document.getElementById('price').value;
    const purchaseDate = document.getElementById('purchaseDate').value;
    const warrantyPeriod = document.getElementById('warrantyPeriod').value;
    const usageFrequency = document.getElementById('usageFrequency').value;
    const usageYears = document.getElementById('usageYears').value;
    const status = document.getElementById('status').value;
    const spaceId = document.getElementById('spaceId').value;
    const remark = document.getElementById('remark').value;
    const categoryId = document.getElementById('categoryId')?.value; // 可选的分类ID
    
    // 表单验证
    if (!name) {
        showMessage('error', '物品名称不能为空');
        return;
    }
    
    if (!quantity || quantity < 1) {
        showMessage('error', '数量必须大于0');
        return;
    }
    
    if (!price || price < 0) {
        showMessage('error', '价格不能为负数');
        return;
    }
    
    // 构建请求数据
    const itemData = {
        id: itemId,
        name: name,
        specification: specification,
        quantity: parseInt(quantity),
        price: parseFloat(price),
        purchaseDate: purchaseDate || null,
        warrantyPeriod: warrantyPeriod ? parseInt(warrantyPeriod) : null,
        usageFrequency: usageFrequency,
        usageYears: usageYears ? parseInt(usageYears) : null,
        status: status,
        spaceId: spaceId || null,
        remark: remark,
        familyId: getFamilyId(),
        categoryId: categoryId || null
    };
    
    // 记录到最近查看的物品
    saveLastViewedItem({
        id: itemId,
        name: name
    });
    
    // 构建API URL和方法
    let url = '/api/items';
    let method = itemId ? 'PUT' : 'POST';
    
    // 发送请求保存物品
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(itemData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', itemId ? '保存成功' : '添加成功');
                
                // 关闭模态框
                const modalId = itemId ? 'itemEditModal' : 'addItemModal';
                const modal = bootstrap.Modal.getInstance(document.getElementById(modalId));
                if (modal) {
                    modal.hide();
                }
                
                // 如果在列表页，刷新物品列表
                if (window.location.pathname.includes('/items') && typeof loadItems === 'function') {
                    loadItems();
                } else {
                    // 否则重定向到物品列表页
                    window.location.href = '/items';
                }
            } else {
                showMessage('error', (itemId ? '保存' : '添加') + '失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', (itemId ? '保存' : '添加') + '失败，请检查网络连接');
        });
}

/**
 * 删除物品
 */
function deleteItem(itemId) {
    // 确认删除
    if (!confirm('确定要删除该物品吗？')) {
        return;
    }
    
    // 构建API URL
    const url = `/api/items/${itemId}`;
    
    // 发送请求删除物品
    fetch(url, {
        method: 'DELETE'
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', '删除成功');
                // 刷新物品列表
                loadItems();
            } else {
                showMessage('error', '删除失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '删除失败，请检查网络连接');
        });
}

/**
 * 初始化表单验证
 */
function initFormValidation() {
    // 可以使用Bootstrap表单验证或其他表单验证库
}

/**
 * 初始化图表
 */
function initCharts() {
    // 加载统计数据
    loadStatisticsData();
}

/**
 * 加载统计数据
 */
function loadStatisticsData() {
    // 获取家庭ID
    const familyId = getFamilyId();
    
    // 加载分类统计
    loadCategoryStats(familyId);
    
    // 加载空间统计
    loadSpaceStats(familyId);
    
    // 加载使用频率统计
    loadFrequencyStats(familyId);
    
    // 加载物品总价值
    loadTotalValue(familyId);
}

/**
 * 加载分类统计
 */
function loadCategoryStats(familyId) {
    // 构建API URL
    const url = `/api/items/stat/category?familyId=${familyId}`;
    
    // 发送请求获取分类统计数据
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderCategoryChart(data.data);
            } else {
                showMessage('error', '加载分类统计失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载分类统计失败，请检查网络连接');
        });
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
 * 获取家庭ID
 */
function getFamilyId() {
    // 从URL中获取家庭ID参数
    const urlParams = new URLSearchParams(window.location.search);
    const familyId = urlParams.get('familyId');
    
    // 如果URL中没有家庭ID，则从localStorage中获取
    if (!familyId) {
        return getFromLocalStorage(LS_KEYS.CURRENT_FAMILY_ID) || '1';
    }
    
    // 如果从URL中获取到了，则保存到localStorage中
    saveToLocalStorage(LS_KEYS.CURRENT_FAMILY_ID, familyId);
    
    return familyId;
}

/**
 * LocalStorage操作工具
 */

/**
 * 保存数据到LocalStorage
 * @param {string} key - 存储键名
 * @param {*} value - 需要存储的值
 */
function saveToLocalStorage(key, value) {
    try {
        if (typeof value === 'object') {
            localStorage.setItem(key, JSON.stringify(value));
        } else {
            localStorage.setItem(key, value);
        }
        return true;
    } catch (error) {
        console.error('保存到LocalStorage失败:', error);
        return false;
    }
}

/**
 * 从LocalStorage获取数据
 * @param {string} key - 存储键名
 * @param {*} defaultValue - 如果未找到数据，返回的默认值
 * @returns {*} 存储的数据或默认值
 */
function getFromLocalStorage(key, defaultValue = null) {
    try {
        const value = localStorage.getItem(key);
        
        if (value === null) {
            return defaultValue;
        }
        
        // 尝试解析JSON
        try {
            return JSON.parse(value);
        } catch (e) {
            // 如果不是JSON，直接返回值
            return value;
        }
    } catch (error) {
        console.error('从LocalStorage获取数据失败:', error);
        return defaultValue;
    }
}

/**
 * 从LocalStorage删除数据
 * @param {string} key - 要删除的键名
 */
function removeFromLocalStorage(key) {
    try {
        localStorage.removeItem(key);
        return true;
    } catch (error) {
        console.error('从LocalStorage删除数据失败:', error);
        return false;
    }
}

/**
 * 清空LocalStorage中的所有数据
 */
function clearLocalStorage() {
    try {
        localStorage.clear();
        return true;
    } catch (error) {
        console.error('清空LocalStorage失败:', error);
        return false;
    }
}

/**
 * 保存用户最近查看的物品
 * @param {Object} item - 物品对象
 */
function saveLastViewedItem(item) {
    if (!item || !item.id) return;
    
    // 获取已保存的最近查看物品
    let lastViewedItems = getFromLocalStorage(LS_KEYS.LAST_VIEWED_ITEMS, []);
    
    // 移除相同ID的物品（如果存在）
    lastViewedItems = lastViewedItems.filter(i => i.id !== item.id);
    
    // 在数组开头添加新物品
    lastViewedItems.unshift({
        id: item.id,
        name: item.name,
        timestamp: new Date().toISOString()
    });
    
    // 限制最多保存10个
    if (lastViewedItems.length > 10) {
        lastViewedItems = lastViewedItems.slice(0, 10);
    }
    
    // 保存回LocalStorage
    saveToLocalStorage(LS_KEYS.LAST_VIEWED_ITEMS, lastViewedItems);
}

/**
 * 获取用户最近查看的物品
 * @returns {Array} 最近查看的物品数组
 */
function getLastViewedItems() {
    return getFromLocalStorage(LS_KEYS.LAST_VIEWED_ITEMS, []);
}

/**
 * 保存过滤器首选项
 * @param {string} pageType - 页面类型，如'items', 'categories'
 * @param {Object} preferences - 过滤器首选项
 */
function saveFilterPreferences(pageType, preferences) {
    // 获取现有首选项
    const allPreferences = getFromLocalStorage(LS_KEYS.FILTER_PREFERENCES, {});
    
    // 更新特定页面的首选项
    allPreferences[pageType] = preferences;
    
    // 保存回LocalStorage
    saveToLocalStorage(LS_KEYS.FILTER_PREFERENCES, allPreferences);
}

/**
 * 获取过滤器首选项
 * @param {string} pageType - 页面类型，如'items', 'categories'
 * @returns {Object} 过滤器首选项
 */
function getFilterPreferences(pageType) {
    const allPreferences = getFromLocalStorage(LS_KEYS.FILTER_PREFERENCES, {});
    return allPreferences[pageType] || {};
}