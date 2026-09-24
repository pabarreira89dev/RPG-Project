package pab.rpg.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pab.rpg.android.ui.sessions.SessionListScreen
import pab.rpg.android.ui.theme.ProjectGmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectGmTheme {
                SessionListScreen()
            }
        }
    }
}
