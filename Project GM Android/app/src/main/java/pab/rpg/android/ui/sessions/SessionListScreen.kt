package pab.rpg.android.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pab.rpg.android.network.dto.SessionResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    onSessionClick: (String) -> Unit,
    viewModel: SessionListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateSessionDialog(
            onConfirm = { characterName ->
                viewModel.createSession(characterName.ifBlank { "Aventurero" })
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Project GM") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva partida")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.sessions.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    ) {
                        Text(uiState.errorMessage.orEmpty())
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = viewModel::loadSessions) { Text("Reintentar") }
                    }
                }
                uiState.sessions.isEmpty() -> {
                    Text(
                        text = "No hay partidas todavía. Crea una con el botón +.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isLoading,
                        onRefresh = viewModel::loadSessions,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.sessions) { session ->
                                SessionRow(session, onClick = { onSessionClick(session.sessionId) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateSessionDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var characterName by remember { mutableStateOf("Aventurero") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva partida") },
        text = {
            OutlinedTextField(
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Nombre del personaje") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { Button(onClick = { onConfirm(characterName.trim()) }) { Text("Crear") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SessionRow(session: SessionResponse, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(session.character.name) },
        supportingContent = { Text("Nivel ${session.character.level} — ${session.status}") },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
