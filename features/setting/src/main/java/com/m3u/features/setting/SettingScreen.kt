package com.m3u.features.setting

import android.content.res.Configuration
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m3u.core.annotation.SetSyncMode
import com.m3u.core.annotation.SyncMode
import com.m3u.core.util.context.toast
import com.m3u.features.setting.components.CheckBoxPreference
import com.m3u.features.setting.components.TextPreference
import com.m3u.ui.components.M3UColumn
import com.m3u.ui.components.M3URow
import com.m3u.ui.components.M3UTextButton
import com.m3u.ui.components.M3UTextField
import com.m3u.ui.model.AppAction
import com.m3u.ui.model.LocalSpacing
import com.m3u.ui.model.SetActions
import com.m3u.ui.util.EventHandler
import com.m3u.ui.util.LifecycleEffect

@Composable
internal fun SettingRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = hiltViewModel(),
    setAppActions: SetActions
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    EventHandler(state.message) {
        context.toast(it)
    }

    val setAppActionsUpdated by rememberUpdatedState(setAppActions)
    LifecycleEffect { event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                val actions = listOf<AppAction>()
                setAppActionsUpdated(actions)
            }

            Lifecycle.Event.ON_PAUSE -> {
                setAppActionsUpdated(emptyList())
            }

            else -> {}
        }
    }

    SettingScreen(
        subscribeEnable = !state.adding,
        title = state.title,
        url = state.url,
        version = state.version,
        syncMode = state.syncMode,
        onTitle = { viewModel.onEvent(SettingEvent.OnTitle(it)) },
        onUrl = { viewModel.onEvent(SettingEvent.OnUrl(it)) },
        onSubscribe = { viewModel.onEvent(SettingEvent.SubscribeUrl) },
        onSyncMode = { viewModel.onEvent(SettingEvent.OnSyncMode(it)) },
        useCommonUIMode = state.useCommonUIMode,
        onUIMode = { viewModel.onEvent(SettingEvent.OnUIMode) },
        modifier = modifier
    )
}

@Composable
private fun SettingScreen(
    subscribeEnable: Boolean,
    version: String,
    title: String,
    url: String,
    @SyncMode syncMode: Int,
    onTitle: (String) -> Unit,
    onUrl: (String) -> Unit,
    onSubscribe: () -> Unit,
    onSyncMode: SetSyncMode,
    useCommonUIMode: Boolean,
    onUIMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .scrollable(
                orientation = Orientation.Vertical,
                state = rememberScrollableState { it }
            )
            .fillMaxSize()
            .testTag("features:setting")
    ) {
        val configuration = LocalConfiguration.current
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                M3UColumn(
                    verticalArrangement = Arrangement.spacedBy(spacing.small),
                    modifier = modifier
                ) {
                    MakeSubscriptionPart(
                        title = title,
                        url = url,
                        subscribeEnable = subscribeEnable,
                        onTitle = onTitle,
                        onUrl = onUrl,
                        onSubscribe = onSubscribe,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SettingItemsPart(
                        syncMode = syncMode,
                        onSyncMode = onSyncMode,
                        useCommonUIMode = useCommonUIMode,
                        onUIMode = onUIMode,
                        modifier = Modifier.fillMaxWidth()
                    )
                    M3UTextButton(
                        text = stringResource(R.string.label_app_version, version),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {

                    }
                }
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                M3URow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    modifier = modifier.padding(horizontal = spacing.medium)
                ) {
                    MakeSubscriptionPart(
                        title = title,
                        url = url,
                        subscribeEnable = subscribeEnable,
                        onTitle = onTitle,
                        onUrl = onUrl,
                        onSubscribe = onSubscribe,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                    )
                    Column(
                        modifier = modifier.weight(1f)
                    ) {
                        SettingItemsPart(
                            syncMode = syncMode,
                            onSyncMode = onSyncMode,
                            useCommonUIMode = useCommonUIMode,
                            onUIMode = onUIMode,
                            modifier = Modifier.fillMaxWidth()
                        )
                        M3UTextButton(
                            text = stringResource(R.string.label_app_version, version),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {

                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun MakeSubscriptionPart(
    title: String,
    url: String,
    subscribeEnable: Boolean,
    onTitle: (String) -> Unit,
    onUrl: (String) -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.small),
        modifier = modifier
    ) {
        val focusRequester = remember { FocusRequester() }
        M3UTextField(
            text = title,
            placeholder = stringResource(R.string.placeholder_title),
            onValueChange = onTitle,
            keyboardActions = KeyboardActions(
                onNext = {
                    focusRequester.requestFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        M3UTextField(
            text = url,
            placeholder = stringResource(R.string.placeholder_url),
            onValueChange = onUrl,
            keyboardActions = KeyboardActions(
                onDone = {
                    onSubscribe()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        val buttonTextResId = if (subscribeEnable) R.string.label_subscribe
        else R.string.label_subscribing
        M3UTextButton(
            enabled = subscribeEnable,
            text = stringResource(buttonTextResId),
            onClick = { onSubscribe() },
            modifier = Modifier.fillMaxWidth()
        )

    }
}

@Composable
private fun SettingItemsPart(
    syncMode: @SyncMode Int,
    onSyncMode: SetSyncMode,
    useCommonUIMode: Boolean,
    onUIMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clip(MaterialTheme.shapes.medium),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        TextPreference(
            title = stringResource(R.string.sync_mode),
            content = when (syncMode) {
                SyncMode.DEFAULT -> stringResource(R.string.sync_mode_default)
                SyncMode.EXCEPT -> stringResource(R.string.sync_mode_favourite_except)
                else -> ""
            },
            onClick = {
                // TODO
                val target = when (syncMode) {
                    SyncMode.DEFAULT -> SyncMode.EXCEPT
                    else -> SyncMode.DEFAULT
                }
                onSyncMode(target)
            }
        )
        CheckBoxPreference(
            title = stringResource(R.string.common_ui_mode),
            subtitle = stringResource(R.string.common_ui_mode_description),
            enabled = true,
            checked = useCommonUIMode,
            onCheckedChange = { newValue ->
                if (newValue != useCommonUIMode) {
                    onUIMode()
                }
            }
        )
    }
}