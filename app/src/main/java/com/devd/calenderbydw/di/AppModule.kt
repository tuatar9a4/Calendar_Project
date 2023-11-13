package com.devd.calenderbydw.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    //use to total app manager like clipboard,contentResolver which use context

}