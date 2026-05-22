package com.example.purrsistence.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.state.TopBarState
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.viewmodel.UserViewModel

@Composable
fun AuthScreen(
    userViewModel: UserViewModel,
    setTopBar: (TopBarState) -> Unit,
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        setTopBar(
            TopBarState(
                title = "Account",
                onBackClick = onBack
            )
        )
    }

    val isLoading by userViewModel.isSupabaseLoading.collectAsState()
    val error by userViewModel.supabaseError.collectAsState()

    // check the supabase remote user state (signed in or out)
    val isSignedIn by userViewModel
        .isSupabaseSignedIn
        .collectAsState()

    // check if account creation was a success and user can safely log in
    val signUpSuccess by userViewModel
        .signUpSuccess
        .collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateInputs(): Boolean {
        var isValid = true

        if (!isLoginMode && username.isBlank()) {
            usernameError = "Username cannot be empty"
            isValid = false
        } else {
            usernameError = null
        }

        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Invalid email format"
            isValid = false
        } else {
            emailError = null
        }

        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }

    // Collect Auth states as LaunchedEffect
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            onAuthSuccess()
        }
    }
    // Redirect to log in if signup was a success and reset fields
    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            isLoginMode = true
            username = ""
            password = ""
            userViewModel.resetSignUpSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoginMode) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (isLoginMode) "Login to see your friends" else "Join the community",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(Spacing.lg)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                usernameError = null
                            },
                            label = { Text("Username") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = usernameError != null,
                            supportingText = {
                                if (usernameError != null) {
                                    Text(text = usernameError!!)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        supportingText = {
                            if (emailError != null) {
                                Text(text = emailError!!)
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(text = passwordError!!)
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    Button(
                        onClick = {
                            if (!validateInputs()) return@Button

                            if (isLoginMode) {
                                userViewModel.signInWithSupabase(
                                    email = email,
                                    password = password
                                )
                            } else {
                                userViewModel.signUpWithSupabase(
                                    email = email,
                                    password = password,
                                    username = username
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isLoginMode) "Login" else "Register")
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    TextButton(
                        onClick = {
                            isLoginMode = !isLoginMode
                            userViewModel.clearSupabaseError()
                            usernameError = null
                            emailError = null
                            passwordError = null
                        }
                    ) {
                        Text(
                            if (isLoginMode) {
                                "Don't have an account? Register"
                            } else {
                                "Already have an account? Login"
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(Spacing.lg))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
            }
        }
    }
}