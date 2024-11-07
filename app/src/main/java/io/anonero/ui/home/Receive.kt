package io.anonero.ui.home

import AnonNeroTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.anonero.icons.AnonIcons
import io.anonero.services.WalletRepo
import io.anonero.ui.components.QrCodeImage
import org.koin.java.KoinJavaComponent.inject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    modifier: Modifier = Modifier,
    onBackPress: () -> Unit = {},
    navigateToSubAddresses: () -> Unit = {}
) {

    val walletRepo: WalletRepo by inject(WalletRepo::class.java)

    val nextAddress by walletRepo.nextAddress.collectAsState(null)

    Scaffold(modifier = Modifier.background(Color.Red),
        topBar = {
            TopAppBar(

                title = {

                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = navigateToSubAddresses
                    ) {
                        Icon(AnonIcons.History, contentDescription = null)
                    }
                }
            )
        }) {
        if (nextAddress != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(60.dp))
                Text(
                    text = nextAddress!!.label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                QrCodeImage(
                    size = 320.dp,
                    content = nextAddress!!.address,
                    modifier = Modifier
                        .padding(20.dp)
                )
                SelectionContainer {
                    Text(
                        nextAddress!!.address,
                        modifier = Modifier.width(250.dp)
                    )
                }
            }
        }
    }
}

@Preview(device = "id:pixel")
@Composable
private fun ReceiveScreenPreview() {
    AnonNeroTheme {
        ReceiveScreen()
    }
}