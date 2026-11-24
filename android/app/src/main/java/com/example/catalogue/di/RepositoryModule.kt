package com.example.catalogue.di

import com.example.catalogue.data.repository.CatalogueRepositoryImpl
import com.example.catalogue.domain.repository.CatalogueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun catalogueRepository(impl: CatalogueRepositoryImpl): CatalogueRepository
}
