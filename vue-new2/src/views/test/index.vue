<template>
  <div class="w-full">
    <el-form
      ref="formRef"
      label-width="auto"
      class="max-w-3xl mx-auto"
      style="max-width: 600px"
      status-icon
    >
      <el-row :gutter="30">
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="名称" prop="name">
            <el-input placeholder="请输入物品名称" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="类型" prop="type">
            <el-input placeholder="请输入物品类型" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="状态" prop="status">
            <el-select
              placeholder="请选择状态"
              class="w-full"
            >
              <el-option label="可用" value="AVAILABLE" />
              <el-option label="使用中" value="IN_USE" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="已处置" value="DISPOSED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="价格" prop="price">
            <el-input-number
              :precision="2"
              :step="0.1"
              :min="0"
              placeholder="请输入价格"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="购买日期" prop="purchaseDate">
            <el-date-picker
              type="date"
              placeholder="请选择购买日期"
              value-format="YYYY-MM-DD"
              class="w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="10">
          <el-form-item label="保修期" prop="warrantyPeriod">
            <el-input-number
              :min="0"
              :max="120"
              placeholder="请输入保修期（月）"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="所属位置" prop="parentId">
        <el-tree

          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          default-expand-all
          :expand-on-click-node="false"
          class="location-tree"
        >
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <span>
                <el-icon v-if="data.type === 'space'"><Folder /></el-icon>
                <el-icon v-else><Goods /></el-icon>
                {{ node.label }}
              </span>
            </div>
          </template>
        </el-tree>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="请输入物品描述"
        />
      </el-form-item>


    
    </el-form>
  </div>
</template>