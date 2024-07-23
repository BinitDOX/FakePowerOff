package com.dox.fpoweroff.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dox.fpoweroff.R
import com.dox.fpoweroff.ui.component.TopBar
import com.dox.fpoweroff.ui.theme.FPowerOffTheme
import com.dox.fpoweroff.utility.Constants
import com.dox.fpoweroff.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val fPowerOffEnabled by settingsViewModel.fPowerOffEnabled.collectAsStateWithLifecycle()
    val dialogCloseTriggerSequence by settingsViewModel.dialogCloseTriggerSequence.collectAsStateWithLifecycle()
    val dndModeEnabled by settingsViewModel.dndModeEnabled.collectAsStateWithLifecycle()
    val lockDeviceEnabled by settingsViewModel.lockDeviceEnabled.collectAsStateWithLifecycle()
    val detectPackageName by settingsViewModel.detectPackageName.collectAsStateWithLifecycle()
    val detectKeywords by settingsViewModel.detectKeywords.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }

    val internalFieldSpacing = 8.dp
    val interFieldSpacing = 16.dp

    Scaffold(
        topBar = {
            TopBar(navController, R.string.screen_settings, true)
        }
    ) { innerPadding ->

        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(interFieldSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(internalFieldSpacing))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.placeholder_enable_fake_power_off),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = fPowerOffEnabled.toBoolean(),
                        onCheckedChange = {
                            settingsViewModel.setFPowerOffEnabled(it.toString())
                            settingsViewModel.saveFPowerOffEnabled()
                        }
                    )
                }
                Text(
                    text = stringResource(id = R.string.setting_desc_enable_fake_power_off),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.placeholder_dnd_mode_enabled),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = dndModeEnabled.toBoolean(),
                        onCheckedChange = {
                            settingsViewModel.setDNDModeEnabled(it.toString())
                            settingsViewModel.saveDNDModeEnabled()
                        }
                    )
                }
                Text(
                    text = stringResource(id = R.string.setting_desc_enable_dnd_mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.placeholder_lock_device_enabled),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = lockDeviceEnabled.toBoolean(),
                        onCheckedChange = {
                            settingsViewModel.setLockDeviceEnabled(it.toString())
                            settingsViewModel.saveLockDeviceEnabled()
                        }
                    )
                }
                Text(
                    text = stringResource(id = R.string.setting_desc_enable_lock_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))

                val isValidTriggerInput: (String) -> Boolean = { input ->
                    input.all { it == 'U' || it == 'D' }
                }

                TextField(
                    value = dialogCloseTriggerSequence,
                    onValueChange = { if(isValidTriggerInput(it)) {settingsViewModel.setDialogCloseTriggerSequence(it)} },
                    label = { Text(stringResource(id = R.string.placeholder_dialog_close_trigger_sequence)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (!it.isFocused) settingsViewModel.saveDialogCloseTriggerSequence() }
                )
                Text(
                    text =  stringResource(id = R.string.setting_desc_power_off_dismiss_sequence) +
                            " Default is: ${Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))


                TextField(
                    value = detectPackageName,
                    onValueChange = { settingsViewModel.setDetectPackageName(it) },
                    label = { Text(stringResource(id = R.string.placeholder_detect_package_name)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (!it.isFocused) settingsViewModel.saveDetectPackageName() }
                )
                Text(
                    text =  buildAnnotatedString {
                                append(stringResource(id = R.string.setting_desc_detect_package_name))
                                withStyle(style = SpanStyle(color = Color.Red.copy(alpha = 0.7f))) {
                                    append(stringResource(R.string.setting_desc_dont_change))
                                }
                                append(" Default is: ${Constants.DETECT_PACKAGE_NAME_DEFAULT}")
                            },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))


                TextField(
                    value = detectKeywords,
                    onValueChange = { settingsViewModel.setDetectKeywords(it) },
                    label = { Text(stringResource(id = R.string.placeholder_detect_keywords)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (!it.isFocused) settingsViewModel.saveDetectKeywords() }
                )
                Text(
                    text =  stringResource(id = R.string.setting_desc_detect_keywords) +
                            " Default is/are: ${Constants.DETECT_KEYWORDS_DEFAULT}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(interFieldSpacing))
            }
        }
    }
}

@Preview
@Composable
private fun AddAssistantScreenPreview() {
    FPowerOffTheme {
        SettingsScreen(NavController(LocalContext.current))
    }
}