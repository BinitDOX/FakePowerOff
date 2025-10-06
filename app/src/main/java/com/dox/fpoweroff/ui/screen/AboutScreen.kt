package com.dox.fpoweroff.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AboutScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("FPowerOff v1.1.0", style = MaterialTheme.typography.titleLarge)
        Text("by BinitDOX", style = MaterialTheme.typography.bodyMedium)
        SourceCodeLink()
    }
}

@Composable
private fun SourceCodeLink() {
    val context = LocalContext.current
    val url = "https://github.com/BinitDOX/FakePowerOff"

    val annotatedString = buildAnnotatedString {
        append("Source code available at: ")
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