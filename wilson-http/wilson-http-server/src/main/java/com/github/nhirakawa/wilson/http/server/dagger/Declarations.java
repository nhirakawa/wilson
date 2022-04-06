package com.github.nhirakawa.wilson.http.server.dagger;

import com.github.nhirakawa.wilson.http.server.HttpMessageSender;
import com.github.nhirakawa.wilson.protocol.service.MessageSender;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class Declarations {

  @Binds
  abstract MessageSender bindsMessageSender(
    HttpMessageSender httpMessageSender
  );
}
