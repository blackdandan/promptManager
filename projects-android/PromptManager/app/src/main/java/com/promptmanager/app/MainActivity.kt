package com.promptmanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import com.promptmanager.app.core.designsystem.theme.PromptManagerTheme
import com.promptmanager.app.feature.auth.login.LoginScreen
import com.promptmanager.app.feature.auth.register.RegisterScreen
import com.promptmanager.app.feature.main.MainScreen
import com.promptmanager.app.feature.prompt.editor.PromptEditorScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PromptManagerTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            onLoginClick = { _, _ -> currentScreen = "main" },
                            onRegisterClick = { currentScreen = "register" },
                            onGuestLoginClick = { currentScreen = "main" }
                        )
                    }
                    "register" -> {
                        RegisterScreen(
                            onBackClick = { currentScreen = "login" },
                            onRegisterClick = { _, _ -> currentScreen = "main" }
                        )
                    }
                    "main" -> {
                        MainScreen(
                            onNavigateToCreate = { currentScreen = "editor" }
                        )
                    }
                    "editor" -> {
                        PromptEditorScreen(
                            onBackClick = { currentScreen = "main" },
                            onSaveClick = { _, _ -> currentScreen = "main" }
                        )
                    }
                }
            }
        }
    }
}
