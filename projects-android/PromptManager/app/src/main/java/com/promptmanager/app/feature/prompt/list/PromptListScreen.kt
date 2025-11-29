package com.promptmanager.app.feature.prompt.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.promptmanager.app.R
import com.promptmanager.app.core.designsystem.theme.BackgroundGrey
import com.promptmanager.app.core.designsystem.theme.ChipSelectedBg
import com.promptmanager.app.core.designsystem.theme.ChipSelectedText
import com.promptmanager.app.core.designsystem.theme.TextGrey

data class PromptItem(
    val title: String,
    val content: String,
    val tags: List<String>,
    val useCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptListScreen(
    onNavigateToCreate: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("全部") }
    
    val filters = listOf("全部", "工作", "写作", "开发", "营销", "数据分析")

    // Mock Data
    val prompts = listOf(
        PromptItem(
            "社交媒体文案生成",
            "为{platform}平台生成一条关于{topic}的文案，要求： - 风格：{tone} - 包含emoji...",
            listOf("写作", "文案", "社交媒体"),
            67
        ),
        PromptItem(
            "文章改写助手",
            "请将以下文章改写为{tone}风格，字数控制在{length}字左右...",
            listOf("写作", "改写"),
            45
        ),
        PromptItem(
            "翻译助手",
            "将以下文本从{source_lang}翻译成{target_lang}...",
            listOf("翻译", "通用"),
            89
        ),
        PromptItem(
            "SEO 标题优化",
            "为以下内容生成 SEO 优化的标题...",
            listOf("营销", "SEO"),
            34
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGrey),
                actions = {
                    IconButton(onClick = { /* TODO: Search Logic */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Prompt")
            }
        },
        containerColor = BackgroundGrey
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        leadingIcon = {
                            // Use ic_folder for all filters as seen in Figma
                             Icon(
                                 painter = painterResource(R.drawable.ic_folder),
                                 contentDescription = null,
                                 modifier = Modifier.size(16.dp),
                                 tint = if (isSelected) ChipSelectedText else TextGrey
                             )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChipSelectedBg,
                            selectedLabelColor = ChipSelectedText,
                            containerColor = Color.White,
                            labelColor = Color(0xFF0A0A0A)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                items(prompts) { prompt ->
                    PromptCard(prompt)
                }
            }
        }
    }
}

@Composable
fun PromptCard(prompt: PromptItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prompt.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_card_icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Unspecified // Keep original colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Preview
            Text(
                text = prompt.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGrey,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(prompt.tags) { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF0A0A0A),
                            modifier = Modifier
                                .background(Color(0xFFECEEF2), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Stats
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_stats),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextGrey
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = prompt.useCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGrey
                    )
                }
            }
        }
    }
}
