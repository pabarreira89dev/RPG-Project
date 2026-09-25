package pab.rpg.android.ui.login

// Which unauthenticated screen to show; SessionListScreen takes over entirely once logged in.
sealed interface AuthScreen {
    data object Login : AuthScreen
    data object Register : AuthScreen
}
