package com.promptmanager.app.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import com.promptmanager.app.R
import com.promptmanager.app.core.designsystem.components.PMButton
import com.promptmanager.app.feature.auth.AuthUiState
import com.promptmanager.app.feature.auth.AuthViewModel
import com.promptmanager.app.core.designsystem.components.PMTextField
import com.promptmanager.app.core.designsystem.theme.ButtonDark
import com.promptmanager.app.core.designsystem.theme.GoogleBtnText
import com.promptmanager.app.core.designsystem.theme.GradientEnd
import com.promptmanager.app.core.designsystem.theme.GradientStart
import com.promptmanager.app.core.designsystem.theme.GuestBtnText
import com.promptmanager.app.core.designsystem.theme.PromptManagerTheme
import com.promptmanager.app.core.designsystem.theme.SurfaceLightGrey
import com.promptmanager.app.core.designsystem.theme.TextLightBlue

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginClick: (String, String) -> Unit = { _, _ -> }, // Kept for API compatibility but logic moved to VM
    onRegisterClick: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {},
    onWeChatLoginClick: () -> Unit = {},
    onGuestLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is AuthUiState.Success) {
            onLoginClick(email, password) // Notify navigation
            viewModel.resetLoginState()
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flexible spacer to push content down
            Spacer(modifier = Modifier.weight(1f))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo_app),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = TextLightBlue
            )

            Spacer(modifier = Modifier.weight(1f))

            // Login Buttons Card
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
                // Email Login Form
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

                PMButton(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = ButtonDark,
                    contentColor = Color.White,
                    enabled = loginState !is AuthUiState.Loading
                ) {
                    if (loginState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(text = stringResource(R.string.login))
                    }
                }
                
                if (loginState is AuthUiState.Error) {
                     Text(
                        text = (loginState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Register Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.register_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    TextButton(onClick = onRegisterClick) {
                        Text(
                            text = stringResource(R.string.register_link),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.or_continue_with),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Divider(modifier = Modifier.weight(1f))
                }

                // Google Login
                PMButton(
                    onClick = onGoogleLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = GoogleBtnText,
                    borderColor = Color(0xFFE0E0E0) // Light grey border
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.login_google))
                }

                // WeChat Login
                PMButton(
                    onClick = onWeChatLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.White,
                    contentColor = GoogleBtnText,
                    borderColor = Color(0xFFE0E0E0)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wechat),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.login_wechat))
                }

                // Guest Login
                PMButton(
                    onClick = onGuestLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SurfaceLightGrey,
                    contentColor = GuestBtnText
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_guest),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.login_guest))
                }

                // Guest info text
                Text(
                    text = stringResource(R.string.login_guest_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = stringResource(R.string.login_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = TextLightBlue,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    PromptManagerTheme {
        LoginScreen()
    }
}
