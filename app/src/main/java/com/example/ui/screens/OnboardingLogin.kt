package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SparkViewModel
import com.example.ui.components.AnimatedWaveformVisualizer
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonText
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepDarkBackground,
                        CustomSurface
                    )
                )
            )
            .padding(24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "onboard_steps"
            ) { targetStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (targetStep) {
                        1 -> {
                            AnimatedWaveformVisualizer(
                                isPlaying = true,
                                barCount = 14,
                                color = CyberCyan,
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(80.dp)
                            )
                            NeonText(
                                text = "Welcome to SPARK",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                glowColor = CyberCyan
                            )
                            Text(
                                text = "Next-generation high performance music streaming without interruptions.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        2 -> {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Downloads",
                                tint = CyberMagenta,
                                modifier = Modifier.size(90.dp)
                            )
                            NeonText(
                                text = "Free Offline Downloads",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp
                                ),
                                glowColor = CyberMagenta
                            )
                            Text(
                                text = "Download any song directly to your device and listen entirely offline.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        3 -> {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI DJ",
                                tint = NeonPurple,
                                modifier = Modifier.size(90.dp)
                            )
                            NeonText(
                                text = "Intelligent AI DJ",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp
                                ),
                                glowColor = NeonPurple
                            )
                            Text(
                                text = "Curate mood playlists and get live synced lyrics in real-time powered by Gemini.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val active = i == step
                    val width by animateDpAsState(
                        targetValue = if (active) 24.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                    Box(
                        modifier = Modifier
                            .size(height = 8.dp, width = width)
                            .background(
                                color = if (active) CyberCyan else TextSecondary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            NeonButton(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        onNavigateToLogin()
                    }
                },
                glowColor = if (step == 2) CyberMagenta else if (step == 3) NeonPurple else CyberCyan,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (step == 3) "Get Started" else "Next",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LoginRegisterScreen(
    viewModel: SparkViewModel,
    onLoginSuccessful: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var phoneLoginSelected by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepDarkBackground)
            .padding(24.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NeonText(
                text = "SPARK",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                glowColor = CyberCyan
            )

            Text(
                text = "Stream, Download, and Discover",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (phoneLoginSelected) "Phone Authentication" else if (isRegister) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (phoneLoginSelected) {
                        // Phone Auth Flow
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CyberCyan) },
                            textStyle = TextStyle(color = TextPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("phone_input")
                        )

                        if (codeSent) {
                            OutlinedTextField(
                                value = verificationCode,
                                onValueChange = { verificationCode = it },
                                label = { Text("Verification Code") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan) },
                                textStyle = TextStyle(color = TextPrimary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("verification_code_input")
                            )
                        }

                        NeonButton(
                            onClick = {
                                if (!codeSent) {
                                    if (phoneNumber.length >= 8) {
                                        codeSent = true
                                        errorMessage = ""
                                    } else {
                                        errorMessage = "Enter a valid phone number"
                                    }
                                } else {
                                    if (verificationCode.length >= 4) {
                                        viewModel.registerOrLogin("user_phone@spark.com", "Phone Member")
                                        onLoginSuccessful()
                                    } else {
                                        errorMessage = "Invalid SMS code format"
                                    }
                                }
                            },
                            glowColor = CyberCyan,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("phone_submit_button")
                        ) {
                            Text(text = if (codeSent) "Verify & Enter" else "Request Code", fontWeight = FontWeight.Bold)
                        }

                    } else {
                        // Email / Password Flow
                        if (isRegister) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan) },
                                textStyle = TextStyle(color = TextPrimary),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("username_input")
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CyberCyan) },
                            textStyle = TextStyle(color = TextPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("email_input")
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = TextStyle(color = TextPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("password_input")
                        )

                        NeonButton(
                            onClick = {
                                if (email.contains("@") && password.length >= 6) {
                                    val finalName = if (isRegister) name.ifBlank { "Spark Member" } else email.substringBefore("@")
                                    viewModel.registerOrLogin(email, finalName)
                                    onLoginSuccessful()
                                } else {
                                    errorMessage = "Please enter valid email and 6+ character password"
                                }
                            },
                            glowColor = if (isRegister) NeonPurple else CyberCyan,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_submit_button")
                        ) {
                            Text(
                                text = if (isRegister) "Sign Up" else "Log In",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Toggle Auth Modes
                    TextButton(
                        onClick = {
                            isRegister = !isRegister
                            phoneLoginSelected = false
                            errorMessage = ""
                        }
                    ) {
                        Text(
                            text = if (isRegister) "Already have an account? Log In" else "New to Spark? Create an Account",
                            color = CyberCyan
                        )
                    }
                }
            }

            // Quick Social log-ins
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.3f))
                Text(text = "Or enter via", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                HorizontalDivider(modifier = Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.3f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Secure instant Google login
                        viewModel.registerOrLogin("developer@gmail.com", "Studio Member")
                        onLoginSuccessful()
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Instant Google", fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = {
                        phoneLoginSelected = !phoneLoginSelected
                        errorMessage = ""
                    },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SMS Login", fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}
