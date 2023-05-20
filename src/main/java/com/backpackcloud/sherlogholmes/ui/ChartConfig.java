package com.backpackcloud.sherlogholmes.ui;

import com.backpackcloud.serializer.JSON;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint("/config")
public class ChartConfig {

  private final String configObject;

  public ChartConfig(Config config, @JSON Serializer serializer) {
    this.configObject = serializer.serialize(config.charts());
  }

  @OnOpen
  public void openSession(Session session) {
    session.getAsyncRemote().sendObject(configObject);
  }

}
