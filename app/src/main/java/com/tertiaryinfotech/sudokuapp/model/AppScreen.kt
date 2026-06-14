package com.tertiaryinfotech.sudokuapp.model

/**
 * Drives top-level navigation. Like the rest of the app this stays lightweight, so
 * the root composable switches on this enum instead of using a NavHost.
 */
enum class AppScreen { HOME, GAME, COMPLETION }

/** A simple alert payload routed through the view model. */
data class GameAlert(
    val title: String,
    val message: String
)
