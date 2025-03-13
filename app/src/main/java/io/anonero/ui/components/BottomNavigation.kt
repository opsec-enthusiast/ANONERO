package io.anonero.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.anonero.ui.home.BottomNavigationItem
import io.anonero.ui.home.getRoutes

val BottomNavHeight = 44.dp

@Composable
fun MainBottomNavigation(
    currentRoute: Any,
    onTabSelected: (BottomNavigationItem) -> Unit
) {

    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        ),
        selectedTextColor = MaterialTheme.colorScheme.primary,
    )

    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        getRoutes().forEach { item ->
            val isSelected = if (currentRoute is String) {
                currentRoute.contains(item.getRouteAsString)
            } else {
                currentRoute == item.getRouteAsString
            }
            NavigationBarItem(
                selected = isSelected,
                colors = navigationItemColors,
                onClick = {
                    if (!isSelected) {
                        onTabSelected(item)
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
            )
        }
    }
}
