package com.github.nhirakawa.wilson.http.client;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module
public class WilsonHttpClientModule {

  @Provides
  @Singleton
  OkHttpClient provideOkHttpClient() {
    return new OkHttpClient();
  }
}
