# 图书阅读进度跟踪应用

> 注：此仓库原为诊所预约管理系统（ClinicAppointmentManagement），现已转为图书阅读进度跟踪应用项目。

这是一个使用Java Swing和H2数据库开发的图书阅读进度跟踪应用程序。

## 环境要求

- IntelliJ IDEA Community Edition 2024.2.0.2
- Java 17
- Maven

## 导入项目

1. 打开IntelliJ IDEA
2. 选择 "File" -> "Open"
3. 选择项目根目录
4. 等待IntelliJ IDEA导入Maven项目

## 运行应用

1. 在项目视图中找到 `com.booktracker.BookTrackerApplication` 类
2. 右键点击并选择 "Run 'BookTrackerApplication.main()'"

## 功能特性

- 图书信息管理
- 阅读进度跟踪
- 多用户支持（每个用户独立数据库文件）
- 阅读时间统计
- 进度统计分析

## 项目结构

```
src/main/java/com/booktracker/
├── BookTrackerApplication.java  // 主程序入口
├── model/                      // 数据模型
├── dao/                        // 数据访问层
├── service/                    // 业务逻辑层
└── ui/                        // 用户界面
    ├── dialogs/              // 对话框
    ├── panels/              // 面板组件
    └── MainWindow.java      // 主窗口
```

## 数据库说明

应用使用H2数据库的文件模式，数据库文件存储在用户目录下：

```
data/users/{username}/booktracker
```

每个用户都有独立的数据库文件，确保数据隔离。

## 使用说明

1. 添加图书：点击"添加图书"按钮，输入书名、作者和总页数
2. 更新进度：选择图书，点击"更新进度"按钮，记录阅读时间和页码
3. 查看统计：在统计面板中查看阅读时间和进度统计

## 技术栈

- Java 17
- Swing UI
- H2 Database
- JDBC
- Maven
>>>>>>> 8ffdf177 (Initial commit: VS Code setup)
