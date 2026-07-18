package com.medislot.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.medislot.app.ui.components.MediSlotButton
import com.medislot.app.ui.components.MediSlotTextField
import com.medislot.app.ui.components.MediSlotTextButton
import com.medislot.app.ui.theme.LocalDimens

@Composable
fun ForgotPasswordScreen(
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(LocalDimens.current.spaceSmall))
            
            Text(
                text = "Enter your email to receive recovery instructions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(LocalDimens.current.spaceLarge))

            MediSlotTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(LocalDimens.current.spaceLarge))

            MediSlotButton(
                text = "Send Instructions",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(LocalDimens.current.spaceLarge))

            MediSlotTextButton(
                text = "Back to Sign In",
                onClick = onNavigateBack
            )
        }
    }
}
