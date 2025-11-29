package com.promptmanager.app.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
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
import com.promptmanager.app.R
import com.promptmanager.app.core.database.entity.FolderEntity
import com.promptmanager.app.core.designsystem.theme.ChipSelectedText
import com.promptmanager.app.core.designsystem.theme.TextGrey

data class FolderUiModel(
    val id: String,
    val name: String,
    val count: Int,
    val children: List<FolderUiModel> = emptyList()
)

@Composable
fun DrawerContent(
    folders: List<FolderEntity> = emptyList(),
    selectedFolderId: String? = null,
    onFolderClick: (String?) -> Unit = {},
    onCloseClick: () -> Unit
) {
    // Convert flat list to tree
    val folderTree = remember(folders) {
        val rootFolders = folders.filter { it.parentId == null }
        rootFolders.map { root ->
            val children = folders.filter { it.parentId == root.id }.map { child ->
                FolderUiModel(child.id, child.name, child.promptCount)
            }
            FolderUiModel(root.id, root.name, root.promptCount, children)
        }
    }
    
    // Add "All" folder
    val allFolders = listOf(FolderUiModel("all", stringResource(R.string.drawer_all), folders.sumOf { it.promptCount })) + folderTree

    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        drawerContainerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.drawer_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCloseClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_drawer),
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recursive List
            allFolders.forEach { folder ->
                DrawerFolderItem(
                    folder = folder,
                    selectedFolderId = selectedFolderId ?: "all",
                    onFolderClick = { 
                        onFolderClick(if (it == "all") null else it) 
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerFolderItem(
    folder: FolderUiModel,
    selectedFolderId: String,
    onFolderClick: (String) -> Unit,
    level: Int = 0
) {
    val isSelected = selectedFolderId == folder.id
    
    NavigationDrawerItem(
        label = { Text(folder.name) },
        selected = isSelected,
        onClick = { onFolderClick(folder.id) },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) ChipSelectedText else TextGrey
            )
        },
        badge = {
            Text(
                text = folder.count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = TextGrey
            )
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .padding(start = (level * 16).dp), // Indentation
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFEFF6FF),
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = Color(0xFF0A0A0A),
            unselectedTextColor = Color(0xFF0A0A0A)
        ),
        shape = RoundedCornerShape(10.dp)
    )

    // Render Children
    if (folder.children.isNotEmpty()) {
        folder.children.forEach { child ->
            DrawerFolderItem(
                folder = child,
                selectedFolderId = selectedFolderId,
                onFolderClick = onFolderClick,
                level = level + 1
            )
        }
    }
}
