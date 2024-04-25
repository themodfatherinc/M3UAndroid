package com.m3u.data.repository.playlist

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.m3u.data.parser.xtream.XtreamStreamInfo
import com.m3u.data.database.model.Playlist
import com.m3u.data.database.model.PlaylistWithCount
import com.m3u.data.database.model.PlaylistWithStreams
import com.m3u.data.database.model.Stream
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun observeAll(): Flow<List<Playlist>>
    fun observeAllEpgs(): Flow<List<Playlist>>
    fun observePlaylistUrls(): Flow<List<String>>
    suspend fun get(url: String): Playlist?
    fun observe(url: String): Flow<Playlist?>
    fun observeWithStreams(url: String): Flow<PlaylistWithStreams?>
    suspend fun getWithStreams(url: String): PlaylistWithStreams?

    suspend fun m3uOrThrow(
        title: String,
        url: String,
        callback: (count: Int) -> Unit = {}
    )

    suspend fun xtreamOrThrow(
        title: String,
        basicUrl: String,
        username: String,
        password: String,
        type: String?,
        callback: (count: Int) -> Unit = {}
    )

    suspend fun epgOrThrow(epg: String)

    suspend fun refresh(url: String)

    suspend fun unsubscribe(url: String): Playlist?

    suspend fun onEditPlaylistTitle(url: String, title: String)

    suspend fun backupOrThrow(uri: Uri)

    suspend fun restoreOrThrow(uri: Uri)

    suspend fun pinOrUnpinCategory(url: String, category: String)

    suspend fun hideOrUnhideCategory(url: String, category: String)

    suspend fun onEditPlaylistUserAgent(url: String, userAgent: String)

    fun observeAllCounts(): Flow<List<PlaylistWithCount>>

    suspend fun readEpisodesOrThrow(series: Stream): List<XtreamStreamInfo.Episode>

    suspend fun addEpgToPlaylist(epgUrl: String, playlistUrl: String)

    suspend fun removeEpgFromPlaylist(epgUrl: String, playlistUrl: String)

    suspend fun deleteEpgPlaylistAndProgrammes(epgUrl: String)

    suspend fun onUpdateEpgPlaylist(useCase: UpdateEpgPlaylistUseCase)

    @Immutable
    data class UpdateEpgPlaylistUseCase(
        val playlistUrl: String,
        val epgUrl: String,
        val action: Boolean
    )
}
