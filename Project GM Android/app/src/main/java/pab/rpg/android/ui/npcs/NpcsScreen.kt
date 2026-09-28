package pab.rpg.android.ui.npcs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pab.rpg.android.network.dto.NpcResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcsScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: NpcsViewModel = viewModel(
        factory = viewModelFactory { initializer { NpcsViewModel(sessionId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NPCs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading && uiState.npcs.isEmpty() && uiState.errorMessage == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                        Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::retry) { Text("Reintentar") }
                    }
                }
                uiState.npcs.isEmpty() -> {
                    Text(
                        text = "No hay NPCs en esta localización.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.npcs, key = { it.id }) { npc ->
                            NpcRow(npc)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NpcRow(npc: NpcResponse) {
    val isDead = npc.status == "DEAD"
    val nameColor = if (isDead) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val (relationshipLabel, relationshipColor) = relationshipLabelAndColor(npc.relationship)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isDead) "${npc.name} (fallecido)" else npc.name,
                color = nameColor,
                style = MaterialTheme.typography.bodyLarge
            )
            if (!npc.faction.isNullOrBlank()) {
                Text(
                    text = npc.faction,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatRelationshipValue(npc.relationship),
                color = relationshipColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(text = relationshipLabel, color = relationshipColor, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatRelationshipValue(value: Int): String = if (value > 0) "+$value" else value.toString()

// Client-side heuristic only: the backend does not document/bound the range of Relationship.value.
private fun relationshipLabelAndColor(value: Int): Pair<String, Color> = when {
    value >= 3 -> "Amistoso" to Color(0xFF2E7D32)
    value >= 1 -> "Cordial" to Color(0xFF2E7D32)
    value == 0 -> "Neutral" to Color(0xFF757575)
    value >= -2 -> "Receloso" to Color(0xFFEF6C00)
    else -> "Hostil" to Color(0xFFC62828)
}
