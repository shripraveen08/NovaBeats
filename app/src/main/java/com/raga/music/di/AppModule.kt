package com.raga.music.di

import android.content.Context
import androidx.room.Room
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.raga.music.data.local.RagaDatabase
import com.raga.music.data.local.dao.*
import com.raga.music.data.remote.archive.ArchiveApiService
import com.raga.music.data.remote.jiosaavn.JioSaavnApiService
import com.raga.music.data.remote.jiosaavn.JioSaavnRepository
import com.raga.music.data.remote.soundcloud.SoundCloudApiService
import com.raga.music.data.remote.soundcloud.SoundCloudRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Database ─────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): RagaDatabase =
        Room.databaseBuilder(ctx, RagaDatabase::class.java, "raga.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSongDao(db: RagaDatabase): SongDao = db.songDao()
    @Provides fun providePlaylistDao(db: RagaDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideRecentDao(db: RagaDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
    @Provides fun provideDownloadDao(db: RagaDatabase): DownloadDao = db.downloadDao()

    // ─── OkHttp ───────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

    
    // ─── Internet Archive (Public Domain Music — Free) ────────────────────────

    @Provides @Singleton @Named("archive")
    fun provideArchiveRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(ArchiveApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideArchiveApi(@Named("archive") retrofit: Retrofit): ArchiveApiService =
        retrofit.create(ArchiveApiService::class.java)

    // ─── JioSaavn (Bollywood Music — Free) ───────────────────────────────────────

    @Provides @Singleton @Named("jiosaavn")
    fun provideJioSaavnRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(JioSaavnApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideJioSaavnApi(@Named("jiosaavn") retrofit: Retrofit): JioSaavnApiService =
        retrofit.create(JioSaavnApiService::class.java)

    @Provides @Singleton
    fun provideJioSaavnRepository(apiService: JioSaavnApiService): JioSaavnRepository =
        JioSaavnRepository(apiService)

    // ─── SoundCloud (Free Music — Legal Alternative) ─────────────────────────────

    @Provides @Singleton @Named("soundcloud")
    fun provideSoundCloudRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(SoundCloudApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideSoundCloudApi(@Named("soundcloud") retrofit: Retrofit): SoundCloudApiService =
        retrofit.create(SoundCloudApiService::class.java)

    @Provides @Singleton
    fun provideSoundCloudRepository(apiService: SoundCloudApiService): SoundCloudRepository =
        SoundCloudRepository(apiService)

    // --- ExoPlayer (Media3) ---

    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context)
            .setLoadControl(DefaultLoadControl())
            .setRenderersFactory(DefaultRenderersFactory(context))
            .setTrackSelector(DefaultTrackSelector(context))
            .build()
}
