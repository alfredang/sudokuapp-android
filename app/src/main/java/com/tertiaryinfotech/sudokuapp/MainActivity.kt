package com.tertiaryinfotech.sudokuapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tertiaryinfotech.sudokuapp.ui.RootScreen
import com.tertiaryinfotech.sudokuapp.ui.theme.SudokuTheme
import com.tertiaryinfotech.sudokuapp.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private var viewModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: GameViewModel = viewModel()
            viewModel = vm
            SudokuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScreen(vm)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-pause the timer when the app leaves the foreground.
        viewModel?.onEnterBackground()
    }
}
