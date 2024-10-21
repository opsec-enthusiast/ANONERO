import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import io.anonero.ui.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = Black,
    primaryContainer = DarkOrange,
    onPrimaryContainer = White,
    secondary = Black,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White
)

@Composable
fun AnonNeroTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
