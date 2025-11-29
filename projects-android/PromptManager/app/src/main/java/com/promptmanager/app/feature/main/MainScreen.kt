package com.promptmanager.app.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.promptmanager.app.R
import com.promptmanager.app.core.designsystem.theme.ChipSelectedText
import com.promptmanager.app.feature.prompt.list.PromptListScreen

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val folders by viewModel.folders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()

    val items = listOf(
        stringResource(R.string.nav_prompt),
        stringResource(R.string.nav_search),
        stringResource(R.string.nav_favorites),
        stringResource(R.string.nav_profile)
    )
    val icons = listOf(
        R.drawable.ic_nav_prompt,
        R.drawable.ic_nav_search,
        R.drawable.ic_nav_fav,
        R.drawable.ic_nav_profile
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                folders = folders,
                selectedFolderId = selectedFolderId,
                onFolderClick = { folderId ->
                    viewModel.selectFolder(folderId)
                    scope.launch { drawerState.close() }
                },
                onCloseClick = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
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
                                painter = androidx.compose.ui.res.painterResource(id = icons[index]),
                                contentDescription = item
                            )
                        },
                            label = { Text(item) },
                            selected = isSelected,
                            onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ChipSelectedText,
                            selectedTextColor = ChipSelectedText,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.White // Use White to hide the pill if Transparent fails
                        )
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Content switching
            when (selectedTab) {
                0 -> {
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        PromptListScreen(
                            selectedFolderId = selectedFolderId,
                            onNavigateToCreate = onNavigateToCreate,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                }
                1 -> PlaceholderScreen("Search", Modifier.padding(innerPadding))
                2 -> PlaceholderScreen("Favorites", Modifier.padding(innerPadding))
                3 -> PlaceholderScreen("Profile", Modifier.padding(innerPadding))
            }
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
