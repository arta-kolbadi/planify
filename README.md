# PLANIFY — Bilingual Study & Productivity Platform (FA / EN)

PLANIFY is a native Android application built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and OpenRouter AI integration.

## Features
- **Full Bilingual Support (FA / EN)**: Instant live switching between Persian (`fa-IR`, RTL layout, `Vazirmatn` font, Jalali/Solar Hijri calendar, Persian numerals) and English (`en-US`, LTR layout, `Inter` font, Gregorian calendar).
- **8-Theme Design System**: Light (`#168BFF`), Dark, AMOLED Black, Ocean Blue, Cozy Green, Sakura Pink, Sunset Orange, and a Custom Theme studio, selectable during onboarding and in Settings.
- **Dashboard & 2D Mascot**: Real-time summary of tasks, focus minutes, streak, XP/Level, weekly progress chart, and an interactive animated 2D companion ("Plani") with an unlockable wardrobe.
- **Planner**: Daily, Weekly, and Monthly Jalali/Gregorian views with task CRUD, subtasks, priorities, categories, recurrence, reminders, reordering, and one-tap focus launch.
- **Focus Timer & Android App Blocker**: 8 modes (Pomodoro, Custom, Stopwatch, Count-Up, Deep Focus, Study Session, Short/Long Break), procedural ambient soundscapes, process-close session recovery, and native `UsageStatsManager` distraction detection with the PLANIFY Focus Shield.
- **OpenRouter AI Assistant**: 5 specialized personas (General, Study, Planner, Focus, Note) with SSE streaming, Markdown/code blocks, Photo Picker image attachments, conversation history, per-assistant models, encrypted on-device API key storage (Android Keystore AES-GCM), and confirmation cards before modifying planner tasks.
- **Academy**: Grades, Subjects, Chapters, Lessons, step-by-step explanations, progressive-hint exercises, interactive quizzes, spaced-repetition flashcards, weak-topic diagnostics, and custom study material uploads.
- **Notes & Daily Journal**: Markdown editor with formatting toolbar, live preview, tags, pin/favorite filters, Photo Picker attachments, and auto-save.
- **Study Room & Analytics & Gamification**: Solo & local Group Study Rooms, real-data focus/task/consistency charts, XP/Levels/Coins, daily/weekly challenges, badges, and JSON backup Export/Import.

## Configuration
- **OpenRouter API Key**: Open **Settings → OpenRouter AI Configuration** inside the app to enter your personal API key, Base URL (`https://openrouter.ai/api/v1/`), and preferred models. Keys are encrypted locally via Android Keystore (`AES/GCM/NoPadding`).
- **Usage Access (Focus Shield)**: Grant **Usage Access** in Android Settings when prompted in the Focus tab to enable distraction detection during active focus sessions.
