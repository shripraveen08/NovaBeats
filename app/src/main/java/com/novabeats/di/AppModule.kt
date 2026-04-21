package com.novabeats.di

import android.content.Context
import androidx.room.Room
import com.novabeats.data.local.NovaBeatDatabase
import com.novabeats.data.local.dao.*
import com.novabeats.data.remote.archive.ArchiveApiService
import com.novabeats.data.remote.jamendo.JamendoApiService
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
    fun provideDatabase(@ApplicationContext ctx: Context): NovaBeatDatabase =
        Room.databaseBuilder(ctx, NovaBeatDatabase::class.java, "novabeats.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSongDao(db: NovaBeatDatabase): SongDao = db.songDao()
    @Provides fun providePlaylistDao(db: NovaBeatDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideRecentDao(db: NovaBeatDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
    @Provides fun provideDownloadDao(db: NovaBeatDatabase): DownloadDao = db.downloadDao()

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

    // ─── Jamendo (CC Music — Free) ────────────────────────────────────────────

    @Provides @Singleton @Named("jamendo")
    fun provideJamendoRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(JamendoApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideJamendoApi(@Named("jamendo") retrofit: Retrofit): JamendoApiService =
        retrofit.create(JamendoApiService::class.java)

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
}
