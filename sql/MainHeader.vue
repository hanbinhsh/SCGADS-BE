<template>
    <div>
        <!-- Desktop Menu -->
        <el-menu :default-active="activeIndex" class="el-menu main-header desktop-menu" mode="horizontal" :ellipsis="false"
            @select="handleSelect" :router="true" v-if="!isMobile">
            <el-menu-item index="HomeView" :class="{ 'is-active': activeIndex === 'HomeView' }">
                <img style="width: 50px" src="../assets/logo.png" alt="logo" />
            </el-menu-item>
            <el-menu-item index="Login" :class="{ 'is-active': activeIndex === 'Login' }" v-if="!userData.userName">
                {{ $t('navigateBar.Login') }}
            </el-menu-item>
            <el-menu-item index="Register" :class="{ 'is-active': activeIndex === 'Register' }"
                v-if="!userData.userName">
                {{ $t('navigateBar.Register') }}
            </el-menu-item>
            <el-sub-menu v-if="userData.userName" index="1">
                <template #title>
                    <el-avatar
                        :src="userData.avatarBase64 ? 'data:image/jpeg;base64,' + userData.avatarBase64 : defaultAvatar"
                        size="small"></el-avatar>&nbsp;
                    {{ userData.userName }}
                </template>
                <el-menu-item index="Profile" :class="{ 'is-active': activeIndex === 'Profile' }" id="Profile">
                    <font-awesome-icon :icon="['far', 'user']" />&nbsp;&nbsp;{{ $t('navigateBar.Profile') }}
                </el-menu-item>
                <el-menu-item @click="logout()">
                    <font-awesome-icon :icon="['fas', 'arrow-right-from-bracket']" />&nbsp;&nbsp;{{ $t('navigateBar.Logout') }}
                </el-menu-item>
            </el-sub-menu>
            <el-sub-menu v-if="userData.userName && userData?.isAdmin" index="2">
                <template #title>
                    {{ $t('navigateBar.Manage') }}
                </template>
                <el-menu-item index="ManageUser" :class="{ 'is-active': activeIndex === 'ManageUser' } "
                    v-if="userData?.isAdmin">
                    <font-awesome-icon :icon="['far', 'address-book']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageUsers') }}
                </el-menu-item>
                <el-menu-item index="ManageCompany" :class="{ 'is-active': activeIndex === 'ManageCompany' } "
                    v-if="userData?.isAdmin">
                    <font-awesome-icon :icon="['far', 'keyboard']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageCompanys') }}
                </el-menu-item>
                <el-menu-item index="ManageTasks" :class="{ 'is-active': activeIndex === 'ManageTasks' }"
                    v-if="userData?.isAdmin">
                    <font-awesome-icon :icon="['fas', 'list-check']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageTasks') }}
                </el-menu-item>
                <el-menu-item index="ManageFeedback" :class="{ 'is-active': activeIndex === 'ManageFeedback' }"
                    v-if="userData.userName && userData.isAdmin">
                    <font-awesome-icon :icon="['far', 'message']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageFeedbacks') }}
                </el-menu-item>
                <el-menu-item index="ManageLogs" :class="{ 'is-active': activeIndex === 'ManageLogs' }"
                    v-if="userData.userName && userData.isAdmin">
                    <font-awesome-icon :icon="['fas', 'clipboard-list']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageLogs') }}
                </el-menu-item>
                <el-menu-item index="ManageModel" :class="{ 'is-active': activeIndex === 'ManageModel' }"
                    v-if="userData.userName && userData.isAdmin">
                    <font-awesome-icon :icon="['fas', 'hexagon-nodes']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageModel') }}
                </el-menu-item>
                <el-menu-item index="SystemSettings" :class="{ 'is-active': activeIndex === 'SystemSettings' }"
                    v-if="userData.userName && userData.isAdmin">
                    <font-awesome-icon :icon="['fas', 'gear']" />&nbsp;&nbsp;{{ $t('navigateBar.SystemSettings') }}
                </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="WorkSpace" :class="{ 'is-active': activeIndex === 'WorkSpace' }" id="WorkSpase"
                v-if="userData.userName">
                {{ $t('navigateBar.WorkSpace') }}
            </el-menu-item>
            <el-menu-item index="Upload" :class="{ 'is-active': activeIndex === 'Upload' }" v-if="userData.userName">
                {{ $t('navigateBar.Upload') }}
            </el-menu-item>
            <el-menu-item index="Virtualization" :class="{ 'is-active': activeIndex === 'Virtualization' }">
                {{ $t('navigateBar.Virtualization') }}
            </el-menu-item>
            <el-menu-item index="Feedback" :class="{ 'is-active': activeIndex === 'Feedback' }"
                v-if="userData.userName">
                {{ $t('navigateBar.Feedback') }}
            </el-menu-item>
            <div class="dark-mode-toggle">
                <el-switch id="dark" v-model="isDarkMode" :active-icon="Sunny" :inactive-icon="Moon" inline-prompt
                    width="15" @click="toggleTheme($event)"></el-switch>
            </div>
            <!-- Language Switcher -->
            <div class="language-switcher">
                <el-dropdown @command="changeLanguage">
                    <span class="language-icon">
                        <font-awesome-icon :icon="['fas', 'language']" size="2x"/>
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="en">English</el-dropdown-item>
                            <el-dropdown-item command="zh">中文</el-dropdown-item>
                            <!-- 添加更多语言选项 -->
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </el-menu>

        <!-- Mobile Menu -->
        <div class="mobile-header" v-if="isMobile">
            <div class="mobile-header-top">
                <img style="width: 40px" src="../assets/logo.png" alt="logo" @click="navigateTo('HomeView')" />
                <div class="mobile-controls">
                    <el-dropdown @command="changeLanguage" trigger="click">
                        <span class="language-icon-mobile">
                            <font-awesome-icon :icon="['fas', 'language']" size="lg"/>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="en">English</el-dropdown-item>
                                <el-dropdown-item command="zh">中文</el-dropdown-item>
                                <!-- 添加更多语言选项 -->
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                    <el-switch id="dark-mobile" v-model="isDarkMode" :active-icon="Sunny" :inactive-icon="Moon" inline-prompt
                        width="15" @click="toggleTheme($event)" class="mobile-dark-toggle"></el-switch>
                    <el-button link @click="mobileMenuOpen = !mobileMenuOpen" class="mobile-menu-button">
                        <el-icon v-if="!mobileMenuOpen"><Menu /></el-icon>
                        <el-icon v-else><Close /></el-icon>
                    </el-button>
                </div>
            </div>
            
            <!-- Mobile Menu Dropdown -->
            <el-collapse-transition>
                <div class="mobile-menu-dropdown" v-if="mobileMenuOpen">
                    <!-- 未登录状态菜单 - 两列 -->
                    <template v-if="!userData.userName">
                        <div class="mobile-grid">
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'Login' }" @click="navigateTo('Login')">
                                <font-awesome-icon :icon="['fas', 'right-to-bracket']" />&nbsp;&nbsp;{{ $t('navigateBar.Login') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'Register' }" @click="navigateTo('Register')">
                                <font-awesome-icon :icon="['fas', 'registered']" />&nbsp;&nbsp;{{ $t('navigateBar.Register') }}
                            </div>
                        </div>
                    </template>
                    
                    <!-- 用户个人菜单 - 两列 -->
                    <template v-else>
                        <div class="mobile-user-profile">
                            <el-avatar :src="userData.avatarBase64 ? 'data:image/jpeg;base64,' + userData.avatarBase64 : defaultAvatar" size="small"></el-avatar>
                            <span>{{ userData.userName }}</span>
                        </div>
                        <div class="mobile-grid">
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'Profile' }" @click="navigateTo('Profile')">
                                <font-awesome-icon :icon="['far', 'user']" />&nbsp;&nbsp;{{ $t('navigateBar.Profile') }}
                            </div>
                            <div class="mobile-menu-item grid-item" @click="logout()">
                                <font-awesome-icon :icon="['fas', 'arrow-right-from-bracket']" />&nbsp;&nbsp;{{ $t('navigateBar.Logout') }}
                            </div>
                        </div>
                    </template>
                    
                    <!-- 管理员菜单 - 两列 -->
                    <template v-if="userData.userName && userData?.isAdmin">
                        <div class="mobile-menu-section">{{ $t('navigateBar.Manage') }}</div>
                        <div class="mobile-grid">
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'ManageUser' }" @click="navigateTo('ManageUser')">
                                <font-awesome-icon :icon="['far', 'address-book']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageUsers') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'ManageCompany' }" @click="navigateTo('ManageCompany')">
                                <font-awesome-icon :icon="['far', 'keyboard']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageCompanys') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'ManageTasks' }" @click="navigateTo('ManageTasks')">
                                <font-awesome-icon :icon="['fas', 'list-check']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageTasks') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'ManageFeedback' }" @click="navigateTo('ManageFeedback')">
                                <font-awesome-icon :icon="['far', 'message']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageFeedbacks') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'ManageLogs' }" @click="navigateTo('ManageLogs')">
                                <font-awesome-icon :icon="['fas', 'clipboard-list']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageLogs') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'ManageModel' }" @click="navigateTo('ManageModel')">
                                <font-awesome-icon :icon="['fas', 'hexagon-nodes']" />&nbsp;&nbsp;{{ $t('navigateBar.ManageModel') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'SystemSettings' }" @click="navigateTo('SystemSettings')">
                                <font-awesome-icon :icon="['fas', 'gear']" />&nbsp;&nbsp;{{ $t('navigateBar.SystemSettings') }}
                            </div>
                        </div>
                    </template>

                    <!-- 用户应用功能菜单 - 两列 -->
                    <template v-if="userData.userName">
                        <div class="mobile-menu-section">{{ $t('navigateBar.Applications') }}</div>
                        <div class="mobile-grid">
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'WorkSpace' }" @click="navigateTo('WorkSpace')">
                                <font-awesome-icon :icon="['fas', 'gauge']" />&nbsp;&nbsp;{{ $t('navigateBar.WorkSpace') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'Upload' }" @click="navigateTo('Upload')">
                                <font-awesome-icon :icon="['fas', 'upload']" />&nbsp;&nbsp;{{ $t('navigateBar.Upload') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'Feedback' }" @click="navigateTo('Feedback')">
                                <font-awesome-icon :icon="['far', 'comment-dots']" />&nbsp;&nbsp;{{ $t('navigateBar.Feedback') }}
                            </div>
                            <div class="mobile-menu-item grid-item" :class="{ active: activeIndex === 'Virtualization' }" @click="navigateTo('Virtualization')">
                                <font-awesome-icon :icon="['fas', 'magnifying-glass-chart']" />&nbsp;&nbsp;{{ $t('navigateBar.Virtualization') }}
                            </div>
                        </div>
                    </template>
                    
                    <!-- 未登录状态下只显示可视化菜单 -->
                    <template v-if="!userData.userName">
                        <div class="mobile-menu-item" :class="{ active: activeIndex === 'Virtualization' }" @click="navigateTo('Virtualization')">
                            <font-awesome-icon :icon="['fas', 'magnifying-glass-chart']" />&nbsp;&nbsp;{{ $t('navigateBar.Virtualization') }}
                        </div>
                    </template>
                </div>
            </el-collapse-transition>
        </div>
    </div>
</template>

<script setup>
import { Sunny, Moon, Menu, Close } from '@element-plus/icons-vue'
import { useDark, useToggle } from '@vueuse/core'
import { ref, onMounted, onUnmounted } from 'vue'; 
const isDark = useDark();
const toggleDark = useToggle(isDark);
const isDarkTag = ref(false);

const isMobile = ref(false);

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
};

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});

const toggleTheme = (event) => {
  const x = event.clientX;
  const y = event.clientY;
  const endRadius = Math.hypot(
      Math.max(x, innerWidth - x),
      Math.max(y, innerHeight - y)
  );

  // 兼容性处理
  if (!document.startViewTransition) {
    toggleDark()
    return
  }
  const transition = document.startViewTransition(() => {})

  transition.ready.then(() => {
    const clipPath = [
      `circle(0px at ${x}px ${y}px)`,
      `circle(${endRadius}px at ${x}px ${y}px)`,
    ];
    document.documentElement.animate(
        {
          clipPath: isDarkTag.value ? [...clipPath].reverse() : clipPath,
        },
        {
          duration: 300,
          easing: 'ease-in',
          pseudoElement: isDarkTag.value
              ? '::view-transition-old(root)'
              : '::view-transition-new(root)',
        }
    );
  });
}
</script>

<script>
import logo from '../assets/logo.png';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faLanguage } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

// 注册 Font Awesome 图标
library.add(faLanguage);

export default {
    name: "MainHeader",
    components: {
        FontAwesomeIcon
    },
    data() {
        return {
            activeIndex: "", // 当前激活的菜单项
            defaultAvatar: logo,
            userData: JSON.parse(sessionStorage.getItem('userData')) || {},
            isDarkMode: false, // 黑暗模式开关
            mobileMenuOpen: false,
            currentLanguage: localStorage.getItem('language') || 'zh', // 默认语言
        };
    },
    methods: {
        handleSelect(index) {
            this.activeIndex = index; // 选择菜单项时更新当前激活项
        },
        logout() {
            window.sessionStorage.clear();
            this.$router.push('/Login');
            if (this.isMobile) {
                this.mobileMenuOpen = false;
            }
        },
        navigateTo(route) {
            this.activeIndex = route;
            this.$router.push('/' + route);
            this.mobileMenuOpen = false;
        },
        changeLanguage(lang) {
            if (this.currentLanguage !== lang) {
                this.currentLanguage = lang;
                localStorage.setItem('language', lang);
                this.$i18n.locale = lang;
                window.location.reload(); // TODO 这里刷新后才会改变el自带组件的语言，逻辑在App.vue
            }
        }
    },
    watch: {
        // 监听路由变化，更新激活菜单项
        $route(to) {
            this.activeIndex = to.name;
        },
        isDarkMode(newVal) {
            this.$emit('darkmodeChanged', this.isDarkMode); // 全局通知，此处用于更新图表图例文字
            // 将黑暗模式状态保存到本地存储
            localStorage.setItem('isDarkMode', newVal);
            document.body.classList.toggle('dark-mode', newVal); // 切换 body 的黑暗模式类
            document.documentElement.classList.toggle('dark', newVal);
        },
    },
    mounted() {
        document.documentElement.classList.remove('dark');
        this.activeIndex = this.$route.name;
        this.isDarkMode = JSON.parse(localStorage.getItem('isDarkMode')) || false;
        if (this.isDarkMode) {
            document.body.classList.toggle('dark-mode', this.isDarkMode);
            document.documentElement.classList.add('dark');
        }
        // 设置初始语言
        this.$i18n.locale = this.currentLanguage;
    },
};
</script>

<style scoped>
/* Desktop styles */
.el-menu--horizontal>.el-menu-item:nth-child(1) {
    margin-right: auto;
}

.el-menu {
    position: fixed;
    z-index: 1000;
    width: 100%;
    border-bottom: 0px;
}

.main-header {
    background: linear-gradient(0deg, rgba(255, 255, 255, 0), #ffffff);
}

.dark-mode-toggle {
    margin: 13px;
    margin-right: 15px;
}

.language-switcher {
    margin: 15px 30px 13px 10px;
    cursor: pointer;
    font-size: 18px;
}

.language-icon {
    color: #409EFF;
}

.dark-mode .language-icon {
    color: #79bbff;
}

.dark-mode .main-header {
    background: #3e3e3e;
}

/* Mobile styles */
.mobile-header {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 1000;
    background: linear-gradient(0deg, rgba(255, 255, 255, 0), #ffffff);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dark-mode .mobile-header {
    background: #3e3e3e;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.mobile-header-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    height: 56px;
}

.mobile-controls {
    display: flex;
    align-items: center;
    gap: 15px;
}

.mobile-dark-toggle {
    margin-right: 5px;
}

.language-icon-mobile {
    font-size: 18px;
    color: #409EFF;
    cursor: pointer;
    margin-top: 2px;
}

.dark-mode .language-icon-mobile {
    color: #79bbff;
}

.mobile-menu-button {
    font-size: 24px;
    padding: 5px;
}

.mobile-menu-dropdown {
    background-color: #fff;
    border-top: 1px solid #eee;
    padding: 0 0 10px 0;
}

.dark-mode .mobile-menu-dropdown {
    background-color: #3e3e3e;
    border-top: 1px solid #555;
}

.mobile-menu-item {
    padding: 12px 20px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.mobile-menu-item:hover, .mobile-menu-item.active {
    background-color: #f5f7fa;
}

.dark-mode .mobile-menu-item:hover, .dark-mode .mobile-menu-item.active {
    background-color: #4e4e4e;
}

.mobile-menu-section {
    padding: 12px 20px;
    font-weight: bold;
    color: #909399;
    border-top: 1px solid #eee;
    margin-top: 5px;
}

.dark-mode .mobile-menu-section {
    color: #a6a9ad;
    border-top: 1px solid #555;
}

.mobile-user-profile {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    border-bottom: 1px solid #eee;
}

.dark-mode .mobile-user-profile {
    border-bottom: 1px solid #555;
}

/* View transition animations */
.dark::view-transition-old(root) {
    z-index: 1;
}
.dark::view-transition-new(root) {
    z-index: 1999;
}

::view-transition-old(root) {
    z-index: 1999;
}
::view-transition-new(root) {
    z-index: 1;
}
::view-transition-old(root),
::view-transition-new(root) {
    animation: none;
    mix-blend-mode: normal;
}

/* Media queries for responsive design */
@media (max-width: 768px) {
    .desktop-menu {
        display: none;
    }
}

@media (min-width: 769px) and (max-width: 1024px) {
    /* Tablet-specific styles */
    .el-menu-item, .el-sub-menu__title {
        padding: 0 10px;
    }
}

/* 网格布局样式 */
.mobile-grid {
    display: grid;
    grid-template-columns: 1fr 1fr; /* 两列等宽 */
    gap: 1px;
    margin-bottom: 5px;
}

.grid-item {
    padding: 12px 10px;
    font-size: 0.9rem;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    border-radius: 4px;
    margin: 2px;
}

/* 调整图标和文字对齐方式 */
.grid-item .svg-inline--fa {
    min-width: 16px;
}

/* 深色模式样式 */
.dark-mode .mobile-grid {
    border-color: #555;
}

/* 确保未登录时的可视化菜单保持单列 */
.mobile-menu-dropdown > template:last-child .mobile-menu-item {
    grid-column: span 2;
}

/* 媒体查询，确保在更小屏幕上也能正常显示 */
@media (max-width: 768px) {
    .grid-item {
        font-size: 0.8rem;
    }
}

@media (max-width: 320px) {
    .grid-item {
        padding: 10px 8px;
        font-size: 0.8rem;
    }
}
</style>