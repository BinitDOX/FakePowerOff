package com.dox.fpoweroff.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dox.fpoweroff.R
import com.dox.fpoweroff.ui.component.TopBar
import com.dox.fpoweroff.utility.Constants
import com.dox.fpoweroff.viewmodel.AdvancedSettingsViewModel

@Composable
fun AdvancedSettingsScreen(
    navController: NavController,
    viewModel: AdvancedSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTestingVoidMode by viewModel.isTestingVoidMode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopBar(navController, R.string.screen_advanced_settings, false) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSwitchItem(
                titleRes = R.string.placeholder_dnd_mode_enabled,
                descriptionRes = R.string.setting_desc_enable_dnd_mode,
                isChecked = uiState.isDndEnabled,
                onCheckedChange = { viewModel.setDndEnabled(it) }
            )

            SettingsSwitchItem(
                titleRes = R.string.placeholder_lock_device_enabled,
                descriptionRes = R.string.setting_desc_enable_lock_device,
                isChecked = uiState.isLockDeviceEnabled,
                onCheckedChange = { viewModel.setLockDeviceEnabled(it) }
            )

            SettingsSwitchItem(
                titleRes = R.string.placeholder_void_mode_enabled,
                descriptionRes = R.string.setting_desc_void_mode,
                isChecked = uiState.isVoidModeEnabled,
                onCheckedChange = { viewModel.setVoidModeEnabled(it) }
            )

            if (uiState.isVoidModeEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Test Void Mode",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Verify that Shizuku can dim your screen. Press again to restore brightness.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.toggleVoidModeTest() },
                            colors = if (isTestingVoidMode) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isTestingVoidMode) "Restore Brightness" else "Set Brightness to 0")
                        }
                    }
                }
            }

            SettingsTextFieldItem(
                labelRes = R.string.placeholder_detect_keywords,
                value = uiState.detectKeywords,
                onValueChange = { viewModel.onDetectKeywordsChanged(it) },
                onFocusLost = { viewModel.saveDetectKeywords() }
            ) {
                Text(
                    text = stringResource(id = R.string.setting_desc_detect_keywords) +
                            " Default is/are: ${Constants.DETECT_KEYWORDS_DEFAULT}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            SettingsTextFieldItem(
                labelRes = R.string.placeholder_detect_package_name,
                value = uiState.detectPackageName,
                onValueChange = { viewModel.onDetectPackageNameChanged(it) },
                onFocusLost = { viewModel.saveDetectPackageName() }
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.setting_desc_detect_package_name))
                        withStyle(style = SpanStyle(color = Color.Red.copy(alpha = 0.7f))) {
                            append(stringResource(R.string.setting_desc_dont_change))
                        }
                        append(" Default is: ${Constants.DETECT_PACKAGE_NAME_DEFAULT}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SettingsTextFieldItem(
    @StringRes labelRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    description: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(stringResource(id = labelRes)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            onFocusLost()
                        }
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
            description()
        }
    }
}