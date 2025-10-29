# Android端设计文档

## 1. 项目概述

### 1.1 项目目标
构建一个现代化的Prompt管理Android应用，提供优秀的移动端用户体验和离线支持。

### 1.2 核心功能
- **用户认证**：注册、登录、三方登录、游客体验
- **Prompt管理**：增删改查、搜索、分类、收藏
- **标签管理**：标签创建、使用统计
- **数据同步**：多端数据同步，支持离线使用
- **分享功能**：Prompt分享和复制

### 1.3 技术栈
- **语言**：Kotlin
- **架构**：MVVM + Repository模式
- **UI框架**：Jetpack Compose
- **数据库**：Room
- **网络**：Retrofit + OkHttp
- **依赖注入**：Hilt/Dagger
- **构建工具**：Gradle

## 2. 项目结构

### 2.1 模块化架构
```
app/
├── data/                # 数据层
│   ├── local/          # 本地数据源
│   ├── remote/         # 远程数据源
│   └── repository/     # 数据仓库
├── domain/             # 领域层
│   ├── model/          # 领域模型
│   ├── usecase/        # 用例
│   └── repository/     # 仓库接口
├── presentation/       # 表现层
│   ├── ui/             # UI组件
│   ├── viewmodel/      # ViewModel
│   └── navigation/     # 导航
└── di/                 # 依赖注入
```

### 2.2 包结构
```
com.promptmanager.android
├── data
│   ├── local
│   │   ├── dao
│   │   └── entity
│   ├── remote
│   │   ├── api
│   │   └── dto
│   └── repository
├── domain
│   ├── model
│   ├── usecase
│   └── repository
├── presentation
│   ├── ui
│   │   ├── auth
│   │   ├── prompts
│   │   └── common
│   ├── viewmodel
│   └── navigation
└── di
```

## 3. 数据层设计

### 3.1 本地数据库设计
```kotlin
// 数据库实体
@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val description: String?,
    val tags: List<String>,
    val category: String,
    val variables: List<String>,
    val useCount: Int,
    val isPublic: Boolean,
    val isFavorite: Boolean,
    val isDeleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastUsedAt: Long?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val color: String?,
    val useCount: Int,
    val createdAt: Long
)

@Entity(tableName = "sync_records")
data class SyncRecordEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val deviceId: String,
    val deviceType: String,
    val lastSyncTime: Long,
    val syncVersion: Long,
    val pendingChanges: Int
)
```

### 3.2 Room数据库配置
```kotlin
@Database(
    entities = [PromptEntity::class, TagEntity::class, SyncRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptDao
    abstract fun tagDao(): TagDao
    abstract fun syncRecordDao(): SyncRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prompt_manager.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

### 3.3 数据访问对象
```kotlin
@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getPromptsByUser(userId: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun getPromptById(id: String): PromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)

    @Update
    suspend fun updatePrompt(prompt: PromptEntity)

    @Query("UPDATE prompts SET isDeleted = 1 WHERE id = :id")
    suspend fun deletePrompt(id: String)

    @Query("SELECT * FROM prompts WHERE syncStatus != 'SYNCED'")
    suspend fun getUnsyncedPrompts(): List<PromptEntity>
}
```

## 4. 网络层设计

### 4.1 API服务定义
```kotlin
interface PromptApiService {
    @GET("prompts")
    suspend fun getPrompts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("tags") tags: List<String>? = null
    ): ApiResponse<PromptListResponse>

    @GET("prompts/{id}")
    suspend fun getPrompt(@Path("id") id: String): ApiResponse<PromptResponse>

    @POST("prompts")
    suspend fun createPrompt(@Body request: CreatePromptRequest): ApiResponse<PromptResponse>

    @PUT("prompts/{id}")
    suspend fun updatePrompt(
        @Path("id") id: String,
        @Body request: UpdatePromptRequest
    ): ApiResponse<PromptResponse>

    @DELETE("prompts/{id}")
    suspend fun deletePrompt(@Path("id") id: String): ApiResponse<Unit>

    @POST("prompts/{id}/favorite")
    suspend fun favoritePrompt(@Path("id") id: String): ApiResponse<PromptResponse>

    @POST("prompts/{id}/use")
    suspend fun usePrompt(@Path("id") id: String): ApiResponse<PromptResponse>
}
```

### 4.2 Retrofit配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePromptApiService(retrofit: Retrofit): PromptApiService {
        return retrofit.create(PromptApiService::class.java)
    }
}
```

### 4.3 认证拦截器
```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            tokenManager.getToken()?.let { token ->
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            // Token过期，清除本地token
            tokenManager.clearToken()
            // 可以触发重新登录逻辑
        }

        return response
    }
}
```

## 5. 领域层设计

### 5.1 领域模型
```kotlin
data class Prompt(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val description: String?,
    val tags: List<String>,
    val category: PromptCategory,
    val variables: List<String>,
    val useCount: Int,
    val isPublic: Boolean,
    val isFavorite: Boolean,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastUsedAt: Instant?,
    val syncStatus: SyncStatus
)

enum class PromptCategory {
    GENERAL, WRITING, PROGRAMMING, ANALYSIS, CREATIVE
}

enum class SyncStatus {
    SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE
}
```

### 5.2 用例定义
```kotlin
class GetPromptsUseCase @Inject constructor(
    private val promptRepository: PromptRepository
) {
    operator fun invoke(
        userId: String,
        searchQuery: String? = null,
        category: PromptCategory? = null,
        tags: List<String>? = null
    ): Flow<List<Prompt>> {
        return promptRepository.getPrompts(userId, searchQuery, category, tags)
    }
}

class CreatePromptUseCase @Inject constructor(
    private val promptRepository: PromptRepository
) {
    suspend operator fun invoke(prompt: Prompt): Result<Prompt> {
        return promptRepository.createPrompt(prompt)
    }
}

class SyncDataUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(userId: String): Result<SyncResult> {
        return syncRepository.syncData(userId)
    }
}
```

## 6. 表现层设计

### 6.1 ViewModel设计
```kotlin
@HiltViewModel
class PromptListViewModel @Inject constructor(
    private val getPromptsUseCase: GetPromptsUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromptListUiState())
    val uiState: StateFlow<PromptListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromptListEvent>()
    val events: SharedFlow<PromptListEvent> = _events.asSharedFlow()

    init {
        loadPrompts()
        startPeriodicSync()
    }

    fun loadPrompts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = userManager.getCurrentUserId()
                getPromptsUseCase(userId).collect { prompts ->
                    _uiState.update { it.copy(prompts = prompts, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(PromptListEvent.ShowError("加载失败: ${e.message}"))
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // 可以添加防抖逻辑
        loadPrompts()
    }

    private fun startPeriodicSync() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000) // 5分钟同步一次
                syncData()
            }
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            val userId = userManager.getCurrentUserId()
            syncDataUseCase(userId).onSuccess { result ->
                if (result.hasChanges) {
                    loadPrompts() // 重新加载数据
                }
            }.onFailure { error ->
                _events.emit(PromptListEvent.ShowError("同步失败: ${error.message}"))
            }
        }
    }
}

data class PromptListUiState(
    val prompts: List<Prompt> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)

sealed class PromptListEvent {
    data class ShowError(val message: String) : PromptListEvent()
    object NavigateToCreate : PromptListEvent()
    data class NavigateToDetail(val promptId: String) : PromptListEvent()
}
```

### 6.2 Compose UI组件
```kotlin
@Composable
fun PromptListScreen(
    viewModel: PromptListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PromptListEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                PromptListEvent.NavigateToCreate -> {
                    // 导航到创建页面
                }
                is PromptListEvent.NavigateToDetail -> {
                    // 导航到详情页面
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prompt Manager") },
                actions = {
                    IconButton(onClick = { /* 打开设置 */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onCreatePrompt() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建Prompt")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 搜索栏
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth()
            )

            // 加载状态
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Prompt列表
            LazyColumn {
                items(uiState.prompts) { prompt ->
                    PromptItem(
                        prompt = prompt,
                        onItemClick = { viewModel.onPromptClick(prompt.id) },
                        onFavoriteClick = { viewModel.onFavoriteClick(prompt.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun PromptItem(
    prompt: Prompt,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable { onItemClick() },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = prompt.title,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (prompt.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "收藏",
                        tint = if (prompt.isFavorite) Color.Red else Color.Gray
                    )
                }
            }

            Text(
                text = prompt.content,
                style = MaterialTheme.typography.body2,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // 标签显示
            FlowRow(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                prompt.tags.forEach { tag ->
                    Chip(
                        onClick = { /* 标签点击 */ },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(text = tag)
                    }
                }
            }

            // 使用统计
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "使用次数",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "使用 ${prompt.useCount} 次",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
```

## 7. 数据同步设计

### 7.1 同步策略
```kotlin
class SyncRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val syncRecordDao: SyncRecordDao
) : SyncRepository {

    override suspend fun syncData(userId: String): Result<SyncResult> {
        return try {
            // 1. 获取本地未同步的变更
            val localChanges = getLocalChanges(userId)
            
            // 2. 推送本地变更到服务器
            val pushResult = pushChangesToServer(localChanges)
            
            // 3. 从服务器拉取最新数据
            val pullResult = pullChangesFromServer(userId)
            
            // 4. 更新同步记录
            updateSyncRecord(userId)
            
            Result.success(SyncResult(
                hasChanges = pushResult.hasChanges || pullResult.hasChanges,
                pushedCount = pushResult.pushedCount,
                pulledCount = pullResult.pulledCount
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getLocalChanges(userId: String): LocalChanges {
        val unsyncedPrompts =
