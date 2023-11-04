package com.m3u.androidApp.navigation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.m3u.core.wrapper.Event
import com.m3u.core.wrapper.eventOf
import com.m3u.features.favorite.FavouriteRoute
import com.m3u.features.favorite.NavigateToLive
import com.m3u.features.main.MainRoute
import com.m3u.features.main.NavigateToFeed
import com.m3u.features.setting.NavigateToAbout
import com.m3u.features.setting.NavigateToConsole
import com.m3u.features.setting.SettingRoute
import com.m3u.material.ktx.Edge
import com.m3u.material.ktx.blurEdge
import com.m3u.material.model.LocalTheme
import com.m3u.ui.Destination
import com.m3u.ui.ResumeEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

const val ROOT_ROUTE = "root_route"

fun NavController.popupToRoot() {
    this.popBackStack(ROOT_ROUTE, false)
}

fun NavGraphBuilder.rootGraph(
    currentPage: Int,
    onCurrentPage: (Int) -> Unit,
    contentPadding: PaddingValues,
    navigateToFeed: NavigateToFeed,
    navigateToLive: NavigateToLive,
    navigateToConsole: NavigateToConsole,
    navigateToAbout: NavigateToAbout,
) {
    composable(ROOT_ROUTE) {
        RootGraph(
            currentPage = currentPage,
            onCurrentPage = onCurrentPage,
            contentPadding = contentPadding,
            navigateToFeed = navigateToFeed,
            navigateToLive = navigateToLive,
            navigateToConsole = navigateToConsole,
            navigateToAbout = navigateToAbout
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RootGraph(
    currentPage: Int,
    onCurrentPage: (Int) -> Unit,
    contentPadding: PaddingValues,
    navigateToFeed: NavigateToFeed,
    navigateToLive: NavigateToLive,
    navigateToConsole: NavigateToConsole,
    navigateToAbout: NavigateToAbout,
    modifier: Modifier = Modifier
) {
    val destinations = Destination.Root.entries
    val pagerState = rememberPagerState { destinations.size }
    val actualOnCurrentPage by rememberUpdatedState(onCurrentPage)

    LaunchedEffect(pagerState) {
        snapshotFlow {
            PagerStateSnapshot(
                pagerState.currentPage,
                pagerState.targetPage,
                pagerState.settledPage,
                pagerState.isScrollInProgress
            )
        }
            .onEach {
                Log.e("PagerState", "$it")
            }
            .launchIn(this)

        snapshotFlow {
            // FIXME:
            //  When a user scrolls the page using gestures on the root screen, it may be 0.
            //  But we cannot use pageState#currentPage because it will not work
            //  when we selects a bottom bar item from 0 -> 2
            pagerState.targetPage
        }
            .onEach(actualOnCurrentPage)
            .launchIn(this)
    }

    LaunchedEffect(currentPage) {
        if (currentPage != pagerState.currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .blurEdge(
                edge = Edge.Bottom,
                color = LocalTheme.current.background
            )
    ) { pagerIndex ->
        when (destinations[pagerIndex]) {
            Destination.Root.Main -> {
                MainRoute(
                    navigateToFeed = navigateToFeed,
                    resume = rememberResumeEvent(currentPage, pagerIndex),
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Destination.Root.Favourite -> {
                FavouriteRoute(
                    navigateToLive = navigateToLive,
                    resume = rememberResumeEvent(currentPage, pagerIndex),
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Destination.Root.Setting -> {
                SettingRoute(
                    navigateToConsole = navigateToConsole,
                    navigateToAbout = navigateToAbout,
                    contentPadding = contentPadding,
                    resume = rememberResumeEvent(currentPage, pagerIndex),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private data class PagerStateSnapshot(
    val current: Int,
    val target: Int,
    val settled: Int,
    val scrolling: Boolean
)

@Composable
private fun rememberResumeEvent(currentPage: Int, targetPage: Int): ResumeEvent =
    remember(currentPage, targetPage) {
        if (currentPage == targetPage) eventOf(Unit)
        else Event.Handled()
    }
