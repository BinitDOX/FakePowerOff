package com.dox.fpoweroff.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dox.fpoweroff.ui.component.TopBar
import com.dox.fpoweroff.R

@Composable
fun HomeScreen(navController: NavController) {
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
                    .verticalScroll(rememberScrollState())
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

                SourceCodeLink()
            }
        }
    }
}

@Composable
fun SourceCodeLink() {
    val context = LocalContext.current
    val url = "https://github.com/BinitDOX/FakePowerOff"

    val annotatedString = buildAnnotatedString {
        append("Source code at: ")
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append(url)
        }
        pop()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        )
    }
}