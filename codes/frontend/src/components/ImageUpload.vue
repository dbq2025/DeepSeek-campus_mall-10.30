<template>
  <div class="image-upload">
    <el-upload
      class="uploader"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="doUpload"
      accept="image/jpeg,image/png,image/webp"
    >
      <div v-if="modelValue" v-loading="uploading" class="preview">
        <img :src="modelValue" alt="商品主图" />
        <div class="preview__mask">
          <el-icon><Refresh /></el-icon>
          <span>重新上传</span>
        </div>
      </div>
      <div v-else v-loading="uploading" class="placeholder">
        <el-icon :size="22"><Plus /></el-icon>
        <span>上传图片</span>
      </div>
    </el-upload>

    <div class="image-upload__hint">
      <span class="text-sub">jpg / png / webp，不超过 2MB</span>
      <el-button v-if="modelValue" link type="danger" @click="clear">移除</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/admin/file'

defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
const MAX_SIZE = 2 * 1024 * 1024
const ALLOWED = ['image/jpeg', 'image/png', 'image/webp']

function beforeUpload(file) {
  if (!ALLOWED.includes(file.type)) {
    ElMessage.warning('仅支持 jpg / png / webp 格式')
    return false
  }
  if (file.size > MAX_SIZE) {
    ElMessage.warning('图片不能超过 2MB')
    return false
  }
  return true
}

async function doUpload(options) {
  uploading.value = true
  try {
    const data = await uploadImage(options.file)
    emit('update:modelValue', data?.url || '')
    ElMessage.success('上传成功')
  } catch (e) {
    // 错误提示已在请求层统一处理
  } finally {
    uploading.value = false
  }
}

function clear() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  display: inline-flex;
  flex-direction: column;
  gap: 6px;
}

.placeholder,
.preview {
  width: 120px;
  height: 120px;
  border-radius: var(--mall-radius);
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.placeholder {
  border: 1px dashed var(--mall-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--mall-text-3);
  font-size: 12px;
  background: var(--mall-bg-subtle);
  transition: border-color var(--mall-dur-fast) ease-out, color var(--mall-dur-fast) ease-out,
    background-color var(--mall-dur-fast) ease-out;
}

.placeholder:hover {
  border-color: var(--mall-primary);
  color: var(--mall-primary);
  background: var(--mall-primary-soft);
}

.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview__mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #fff;
  font-size: 12px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.2), rgba(15, 23, 42, 0.6));
  opacity: 0;
  transition: opacity var(--mall-dur) var(--mall-ease);
}

.preview:hover .preview__mask {
  opacity: 1;
}

.preview:hover .preview__mask {
  opacity: 1;
}

.image-upload__hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
</style>
