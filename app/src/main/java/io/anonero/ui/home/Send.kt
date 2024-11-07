package io.anonero.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SendScreen(modifier: Modifier = Modifier) {
    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            Text(text = "Send", modifier = Modifier.align(Alignment.Center))
        }
    }
}