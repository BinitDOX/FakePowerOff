package com.dox.fpoweroff.ui.screen

import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dox.fpoweroff.R
import com.dox.fpoweroff.manager.TestSequenceManager
import com.dox.fpoweroff.ui.component.TopBar
import com.dox.fpoweroff.ui.data.RouteItem
import com.dox.fpoweroff.viewmodel.DashboardViewModel
import com.dox.fpoweroff.viewmodel.RecommendationLevel
import com.dox.fpoweroff.viewmodel.RecommendedKeyword
import com.dox.fpoweroff.viewmodel.TutorialUiState
import com.dox.fpoweroff.viewmodel.TutorialViewModel
import kotlinx.coroutines.launch

const val TUTORIAL_PAGE_COUNT = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    navController: NavController,
    viewModel: TutorialViewModel = hiltViewModel(),
    testSequenceManager: TestSequenceManager = hiltViewModel<DashboardViewModel>().testSequenceManager
) {
    val pagerState = rememberPagerState(pageCount = { TUTORIAL_PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val progress by animateFloatAsState(
        targetValue = (pagerState.currentPage + 1) / TUTORIAL_PAGE_COUNT.toFloat(),
        label = "TutorialProgress"
    )

    Scaffold(
        topBar = { TopBar(navController, R.string.screen_tutorial, false) },
        bottomBar = {
            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                TutorialNavigationButtons(
                    currentPage = pagerState.currentPage,
                    isNextEnabled = isNextEnabled(pagerState.currentPage, uiState),
                    onNextClicked = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    onBackClicked = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    onFinishClicked = {
                        viewModel.onTutorialComplete()
                        navController.navigate(RouteItem.Dashboard.route) {
                            popUpTo(RouteItem.Tutorial.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WelcomeStep(viewModel)
                1 -> SequenceStep(uiState.recoverySequence, viewModel)
                2 -> PracticeStep(uiState, viewModel)
                3 -> KeywordsStep(uiState, viewModel)
                4 -> CompletionStep(uiState, viewModel, testSequenceManager)
            }
        }
    }
}

@Composable
private fun TutorialStepLayout(title: String, description: String, content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        content()
    }
}

@Composable
private fun WelcomeStep(viewModel: TutorialViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkAccessibilityPermission()
    }

    TutorialStepLayout(
        title = stringResource(R.string.tutorial_step_1_title),
        description = stringResource(R.string.tutorial_step_1_desc)
    ) {
        Button(onClick = {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        }) {
            Text(stringResource(R.string.tutorial_button_open_settings))
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.isAccessibilityEnabled) {
            Text(stringResource(R.string.tutorial_permission_granted), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        } else {
            OutlinedButton(onClick = { viewModel.checkAccessibilityPermission() }) {
                Text(stringResource(R.string.tutorial_button_check_permission))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.tutorial_permission_not_granted), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SequenceStep(sequence: String, viewModel: TutorialViewModel) {
    TutorialStepLayout(
        title = stringResource(R.string.tutorial_step_2_title),
        description = stringResource(R.string.tutorial_step_2_desc)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (sequence.isEmpty()) {
                    Text(stringResource(R.string.tutorial_sequence_empty), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(text = sequence, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.onSequenceCharAdded('U') }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.tutorial_button_volume_up))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { viewModel.onSequenceCharAdded('D') }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.tutorial_button_volume_down))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { viewModel.onSequenceCharDeleted() }, enabled = sequence.isNotEmpty()) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.tutorial_button_delete))
        }
    }
}

@Composable
private fun PracticeStep(uiState: TutorialUiState, viewModel: TutorialViewModel) {
    DisposableEffect(Unit) {
        viewModel.startPractice()
        onDispose { viewModel.stopPractice() }
    }

    TutorialStepLayout(
        title = stringResource(R.string.tutorial_step_3_title),
        description = "Practice mode is now active. Use your device's volume buttons to enter the sequence."
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.tutorial_practice_attempts, uiState.practiceAttempts),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (uiState.practiceAttempts > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    val displayText = if (uiState.practiceInput.isEmpty()) "Press volume keys..." else uiState.practiceInput
                    Text(text = displayText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { viewModel.resetPractice() }) {
            Text(stringResource(R.string.tutorial_button_reset_practice))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.tutorial_button_reset_practice_desc), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun KeywordsStep(uiState: TutorialUiState, viewModel: TutorialViewModel) {
    DisposableEffect(Unit) {
        viewModel.startKeywordDetection()
        onDispose { viewModel.stopKeywordDetection() }
    }

    TutorialStepLayout(
        title = stringResource(R.string.tutorial_step_4_title),
        description = stringResource(R.string.tutorial_step_4_desc)
    ) {
        if (uiState.recommendedKeywords.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Listening for power menu...",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Text(stringResource(R.string.tutorial_step_4_success_desc), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                uiState.recommendedKeywords.forEach { recommendedKeyword ->
                    KeywordListItem(
                        recommendedKeyword = recommendedKeyword,
                        isSelected = (recommendedKeyword.keyword == uiState.selectedKeyword),
                        onClick = { viewModel.onKeywordSelected(recommendedKeyword.keyword) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeywordListItem(
    recommendedKeyword: RecommendedKeyword,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = recommendedKeyword.keyword, style = MaterialTheme.typography.bodyLarge)

            if (recommendedKeyword.level != RecommendationLevel.NONE) {
                val (text, color) = when (recommendedKeyword.level) {
                    RecommendationLevel.MOST_RECOMMENDED -> stringResource(R.string.recommendation_most) to MaterialTheme.colorScheme.primary
                    RecommendationLevel.RECOMMENDED -> stringResource(R.string.recommendation_recommended) to MaterialTheme.colorScheme.secondary
                    else -> "" to Color.Transparent
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompletionStep(uiState: TutorialUiState, viewModel: TutorialViewModel,
                           testSequenceManager: TestSequenceManager) {
    val context = LocalContext.current

    TutorialStepLayout(
        title = stringResource(R.string.tutorial_step_5_title),
        description = stringResource(R.string.tutorial_step_5_desc)
    ) {
        // How-to-use Instructions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("1. ${stringResource(R.string.tutorial_step_5_instruction_1)}")
                Text("2. ${stringResource(R.string.tutorial_step_5_instruction_2)}")
                Text("3. ${stringResource(R.string.tutorial_step_5_instruction_3)}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Final Simulation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.tutorial_step_5_final_prompt),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        testSequenceManager.startTestSequence(
                            context = context,
                            sequenceToTest = uiState.recoverySequence,
                            onSuccess = {
                                viewModel.onFinalSimulationCompleted()
                            }
                        )
                    },
                    enabled = !uiState.isFinalSimulationCompleted
                ) {
                    Text(stringResource(R.string.tutorial_step_5_button_run_sim))
                }
            }
        }
    }
}

@Composable
private fun TutorialNavigationButtons(
    currentPage: Int,
    isNextEnabled: Boolean,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onFinishClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPage > 0) {
            OutlinedButton(onClick = onBackClicked) {
                Text(stringResource(R.string.tutorial_button_back))
            }
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }

        if (currentPage < TUTORIAL_PAGE_COUNT - 1) {
            Button(onClick = onNextClicked, enabled = isNextEnabled) {
                Text(stringResource(R.string.tutorial_button_next))
            }
        } else {
            Button(onClick = onFinishClicked, enabled = isNextEnabled) {
                Text(stringResource(R.string.tutorial_button_finish))
            }
        }
    }
}

private fun isNextEnabled(currentPage: Int, uiState: TutorialUiState): Boolean {
    return when (currentPage) {
        0 -> uiState.isAccessibilityEnabled
        1 -> uiState.recoverySequence.length >= 4
        2 -> uiState.practiceAttempts >= 3
        3 -> uiState.selectedKeyword.isNotBlank()
        4 -> uiState.isFinalSimulationCompleted
        else -> true
    }
}