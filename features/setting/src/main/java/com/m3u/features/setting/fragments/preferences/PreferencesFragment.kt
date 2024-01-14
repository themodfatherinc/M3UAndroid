package com.m3u.features.setting.fragments.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import com.m3u.material.ktx.isTelevision
import com.m3u.material.ktx.plus
import com.m3u.material.model.LocalSpacing
import com.m3u.ui.Destination.Root.Setting.SettingFragment

@Composable
internal fun PreferencesFragment(
    fragment: SettingFragment,
    contentPadding: PaddingValues,
    versionName: String,
    versionCode: Int,
    navigateToPlaylistManagement: () -> Unit,
    navigateToScriptManagement: () -> Unit,
    navigateToThemeSelector: () -> Unit,
    navigateToConsole: () -> Unit,
    navigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val tv = isTelevision()
    if (!tv) {
        LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item {
                RegularPreferences(
                    fragment = fragment,
                    navigateToPlaylistManagement = navigateToPlaylistManagement,
                    navigateToThemeSelector = navigateToThemeSelector
                )
                HorizontalDivider()
            }
            item {
                OptionalPreferences()
                HorizontalDivider()
            }
            item {
                ExperimentalPreference(
                    navigateToScriptManagement = navigateToScriptManagement,
                    navigateToConsole = navigateToConsole
                )
                HorizontalDivider()
            }
            item {
                OtherPreferences(
                    versionName = versionName,
                    versionCode = versionCode,
                    navigateToAbout = navigateToAbout
                )
            }
        }
    } else {
        TvLazyColumn(
            modifier = modifier,
            contentPadding = contentPadding + PaddingValues(spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            item {
                RegularPreferences(
                    fragment = fragment,
                    navigateToPlaylistManagement = navigateToPlaylistManagement,
                    navigateToThemeSelector = navigateToThemeSelector
                )
            }
            item {
                OptionalPreferences()
            }
            item {
                ExperimentalPreference(
                    navigateToScriptManagement = navigateToScriptManagement,
                    navigateToConsole = navigateToConsole
                )
            }
            item {
                OtherPreferences(
                    versionName = versionName,
                    versionCode = versionCode,
                    navigateToAbout = navigateToAbout
                )
            }
        }
    }
}