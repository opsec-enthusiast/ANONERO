package io.anonero.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import io.anonero.ui.home.BottomNavigationItem
import io.anonero.ui.home.getRoutes

val BottomNavHeight = 44.dp

@Composable
fun MainBottomNavigation(
    hazeState: HazeState,
    currentRoute: Any,
    onTabSelected: (BottomNavigationItem) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) }

    LaunchedEffect(currentRoute) {
        getRoutes().forEachIndexed { index, bottomNavigationItem ->
            val isSelected = if (currentRoute is String) {
                currentRoute.contains(bottomNavigationItem.getRouteAsString)
            } else {
                currentRoute == bottomNavigationItem.getRouteAsString
            }
            if (isSelected) {
                selectedTabIndex = index
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .safeGesturesPadding()
            .fillMaxWidth()
            .height(BottomNavHeight)
            .hazeChild(state = hazeState, shape = CircleShape)
            .border(
                width = Dp.Hairline,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = .3f),
                        Color.Black.copy(alpha = .8f),
                        Color.White.copy(alpha = .2f),
                    ),
                ),
                shape = CircleShape
            )
            .background(Color.Black.copy(alpha = .85f), shape = CircleShape)

    ) {
        BottomBarTabs(
            getRoutes(),
            selectedTab = selectedTabIndex,
            onTabSelected = {
                onTabSelected(it)
                selectedTabIndex = getRoutes().indexOf(it)
            }
        )

        val animatedSelectedTabIndex by animateFloatAsState(
            targetValue = selectedTabIndex.toFloat(), label = "animatedSelectedTabIndex",
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy,
            )
        )

        val animatedColor by animateColorAsState(
            targetValue = MaterialTheme.colorScheme.primary,
            label = "animatedColor",
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
            )
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .blur(58.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val tabWidth = size.width / getRoutes().size
            drawCircle(
                color = animatedColor.copy(alpha = .6f),
                radius = size.height / 2,
                center = Offset(
                    (tabWidth * animatedSelectedTabIndex) + tabWidth / 2,
                    size.height / 2
                )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            val path = Path().apply {
                addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height)))
            }
            val length = PathMeasure().apply { setPath(path, false) }.length

            val tabWidth = size.width / getRoutes().size
            drawPath(
                path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0f),
                        animatedColor.copy(alpha = 1f),
                        animatedColor.copy(alpha = 1f),
                        animatedColor.copy(alpha = 0f),
                    ),
                    startX = tabWidth * animatedSelectedTabIndex,
                    endX = tabWidth * (animatedSelectedTabIndex + 1),
                ),
                style = Stroke(
                    width = 6f,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(length / 2, length)
                    )
                )
            )
        }
    }
}

@Composable
fun BottomBarTabs(
    tabs: List<BottomNavigationItem>,
    selectedTab: Int,
    onTabSelected: (BottomNavigationItem) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        for (tab in tabs) {
            val alpha by animateFloatAsState(
                targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .35f,
                label = "alpha"
            )
            val colorAnim by animateColorAsState(
                targetValue = if (selectedTab == tabs.indexOf(tab)) MaterialTheme.colorScheme.primary else Color.White,
                label = "color"
            )
            val scale by animateFloatAsState(
                targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .98f,
                visibilityThreshold = .000001f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                ),
                label = "scale"
            )
            Column(
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
                    .fillMaxHeight()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onTabSelected(tab)
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(imageVector = tab.icon, contentDescription = "tab ${tab.title}", tint = colorAnim)
            }
        }
    }
}