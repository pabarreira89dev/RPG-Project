package pab.rpg.android.ui.quests

// Client-side only: the backend has no endpoint to list the quest catalog or per-stage transitions,
// so branch buttons for the 3 seeded quests (V5 migration) are hardcoded here (same precedent as
// NpcCatalog/LocationCatalog in "Project GM automatics").

data class QuestChoice(val choiceKey: String, val label: String)

data class KnownQuest(
    val code: String,
    val title: String,
    val choicesByStage: Map<String, List<QuestChoice>>
)

object QuestCatalog {
    val ALL: List<KnownQuest> = listOf(
        KnownQuest(
            code = "aron_debt",
            title = "La deuda de Aron",
            choicesByStage = mapOf(
                "quest_started" to listOf(
                    QuestChoice("pay", "Pagar la deuda"),
                    QuestChoice("confront", "Enfrentar al prestamista")
                )
            )
        ),
        KnownQuest(
            code = "forest_threat",
            title = "La amenaza del bosque",
            choicesByStage = mapOf(
                "quest_started" to listOf(
                    QuestChoice("investigate", "Investigar"),
                    QuestChoice("ignore", "Ignorar")
                )
            )
        ),
        KnownQuest(
            code = "village_elder_history",
            title = "La memoria de la anciana",
            choicesByStage = mapOf(
                "quest_started" to listOf(
                    QuestChoice("listen", "Escuchar"),
                    QuestChoice("dismiss", "Descartar")
                )
            )
        )
    )
}
