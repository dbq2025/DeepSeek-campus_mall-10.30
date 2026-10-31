<template>
  <div class="product-edit">
    <div class="page-card">
      <div class="product-edit__head">
        <h1 class="page-title">{{ isEdit ? '编辑商品' : '新增商品' }}</h1>
        <el-button @click="router.push('/admin/products')">返回列表</el-button>
      </div>

      <div v-if="loading" class="product-edit__loading">
        <div v-for="n in 6" :key="n" class="skeleton-block product-edit__skeleton"></div>
      </div>

      <EmptyState
        v-else-if="loadError"
        type="error"
        title="商品加载失败"
        :desc="loadError"
        action-text="返回列表"
        @action="router.push('/admin/products')"
        @retry="load"
      />

      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="product-edit__form"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="100" show-word-limit placeholder="如：无线蓝牙耳机 Pro" />
        </el-form-item>

        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" maxlength="200" show-word-limit placeholder="一句话卖点" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 220px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="主图" prop="mainImage">
          <ImageUpload v-model="form.mainImage" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="售价（元）" prop="price">
              <el-input v-model="form.price" placeholder="如 99.00" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :max="999999" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="规格定义" prop="specJson">
          <SpecEditor v-model="form.specJson" />
        </el-form-item>

        <el-form-item label="商品详情" prop="detail">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="支持换行，前台按原格式展示"
          />
        </el-form-item>

        <el-form-item label="上架状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">立即上架</el-radio>
            <el-radio :value="0">暂不销售</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="onSubmit">
            {{ isEdit ? '保存修改' : '创建商品' }}
          </el-button>
          <el-button @click="router.push('/admin/products')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createProduct, fetchProductById, updateProduct } from '@/api/admin/product'
import { fetchCategoryOptions } from '@/api/admin/category'
import { checkPrice, checkSpecJson, checkStock } from '@/utils/validate'
import ImageUpload from '@/components/ImageUpload.vue'
import EmptyState from '@/components/EmptyState.vue'
import SpecEditor from '@/views/admin/components/SpecEditor.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref(null)
const categories = ref([])
const loading = ref(false)
const loadError = ref('')
const submitting = ref(false)

const isEdit = computed(() => Boolean(route.params.id))
const productId = computed(() => String(route.params.id || ''))

const form = reactive({
  name: '',
  subtitle: '',
  categoryId: '',
  mainImage: '',
  price: '',
  stock: 0,
  specJson: '',
  detail: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ validator: checkPrice, trigger: 'blur' }],
  stock: [{ validator: checkStock, trigger: 'blur' }],
  specJson: [{ validator: checkSpecJson, trigger: 'change' }]
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    categories.value = await fetchCategoryOptions()
    if (isEdit.value) {
      const data = await fetchProductById(productId.value)
      form.name = data.name || ''
      form.subtitle = data.subtitle || ''
      form.categoryId = data.categoryId || ''
      form.mainImage = data.mainImage || ''
      form.price = data.price || ''
      form.stock = Number(data.stock || 0)
      form.specJson = data.specJson || ''
      form.detail = data.detail || ''
      form.status = Number(data.status)
    } else {
      // 新增：清空为默认值（组件复用时不残留上一件商品的数据）
      form.name = ''
      form.subtitle = ''
      form.categoryId = ''
      form.mainImage = ''
      form.price = ''
      form.stock = 0
      form.specJson = ''
      form.detail = ''
      form.status = 1
    }
  } catch (e) {
    loadError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (submitting.value) return
  const valid = await formRef.value?.validate().catch((fields) => {
    // 规格 JSON 的格式错误由校验器给出，这里只做聚焦提示
    if (fields?.specJson) ElMessage.warning('请检查规格定义')
    return false
  })
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      subtitle: form.subtitle.trim(),
      categoryId: form.categoryId,
      mainImage: form.mainImage,
      price: String(form.price),
      stock: Number(form.stock),
      specJson: form.specJson || '',
      detail: form.detail,
      status: Number(form.status)
    }
    if (isEdit.value) {
      await updateProduct(productId.value, payload)
      ElMessage.success('保存成功')
    } else {
      await createProduct(payload)
      ElMessage.success('创建成功')
    }
    router.push('/admin/products')
  } catch (e) {
    // 请求层已提示
  } finally {
    submitting.value = false
  }
}

// 新增页与编辑页复用同一组件，路径变化时重新初始化表单
watch(
  () => route.fullPath,
  () => {
    formRef.value?.clearValidate()
    load()
  }
)

onMounted(load)
</script>

<style scoped>
.product-edit__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--mall-space-2);
}

.product-edit__head .page-title {
  margin-bottom: 0;
}

.product-edit__form {
  max-width: 860px;
}

.product-edit__loading {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.product-edit__skeleton {
  height: 40px;
}

@media (max-width: 767px) {
  .product-edit__form {
    max-width: 100%;
  }

  .product-edit__form :deep(.el-form-item__label) {
    width: 84px !important;
  }
}
</style>
