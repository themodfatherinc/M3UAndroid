package com.m3u.features.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m3u.core.architecture.dispatcher.Dispatcher
import com.m3u.core.architecture.dispatcher.M3uDispatchers.IO
import com.m3u.core.architecture.pref.Pref
import com.m3u.core.architecture.pref.observeAsFlow
import com.m3u.core.wrapper.Resource
import com.m3u.core.wrapper.mapResource
import com.m3u.core.wrapper.resource
import com.m3u.data.parser.xtream.XtreamStreamInfo
import com.m3u.data.database.model.Playlist
import com.m3u.data.database.model.PlaylistWithCount
import com.m3u.data.database.model.Stream
import com.m3u.data.repository.PlaylistRepository
import com.m3u.data.repository.StreamRepository
import com.m3u.features.foryou.components.recommend.Recommend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class ForyouViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    streamRepository: StreamRepository,
    pref: Pref,
    @Dispatcher(IO) ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    internal val playlistCounts: StateFlow<ImmutableList<PlaylistWithCount>> = playlistRepository
        .observePlaylistCounts()
        .map { it.toPersistentList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf()
        )

    private val unseensDuration = pref
        .observeAsFlow { it.unseensMilliseconds }
        .map { it.toDuration(DurationUnit.MILLISECONDS) }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = Duration.INFINITE
        )

    internal val recommend: StateFlow<Recommend> = unseensDuration
        .flatMapLatest { streamRepository.observeAllUnseenFavourites(it) }
        .map { prev -> Recommend(prev.map { Recommend.UnseenSpec(it) }) }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = Recommend()
        )

    internal fun unsubscribe(url: String) {
        viewModelScope.launch {
            playlistRepository.unsubscribe(url)
        }
    }

    internal fun rename(playlistUrl: String, target: String) {
        viewModelScope.launch {
            playlistRepository.rename(playlistUrl, target)
        }
    }

    internal fun updateUserAgent(playlistUrl: String, userAgent: String?) {
        viewModelScope.launch {
            playlistRepository.updateUserAgent(playlistUrl, userAgent)
        }
    }

    private val series = MutableStateFlow<Stream?>(null)
    internal val episodes: StateFlow<Resource<ImmutableList<XtreamStreamInfo.Episode>>> = series
        .flatMapLatest { series ->
            if (series == null) flow { }
            else resource { playlistRepository.readEpisodesOrThrow(series) }
                .mapResource { it.toPersistentList() }
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = Resource.Loading,
            started = SharingStarted.WhileSubscribed(5_000L)
        )

    internal fun onRequestEpisodes(series: Stream) {
        this.series.value = series
    }

    internal fun onClearEpisodes() {
        this.series.value = null
    }

    internal suspend fun getPlaylist(playlistUrl: String): Playlist? =
        playlistRepository.get(playlistUrl)
}
