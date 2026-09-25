package pab.rpg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pab.rpg.android.network.ApiClient
import pab.rpg.android.ui.login.AuthScreen
import pab.rpg.android.ui.login.LoginScreen
import pab.rpg.android.ui.login.RegisterScreen
import pab.rpg.android.ui.sessions.SessionListScreen
import pab.rpg.android.ui.theme.ProjectGmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var loggedIn by remember { mutableStateOf(ApiClient.authSessionManager.isLoggedIn()) }
            var authScreen by remember { mutableStateOf<AuthScreen>(AuthScreen.Login) }

            ProjectGmTheme {
                when {
                    loggedIn -> SessionListScreen()
                    authScreen is AuthScreen.Register -> RegisterScreen(
                        onRegistered = { authScreen = AuthScreen.Login },
                        onBackToLogin = { authScreen = AuthScreen.Login }
                    )
                    else -> LoginScreen(
                        onLoginSuccess = { loggedIn = true },
                        onNavigateToRegister = { authScreen = AuthScreen.Register }
                    )
                }
            }
        }
    }
}

