package com.medislot.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotCard
import com.medislot.app.ui.components.MediSlotTextField
import com.medislot.app.ui.components.MediSlotTextButton
import com.medislot.app.ui.theme.LocalDimens
import com.medislot.app.utils.ValidationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    role: String,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToSuperAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    var logoClickCount by remember { mutableStateOf(0) }
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val roleTitle = when (role) {
        "patient" -> "Patient Portal"
        "doctor" -> "Doctor Portal"
        "hospital" -> "Hospital Coordinator"
        "super_admin" -> "Super Admin Console"
        else -> "MediSlot Portal"
    }

    val roleIcon = when (role) {
        "patient" -> Icons.Default.Person
        "doctor" -> Icons.Default.MedicalServices
        "hospital" -> Icons.Default.LocalHospital
        "super_admin" -> Icons.Default.Settings
        else -> Icons.Default.MonitorHeart
    }

    val themeColor = when (role) {
        "patient" -> MaterialTheme.colorScheme.primary
        "doctor" -> MaterialTheme.colorScheme.secondary
        "hospital" -> Color(0xFFF59E0B) // Amber
        "super_admin" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    fun handleLogin() {
        // Reset errors
        usernameError = null
        passwordError = null
        
        var isValid = true

        // Validate username/email
        if (usernameOrEmail.isBlank()) {
            usernameError = "Username or Email is required"
            isValid = false
        } else if (usernameOrEmail.contains("@")) {
            val emailErr = ValidationUtils.validateEmail(usernameOrEmail)
            if (emailErr != null) {
                usernameError = emailErr
                isValid = false
            }
        }

        // Validate password
        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        }

        if (isValid) {
            coroutineScope.launch {
                isLoading = true
                val repo = com.medislot.app.data.repository.AuthenticationRepositoryImpl()
                val result = repo.login(usernameOrEmail.trim(), password)
                isLoading = false
                result.fold(
                    onSuccess = { response ->
                        onLoginSuccess(response.email)
                    },
                    onFailure = { err ->
                        val errorMessage = com.medislot.app.utils.NetworkErrorUtils.getReadableErrorMessage(err)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(LocalDimens.current.paddingLarge)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand / Role Header Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = roleIcon,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = roleTitle,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign in to access your dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form container card
            MediSlotCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    MediSlotTextField(
                        value = usernameOrEmail,
                        onValueChange = { 
                            usernameOrEmail = it
                            usernameError = null
                        },
                        label = "Username or Email",
                        leadingIcon = Icons.Default.Person,
                        errorMessage = usernameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MediSlotTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null
                        },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPasswordField = true,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        MediSlotTextButton(
                            text = "Forgot Password?",
                            onClick = onNavigateToForgotPassword
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MediSlotButton(
                text = "Login",
                onClick = { handleLogin() },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                MediSlotTextButton(
                    text = "Create Account",
                    onClick = onNavigateToRegister
                )
            }
        }
    }
}
