package com.promptmanager.app.feature.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import com.promptmanager.app.R
import com.promptmanager.app.core.designsystem.components.PMButton
import com.promptmanager.app.core.designsystem.components.PMTextField
import com.promptmanager.app.feature.auth.AuthUiState
import com.promptmanager.app.feature.auth.AuthViewModel
import com.promptmanager.app.core.designsystem.theme.ButtonDark
import com.promptmanager.app.core.designsystem.theme.GradientEnd
import com.promptmanager.app.core.designsystem.theme.GradientStart
import com.promptmanager.app.core.designsystem.theme.PromptManagerTheme

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterClick: (String, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is AuthUiState.Success) {
            onRegisterClick(email, password)
            viewModel.resetRegisterState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White,
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PMTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "用户名" // TODO: Add to strings.xml
                )

                PMTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.email)
                )

                PMTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.password),
                    visualTransformation = PasswordVisualTransformation()
                )

                PMTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(R.string.confirm_password),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PMButton(
                    onClick = { 
                        if (password == confirmPassword) {
                            error = null
                            viewModel.register(username, email, password, username) // Use username as display name for now
                        } else {
                            error = "Passwords do not match"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = ButtonDark,
                    contentColor = Color.White,
                    enabled = registerState !is AuthUiState.Loading
                ) {
                    if (registerState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(text = stringResource(R.string.register_button))
                    }
                }
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (registerState is AuthUiState.Error) {
                    Text(
                        text = (registerState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    PromptManagerTheme {
        RegisterScreen()
    }
}
