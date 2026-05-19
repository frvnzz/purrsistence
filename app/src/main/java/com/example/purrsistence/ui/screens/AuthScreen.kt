package com.example.purrsistence.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Text(
                text = if (isLoginMode) {
                    "Login"
                } else {
                    "Create Account"
                },
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Text(
                text = "and see what your Friends are doing...",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TODO: Complete validation input (no spaces possible, password confirmation etc...)
                Button(
                    onClick = {
                        if (isLoginMode) {
                            if (email.isBlank() || password.isBlank()) return@Button
                            userViewModel.signInWithSupabase(
                                email = email,
                                password = password
                            )
                        } else {
                            // validate user input
                            if (
                                username.isBlank() ||
                                email.isBlank() ||
                                password.isBlank()
                            ) {
                                return@Button
                            }
                            // sign up
                            userViewModel.signUpWithSupabase(
                                email = email,
                                password = password,
                                username = username
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isLoginMode) {
                            "Login"
                        } else {
                            "Create Account"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        userViewModel.clearSupabaseError()
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

                if (isLoading) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    CircularProgressIndicator()
                }

                // ERROR TEXT
                // Todo: Maybe replace with snackbar alert?
                Spacer(modifier = Modifier.height(Spacing.lg))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    minLines = 2
                )
            }
        }
    }
}