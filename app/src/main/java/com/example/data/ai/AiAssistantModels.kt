package com.example.data.ai

import com.example.core.i18n.AppStrings

enum class AssistantPersona(val id: String) {
    GENERAL("GENERAL"),
    STUDY("STUDY"),
    PLANNER("PLANNER"),
    FOCUS("FOCUS"),
    NOTE("NOTE");

    fun label(strings: AppStrings): String = when (this) {
        GENERAL -> strings.aiAssistantGeneral
        STUDY -> strings.aiAssistantStudy
        PLANNER -> strings.aiAssistantPlanner
        FOCUS -> strings.aiAssistantFocus
        NOTE -> strings.aiAssistantNote
    }

    fun defaultSystemPrompt(): String = when (this) {
        GENERAL -> "You are PLANIFY General Assistant, an intelligent, encouraging productivity and study coach. Reply in the user's language (Persian or English) with clear structure."
        STUDY -> "You are PLANIFY Study AI. Explain concepts step by step, create practice quizzes, and generate concise flashcards. Reply in the user's language."
        PLANNER -> "You are PLANIFY Planner AI. Help design realistic daily schedules and break goals into actionable tasks. Whenever you propose concrete tasks for the user's planner, include one line per task formatted strictly as [[PLANIFY_TASK|Task Title|DurationMinutes|HIGH/MEDIUM/LOW|Study]] so the app can ask the user for confirmation before adding it."
        FOCUS -> "You are PLANIFY Focus AI. Analyze focus habits, suggest Pomodoro/deep-work rhythms, and provide calm strategies to overcome procrastination."
        NOTE -> "You are PLANIFY Note AI. Summarize study notes cleanly into bullet points, extract key terms, and organize Markdown outlines."
    }
}

data class ProposedTask(
    val title: String,
    val estimatedMinutes: Int,
    val priority: String,
    val categoryName: String
)

object TaskProposalParser {
    private val regex = Regex("""\[\[PLANIFY_TASK\|([^|\n]+)\|(\d+)\|(HIGH|MEDIUM|LOW)\|([^\]\n]+)\]\]""")

    fun extractProposals(markdown: String): List<ProposedTask> =
        regex.findAll(markdown).map { match ->
            ProposedTask(
                title = match.groupValues[1].trim(),
                estimatedMinutes = match.groupValues[2].toIntOrNull()?.coerceIn(5, 480) ?: 45,
                priority = match.groupValues[3],
                categoryName = match.groupValues[4].trim()
            )
        }.toList()

    fun stripTags(markdown: String): String =
        regex.replace(markdown) { match ->
            "• **${match.groupValues[1].trim()}** (${match.groupValues[2]}m, ${match.groupValues[3]})"
        }.trim()
}

val SuggestedOpenRouterModels = listOf(
    "openai/gpt-4o-mini",
    "anthropic/claude-3.5-sonnet",
    "google/gemini-2.5-flash",
    "meta-llama/llama-3.3-70b-instruct",
    "deepseek/deepseek-chat",
    "qwen/qwen-2.5-72b-instruct"
)
