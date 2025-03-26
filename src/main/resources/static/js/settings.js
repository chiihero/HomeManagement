/**
 * 系统设置页面的JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 初始化Bootstrap组件
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // 头像上传相关
    const changeAvatarBtn = document.getElementById('changeAvatarBtn');
    const avatarInput = document.getElementById('avatarInput');
    const avatarModal = new bootstrap.Modal(document.getElementById('avatarModal'));
    let cropper;
    
    if (changeAvatarBtn && avatarInput) {
        changeAvatarBtn.addEventListener('click', function() {
            avatarInput.click();
        });
        
        avatarInput.addEventListener('change', function(e) {
            if (e.target.files.length) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    // 显示裁剪模态框
                    const cropperContainer = document.getElementById('avatar-cropper');
                    cropperContainer.innerHTML = '<img id="avatar-image" src="' + e.target.result + '" style="max-width: 100%;">';
                    
                    // 初始化裁剪工具
                    const image = document.getElementById('avatar-image');
                    cropper = new Cropper(image, {
                        aspectRatio: 1,
                        viewMode: 1,
                        dragMode: 'move',
                        autoCropArea: 0.8,
                        restore: false,
                        guides: true,
                        center: true,
                        highlight: false,
                        cropBoxMovable: true,
                        cropBoxResizable: true,
                        toggleDragModeOnDblclick: false
                    });
                    
                    avatarModal.show();
                };
                reader.readAsDataURL(e.target.files[0]);
            }
        });
        
        // 保存裁剪后的头像
        const saveAvatarBtn = document.getElementById('saveAvatarBtn');
        if (saveAvatarBtn) {
            saveAvatarBtn.addEventListener('click', function() {
                if (!cropper) return;
                
                // 获取裁剪后的图片数据
                const canvas = cropper.getCroppedCanvas({
                    width: 300,
                    height: 300,
                    minWidth: 150,
                    minHeight: 150,
                    maxWidth: 500,
                    maxHeight: 500,
                    fillColor: '#fff',
                    imageSmoothingEnabled: true,
                    imageSmoothingQuality: 'high'
                });
                
                // 转换为Blob
                canvas.toBlob(function(blob) {
                    // 创建FormData对象
                    const formData = new FormData();
                    formData.append('avatar', blob, 'avatar.png');
                    
                    // 发送到服务器
                    fetch('/api/user/avatar', {
                        method: 'POST',
                        body: formData
                    })
                    .then(response => response.json())
                    .then(data => {
                        if (data.code === 200) {
                            // 更新页面上的头像
                            const avatarImages = document.querySelectorAll('.avatar-image');
                            avatarImages.forEach(img => {
                                img.src = data.data.avatarUrl + '?t=' + new Date().getTime();
                            });
                            
                            // 关闭模态框
                            avatarModal.hide();
                            
                            // 显示成功消息
                            showToast('头像更新成功', 'success');
                        } else {
                            showToast('头像更新失败: ' + data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showToast('头像更新失败，请检查网络连接', 'error');
                    });
                }, 'image/png');
            });
        }
    }
    
    // 个人资料表单提交
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const userData = {
                username: document.getElementById('username').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value
            };
            
            // 发送到服务器
            fetch('/api/user/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200) {
                    showToast('个人资料更新成功', 'success');
                } else {
                    showToast('个人资料更新失败: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('个人资料更新失败，请检查网络连接', 'error');
            });
        });
    }
    
    // 密码修改表单提交
    const passwordForm = document.getElementById('passwordForm');
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            // 验证新密码和确认密码是否一致
            if (newPassword !== confirmPassword) {
                showToast('新密码和确认密码不一致', 'error');
                return;
            }
            
            // 发送到服务器
            fetch('/api/user/password', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    currentPassword: currentPassword,
                    newPassword: newPassword
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200) {
                    showToast('密码修改成功', 'success');
                    // 清空表单
                    passwordForm.reset();
                } else {
                    showToast('密码修改失败: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('密码修改失败，请检查网络连接', 'error');
            });
        });
    }
    
    // 通知设置表单提交
    const notificationForm = document.getElementById('notificationForm');
    if (notificationForm) {
        notificationForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const notificationData = {
                emailNotification: document.getElementById('emailNotification').checked,
                warrantyNotification: document.getElementById('warrantyNotification').checked,
                expiryNotification: document.getElementById('expiryNotification').checked,
                reminderDays: parseInt(document.getElementById('reminderDays').value)
            };
            
            // 发送到服务器
            fetch('/api/user/notifications', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(notificationData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200) {
                    showToast('通知设置更新成功', 'success');
                } else {
                    showToast('通知设置更新失败: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('通知设置更新失败，请检查网络连接', 'error');
            });
        });
    }
    
    // 显示提示消息
    function showToast(message, type) {
        // 创建toast元素
        const toastContainer = document.createElement('div');
        toastContainer.className = 'position-fixed bottom-0 end-0 p-3';
        toastContainer.style.zIndex = '5';
        
        const toastEl = document.createElement('div');
        toastEl.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'}`;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');
        
        const toastBody = document.createElement('div');
        toastBody.className = 'd-flex';
        
        const messageDiv = document.createElement('div');
        messageDiv.className = 'toast-body';
        messageDiv.textContent = message;
        
        const closeButton = document.createElement('button');
        closeButton.type = 'button';
        closeButton.className = 'btn-close btn-close-white me-2 m-auto';
        closeButton.setAttribute('data-bs-dismiss', 'toast');
        closeButton.setAttribute('aria-label', '关闭');
        
        toastBody.appendChild(messageDiv);
        toastBody.appendChild(closeButton);
        toastEl.appendChild(toastBody);
        toastContainer.appendChild(toastEl);
        
        document.body.appendChild(toastContainer);
        
        const toast = new bootstrap.Toast(toastEl, {
            animation: true,
            autohide: true,
            delay: 3000
        });
        
        toast.show();
        
        // 监听隐藏事件，移除DOM元素
        toastEl.addEventListener('hidden.bs.toast', function() {
            document.body.removeChild(toastContainer);
        });
    }
    
    // 系统设置表单提交
    const systemForm = document.getElementById('systemForm');
    if (systemForm) {
        // 从LocalStorage加载用户设置
        const userSettings = getFromLocalStorage(LS_KEYS.USER_SETTINGS, {
            language: 'zh_CN',
            theme: 'light',
            dateFormat: 'yyyy-MM-dd',
            currency: 'CNY',
            autoBackup: false,
            backupFrequency: 'monthly'
        });
        
        // 设置表单初始值
        document.getElementById('language').value = userSettings.language;
        document.getElementById('theme').value = userSettings.theme;
        document.getElementById('dateFormat').value = userSettings.dateFormat;
        document.getElementById('currency').value = userSettings.currency;
        document.getElementById('autoBackup').checked = userSettings.autoBackup;
        document.getElementById('backupFrequency').value = userSettings.backupFrequency;
        
        // 应用当前主题
        if (userSettings.theme === 'dark') {
            document.body.classList.add('dark-theme');
        } else {
            document.body.classList.remove('dark-theme');
        }
        
        systemForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const systemData = {
                language: document.getElementById('language').value,
                theme: document.getElementById('theme').value,
                dateFormat: document.getElementById('dateFormat').value,
                currency: document.getElementById('currency').value,
                autoBackup: document.getElementById('autoBackup').checked,
                backupFrequency: document.getElementById('backupFrequency').value
            };
            
            // 保存设置到LocalStorage
            saveToLocalStorage(LS_KEYS.USER_SETTINGS, systemData);
            saveToLocalStorage(LS_KEYS.THEME, systemData.theme);
            
            // 发送到服务器
            fetch('/api/settings', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(systemData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.code === 200) {
                    showToast('系统设置更新成功', 'success');
                    
                    // 应用主题设置
                    if (systemData.theme === 'dark') {
                        document.body.classList.add('dark-theme');
                    } else if (systemData.theme === 'light') {
                        document.body.classList.remove('dark-theme');
                    }
                } else {
                    showToast('系统设置更新失败: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('系统设置更新失败，请检查网络连接', 'error');
            });
        });
    }
});