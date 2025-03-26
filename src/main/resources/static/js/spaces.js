/**
 * 空间管理页面的JavaScript文件
 */

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化JSTree
    initSpaceTree();
    
    // 初始化新增空间按钮事件
    document.getElementById('addNewSpace').addEventListener('click', function() {
        // 加载父级空间选项
        loadParentSpaceOptions();
        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('addSpaceModal'));
        modal.show();
    });
    
    // 初始化保存空间按钮事件
    document.getElementById('saveSpaceBtn').addEventListener('click', function() {
        saveSpace();
    });
    
    // 初始化更新空间按钮事件
    document.getElementById('updateSpaceBtn').addEventListener('click', function() {
        updateSpace();
    });
    
    // 初始化确认删除按钮事件
    document.getElementById('confirmDeleteBtn').addEventListener('click', function() {
        const spaceId = this.getAttribute('data-space-id');
        deleteSpace(spaceId);
    });
});

/**
 * 初始化空间树
 */
function initSpaceTree() {
    // 获取家庭ID
    const familyId = document.getElementById('familyId').value || getFamilyId();
    
    // 加载空间树数据
    fetch(`/api/spaces/tree?familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const spaces = data.data;
                // 构建JSTree数据
                const treeData = buildTreeData(spaces);
                // 初始化JSTree
                $('#space-tree').jstree({
                    'core': {
                        'data': treeData,
                        'themes': {
                            'name': 'proton',
                            'responsive': true
                        }
                    },
                    'plugins': ['wholerow', 'contextmenu'],
                    'contextmenu': {
                        'items': function(node) {
                            return {
                                'edit': {
                                    'label': '编辑',
                                    'action': function() {
                                        editSpace(node.id);
                                    }
                                },
                                'delete': {
                                    'label': '删除',
                                    'action': function() {
                                        confirmDeleteSpace(node.id);
                                    }
                                }
                            };
                        }
                    }
                });
                
                // 绑定节点点击事件
                $('#space-tree').on('select_node.jstree', function(e, data) {
                    showSpaceDetails(data.node.id);
                });
            } else {
                showMessage('error', '加载空间树失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载空间树失败，请检查网络连接');
        });
}

/**
 * 构建空间树数据
 * @param {Array} spaces 空间数据
 * @returns {Array} JSTree格式的数据
 */
function buildTreeData(spaces) {
    return spaces.map(space => ({
        id: space.id,
        text: space.name,
        parent: space.parentId ? space.parentId : '#',
        children: space.children ? buildTreeData(space.children) : []
    }));
}

/**
 * 加载父级空间选项
 * @param {Long} selectedId 选中的空间ID
 * @param {String} selectId 下拉框ID
 */
function loadParentSpaceOptions(selectedId = null, selectId = 'parentSpace') {
    // 获取家庭ID
    const familyId = document.getElementById('familyId').value || getFamilyId();
    
    // 加载空间树数据
    fetch(`/api/spaces/tree?familyId=${familyId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const spaces = data.data;
                // 清空选项
                const select = document.getElementById(selectId);
                // 保留第一个选项
                select.innerHTML = '<option value="">无 (作为根空间)</option>';
                
                // 递归构建选项
                function buildOptions(spaceList, level = 0) {
                    spaceList.forEach(space => {
                        // 创建选项
                        const option = document.createElement('option');
                        option.value = space.id;
                        option.text = '　'.repeat(level) + space.name;
                        // 如果是当前编辑的空间，不能选择自己作为父级
                        if (selectedId !== space.id) {
                            select.appendChild(option);
                        }
                        
                        // 如果有子空间，递归处理
                        if (space.children && space.children.length > 0) {
                            buildOptions(space.children, level + 1);
                        }
                    });
                }
                
                // 构建选项
                buildOptions(spaces);
                
                // 如果有选中的值，设置选中
                if (selectedId) {
                    // 加载当前空间信息，获取父级ID
                    fetch(`/api/spaces/${selectedId}`)
                        .then(response => response.json())
                        .then(data => {
                            if (data.code === 200) {
                                const parentId = data.data.parentId;
                                if (parentId) {
                                    select.value = parentId;
                                } else {
                                    select.value = '';
                                }
                            }
                        });
                }
            } else {
                showMessage('error', '加载父级空间选项失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载父级空间选项失败，请检查网络连接');
        });
}

/**
 * 保存空间
 */
function saveSpace() {
    // 获取表单数据
    const name = document.getElementById('spaceName').value;
    const parentId = document.getElementById('parentSpace').value;
    const sort = document.getElementById('spaceSort').value;
    const familyId = document.getElementById('familyId').value || getFamilyId();
    
    // 表单验证
    if (!name) {
        showMessage('error', '空间名称不能为空');
        return;
    }
    
    // 构建请求数据
    const spaceData = {
        name: name,
        parentId: parentId ? parseInt(parentId) : null,
        sort: parseInt(sort),
        familyId: parseInt(familyId)
    };
    
    // 发送请求保存空间
    fetch('/api/spaces', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(spaceData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showMessage('success', '添加空间成功');
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('addSpaceModal'));
            modal.hide();
            // 重置表单
            document.getElementById('addSpaceForm').reset();
            // 刷新空间树
            refreshSpaceTree();
        } else {
            showMessage('error', '添加空间失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('error', '添加空间失败，请检查网络连接');
    });
}

/**
 * 编辑空间
 * @param {Long} spaceId 空间ID
 */
function editSpace(spaceId) {
    // 加载空间详情
    fetch(`/api/spaces/${spaceId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const space = data.data;
                // 设置表单值
                document.getElementById('editSpaceId').value = space.id;
                document.getElementById('editSpaceName').value = space.name;
                document.getElementById('editSpaceSort').value = space.sort || 0;
                
                // 加载父级空间选项
                loadParentSpaceOptions(space.id, 'editParentSpace');
                
                // 显示模态框
                const modal = new bootstrap.Modal(document.getElementById('editSpaceModal'));
                modal.show();
            } else {
                showMessage('error', '加载空间详情失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载空间详情失败，请检查网络连接');
        });
}

/**
 * 更新空间
 */
function updateSpace() {
    // 获取表单数据
    const id = document.getElementById('editSpaceId').value;
    const name = document.getElementById('editSpaceName').value;
    const parentId = document.getElementById('editParentSpace').value;
    const sort = document.getElementById('editSpaceSort').value;
    const familyId = document.getElementById('familyId').value || getFamilyId();
    
    // 表单验证
    if (!name) {
        showMessage('error', '空间名称不能为空');
        return;
    }
    
    // 构建请求数据
    const spaceData = {
        id: parseInt(id),
        name: name,
        parentId: parentId ? parseInt(parentId) : null,
        sort: parseInt(sort),
        familyId: parseInt(familyId)
    };
    
    // 发送请求更新空间
    fetch('/api/spaces', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(spaceData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showMessage('success', '更新空间成功');
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('editSpaceModal'));
            modal.hide();
            // 刷新空间树
            refreshSpaceTree();
        } else {
            showMessage('error', '更新空间失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('error', '更新空间失败，请检查网络连接');
    });
}

/**
 * 确认删除空间
 * @param {Long} spaceId 空间ID
 */
function confirmDeleteSpace(spaceId) {
    // 设置确认删除按钮的data-space-id属性
    document.getElementById('confirmDeleteBtn').setAttribute('data-space-id', spaceId);
    // 显示确认删除模态框
    const modal = new bootstrap.Modal(document.getElementById('deleteSpaceModal'));
    modal.show();
}

/**
 * 删除空间
 * @param {Long} spaceId 空间ID
 */
function deleteSpace(spaceId) {
    // 发送请求删除空间
    fetch(`/api/spaces/${spaceId}`, {
        method: 'DELETE'
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            showMessage('success', '删除空间成功');
            // 关闭模态框
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteSpaceModal'));
            modal.hide();
            // 刷新空间树
            refreshSpaceTree();
        } else {
            showMessage('error', '删除空间失败: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showMessage('error', '删除空间失败，请检查网络连接');
    });
}

/**
 * 刷新空间树
 */
function refreshSpaceTree() {
    // 销毁现有树
    $('#space-tree').jstree('destroy');
    // 重新初始化
    initSpaceTree();
}

/**
 * 显示空间详情
 * @param {Long} spaceId 空间ID
 */
function showSpaceDetails(spaceId) {
    // 加载空间详情
    fetch(`/api/spaces/${spaceId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const space = data.data;
                // 更新详情卡片
                const detailsCard = document.querySelector('.card-details');
                if (detailsCard) {
                    detailsCard.querySelector('.card-title').textContent = space.name;
                    
                    // 构建详情HTML
                    let detailsHtml = `
                        <p><strong>ID:</strong> ${space.id}</p>
                        <p><strong>名称:</strong> ${space.name}</p>
                        <p><strong>排序:</strong> ${space.sort || 0}</p>
                        <p><strong>层级:</strong> ${space.level || 1}</p>
                    `;
                    
                    // 如果有父级ID，加载父级空间名称
                    if (space.parentId) {
                        fetch(`/api/spaces/${space.parentId}`)
                            .then(response => response.json())
                            .then(parentData => {
                                if (parentData.code === 200) {
                                    const parentName = parentData.data.name;
                                    detailsHtml += `<p><strong>父级空间:</strong> ${parentName}</p>`;
                                } else {
                                    detailsHtml += `<p><strong>父级空间:</strong> 未知</p>`;
                                }
                                
                                // 添加路径信息
                                detailsHtml += `<p><strong>路径:</strong> ${space.path || ''}</p>`;
                                
                                // 更新详情内容
                                detailsCard.querySelector('.card-details-content').innerHTML = detailsHtml;
                            });
                    } else {
                        detailsHtml += `<p><strong>父级空间:</strong> 无 (根空间)</p>`;
                        detailsHtml += `<p><strong>路径:</strong> ${space.path || ''}</p>`;
                        
                        // 更新详情内容
                        detailsCard.querySelector('.card-details-content').innerHTML = detailsHtml;
                    }
                    
                    // 显示操作按钮
                    const actionButtons = detailsCard.querySelector('.card-actions');
                    if (actionButtons) {
                        actionButtons.innerHTML = `
                            <button type="button" class="btn btn-primary" onclick="editSpace(${space.id})">
                                <i class="bi bi-pencil"></i> 编辑
                            </button>
                            <button type="button" class="btn btn-danger" onclick="confirmDeleteSpace(${space.id})">
                                <i class="bi bi-trash"></i> 删除
                            </button>
                        `;
                    }
                }
            } else {
                showMessage('error', '加载空间详情失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('error', '加载空间详情失败，请检查网络连接');
        });
}

/**
 * 显示消息提示
 * @param {String} type 消息类型：success、error
 * @param {String} message 消息内容
 */
function showMessage(type, message) {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert ${alertClass} alert-dismissible fade show position-fixed top-0 end-0 m-3`;
    alertDiv.style.zIndex = '9999';
    alertDiv.role = 'alert';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // 3秒后自动关闭
    setTimeout(() => {
        const alert = new bootstrap.Alert(alertDiv);
        alert.close();
    }, 3000);
}

/**
 * 获取家庭ID
 * @returns {String} 家庭ID
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