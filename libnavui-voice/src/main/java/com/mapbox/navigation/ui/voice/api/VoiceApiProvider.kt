package com.mapbox.navigation.ui.voice.api

import android.content.Context
import com.mapbox.navigation.core.internal.accounts.MapboxNavigationAccounts
import java.io.File

internal object VoiceApiProvider {

    private const val MAPBOX_INSTRUCTIONS_CACHE = "mapbox_instructions_cache"

    fun retrieveMapboxVoiceApi(
        context: Context,
        accessToken: String,
        language: String,
        baseUri: String?
    ): MapboxVoiceApi = MapboxVoiceApi(
        MapboxSpeechProvider(
            accessToken,
            language,
            MapboxNavigationAccounts,
            baseUri
        ),
        MapboxSpeechFileProvider(
            File(
                context.applicationContext.cacheDir,
                MAPBOX_INSTRUCTIONS_CACHE
            ).also { it.mkdirs() }
        )
    )
}
