package com.erdees.places.data.di

import com.erdees.places.data.places.PlacesApiClient
import com.erdees.places.data.places.PlacesApiClientImpl
import com.erdees.places.data.places.PlacesRepositoryImpl
import com.erdees.places.domain.places.PlacesRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    single<PlacesApiClient> { PlacesApiClientImpl(get()) }
    single<PlacesRepository> { PlacesRepositoryImpl(get()) }
}