import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.anonero.R
import io.anonero.ui.theme.Black
import io.anonero.ui.theme.DarkGray
import io.anonero.ui.theme.DarkOrange
import io.anonero.ui.theme.Orange
import io.anonero.ui.theme.Typography
import io.anonero.ui.theme.White

private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = Black,
    primaryContainer = DarkOrange,
    onPrimaryContainer = White,
    secondary = DarkGray,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White
)

val robotoMonoFamily = FontFamily(
    Font(R.font.robotomono_light, FontWeight.Light),
    Font(R.font.robotomono_regular, FontWeight.Normal),
    Font(R.font.robotomono_medium, FontWeight.Medium),
    Font(R.font.robotomono_bold, FontWeight.Bold),
    Font(R.font.robotomono_semi_bold, FontWeight.SemiBold),
)


@Composable
fun AnonNeroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
        typography = Typography.copy(
            titleLarge = Typography.titleLarge.copy(fontFamily = robotoMonoFamily),
            titleSmall = Typography.titleSmall.copy(fontFamily = robotoMonoFamily),
            titleMedium = Typography.titleMedium.copy(fontFamily = robotoMonoFamily),
            headlineMedium = Typography.headlineMedium.copy(fontFamily = robotoMonoFamily),
            headlineLarge = Typography.headlineLarge.copy(fontFamily = robotoMonoFamily),
            headlineSmall = Typography.headlineSmall.copy(fontFamily = robotoMonoFamily),
            bodyLarge = Typography.bodyLarge.copy(fontFamily = robotoMonoFamily),
            bodySmall = Typography.bodySmall.copy(fontFamily = robotoMonoFamily),
            bodyMedium = Typography.bodyMedium.copy(fontFamily = robotoMonoFamily),
            displayLarge = Typography.displayLarge.copy(fontFamily = robotoMonoFamily),
            displaySmall = Typography.displaySmall.copy(fontFamily = robotoMonoFamily),
            displayMedium = Typography.displayMedium.copy(fontFamily = robotoMonoFamily),
            labelLarge = Typography.labelLarge.copy(fontFamily = robotoMonoFamily),
            labelSmall = Typography.labelSmall.copy(fontFamily = robotoMonoFamily),
            labelMedium = Typography.labelMedium.copy(fontFamily = robotoMonoFamily),
        )
    )
}
