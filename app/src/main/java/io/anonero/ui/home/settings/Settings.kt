package io.anonero.ui.home.settings

import AnonNeroTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.anonero.AnonConfig
import io.anonero.ui.home.graph.routes.ProxySettingsRoute
import io.anonero.ui.home.graph.routes.SecureWipeRoute
import io.anonero.ui.home.graph.routes.SettingsExportBackUp
import io.anonero.ui.home.graph.routes.SettingsLogs
import io.anonero.ui.home.graph.routes.SettingsNodeRoute
import io.anonero.ui.home.graph.routes.SettingsViewSeedRoute

typealias NavigateTo<T> = (to: T) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    onBackPress: () -> Unit = {},
    navigateTo: (param: Any) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text("")
                }
            )
        }
    ) {
        val settingsMenuHeaderStyle = MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.primary
        )

        Box(modifier = Modifier.padding(it)) {
            LazyColumn(
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            ) {
                item {
                    Text(
                        "Connection", style = settingsMenuHeaderStyle, modifier = Modifier.padding(
                            vertical = 12.dp,
                            horizontal = 8.dp
                        )
                    )
                }
                item {
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                    SettingsMenuItem(
                        title = "Node Settings",
                        onClick = {
                            navigateTo(SettingsNodeRoute)
                        })
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                }
                item {
                    SettingsMenuItem(title = "Proxy Settings", onClick = {
                        navigateTo(ProxySettingsRoute)
                    })
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                }
                item {
                    Text(
                        "Security", style = settingsMenuHeaderStyle, modifier = Modifier.padding(
                            vertical = 12.dp,
                            horizontal = 8.dp
                        )
                    )
                }
                if (!AnonConfig.viewOnly)
                    item {
                        SettingsMenuItem(title = "Seed ", onClick = {
                            navigateTo(SettingsViewSeedRoute)
                        })
                        HorizontalDivider(
                            thickness = 2.5.dp
                        )
                    }
                item {
                    SettingsMenuItem(title = "Change PIN")
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                }
                item {
                    SettingsMenuItem(title = "Export Backup", onClick = {
                        navigateTo(SettingsExportBackUp)
                    })
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                }
                item {
                    SettingsMenuItem(
                        title = "Secure Wipe",
                        onClick = {
                            navigateTo(SecureWipeRoute)
                        })
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                }
                item {
                    SettingsMenuItem(title = "Logs", onClick = {
                        navigateTo(SettingsLogs)
                    })
                    HorizontalDivider(
                        thickness = 2.5.dp
                    )
                }
            }
        }
    }
}


@Composable
fun SettingsMenuItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    title: String = "",
) {
    val settingsMenuStyle = MaterialTheme.typography.titleMedium.copy()

    ListItem(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ),
        headlineContent = {
            Text(
                title, style = settingsMenuStyle,
            )
        }
    )
}


@Preview(device = "id:pixel_5")
@Composable
private fun SettingsPagePrev() {
    AnonNeroTheme {
        SettingsPage()
    }
}
