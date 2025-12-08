package hu.bme.ait.sean.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.bme.ait.sean.ui.theme.Background1
import hu.bme.ait.sean.ui.theme.Background2
import hu.bme.ait.sean.ui.theme.Primary
import hu.bme.ait.sean.ui.theme.TextColor
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit //go to user profile page
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("sean@gimble.com") }
    var password by rememberSaveable { mutableStateOf("password") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background1),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.size(100.dp))
        Text(
            //
            "SEAN",
            color = Primary,
            fontSize = 100.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .padding(top = 100.dp),
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            label = {
                Text("Email")
            },
            value = email,
            onValueChange = {
                email = it
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Background2,
                unfocusedContainerColor = Background1
            )
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            label = {
                Text("Password")
            },
            value = password,
            onValueChange = {
                password = it
            },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Background2,
                unfocusedContainerColor = Background1
            ),
            trailingIcon = {
                IconButton(onClick = {showPassword = !showPassword}) {
                    if (showPassword) {
                        Icon(Icons.Outlined.VisibilityOff, contentDescription = "")
                    }
                    else {
                        Icon(Icons.Outlined.Visibility, contentDescription = "")
                    }
                }
            }
        )
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val result = viewModel.loginUser(email, password)
                        if (result?.user != null) {
                            onLoginSuccess()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextColor
                )
            ) {
                Text("Login", color = Color.White)
            }
            Spacer(modifier = Modifier.size(5.dp))
            OutlinedButton(
                onClick = {
                    viewModel.registerUser(email, password)
                },

                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextColor
                )
            ) {
                Text("Register", color = Color.White)
            }
        }
        Spacer(Modifier.size(30.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (viewModel.loginUiState) {
                is LoginUiState.Error -> {
                    Text("Error: {${(viewModel.loginUiState as LoginUiState.Error).errorMessage}}")
                }
                LoginUiState.Init -> {}
                LoginUiState.Loading -> CircularProgressIndicator()
                LoginUiState.LoginSuccess -> Text("Success!")
                LoginUiState.RegisterSuccess -> Text("User Registered!")
            }
        }
    }
}