<template>
  <div class="dashboard-container">
    <MainHeader></MainHeader>
    
    <div class="content-container">
      <!-- Left Column with 4 Cards -->
      <div class="left-column" :class="{ 'with-expanded-right': isRightColumnExpanded }">
        <!-- Card 1: Status Chart -->
        <el-card class="dashboard-card animate__animated animate__fadeInLeft" :body-style="{ height: '100%' }" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span>{{ $t('workSpace.TaskStatus') }}</span>
            </div>
          </template>

          <div class="chart-container">
            <el-row style="height: 100%;">
              <!-- 左侧：圆环图 + 任务统计 -->
              <el-col v-if="!isMobileView" :span="isRightColumnExpanded ? 24 : 8" class="left-section">
                <div ref="statusChart" class="status-chart"></div>
                <!-- 任务统计信息 -->
                <el-row>
                  <el-col :span="12">
                    <el-row><el-tag type="warning" style="width: 80px;">{{ $t('status.Pending') }}</el-tag></el-row>
                    <el-row><el-tag type="primary" style="width: 80px;">{{ $t('status.Processing') }}</el-tag></el-row>
                    <el-row><el-tag type="success" style="width: 80px;">{{ $t('status.Completed') }}</el-tag></el-row>
                    <el-row><el-tag type="danger"  style="width: 80px;">{{ $t('status.Error') }}</el-tag></el-row>
                  </el-col>
                  <el-col :span="10">
                    <el-row><span class="status-count">{{ pendingCount }}</span></el-row>
                    <el-row><span class="status-count">{{ processingCount }}</span></el-row>
                    <el-row><span class="status-count">{{ completedCount }}</span></el-row>
                    <el-row><span class="status-count">{{ errorCount }}</span></el-row>
                  </el-col>
                </el-row>
              </el-col>

              <!-- 右侧：最近完成任务栏 -->
              <el-col :span="isMobileView ? 24 : isRightColumnExpanded ? 0 : 16" class="right-section">
                <div class="success-tasks-list">
                  <div v-if="completedCount === 0" class="empty-state">
                    {{ $t('workSpace.Norecentcompletedtaskfound') }}
                  </div>
                  <div v-for="(task, index) in sortedCompletedTasks" :key="index" class="success-task-item">
                    <div class="success-task-header">
                      <font-awesome-icon :style="{ color: '#67C23A' }" :icon="['fas', 'circle']" />
                      <span class="success-task-name">{{ task.task_name }}</span>
                      <el-button link type="success" size="small" @click="showCharts(task.task_name)" :disabled="task.status !== 2" style="margin-left: auto;">
                        {{ $t('navigateBar.Virtualization') }}
                      </el-button>
                    </div>
                    <div class="success-task-details">
                      {{ task.details || "No details available" }}
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- Card 2: Shares -->
        <el-card class="dashboard-card animate__animated animate__fadeInLeft" :body-style="{ height: '100%' }" v-loading="shareLoading">
          <template #header>
            <div class="card-header">
              <span>{{ $t('workSpace.MyShares') }}</span>
            </div>
          </template>
          <div class="success-tasks-list">
            <div v-if="shareCount === 0" class="empty-state">
              {{ $t('workSpace.Norecentsharesfound') }}
            </div>
            <div v-for="(data, index) in shareList" :key="index" class="success-task-item">
              <div class="success-task-header">
                <font-awesome-icon :style="{ color: getStatusColor(data.status)}" :icon="['fas', 'circle']" />
                <span class="success-task-name">{{ data.task_name }}</span>

                <!-- 任务分享时间状态 -->
                <span v-if="!data.due_time && !isRightColumnExpanded" style="color: #409EFF; margin-left: 10px;"> {{ $t('workSpace.Indefinite') }} </span>
                <span v-if="new Date() > new Date(data.due_time) && !isRightColumnExpanded" style="color: red; margin-left: 10px;"> {{ $t('workSpace.Expired') }} </span>
                <span v-if="data.due_time && !isRightColumnExpanded" style="margin-left: 10px; font-size: 12px; color: #666;">
                  {{ $t('workSpace.Expire') }}: {{ formatDate(data.due_time) }}
                </span>
                <el-progress 
                  v-if="new Date() <= new Date(data.due_time) && !isRightColumnExpanded"
                  :percentage="getShareProgress(data.shared_time, data.due_time)" 
                  type="line"
                  style="margin-left: 10px; width: 80px;" 
                  :stroke-width="10"
                  :show-text="false"
                />
                <el-button link type="info" size="small" @click="" style="margin-left: auto;" v-if="!isRightColumnExpanded">
                  {{ $t('workSpace.CopyLink') }}
                </el-button>
                <el-button link type="primary" size="small" @click="" v-if="!isRightColumnExpanded">
                  {{ $t('Edit') }}
                </el-button>
                <el-button link type="danger" size="small" @click="" v-if="!isRightColumnExpanded">
                  {{ $t('Delete') }}
                </el-button>
              </div>

              <div class="success-task-details">
                {{ formatDate(data.shared_time) }}
                <el-button link type="info" size="small" @click="" style="margin-left: auto" v-if="!isRightColumnExpanded">
                  {{ $t('Detail') }}
                </el-button>
                <el-button link type="success" size="small" @click="" :disabled="data.status !== 2" v-if="!isRightColumnExpanded">
                  {{ $t('navigateBar.Virtualization') }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- My Models -->
        <el-card class="dashboard-card animate__animated animate__fadeInLeft">
          <template #header>
            <div class="card-header">
              <span>{{ $t('workSpace.MyModels') }}</span>
            </div>
          </template>
          <div class="empty-state">
            {{ $t('workSpace.Nomodelsfound') }}
          </div>
        </el-card>
        
        <!-- Shares Received -->
        <el-card class="dashboard-card animate__animated animate__fadeInLeft" :body-style="{ height: '100%' }" v-loading="shareLoading">
          <template #header>
            <div class="card-header">
              <span>{{ $t('workSpace.ShareReceived') }}</span>
            </div>
          </template>
          <div class="success-tasks-list">
            <div v-if="receivedShareCount === 0" class="empty-state">
              {{ $t('workSpace.Norecentreceivedsharesfound') }}
            </div>
            <div v-for="(data, index) in shareReceivedList" :key="index" class="success-task-item">
              <div class="success-task-header">
                <font-awesome-icon :style="{ color: getStatusColor(data.status)}" :icon="['fas', 'circle']" />
                <span class="success-task-name">{{ data.task_name }}</span>

                <!-- 任务分享时间状态 -->
                <span v-if="!data.due_time && !isRightColumnExpanded" style="color: #409EFF; margin-left: 10px;"> {{ $t('workSpace.Indefinite') }} </span>
                <span v-if="new Date() > new Date(data.due_time) && !isRightColumnExpanded" style="color: red; margin-left: 10px;"> {{ $t('workSpace.Expired') }} </span>
                <span v-if="data.due_time && !isRightColumnExpanded" style="margin-left: 10px; font-size: 12px; color: #666;">
                  {{ $t('workSpace.Expire') }}: {{ formatDate(data.due_time) }}
                </span>
                <el-progress 
                  v-if="new Date() <= new Date(data.due_time) && !isRightColumnExpanded"
                  :percentage="getShareProgress(data.shared_time, data.due_time)" 
                  type="line"
                  style="margin-left: 10px; width: 80px;" 
                  :stroke-width="10"
                  :show-text="false"
                />
                <el-button link type="info" size="small" @click="" style="margin-left: auto;" v-if="!isRightColumnExpanded">
                  {{ $t('workSpace.CopyLink') }}
                </el-button>
                <el-button link type="info" size="small" @click=""  v-if="!isRightColumnExpanded">
                  {{ $t('Detail') }}
                </el-button>
              </div>

              <div class="success-task-details">
                {{ formatDate(data.shared_time) }}
                <el-button link type="success" size="small" @click="" :disabled="data.status !== 2" style="margin-left: auto" v-if="!isRightColumnExpanded">
                  {{ $t('navigateBar.Virtualization') }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- Right Column with Collapsible Task List -->
      <div class="right-column animate__animated animate__fadeInRight"
        :class="{ 'expanded': isRightColumnExpanded, 'collapsed': !isRightColumnExpanded }">
        <div class="column-toggle" @click="toggleRightColumn">
          <el-button type="primary" :icon="isRightColumnExpanded ? 'arrow-right' : 'arrow-left'">
            {{ isRightColumnExpanded ? $t('workSpace.Collapse') : $t('workSpace.Expand') }}
          </el-button>
        </div>
        
        <!-- Collapsed -->
        <div v-if="!isRightColumnExpanded" class="collapsed-task-list">
          <el-table :data="paginatedTaskList" 
            style="width: 100%" 
            v-loading="loading">
            <el-table-column prop="task_name" :label="$t('database.task.task_name')">
              <template #default="{ row }">
                <font-awesome-icon :style="{ color: getStatusColor(row.status)}" :icon="['fas', 'circle']" />
                {{ row.task_name }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- Simplified Pagination -->
          <el-pagination 
            class="pagination" 
            @current-change="handleCurrentChange"
            :current-page="currentPage" 
            :page-size="pageSize"
            layout="prev, pager, next" 
            :total="taskList.length">
          </el-pagination>
        </div>
        
        <!-- Expanded -->
        <div v-else class="expanded-task-list">
          <!-- Original Full Table Layout -->
          <el-table :data="paginatedTaskList" 
            style="width: 100%"
            @selection-change="handleSelectionChange"
            @sort-change="handleSortChange"
            v-loading="loading">
            <!-- 多选框 -->
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="task_name" :label="$t('database.task.task_name')" sortable>
              <template #default="{ row }">
                <font-awesome-icon :style="{ color: getStatusColor(row.status)}" :icon="['fas', 'circle']" />
                {{ row.task_name }}
              </template>
            </el-table-column>
            <el-table-column prop="type" :label="$t('database.task.type')" sortable>
              <template #default="{ row }">
                {{ (row.type?.split(':')[1] || "") === "single"     ? $t('taskType.Singleomic') :
                   (row.type?.split(':')[1] || "") === "multi"      ? $t('taskType.Multiomics') :
                   (row.type?.split(':')[1] || "") === "deno"       ? $t('taskType.Denoising')  : $t('taskType.Unknown')}}
                {{ (row.type?.split(':')[0] || "") === "annotation" ? $t('taskType.Annotation') :
                   (row.type?.split(':')[0] || "") === "trainning"  ? $t('taskType.Trainning')  :
                   (row.type?.split(':')[0] || "") === "denoising"  ? "" : $t('taskType.Unknown')}}
              </template>
            </el-table-column>
            <el-table-column prop="model_name" :label="$t('database.models.model_name')" sortable>
              <template #default="{ row }">
                {{ row.model_name ?? "Unknown" }}
              </template>
            </el-table-column>
            <el-table-column prop="start_time" :label="$t('database.task.start_time')" sortable>
              <template #default="{ row }">
                {{ formatDate(row.start_time) }}
              </template>
            </el-table-column>
            <el-table-column prop="end_time" :label="$t('database.task.end_time')" sortable>
              <template #default="{ row }">
                {{ (row.end_time&&row.status===2) ? formatDate(row.end_time) :  $t('Notcompletedyet') }}
              </template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('database.task.status')" sortable>
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" :label="$t('Operations')" width="300">
              <template #default="{ row }">
                <el-button link type="success" size="small" @click="showCharts(row.task_name)" :disabled="row.status !== 2">
                  {{ $t('navigateBar.Virtualization') }}
                </el-button>
                <el-button link type="primary" size="small" @click="showDetailDialog(row)">
                  {{ $t('Detail') }}
                </el-button>
                <el-button link type="danger" size="small" @click="showDeleteDialog(row)">
                  {{ $t('Delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Full Pagination -->
          <el-pagination 
            class="pagination" 
            @size-change="handleSizeChange" 
            @current-change="handleCurrentChange"
            :current-page="currentPage" 
            :page-sizes="[5, 10, 20, 50]" 
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper" 
            :total="taskList.length">
          </el-pagination>
          
          <!-- Button Row -->
          <div class="footer">
            <div class="footer-button-row">
              <el-button type="success" @click="Refresh">
                Refresh
              </el-button>
              <el-button type="danger" @click="showBatchDeleteDialog" :disabled="selectedTasks.length === 0">
                Batch Delete
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Dialogs (unchanged) -->
    <el-dialog v-model="batchDeleteDialogVisible" title="Warning" width="500">
      <span>The selected tasks will be deleted. Are you sure?</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchDeleteDialogVisible = false">Cancel</el-button>
          <el-button type="danger" @click="confirmBatchDelete">Confirm</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="deleteDialogVisible" title="Warning" width="500" align-center>
      <span>Task <strong style="color: #e74c3c;">{{ selectedTask?.task_name }}</strong> will be deleted</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">Cancel</el-button>
          <el-button type="danger" @click="deleteDialogVisible = false; deleteTask()">
            Confirm
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="Detail" width="500" align-center>
      <span>{{ selectedTask?.details }}</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="">Parameters</el-button>
          <el-button type="primary" @click="detailDialogVisible = false">Confirm</el-button>
        </div>
      </template>
    </el-dialog>
  </div>

  <div class="mobile-task-drawer">
    <el-drawer
      v-model="mobileTaskDrawerVisible"
      title="Tasks"
      direction="btt"
      size="80%"
    >
      <el-table 
        :data="paginatedTaskList" 
        style="width: 100%"
        v-loading="loading">
        <el-table-column prop="task_name" :label="$t('database.task.task_name')">
          <template #default="{ row }">
            <font-awesome-icon :style="{ color: getStatusColor(row.status)}" :icon="['fas', 'circle']" />
            {{ row.task_name }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('database.task.status')">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" :label="$t('Operations')" width="60">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showMobileActionSheet(row)">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination 
        class="pagination" 
        @current-change="handleCurrentChange"
        :current-page="currentPage" 
        :page-size="pageSize"
        layout="prev, pager, next" 
        :total="taskList.length">
      </el-pagination>
    </el-drawer>

    <!-- Mobile Action Sheet -->
    <el-dialog
      v-model="mobileActionSheetVisible"
      :title="selectedTask?.task_name"
      width="95%"
      class="mobile-action-dialog"
    >
      <div class="mobile-task-details">
        <div class="detail-item">
          <span class="detail-label">Status:</span>
          <el-tag :type="statusType(selectedTask?.status)">{{ statusText(selectedTask?.status) }}</el-tag>
        </div>
        <div class="detail-item">
          <span class="detail-label">Type:</span>
          <span>{{ getTaskType(selectedTask) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Model:</span>
          <span>{{ selectedTask?.model_name ?? "Unknown" }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Start Time:</span>
          <span>{{ formatDate(selectedTask?.start_time) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">End Time:</span>
          <span>{{ (selectedTask?.end_time && selectedTask?.status === 2) ? formatDate(selectedTask?.end_time) : $t('Notcompletedyet') }}</span>
        </div>
      </div>
      <div class="mobile-action-buttons">
        <el-button type="success" @click="showCharts(selectedTask?.task_name)" :disabled="selectedTask?.status !== 2" block>
          {{ $t('navigateBar.Virtualization') }}
        </el-button>
        <el-button type="primary" @click="showDetailDialog(selectedTask)" block>
          {{ $t('Detail') }}
        </el-button>
        <el-button type="danger" @click="showDeleteDialog(selectedTask)" block>
          {{ $t('Delete') }}
        </el-button>
      </div>
    </el-dialog>
  </div>

  <div class="mobile-task-button" v-if="isMobileView">
    <el-button type="primary" circle @click="mobileTaskDrawerVisible = true">
      <el-icon><List /></el-icon>
    </el-button>
  </div>
</template>

<script>
import MainHeader from "../components/MainHeader.vue";
import axios from "axios";
import { ElMessage } from "element-plus";
import * as echarts from 'echarts';  // Import echarts
import { List, MoreFilled } from '@element-plus/icons-vue';

export default {
  name: "WorkSpace",
  components: {
    MainHeader,
    List,
    MoreFilled
  },
  data() {
    return {
      userData: JSON.parse(sessionStorage.getItem("userData")) || {},
      taskList: [], // 存储任务数据
      shareList: [], // 分享数据
      shareReceivedList: [], // 收到的分享数据
      paginatedTaskList: [], // 当前页的任务数据
      deleteDialogVisible: false,
      batchDeleteDialogVisible: false,
      detailDialogVisible: false,
      selectedTask: null,
      selectedTasks: [], // 存储多选选中的任务
      currentPage: 1, // 当前页
      pageSize: 10, // 每页显示条数
      sortOrder: '', // 当前排序方向
      sortProp: '', // 当前排序属性
      loading: false,
      shareLoading: false,
      isRightColumnExpanded: false, // 控制右侧列表是否展开
      statusChart: null, // 存储ECharts实例
      isMobileView: false,
      mobileTaskDrawerVisible: false,
      mobileActionSheetVisible: false,
    };
  },
  computed: {
    // 计算各状态任务数量
    pendingCount() {
      return this.taskList.filter(task => task.status === 0).length;
    },
    processingCount() {
      return this.taskList.filter(task => task.status === 1).length;
    },
    completedCount() {
      return this.taskList.filter(task => task.status === 2).length;
    },
    errorCount() {
      return this.taskList.filter(task => task.status === -1).length;
    },
    receivedShareCount() {
      return this.shareReceivedList.length;
    },
    shareCount(){
      return this.shareList.length;
    },
    // 获取错误任务列表
    completedTasks() {
      return this.taskList.filter(task => task.status === 2);
    },
    sortedCompletedTasks() {
      return [...this.completedTasks].sort((a, b) => new Date(b.endTime) - new Date(a.endTime));
    }
  },
  methods: {
    getShareProgress(startTime, dueTime) {
      if (!startTime || !dueTime) return 100; // 处理异常情况，默认 100%

      const start = new Date(startTime).getTime();
      const end = new Date(dueTime).getTime();
      const now = new Date().getTime();

      if (now >= end) return 100; // 已过期
      if (now <= start) return 0;  // 刚开始

      return Math.min(100, ((now - start) / (end - start)) * 100); // 计算进度
    },
    Refresh() {
      this.fetchShareList();
      this.fetchTaskList();
    },
    // 切换右侧列表的展开/折叠状态
    toggleRightColumn() {
      this.isRightColumnExpanded = !this.isRightColumnExpanded;
      // 如果展开，可能需要重新计算分页
      if (this.isRightColumnExpanded) {
        this.updatePaginatedTaskList();
      }
      this.$nextTick(() => {
        setTimeout(() => {
          this.initStatusChart();
        }, 300); // 延迟 0.3 秒
      });
    },
    async fetchShareList() {
      try {
        this.shareLoading = true; // ShareLoading
        const response = await axios.get("/api/share/findSharesByUserId?userID=" + this.userData.userId);
        const responseReceived = await axios.get("/api/share/findSharesReceivedByUserId?userID=" + this.userData.userId);
        if (response.data.code === 200 && responseReceived.data.code === 200) {
          this.shareList = response.data.data;
          this.shareReceivedList = responseReceived.data.data;
        } else {
          console.error("Failed to fetch share list:", response.data.msg + responseReceived.data.msg);
        }
        this.shareLoading = false;
      } catch (error) {
        console.error("Failed to fetch share list:", error);
        this.shareLoading = false;
      }
    },
    // 调整图表大小方法
    resizeCharts() {
      if (this.statusChart) {
        this.statusChart.resize();
      }
    },
    // 初始化状态分布图表
    initStatusChart() {
      if (this.$refs.statusChart) {
        // 如果图表实例已存在，先销毁
        if (this.statusChart) {
          this.statusChart.dispose();
        }
        
        // 创建新的图表实例
        this.statusChart = echarts.init(this.$refs.statusChart);
        
        // 设置图表选项
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c} ({d}%)'
          },
          series: [
            {
              name: 'Task Status',
              type: 'pie',
              radius: ['50%', '70%'], // 设置为环形图
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
              data: [
                { value: this.pendingCount, name: 'Pending', itemStyle: { color: '#E6A23C' } },
                { value: this.processingCount, name: 'Processing', itemStyle: { color: '#409EFF' } },
                { value: this.completedCount, name: 'Completed', itemStyle: { color: '#67C23A' } },
                { value: this.errorCount, name: 'Error', itemStyle: { color: '#F56C6C' } }
              ]
            }
          ]
        };
        
        // 使用配置项设置图表
        this.statusChart.setOption(option);
      }
    },
    getStatusColor(status) {
      const colors = {
        0: "#E6A23C",
        1: "#409EFF",
        2: "#67C23A",
        '-1': "#F56C6C",
        4: "#909399"
      };
      return colors[status] || "#909399";
    },
    showDeleteDialog(task) {
      this.deleteDialogVisible = true;
      this.selectedTask = task;
    },
    showCharts(taskName) {  
      this.$router.push({ name: "Virtualization", query: { taskName } });  
    },
    showDetailDialog(task) {
      this.detailDialogVisible = true;
      this.selectedTask = task;
    },
    showBatchDeleteDialog() {
      if (this.selectedTasks.length === 0) {
        ElMessage.warning("Please select at least one task.");
        return;
      }
      this.batchDeleteDialogVisible = true;
    },
    async deleteTask() {
      try {
        await axios.get("/api/deleteTaskByTaskName?userName="+ this.userData.userName +"&taskName=" + this.selectedTask.task_name);
        ElMessage.success("Delete success.");
        this.Refresh();
      } catch (error) {
        console.error("Delete failed:", error);
        ElMessage.error("Delete failed.");
      }
    },
    async confirmBatchDelete() {
      this.batchDeleteDialogVisible = false;
      for (const task of this.selectedTasks) {
        await this.deleteTaskByTaskName(task.task_name);
      }
      ElMessage.success("Batch delete completed.");
      this.Refresh();
    },
    async deleteTaskByTaskName(taskName) {
      try {
        await axios.get("/api/deleteTaskByTaskName?userName="+ this.userData.userName +"&taskName=" + taskName);
      } catch (error) {
        console.error("Delete failed:", error);
      }
    },
    async fetchTaskList() {
      try {
        this.loading = true;
        const response = await axios.get("/api/findTasksByUserID?userID=" + this.userData.userId);
        if (response.data.code === 200) {
          this.taskList = response.data.data;
          this.applySorting(); // 调用排序函数
          // 获取数据后重新初始化图表
          this.$nextTick(() => {
            setTimeout(() => {
              this.initStatusChart();
            }, 300); // 延迟 0.3 秒
          });
        } else {
          console.error("Failed to fetch task list:", response.data.msg);
        }
        this.loading = false;
      } catch (error) {
        console.error("Failed to fetch task list:", error);
        this.loading = false;
      }
    },
    handleSortChange({ prop, order }) {
      this.sortProp = prop;
      this.sortOrder = order;
      this.applySorting(); // 调用排序函数
    },
    applySorting() {
      if (this.sortProp && this.sortOrder) {
        this.taskList.sort((a, b) => {
          const valueA = a[this.sortProp];
          const valueB = b[this.sortProp];

          if (this.sortOrder === 'ascending') {
            return valueA > valueB ? 1 : -1;
          } else if (this.sortOrder === 'descending') {
            return valueA < valueB ? 1 : -1;
          } else {
            return 0;
          }
        });
      }
      this.updatePaginatedTaskList();
    },
    statusText(status) {
      switch (status) {
        case 0:
          return this.$t('status.Pending');
        case 1:
          return this.$t('status.Processing');
        case 2:
          return this.$t('status.Completed');
        case -1:
          return this.$t('status.Error');
        default:
          return this.$t('status.Unknown');
      }
    },
    statusType(status) {
      switch (status) {
        case 0:
          return "info";
        case 1:
          return "warning";
        case 2:
          return "success";
        case -1:
          return "danger";
        default:
          return "";
      }
    },
    formatDate(dateString) {
      const options = { year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit" };
      return new Date(dateString).toLocaleString(undefined, options);
    },
    handleSelectionChange(val) {
      this.selectedTasks = val;
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.updatePaginatedTaskList();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.updatePaginatedTaskList();
    },
    updatePaginatedTaskList() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.paginatedTaskList = this.taskList.slice(start, end);
    },
    checkScreenSize() {
      this.isMobileView = window.innerWidth < 768;
      if (this.isMobileView && this.isRightColumnExpanded) {
        this.isRightColumnExpanded = false;
      }
    },
    
    showMobileActionSheet(task) {
      this.selectedTask = task;
      this.mobileActionSheetVisible = true;
    },
    
    getTaskType(row) {
      if (!row) return '';
      
      let typeText = '';
      
      if (row.type?.split(':')[1]) {
        typeText += (row.type.split(':')[1] === "single" ? this.$t('taskType.Singleomic') :
                    row.type.split(':')[1] === "multi" ? this.$t('taskType.Multiomics') :
                    row.type.split(':')[1] === "deno" ? this.$t('taskType.Denoising') : 
                    this.$t('taskType.Unknown'));
      }
      
      if (row.type?.split(':')[0]) {
        if (row.type.split(':')[0] === "annotation") {
          typeText += ' ' + this.$t('taskType.Annotation');
        } else if (row.type.split(':')[0] === "trainning") {
          typeText += ' ' + this.$t('taskType.Trainning');
        }
      }
      
      return typeText || this.$t('taskType.Unknown');
    },
  },
  mounted() {
    this.Refresh(); // 组件挂载后获取任务数据
    // 设置图表响应式
    window.addEventListener('resize', this.resizeCharts);
    this.checkScreenSize();
    window.addEventListener('resize', this.checkScreenSize);
  },
  beforeUnmount() {
    // 组件销毁前清理图表实例和事件监听
    if (this.statusChart) {
      this.statusChart.dispose();
    }
    window.removeEventListener('resize', this.resizeCharts);
    window.removeEventListener('resize', this.checkScreenSize);
  }
};
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
}
.el-row:last-child {
  margin-bottom: 0;
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.content-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  margin-top: 60px; /* Add top margin to accommodate the fixed header */
  height: calc(100vh - 60px); /* Adjust height to account for header */
}

/* Left Column Styles */
.left-column {
  width: 88%; /* Default width when right column is collapsed */
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 20px;
  overflow-y: auto;
  transition: width 0.3s ease;
}

.left-column.with-expanded-right {
  width: 25%; /* Width when right column is expanded */
}

.dashboard-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.donut-chart-wrapper {
  flex: 1;
}

.status-chart {
  width: 100%;
  height: 180px;
  /* position: absolute; */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.status-count {
  font-size: 18px;
  font-weight: bold;
  display: block; /* 让 span 变为块级元素 */
  text-align: right; /* 文字右对齐 */
  width: 100%; /* 确保占满父容器 */
}

.success-tasks-list {
  overflow-y: auto;
  background-color: #f0f0f0;
  height: 100%;
  border-radius: 10px;
}

.dark-mode .success-tasks-list {
  background-color: #302E2C;
}

.success-task-item {
  margin: 15px 15px 0px 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #302E2C;
}

.dark-mode .success-task-item{
  border-bottom: 1px solid #f0f0f0;
}

.success-task-item:last-child {
  border-bottom: none;
}

.success-task-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.success-task-name {
  font-weight: bold;
}

.success-task-details {
  display: flex;
  padding-left: 20px;
  color: #606266;
  font-size: 14px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
  font-style: italic;
}

/* Right Column Styles */
.right-column {
  transition: width 0.3s ease;
  border-left: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  position: relative;
}

.right-column.collapsed {
  width: 12%; /* Collapsed width */
}

.right-column.expanded {
  width: 100%; /* Expanded width */
}

.column-toggle {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
}

.collapsed-task-list, 
.expanded-task-list {
  padding: 15px;
  padding-top: 60px; /* Space for the toggle button */
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.footer {
  margin-top: auto;
  padding: 10px;
  border-top: 1px solid #e6e6e6;
  position: absolute;
}

.footer-button-row {
  display: flex;
  justify-content: space-between;
}

.pagination {
  margin-top: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

/* Accommodate to dark mode when active */
:deep(.dark-mode) .dashboard-card,
:deep(.dark) .dashboard-card {
  background-color: #1d1e1f;
  border-color: #3e3e3e;
  color: #e0e0e0;
}

:deep(.dark-mode) .success-task-details,
:deep(.dark) .success-task-details {
  color: #a0a0a0;
}

:deep(.dark-mode) .empty-state,
:deep(.dark) .empty-state {
  color: #a0a0a0;
}

:deep(.dark-mode) .right-column,
:deep(.dark) .right-column {
  border-left-color: #3e3e3e;
}

:deep(.dark-mode) .footer,
:deep(.dark) .footer {
  border-top-color: #3e3e3e;
}

/* Mobile Responsive Styles */
@media (max-width: 767px) {
  .content-container {
    flex-direction: column;
    height: auto;
    overflow-y: auto;
  }
  
  .left-column {
    width: 95% !important;
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    padding: 10px;
    margin-top: 20px;
  }
  
  .right-column {
    display: none;
  }
  
  .dashboard-card {
    height: auto;
    min-height: 350px;
  }

  .success-tasks-list {
    max-height: 230px;
  }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .left-column {
    grid-template-columns: 1fr;
    width: 70% !important;
  }
  
  .right-column.collapsed {
    width: 30%;
  }
}

/* Mobile Task Drawer */
.mobile-task-button {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  background: transparent;
  width: 50px;
  height: 50px;
}

.mobile-action-buttons .el-button + .el-button {
  margin-left: 0;        /* 去除横向间距 */
}

.mobile-task-details {
  margin-bottom: 20px;
}

.detail-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.detail-label {
  font-weight: bold;
  width: 100px;
  color: #606266;
}

.mobile-action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* Chart responsive fix */
.status-chart {
  height: 150px;
  min-height: 150px;
}

.left-section {
  display: flex;
  flex-direction: column;
}

/* Adjust card for smaller screens */
@media (max-width: 767px) {
  .card-header {
    padding: 10px;
  }
  
  .status-count {
    font-size: 14px;
  }
  
  .status-chart {
    height: 120px;
  }
}

/* Make mobile dialog take more screen space */
.mobile-action-dialog :deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>