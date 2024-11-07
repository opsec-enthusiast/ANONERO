package io.anonero.ui.onboard

import AnonNeroTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.anonero.R


@Composable
fun OnboardingWelcome(
    onCreateClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
) {

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.size(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_anon),
                    contentDescription = "Anon nero icon",
                    modifier = Modifier
                        .size(120.dp)
                )
            }
            Column(
                modifier=Modifier.fillMaxHeight(0.5f).weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Bottom),
            ) {
                OutlinedButton(
                    onClick = onCreateClick,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Create Wallet")
                }
                OutlinedButton(
                    onClick = onRestoreClick,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Restore Wallet")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

        }
    }
}


@Preview(heightDp = 640, widthDp = 380)
@Composable
private fun OnboardingWelcomePreview() {
    AnonNeroTheme {
        OnboardingWelcome()
    }
}
