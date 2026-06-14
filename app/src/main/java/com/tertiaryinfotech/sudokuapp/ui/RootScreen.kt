package com.tertiaryinfotech.sudokuapp.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.tertiaryinfotech.sudokuapp.model.AppScreen
import com.tertiaryinfotech.sudokuapp.viewmodel.GameViewModel

/**
 * Top-level router. Shows the 18+ age gate until confirmed, then switches between
 * the home, game, and completion screens.
 */
@Composable
fun RootScreen(vm: GameViewModel) {
    val alert = vm.activeAlert
    if (alert != null) {
        AlertDialog(
            onDismissRequest = { vm.activeAlert = null },
            confirmButton = {
                TextButton(onClick = { vm.activeAlert = null }) { Text("OK") }
            },
            title = { Text(alert.title) },
            text = { Text(alert.message) }
        )
    }

    AnimatedContent(
        targetState = if (!vm.isAgeConfirmed) null else vm.screen,
        transitionSpec = {
            (fadeIn(tween(250)) togetherWith fadeOut(tween(250)))
        },
        label = "rootScreen"
    ) { target ->
        when (target) {
            null -> AgeGateScreen(vm)
            AppScreen.HOME -> HomeScreen(vm)
            AppScreen.GAME -> GameScreen(vm)
            AppScreen.COMPLETION -> CompletionScreen(vm)
        }
    }
}
