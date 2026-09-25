package pab.rpg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pab.rpg.android.network.ApiClient
import pab.rpg.android.ui.login.LoginScreen
import pab.rpg.android.ui.login.RegisterScreen
import pab.rpg.android.ui.narration.NarrationScreen
import pab.rpg.android.ui.sessions.SessionListScreen
import pab.rpg.android.ui.theme.ProjectGmTheme

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_SESSIONS = "sessions"
private const val ROUTE_NARRATION = "narration/{sessionId}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val startDestination = if (ApiClient.authSessionManager.isLoggedIn()) ROUTE_SESSIONS else ROUTE_LOGIN

            ProjectGmTheme {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable(ROUTE_LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(ROUTE_SESSIONS) {
                                    popUpTo(ROUTE_LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate(ROUTE_REGISTER) }
                        )
                    }
                    composable(ROUTE_REGISTER) {
                        RegisterScreen(
                            onRegistered = { navController.popBackStack() },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }
                    composable(ROUTE_SESSIONS) {
                        SessionListScreen(
                            onSessionClick = { sessionId -> navController.navigate("narration/$sessionId") }
                        )
                    }
                    composable(
                        ROUTE_NARRATION,
                        arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
                        NarrationScreen(sessionId = sessionId, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

