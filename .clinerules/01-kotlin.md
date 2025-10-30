# Kotlin 通用规范（Kotlin General Guidelines）

## 基本原则（Basic Principles）
- 所有文档均使用中文。
- 所有变量和函数（包括参数与返回值）必须显式声明类型：
  - 禁止使用 any。
  - 必要时创建明确的类型。
- 函数内部不允许空行。

## 命名规范（Nomenclature）
- 类名使用 PascalCase（大驼峰）。
- 变量、函数、方法使用 camelCase（小驼峰）。
- 文件与目录名使用 underscored_case（下划线命名法）。
- 环境变量使用 UPPERCASE。
- 禁止使用魔法数字，应定义为常量。
- 函数必须以动词开头。
- 布尔变量使用动词前缀命名，例如：isLoading、hasError、canDelete。
- 避免缩写，使用完整单词：
  - 除非是标准缩写（如 API、URL 等）。
  - 或广泛认可的缩写，如：
    - i, j：用于循环
    - err：用于错误
    - ctx：用于上下文
    - req, res, next：用于中间件函数参数

## 函数（Functions）
- 函数（包括方法）应短小，职责单一。代码行数不超过 20。
- 函数名需由动词 + 名词构成：
  - 返回布尔值的函数以 isX、hasX、canX 等命名。
  - 不返回任何内容的函数以 executeX、saveX 等命名。
- 避免代码嵌套，方法包括：
  - 提前检查并返回（early return）。
  - 提取为辅助函数。
  - 尽可能使用高阶函数（如 map、filter、reduce）避免嵌套。
- 简单函数（3 行以内）使用箭头函数（lambda）。
- 非简单函数使用命名函数。
- 使用默认参数值，避免在函数中判断 null。
- 遵循 RO-RO（Request Object - Response Object）：
  - 多个参数传入时使用对象封装。
  - 函数返回值也用对象封装。
  - 所有输入输出类型均需显式声明。
- 保持单一抽象层级。

## 数据（Data）
- 使用 data class 表示数据结构。
- 不滥用原始类型，应使用复合类型封装。
- 避免在函数中处理数据校验，使用内部封装校验的类。
- 数据尽量保持不可变：
  - 不会变化的数据使用只读。
  - 字面量常量使用 val。

## 类（Classes）
- 遵循 SOLID 原则。
- 优先使用组合而非继承。
- 使用接口定义契约。
- 类应职责单一，结构小巧：
  - 不超过 200 行代码。
  - 公共方法不超过 10 个。
  - 成员属性不超过 10 个。

## 异常（Exceptions）
- 使用异常处理不可预期的错误。
- 捕获异常时，必须是为了：
  - 修复预期的问题；
  - 添加上下文信息；
  - 否则应交由全局异常处理器处理。

## 测试（Testing）
- 使用 AAA 模式（Arrange-Act-Assert）组织测试。
- 测试变量命名需明确：
  - 遵循命名规则：inputX、mockX、actualX、expectedX。
- 为每个公共函数编写单元测试。
- 使用测试替身（Mock、Stub 等）模拟依赖项。
  - 除非是开销极小的第三方依赖。
- 每个模块编写验收测试（Acceptance Test）。
- 遵循 Given-When-Then 结构。

## Android 专属规范（Specific to Android）

### 基本原则
- 使用 Clean Architecture（整洁架构）：
  - 若需要组织数据访问，请使用 Repository 层。
  - 数据持久化采用 Repository 模式。
  - 缓存请使用 cache 层。
- 状态管理使用 MVI 模式（Model-View-Intent）：
  - ViewModel 负责管理状态与事件；
  - Activity / Fragment 触发事件并渲染状态。
- 身份认证流程应通过 Auth Activity 管理，包括：
  - 启动页（Splash Screen）
  - 登录（Login）
  - 注册（Register）
  - 忘记密码（Forgot Password）
  - 验证邮箱（Verify Email）
- 页面导航使用 Navigation Component。
- 主导航使用 MainActivity 管理，使用 BottomNavigationView 底部导航：
  - 首页（Home）
  - 个人中心（Profile）
  - 设置（Settings）
  - 患者列表（Patients）
  - 预约列表（Appointments）
- 使用 ViewBinding 管理视图。
- 使用 Flow / LiveData 管理 UI 状态。
- 使用 XML 与 Fragment（而非 Jetpack Compose）。
- UI 使用 Material 3。
- 布局使用 ConstraintLayout。
