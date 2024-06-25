package com.dox.fpoweroff.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dox.fpoweroff.ui.component.TopBar
import com.dox.fpoweroff.R

@Composable
fun HomeScreen(navController: NavController) {
    val internalFieldSpacing = 8.dp
    val interFieldSpacing = 16.dp

    Scaffold(
        topBar = {
            TopBar(navController, R.string.screen_home, false)
        }
    ) { innerPadding ->

        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(interFieldSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    modifier = Modifier
                        .offset(y = (-64).dp)
                        .size(256.dp),
                    imageVector = Icons.Outlined.PowerSettingsNew,
                    contentDescription = "Power Off Text",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )

                Text(
                    text = stringResource(id = R.string.text_about),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                Text(
                    text = stringResource(id = R.string.text_instructions),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                Text(
                    text = "Source code at: https://github.com/BinitDOX/fpoweroff",
                    style = MaterialTheme.typography.bodySmall
                )

                /*Row {
                    Icon(
                        imageVector = Icons.Outlined.DoDisturb,
                        contentDescription = "Accessibility Permission",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(internalFieldSpacing))
                    Text(
                        text = "Accessibility Permission",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(interFieldSpacing))

                Row {
                    Icon(
                        imageVector = Icons.Outlined.DoDisturb,
                        contentDescription = "DND Permission",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(internalFieldSpacing))
                    Text(
                        text = "Accessibility Permission",
                        style = MaterialTheme.typography.bodySmall
                    )
                }*/
            }
        }
    }
}