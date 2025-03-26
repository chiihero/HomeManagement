/**
 * 数据备份与恢复功能
 */

// 当页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 备份按钮事件
    const backupBtn = document.getElementById('backupBtn');
    if (backupBtn) {
        backupBtn.addEventListener('click', function() {
            showBackupDialog();
        });
    }
    
    // 恢复按钮事件
    const restoreBtn = document.getElementById('restoreBtn');
    if (restoreBtn) {
        restoreBtn.addEventListener('click', function() {
            showRestoreDialog();
        });
    }
    
    // 确认备份按钮事件
    const confirmBackupBtn = document.getElementById('confirmBackupBtn');
    if (confirmBackupBtn) {
        confirmBackupBtn.addEventListener('click', function() {
            backupData();
        });
    }
    
    // 确认恢复按钮事件
    const confirmRestoreBtn = document.getElementById('confirmRestoreBtn');
    if (confirmRestoreBtn) {
        confirmRestoreBtn.addEventListener('click', function() {
            restoreData();
        });
    }
});

/**
 * 显示备份对话框
 */
function showBackupDialog() {
    const backupModal = new bootstrap.Modal(document.getElementById('backupModal'));
    backupModal.show();
}

/**
 * 显示恢复对话框
 */
function showRestoreDialog() {
    const restoreModal = new bootstrap.Modal(document.getElementById('restoreModal'));
    restoreModal.show();
}

/**
 * 备份数据
 */
function backupData() {
    // 获取家庭ID
    const familyId = getFamilyId();
    
    // 发送备份请求
    fetch(`/api/backup?familyId=${familyId}`, {
        method: 'GET'
    })
    .then(response => {
        if (response.ok) {
            return response.blob();
        } else {
            throw new Error('备份请求失败');
        }
    })
    .then(blob => {
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        
        // 设置文件名
        const date = new Date();
        const formattedDate = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
        a.download = `家庭物品管理系统备份_${formattedDate}.zip`;
        
        // 触发下载
        document.body.appendChild(a);
        a.click();
        
        // 清理
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        
        // 关闭模态框
        const backupModal = bootstrap.Modal.getInstance(document.getElementById('backupModal'));
        backupModal.hide();
        
        // 显示成功提示
        showMessage('备份成功，文件已开始下载', 'success');
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('备份失败，请检查网络连接或联系管理员', 'error');
    });
}

/**
 * 恢复数据
 */
function restoreData() {
    // 获取上传的文件
    const fileInput = document.getElementById('restoreFile');
    if (!fileInput.files.length) {
        showMessage('请选择备份文件', 'error');
        return;
    }
    
    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);
    formData.append('familyId', getFamilyId());
    
    // 发送恢复请求
    fetch('/api/restore', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            // 关闭模态框
            const restoreModal = bootstrap.Modal.getInstance(document.getElementById('restoreModal'));
            restoreModal.hide();
            
            // 重置表单
            document.getElementById('restoreForm').reset();
            
            // 显示成功提示
            showMessage('数据恢复成功', 'success');
            
            // 3秒后刷新页面
            setTimeout(() => {
                window.location.reload();
            }, 3000);
        } else {
            showMessage(`恢复失败: ${data.message}`, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('恢复失败，请检查网络连接或联系管理员', 'error');
    });
}

/**
 * 显示消息提示
 * 
 * @param {string} message 消息内容
 * @param {string} type 消息类型：success 或 error
 */
function showMessage(message, type) {
    // 创建提示元素
    const toastContainer = document.createElement('div');
    toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    toastContainer.style.zIndex = '1050';
    
    const toastEl = document.createElement('div');
    toastEl.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'}`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');
    
    const toastBody = document.createElement('div');
    toastBody.className = 'd-flex';
    
    const toastText = document.createElement('div');
    toastText.className = 'toast-body';
    toastText.textContent = message;
    
    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.className = 'btn-close btn-close-white me-2 m-auto';
    closeButton.setAttribute('data-bs-dismiss', 'toast');
    closeButton.setAttribute('aria-label', '关闭');
    
    toastBody.appendChild(toastText);
    toastBody.appendChild(closeButton);
    toastEl.appendChild(toastBody);
    toastContainer.appendChild(toastEl);
    document.body.appendChild(toastContainer);
    
    const toast = new bootstrap.Toast(toastEl, {
        delay: 3000
    });
    toast.show();
    
    // 自动删除元素
    toastEl.addEventListener('hidden.bs.toast', function() {
        toastContainer.remove();
    });
}

/**
 * 获取家庭ID
 * @returns {string} 家庭ID
 */
function getFamilyId() {
    return localStorage.getItem('currentFamilyId') || sessionStorage.getItem('currentFamilyId') || '1';
} 