package com.promptmanager.app.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.promptmanager.app.R
import com.promptmanager.app.core.designsystem.theme.ChipSelectedText
import com.promptmanager.app.feature.prompt.list.PromptListScreen

@Composable
fun MainScreen(
    onNavigateToCreate: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    val items = listOf("Prompt", "Search", "Favorites", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Search,
        Icons.Default.FavoriteBorder,
        Icons.Default.Person
    )
    val selectedIcons = listOf(
        Icons.Default.Home, // Filled version if available
        Icons.Default.Search,
        Icons.Default.Favorite,
        Icons.Default.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (isSelected) selectedIcons[index] else icons[index],
                                contentDescription = item
                            )
                        },
                        label = { Text(item) },
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChipSelectedText,
                            selectedTextColor = ChipSelectedText,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Content switching
        when (selectedTab) {
            0 -> {
                // Pass modifier with padding to content
                // PromptListScreen needs to handle padding or be wrapped
                // Since PromptListScreen has its own Scaffold, we might have nested Scaffolds which is okay but padding handling is key.
                // Actually PromptListScreen has Scaffold, so maybe MainScreen shouldn't wrap it in Scaffold content directly?
                // Better approach: MainScreen manages the high level nav, PromptListScreen is just content.
                // But PromptListScreen needs FAB.
                // Let's wrap PromptListScreen in a Box with padding for now.
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    PromptListScreen(onNavigateToCreate = onNavigateToCreate)
                }
            }
            1 -> PlaceholderScreen("Search", Modifier.padding(innerPadding))
            2 -> PlaceholderScreen("Favorites", Modifier.padding(innerPadding))
            3 -> PlaceholderScreen("Profile", Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun PlaceholderScreen(text: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = text)
    }
}
