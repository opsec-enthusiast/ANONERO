package io.anonero.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import io.anonero.icons.AnonIcons
import io.anonero.ui.home.graph.ReceiveRoute
import io.anonero.ui.home.graph.SendRoute
import io.anonero.ui.home.graph.SettingsRoute
import io.anonero.ui.home.graph.TransactionsRoute


data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
) {

    val getRouteAsString
        get(): String {
            return route::class.qualifiedName ?: ""
        }
}

fun getRoutes(): ArrayList<BottomNavigationItem> {
    return arrayListOf(
        BottomNavigationItem(
            title = "Transactions",
            icon = AnonIcons.Home,
            route = TransactionsRoute
        ),
        BottomNavigationItem(
            title = "Receive",
            icon = AnonIcons.QrCode,
            route = ReceiveRoute
        ),
        BottomNavigationItem(
            title = "Send",
            icon = Icons.AutoMirrored.Outlined.Send,
            route = SendRoute
        ),
        BottomNavigationItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            route = SettingsRoute
        ),

        )
}