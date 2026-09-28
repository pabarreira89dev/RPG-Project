package pab.rpg.android.ui.quests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: QuestsViewModel = viewModel(
        factory = viewModelFactory { initializer { QuestsViewModel(sessionId) } }
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
                title = { Text("Misiones") },
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
            when {
                uiState.isLoading && uiState.activeAndAvailable.isEmpty() && uiState.completed.isEmpty() &&
                    uiState.errorMessage == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(24.dp)) {
                        Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = viewModel::retry) { Text("Reintentar") }
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.activeAndAvailable, key = { it.code }) { quest ->
                            val freeText = uiState.freeTextByCode[quest.code].orEmpty()
                            QuestCard(
                                quest = quest,
                                isBusy = uiState.busyQuestCode == quest.code,
                                freeText = freeText,
                                onStart = { viewModel.startQuest(quest.code) },
                                onChoice = { choiceKey -> viewModel.advanceWithChoice(quest.code, choiceKey) },
                                onFreeTextChange = { text -> viewModel.onFreeTextChange(quest.code, text) },
                                onSubmitFreeText = { viewModel.advanceWithText(quest.code, freeText) }
                            )
                            HorizontalDivider()
                        }
                        item {
                            CompletedHeader(
                                count = uiState.completed.size,
                                expanded = uiState.completedExpanded,
                                onToggle = viewModel::toggleCompletedExpanded
                            )
                        }
                        if (uiState.completedExpanded) {
                            items(uiState.completed, key = { it.code }) { quest ->
                                CompletedQuestRow(quest)
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedHeader(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Misiones completadas ($count)", style = MaterialTheme.typography.titleSmall)
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Contraer" else "Expandir"
        )
    }
}

@Composable
private fun QuestCard(
    quest: QuestItemUi,
    isBusy: Boolean,
    freeText: String,
    onStart: () -> Unit,
    onChoice: (String) -> Unit,
    onFreeTextChange: (String) -> Unit,
    onSubmitFreeText: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(quest.title, style = MaterialTheme.typography.bodyLarge)

        when (quest.status) {
            QuestStatusUi.NOT_STARTED -> {
                Spacer(modifier = Modifier.height(8.dp))
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Button(onClick = onStart) { Text("Iniciar") }
                }
            }
            QuestStatusUi.ACTIVE -> {
                Text(
                    text = "En curso",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                quest.stageDescription?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quest.choices.forEach { choice ->
                        OutlinedButton(onClick = { onChoice(choice.choiceKey) }, enabled = !isBusy) {
                            Text(choice.label)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = freeText,
                        onValueChange = onFreeTextChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("¿Qué decides?") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.width(32.dp).height(32.dp))
                    } else {
                        Button(onClick = onSubmitFreeText, enabled = freeText.isNotBlank()) { Text("Enviar") }
                    }
                }
            }
            QuestStatusUi.COMPLETED -> Unit // shown only in the collapsed "completed" section below
        }
    }
}

@Composable
private fun CompletedQuestRow(quest: QuestItemUi) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "${quest.title} (Completada)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        quest.stageDescription?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
