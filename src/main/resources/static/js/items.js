/**
 * 家庭物品管理系统物品管理JavaScript文件
 */

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 获取当前页面路径
    const path = window.location.pathname;
    
    // 根据不同页面初始化不同功能
    if (path.includes('/items/edit/')) {
        initItemEditPage();
    } else if (path.match(/\/items\/\d+$/)) {
        initItemDetailPage();
    } else if (path.includes('/items')) {
        initItemListPage();
    }
});

/**
 * 初始化物品列表页面
 */
function initItemListPage() {
    // 加载物品列表
    loadItems();
    
    // 初始化搜索功能
    document.getElementById('searchButton').addEventListener('click', function() {
        const keyword = document.getElementById('searchInput').value.trim();
        if (keyword) {
            searchItems(keyword);
        } else {
            loadItems();
        }
    });
    
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const keyword = this.value.trim();
            if (keyword) {
                searchItems(keyword);
            } else {
                loadItems();
            }
        }
    });
    
    // 初始化添加物品表单
    initAddItemForm();
}

/**
 * 初始化物品详情页面
 */
function initItemDetailPage() {
    // 获取物品ID
    const itemId = window.location.pathname.split('/').pop();
    
    // 初始化删除功能
    document.querySelector('#deleteConfirmModal .btn-danger').addEventListener('click', function() {
        deleteItem(itemId);
    });
    
    // 初始化物品生命周期管理
    initLifecycleManagement(itemId);
    
    // 初始化物品借用管理
    initBorrowManagement(itemId);
}

/**
 * 初始化物品编辑页面
 */
function initItemEditPage() {
    // 获取物品ID
    const itemId = document.getElementById('itemId').value;
    
    // 加载分类下拉框
    loadCategoriesDropdown();
    
    // 加载空间下拉框
    loadSpacesDropdown();
    
    // 初始化表单提交
    document.getElementById('editItemForm').addEventListener('submit', function(e) {
        e.preventDefault();
        updateItem(itemId);
    });
}

/**
 * 初始化物品生命周期管理
 */
function initLifecycleManagement(itemId) {
    // 加载维修记录
    loadRepairRecords(itemId);
    
    // 加载转移记录
    loadTransferRecords(itemId);
    
    // 初始化添加维修记录表单
    document.getElementById('addRepairBtn').addEventListener('click', function() {
        // 显示添加维修记录模态框
        const modal = new bootstrap.Modal(document.getElementById('addRepairModal'));
        modal.show();
    });
    
    // 初始化添加转移记录表单
    document.getElementById('addTransferBtn').addEventListener('click', function() {
        // 显示添加转移记录模态框
        const modal = new bootstrap.Modal(document.getElementById('addTransferModal'));
        modal.show();
    });
    
    // 初始化报废处理表单
    document.getElementById('discardItemBtn').addEventListener('click', function() {
        // 显示报废处理模态框
        const modal = new bootstrap.Modal(document.getElementById('discardItemModal'));
        modal.show();
    });
}

/**
 * 初始化物品借用管理
 */
function initBorrowManagement(itemId) {
    // 加载借用记录
    loadBorrowRecords(itemId);
    
    // 初始化添加借用记录表单
    document.getElementById('addBorrowBtn').addEventListener('click', function() {
        // 显示添加借用记录模态框
        const modal = new bootstrap.Modal(document.getElementById('addBorrowModal'));
        modal.show();
    });
}

/**
 * 加载维修记录
 */
function loadRepairRecords(itemId) {
    fetch(`/api/items/${itemId}/repairs`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderRepairRecords(data.data);
            } else {
                showMessage('error', '加载维修记录失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载维修记录失败，请检查网络连接');
        });
}

/**
 * 加载转移记录
 */
function loadTransferRecords(itemId) {
    fetch(`/api/items/${itemId}/transfers`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderTransferRecords(data.data);
            } else {
                showMessage('error', '加载转移记录失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载转移记录失败，请检查网络连接');
        });
}

/**
 * 加载借用记录
 */
function loadBorrowRecords(itemId) {
    fetch(`/api/items/${itemId}/borrows`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                renderBorrowRecords(data.data);
            } else {
                showMessage('error', '加载借用记录失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载借用记录失败，请检查网络连接');
        });
}

/**
 * 渲染维修记录
 */
function renderRepairRecords(records) {
    const container = document.getElementById('repair-records');
    if (!container) return;
    
    if (records.length === 0) {
        container.innerHTML = '<div class="alert alert-info">暂无维修记录</div>';
        return;
    }
    
    let html = '<div class="list-group">';
    records.forEach(record => {
        html += `
            <div class="list-group-item">
                <div class="d-flex w-100 justify-content-between">
                    <h5 class="mb-1">${record.repairType}</h5>
                    <small>${formatDate(record.repairDate)}</small>
                </div>
                <p class="mb-1">${record.description}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small>维修费用: ¥${record.cost.toFixed(2)}</small>
                    <small>维修人: ${record.repairPerson}</small>
                </div>
            </div>
        `;
    });
    html += '</div>';
    
    container.innerHTML = html;
}

/**
 * 渲染转移记录
 */
function renderTransferRecords(records) {
    const container = document.getElementById('transfer-records');
    if (!container) return;
    
    if (records.length === 0) {
        container.innerHTML = '<div class="alert alert-info">暂无转移记录</div>';
        return;
    }
    
    let html = '<div class="list-group">';
    records.forEach(record => {
        html += `
            <div class="list-group-item">
                <div class="d-flex w-100 justify-content-between">
                    <h5 class="mb-1">${record.transferType}</h5>
                    <small>${formatDate(record.transferDate)}</small>
                </div>
                <p class="mb-1">${record.description}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small>接收人: ${record.receiver}</small>
                    <small>价值: ¥${record.value.toFixed(2)}</small>
                </div>
            </div>
        `;
    });
    html += '</div>';
    
    container.innerHTML = html;
}

/**
 * 渲染借用记录
 */
function renderBorrowRecords(records) {
    const container = document.getElementById('borrow-records');
    if (!container) return;
    
    if (records.length === 0) {
        container.innerHTML = '<div class="alert alert-info">暂无借用记录</div>';
        return;
    }
    
    let html = '<div class="list-group">';
    records.forEach(record => {
        const isOverdue = new Date(record.expectedReturnDate) < new Date() && record.status === 'borrowed';
        const statusClass = record.status === 'returned' ? 'bg-success' : (isOverdue ? 'bg-danger' : 'bg-info');
        const statusText = record.status === 'returned' ? '已归还' : (isOverdue ? '已逾期' : '借出中');
        
        html += `
            <div class="list-group-item">
                <div class="d-flex w-100 justify-content-between">
                    <h5 class="mb-1">${record.borrower}</h5>
                    <span class="badge ${statusClass}">${statusText}</span>
                </div>
                <p class="mb-1">${record.purpose}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small>借出日期: ${formatDate(record.borrowDate)}</small>
                    <small>预计归还: ${formatDate(record.expectedReturnDate)}</small>
                </div>
                ${record.status === 'returned' ? `<small>实际归还: ${formatDate(record.actualReturnDate)}</small>` : ''}
                ${record.status === 'borrowed' ? `<button class="btn btn-sm btn-outline-primary mt-2" onclick="returnItem(${record.id})">标记归还</button>` : ''}
            </div>
        `;
    });
    html += '</div>';
    
    container.innerHTML = html;
}

/**
 * 添加维修记录
 */
function addRepairRecord(itemId) {
    const repairType = document.getElementById('repairType').value;
    const repairDate = document.getElementById('repairDate').value;
    const repairPerson = document.getElementById('repairPerson').value;
    const cost = document.getElementById('repairCost').value;
    const description = document.getElementById('repairDescription').value;
    
    const data = {
        itemId: itemId,
        repairType: repairType,
        repairDate: repairDate,
        repairPerson: repairPerson,
        cost: cost,
        description: description
    };
    
    fetch('/api/items/repairs', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', '添加维修记录成功');
                // 关闭模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('addRepairModal'));
                modal.hide();
                // 重新加载维修记录
                loadRepairRecords(itemId);
            } else {
                showMessage('error', '添加维修记录失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '添加维修记录失败，请检查网络连接');
        });
}

/**
 * 添加转移记录
 */
function addTransferRecord(itemId) {
    const transferType = document.getElementById('transferType').value;
    const transferDate = document.getElementById('transferDate').value;
    const receiver = document.getElementById('receiver').value;
    const value = document.getElementById('transferValue').value;
    const description = document.getElementById('transferDescription').value;
    
    const data = {
        itemId: itemId,
        transferType: transferType,
        transferDate: transferDate,
        receiver: receiver,
        value: value,
        description: description
    };
    
    fetch('/api/items/transfers', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', '添加转移记录成功');
                // 关闭模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('addTransferModal'));
                modal.hide();
                // 重新加载转移记录
                loadTransferRecords(itemId);
            } else {
                showMessage('error', '添加转移记录失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '添加转移记录失败，请检查网络连接');
        });
}

/**
 * 添加借用记录
 */
function addBorrowRecord(itemId) {
    const borrower = document.getElementById('borrower').value;
    const borrowDate = document.getElementById('borrowDate').value;
    const expectedReturnDate = document.getElementById('expectedReturnDate').value;
    const purpose = document.getElementById('borrowPurpose').value;
    
    const data = {
        itemId: itemId,
        borrower: borrower,
        borrowDate: borrowDate,
        expectedReturnDate: expectedReturnDate,
        purpose: purpose,
        status: 'borrowed'
    };
    
    fetch('/api/items/borrows', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', '添加借用记录成功');
                // 关闭模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('addBorrowModal'));
                modal.hide();
                // 重新加载借用记录
                loadBorrowRecords(itemId);
            } else {
                showMessage('error', '添加借用记录失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '添加借用记录失败，请检查网络连接');
        });
}

/**
 * 标记物品归还
 */
function returnItem(borrowId) {
    const data = {
        id: borrowId,
        status: 'returned',
        actualReturnDate: new Date().toISOString().split('T')[0]
    };
    
    fetch(`/api/items/borrows/${borrowId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', '物品归还成功');
                // 获取物品ID
                const itemId = window.location.pathname.split('/').pop();
                // 重新加载借用记录
                loadBorrowRecords(itemId);
            } else {
                showMessage('error', '物品归还失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '物品归还失败，请检查网络连接');
        });
}

/**
 * 报废处理
 */
function discardItem(itemId) {
    const discardDate = document.getElementById('discardDate').value;
    const discardReason = document.getElementById('discardReason').value;
    
    const data = {
        id: itemId,
        status: 'discarded',
        discardDate: discardDate,
        discardReason: discardReason
    };
    
    fetch(`/api/items/${itemId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                showMessage('success', '物品报废成功');
                // 关闭模态框
                const modal = bootstrap.Modal.getInstance(document.getElementById('discardItemModal'));
                modal.hide();
                // 刷新页面
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            } else {
                showMessage('error', '物品报废失败：' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '物品报废失败，请检查网络连接');
        });
}

/**
 * 格式化日期
 */
function formatDate(dateString) {
    if (!dateString) return '未知';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
}

/**
 * 显示消息提示
 */
function showMessage(type, message) {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    
    const alertContainer = document.getElementById('alert-container');
    if (alertContainer) {
        alertContainer.innerHTML = alertHtml;
        
        // 5秒后自动关闭
        setTimeout(() => {
            const alert = alertContainer.querySelector('.alert');
            if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    }
}