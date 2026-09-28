package pab.rpg.android.ui.combat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pab.rpg.android.network.dto.CombatParticipantResponse
import pab.rpg.android.network.dto.CombatResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: CombatViewModel = viewModel(
        factory = viewModelFactory { initializer { CombatViewModel(sessionId) } }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeActionError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Combate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val combat = uiState.combat
            when {
                uiState.isLoading && combat == null && !uiState.noActiveCombat && uiState.errorMessage == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                        Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::retry) { Text("Reintentar") }
                    }
                }
                uiState.noActiveCombat || combat == null -> {
                    Text(
                        text = "No hay ningún combate activo. Ataca a un NPC desde la pantalla de NPCs para empezar.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                else -> {
                    CombatContent(
                        combat = combat,
                        selectedTargetId = uiState.selectedTargetId,
                        freeText = uiState.freeText,
                        isSubmittingAttack = uiState.isSubmittingAttack,
                        isEnemyTurnProcessing = uiState.isEnemyTurnProcessing,
                        onSelectTarget = viewModel::selectTarget,
                        onFreeTextChange = viewModel::onFreeTextChange,
                        onAttackSelected = viewModel::attackSelectedTarget,
                        onAttackWithText = viewModel::attackWithFreeText,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun CombatContent(
    combat: CombatResponse,
    selectedTargetId: String?,
    freeText: String,
    isSubmittingAttack: Boolean,
    isEnemyTurnProcessing: Boolean,
    onSelectTarget: (String) -> Unit,
    onFreeTextChange: (String) -> Unit,
    onAttackSelected: () -> Unit,
    onAttackWithText: () -> Unit,
    onBack: () -> Unit
) {
    val isCompleted = combat.status == "COMPLETED"
    val currentParticipant = combat.participants.firstOrNull { it.id == combat.currentParticipantId }
    val isPlayerTurn = !isCompleted && currentParticipant?.team == "PLAYER"
    val busy = isSubmittingAttack || isEnemyTurnProcessing

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Ronda ${combat.roundNumber}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(combat.participants.sortedBy { it.turnOrder }, key = { it.id }) { participant ->
                ParticipantCard(
                    participant = participant,
                    isCurrentTurn = participant.id == combat.currentParticipantId,
                    isSelected = participant.id == selectedTargetId,
                    isSelectable = !isCompleted && isPlayerTurn && !busy &&
                        participant.team == "ENEMY" && participant.status == "ACTIVE",
                    onClick = { onSelectTarget(participant.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            combat.narration?.let { narration ->
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(narration, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        when {
            isCompleted -> {
                val victory = combat.participants.filter { it.team == "ENEMY" }.all { it.status != "ACTIVE" }
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = if (victory) "Victoria" else "Derrota",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (victory) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onBack) { Text("Volver") }
                }
            }
            isEnemyTurnProcessing -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("El enemigo está atacando…")
                }
            }
            isPlayerTurn -> {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = onAttackSelected,
                        enabled = selectedTargetId != null && !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Atacar objetivo seleccionado") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = freeText,
                            onValueChange = onFreeTextChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("¿Qué haces?") },
                            enabled = !busy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isSubmittingAttack) {
                            CircularProgressIndicator(modifier = Modifier.width(32.dp).height(32.dp))
                        } else {
                            Button(onClick = onAttackWithText, enabled = freeText.isNotBlank() && !busy) { Text("Enviar") }
                        }
                    }
                }
            }
            else -> {
                Text("Esperando turno…", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ParticipantCard(
    participant: CombatParticipantResponse,
    isCurrentTurn: Boolean,
    isSelected: Boolean,
    isSelectable: Boolean,
    onClick: () -> Unit
) {
    val isDown = participant.status != "ACTIVE"
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isCurrentTurn -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = if (isSelected || isCurrentTurn) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isSelectable, onClick = onClick)
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isDown) "${participant.name} (${statusLabel(participant.status)})" else participant.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDown) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (participant.team == "PLAYER") "Tú" else "Enemigo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (participant.healthMaximum > 0) participant.healthCurrent.coerceAtLeast(0) / participant.healthMaximum.toFloat() else 0f },
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
        Text(
            text = "Vida: ${participant.healthCurrent}/${participant.healthMaximum}",
            style = MaterialTheme.typography.bodySmall
        )
        if (isCurrentTurn) {
            Text(
                text = "Turno actual",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun statusLabel(status: String): String = when (status) {
    "DOWNED" -> "derribado"
    "DEAD" -> "muerto"
    else -> status
}
