package io.anonero.ui.home

import AnonNeroTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.anonero.icons.AnonIcons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenComposable(modifier: Modifier = Modifier) {

    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        ),
        selectedTextColor = MaterialTheme.colorScheme.primary,
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Anon") },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                NavigationBarItem(
                    selected = true,
                    colors = navigationItemColors,
                    onClick = { /*TODO*/ },
                    icon = { Icon(AnonIcons.Home, contentDescription = "Home") },
                )
                NavigationBarItem(
                    selected = false,
                    colors = navigationItemColors,
                    onClick = { /*TODO*/ },
                    icon = { Icon(AnonIcons.QrCode, contentDescription = "Home") },
                )
                NavigationBarItem(
                    selected = false,
                    colors = navigationItemColors,
                    onClick = { /*TODO*/ },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Home") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /*TODO*/ },
                    colors = navigationItemColors,
                    icon = { Icon(Icons.TwoTone.Settings, contentDescription = "Home") },
                )
            }
        }
    ) {
        Box(modifier = modifier.padding(it)) {

        }
    }
}


@Preview(device = "id:pixel_8")
@Composable
private fun HomeScreenComposablePrev() {
    AnonNeroTheme {
        HomeScreenComposable()
    }
}