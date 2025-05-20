<template>
    <div class="scan-upload-page">
        <el-card class="scan-card" :body-style="{ padding: '5px' }">
            <template #header>
                <div class="card-header">
                    <span class="header-title">扫码拍照上传</span>
                    <el-tooltip content="扫描物品二维码后上传图片" placement="top">
                        <el-icon class="header-icon">
                            <InfoFilled />
                        </el-icon>
                    </el-tooltip>
                </div>
            </template>

            <div v-if="!itemId" class="scan-container">
                <div class="camera-container">
                    <div class="camera">
                        <video ref="qrVideoRef" autoplay playsinline></video>
                        <div class="scan-overlay">
                            <div class="item-1 item"></div>
                            <div class="item-2 item"></div>
                            <div class="item-3 item"></div>
                            <div class="item-4 item"></div>
                            <div class="plate-rank-content item-5">
                                <div class="angle-border left-top-border"></div>
                                <div class="angle-border right-top-border"></div>
                                <div class="angle-border left-bottom-border"></div>
                                <div class="angle-border right-bottom-border"></div>
                                <div class="scan-line"></div>
                            </div>
                        </div>
                    </div>
                    <div class="scan-instructions">
                        <el-icon class="instruction-icon">
                            <Aim />
                        </el-icon>
                        <span>请将二维码对准扫描框，卡顿请重置扫描</span>
                    </div>

                </div>
                <el-button @click="restartQrScan" type="primary">重置扫描</el-button>

                <div v-if="qrScanError" class="error-message">
                    <el-alert :title="qrScanError" type="error" show-icon />
                </div>
            </div>

            <!-- 扫码成功后显示上传组件 -->
            <div v-else class="upload-container">
                <div class="success-info">
                    <el-result icon="success" title="请上传物品照片">
                        <template #extra>
                            <el-row justify="center" align="middle" class="button-group">
                                <el-col :span="24">
                                    <el-tag size="large" type="success" effect="dark" class="item-tag">物品ID: {{
                                        itemId }}</el-tag>
                                </el-col>
                                <el-col :span="24">
                                    <el-tag size="large" type="success" effect="dark" class="item-tag">物品名称: {{
                                        itemName || '未知' }}</el-tag>
                                </el-col>
                                <el-col :span="24">

                                    <el-tag size="large" type="success" effect="dark" class="item-tag">物品类型: {{
                                        itemType || '未知' }}</el-tag>
                                </el-col>
                            </el-row>
                        </template>
                    </el-result>
                </div>

                <div class="upload-area">
                    <el-upload v-model:file-list="fileList" action="#" list-type="picture-card" :auto-upload="false"
                        :on-change="handleFileChange" :before-upload="beforeUpload" :on-remove="handleRemove"
                        :on-preview="handlePicturePreview" multiple>
                        <template #default>
                            <div class="upload-trigger">
                                <el-icon class="upload-icon">
                                    <Plus />
                                </el-icon>
                                <div class="upload-text">点击上传</div>
                            </div>
                        </template>
                    </el-upload>
                </div>

                <el-dialog v-model="dialogVisible" width="80%" destroy-on-close center>
                    <div class="preview-container">
                        <img :src="dialogImageUrl" alt="Preview Image" class="preview-image" />
                    </div>
                </el-dialog>

                <el-row justify="center" align="middle" class="button-group">
                    <el-col :span="24">
                        <el-button type="primary" @click="submitUpload" :loading="uploading" icon="Upload">
                            {{ uploading ? '上传中...' : '上传图片' }}
                        </el-button>
                    </el-col>
                    <el-col :span="24">
                        <el-button @click="resetScan" icon="Refresh" plain>重新扫描</el-button>
                    </el-col>

                </el-row>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElCard, ElButton, ElUpload, ElDialog, ElAlert, ElResult, ElTag, ElTooltip, UploadFile, UploadProps } from 'element-plus';
import { Plus, InfoFilled, Aim } from '@element-plus/icons-vue';
import { BrowserMultiFormatReader, NotFoundException } from '@zxing/library';
import { uploadEntityImage } from '@/api/image'; // Use the existing API function

defineOptions({
    name: "ScanUploadPage"
});

const qrVideoRef = ref<HTMLVideoElement | null>(null);

const itemId = ref<string | null>(null);
const itemName = ref<string | null>(null);
const itemType = ref<string | null>(null);

const qrScanError = ref<string | null>(null);
const qrCodeReader = new BrowserMultiFormatReader();

// 上传相关变量
const fileList = ref<UploadFile[]>([]);
const dialogImageUrl = ref<string>('');
const dialogVisible = ref<boolean>(false);
const uploading = ref<boolean>(false);

// 在组件挂载后
onMounted(() => {
    startQrScan();
});

const startQrScan = async () => {
    console.log('startQrScan called'); // Debugging line
    if (!qrVideoRef) {
        console.error('qrVideoRef is not available'); // Debugging line
        return;
    }
    qrScanError.value = null;
    try {
        // 开始扫描
        decodeFromInputVideoFunc();
    } catch (err: any) {
        qrScanError.value = `扫描初始化失败: ${err.message || '未知错误'}`;
        console.error('扫描初始化失败:', err);
        alert(err);
    }
};
// 保存当前扫描会话的ID，用于在需要时停止特定会话
let currentScanSessionId = '';

let decodeFromInputVideoFunc = () => {
    try {
        // 先确保之前的扫描已停止
        stopQrScan();

        // 生成新的会话ID
        currentScanSessionId = Date.now().toString();

        // 获取视频设备并开始扫描
        qrCodeReader.listVideoInputDevices()
            .then(videoInputDevices => {
                if (videoInputDevices.length === 0) {
                    throw new Error('未找到可用的摄像头设备');
                }

                // 缓存设备信息避免重复获取
                let deviceId = videoInputDevices[0].deviceId;
                const deviceName = JSON.stringify(videoInputDevices[0]);

                // 优先使用后置摄像头
                if (videoInputDevices.length > 1 && !deviceName.includes("back")) {
                    deviceId = videoInputDevices[1].deviceId;
                }

                // 添加帧率限制
                const constraints = {
                    video: {
                        deviceId: { exact: deviceId },
                        width: { ideal: 720 },
                        height: { ideal: 720 },
                        frameRate: { ideal: 15, max: 30 }
                    }
                };
                // 使用已经设置好的视频元素进行扫描
                qrCodeReader.decodeFromVideoDevice(deviceId, qrVideoRef.value, (result: any, err: any) => {
                    if (result) {
                        handleScanResult(result);
                    }
                    if (err && !(err instanceof NotFoundException)) {
                        console.error(err);
                        qrScanError.value = `扫描错误: ${err.message || '未知错误'}`;
                    }
                }).then(() => {
                    // 应用视频约束
                    if (qrVideoRef.value && qrVideoRef.value.srcObject) {
                        const stream = qrVideoRef.value.srcObject as MediaStream;
                        stream.getVideoTracks().forEach(track => {
                            track.applyConstraints(constraints.video);
                        });
                    }
                });

                // 每10秒自动重置一次扫描以防止卡顿
                setTimeout(() => {
                    if (currentScanSessionId) {
                        restartQrScan();
                    }
                }, 10000);
            })
            .catch(error => {
                console.error('获取视频设备失败:', error);
                qrScanError.value = `无法获取视频设备: ${error.message || '未知错误'}`;
            });
    } catch (error: any) {
        console.error('启动扫描失败:', error);
        qrScanError.value = `无法启动扫描: ${error.message || '未知错误'}`;
    }
};


const stopQrScan = () => {
    // 确保完全停止扫描和释放资源
    try {
        // 停止视频流
        if (qrVideoRef.value && qrVideoRef.value.srcObject) {
            const stream = qrVideoRef.value.srcObject as MediaStream;
            if (stream) {
                stream.getTracks().forEach(track => {
                    track.stop();
                    track.enabled = false;
                });
                // 强制释放视频资源
                qrVideoRef.value.srcObject = null;
                qrVideoRef.value.pause();
                qrVideoRef.value.load(); // 强制重新加载视频元素
            }
        }

        // 重置二维码读取器
        qrCodeReader.reset();

        // 清除会话ID
        currentScanSessionId = '';
    } catch (error) {
        console.error('停止扫描时出错:', error);
    }
};

const restartQrScan = () => {
    stopQrScan();
    startQrScan();
};

// 添加防抖函数，避免频繁处理扫描结果
const debounce = (fn: Function, delay: number) => {
    let timer: number | null = null;
    return function (...args: any[]) {
        if (timer) clearTimeout(timer);
        timer = window.setTimeout(() => {
            fn.apply(this, args);
            timer = null;
        }, delay);
    };
};

// 使用防抖处理扫描结果
const handleScanResult = debounce((result: any) => {
    console.log(result, "扫描结果");
    const qrText = result.getText();

    try {
        // 尝试解析JSON格式的二维码内容
        const qrData = JSON.parse(qrText);

        // 检查是否符合指定格式 {"id":number,"name":string,"type":string}
        if (qrData && qrData.id && qrData.name && qrData.type) {
            console.log('扫描到符合格式的物品信息:', qrData);
            // 设置物品ID和完整数据
            itemId.value = qrData.id.toString();
            itemName.value = qrData.name.toString();
            itemType.value = qrData.type.toString();

            // 停止扫描
            stopQrScan(); // 短暂延迟以确保UI更新
        } else {
            // 不符合格式，继续扫描
            qrScanError.value = '二维码格式不符合要求，请重新扫描';
            console.error('二维码格式不符合要求:', qrData);
        }
    } catch (error) {
        // 解析JSON失败，可能不是JSON格式
        console.log('非JSON格式二维码，使用原始文本:', qrText);
        itemId.value = qrText;
        stopQrScan();
    }
}, 300);

// 图片上传前的验证
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    const isImage = file.type.startsWith('image/');

    if (!isImage) {
        ElMessage.error('只能上传图片文件!');
        return false;
    }

    return true;
};

// 处理文件变更
const handleFileChange = (uploadFile: UploadFile) => {
    console.log('文件变更:', uploadFile);
};

// 处理文件移除
const handleRemove = (uploadFile: UploadFile) => {
    console.log('移除文件:', uploadFile);
    const index = fileList.value.findIndex(file => file.uid === uploadFile.uid);
    if (index !== -1) {
        fileList.value.splice(index, 1);
    }
};

// 处理图片预览
const handlePicturePreview = (uploadFile: UploadFile) => {
    dialogImageUrl.value = uploadFile.url || '';
    dialogVisible.value = true;
};

// 提交上传
const submitUpload = async () => {
    if (!itemId.value) {
        ElMessage.warning('请先扫描物品二维码');
        return;
    }

    if (fileList.value.length === 0) {
        ElMessage.warning('请至少选择一张图片');
        return;
    }

    uploading.value = true;
    try {
        // 创建上传任务
        const uploadTasks = fileList.value.map(async (file) => {
            if (!file.raw) {
                return { success: false, message: '文件数据无效' };
            }

            try {
                const response = await uploadEntityImage(
                    itemId.value as string,
                    file.raw as File,
                    'normal' // 默认为普通图片类型
                );

                // 使用类型断言处理response
                const responseData = response as any;

                if (responseData && responseData.code === 200) {
                    console.log('图片上传成功:', responseData.data);
                    return { success: true };
                } else {
                    console.error('图片上传失败:', response);
                    return { success: false, message: responseData?.message || '上传失败' };
                }
            } catch (error: any) {
                console.error('图片上传出错:', error);
                return { success: false, message: error.message || '上传出错' };
            }
        });

        // 等待所有上传任务完成
        const results = await Promise.all(uploadTasks);

        // 判断是否所有图片都上传成功
        const allSuccess = results.every(result => result.success === true);
        const successCount = results.filter(r => r.success === true).length;

        if (allSuccess) {
            ElMessage.success('所有图片上传成功');
            // 清空文件列表
            fileList.value = [];
        } else {
            ElMessage.warning(
                `部分图片上传失败，成功 ${successCount}/${fileList.value.length}`
            );
        }
    } catch (error: any) {
        console.error('上传过程出错:', error);
        ElMessage.error(error.message || '上传过程出错');
    } finally {
        uploading.value = false;
    }
};

// 重置扫描
const resetScan = () => {
    // 清空文件列表
    fileList.value = [];
    // 重置物品ID
    itemId.value = null;
    // 重新开始扫描
    startQrScan();

};

onUnmounted(() => {
    // 确保在组件卸载时清理所有资源
    stopQrScan();
    // 清除任何可能的定时器
    qrCodeReader.reset();
});

</script>


<style scoped lang="scss">
.scan-upload-page {
    max-width: 800px;
    margin: 0 auto;
}

.button-group {
    display: flex;
    flex-direction: column;
    gap: 14px;
    margin-top: 24px;

    .el-button {
        padding: 24px 32px;
        font-size: 21px;
        min-width: 120px;
    }
}

.scan-card {
    @apply rounded-lg shadow transition-all duration-300;

    &:hover {
        @apply shadow-lg;
    }

    .card-header {
        @apply flex items-center justify-between;

        .header-title {
            @apply text-lg font-semibold text-gray-900;
        }

        .header-icon {
            @apply text-gray-500 cursor-pointer ml-2 hover:text-blue-500;
        }
    }
}

// 扫描容器样式
.scan-container {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.camera-container {
    width: 100%;
    max-width: 500px;
    margin: 0 auto;
}

.camera {
    @apply h-[50vh] max-h-[400px] relative rounded-lg overflow-hidden shadow mb-4;

    video {
        @apply w-full h-full object-cover bg-black;
    }

    .scan-overlay {
        position: absolute;
        top: 0;
        height: 100%;
        width: 100%;
        display: grid;
        grid-template-columns: 1fr 1fr 1fr 1fr;
        grid-template-rows: 1fr 1fr 1fr 1fr;
        grid-template-areas:
            'a a a a'
            'b e e c'
            'b e e c'
            'd d d d'
        ;

        .plate-rank-content {
            box-shadow: inset 0px 0px 10px 0px rgba(32, 116, 247, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.4);
            position: relative;


            .angle-border {
                position: absolute;
                width: 24px;
                height: 24px;
            }

            .left-top-border {
                top: -3px;
                left: -3px;
                border-left: 3px solid #f56c6c;
                border-top: 3px solid #f56c6c;
            }

            .right-top-border {
                top: -3px;
                right: -3px;
                border-right: 3px solid #f56c6c;
                border-top: 3px solid #f56c6c;
            }

            .left-bottom-border {
                bottom: -3px;
                left: -3px;
                border-bottom: 3px solid #f56c6c;
                border-left: 3px solid #f56c6c;
            }

            .right-bottom-border {
                bottom: -3px;
                right: -3px;
                border-right: 3px solid #f56c6c;
                border-bottom: 3px solid #f56c6c;
            }
        }

        .item {
            background-color: rgba(0, 0, 0, .4);
        }

        .item-1 {
            grid-area: a;
        }

        .item-2 {
            grid-area: b;
        }

        .item-3 {
            grid-area: c;
        }

        .item-4 {
            grid-area: d;
        }

        .item-5 {
            grid-area: e;
        }
    }
}

.scan-instructions {
    @apply flex items-center justify-center mt-2 text-gray-500 text-sm;

    .instruction-icon {
        @apply mr-2 text-blue-500;
    }
}

.error-message {
    width: 100%;
    margin-top: 16px;
}

// 上传容器样式
.upload-container {
    padding: 12px 0;
}

.success-info {
    margin-bottom: 12px;

}

.upload-area {
    @apply my-6 flex justify-center;

    .upload-trigger {
        @apply flex flex-col items-center justify-center h-full;

        .upload-icon {
            @apply text-gray-400 mb-2 text-2xl;
        }

        .upload-text {
            @apply text-gray-400 text-sm;
        }
    }
}

.preview-container {
    display: flex;
    justify-content: center;
    align-items: center;

    .preview-image {
        max-width: 100%;
        max-height: 70vh;
        object-fit: contain;
    }
}

// 响应式调整
@media (max-width: 768px) {
    .camera {
        height: 40vh;
    }

}
</style>
