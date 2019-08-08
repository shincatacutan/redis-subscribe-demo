package com.markandshin.springboot.redis.redispubsubdemo;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SubscribeController {

  @Autowired
  private ResourceLoader resourceLoader;

  @Autowired
  private SimpMessagingTemplate template;

  private RedissonClient client;

  @Value("${redis.config.path:file:redis-config.yaml}")
  private String redisConfigPath;

  @Autowired
  @PostConstruct
  public void init() {
    Config config;
    try {
      config = Config.fromYAML(resourceLoader.getResource(redisConfigPath).getInputStream());
      client = Redisson.create(config);
    } catch (final IOException e) {
      System.out.println(e.getMessage());
    }
  }

  @MessageMapping("/subscribe")
  public void subscribe(@Payload String topic) {
    System.out.println("Subscribing to topic: " + topic);
    RTopic subscribeTopic = client.getTopic(topic);

    subscribeTopic.addListenerAsync(String.class, new MessageListener<String>() {
      @Override
      public void onMessage(CharSequence channel, String msg) {
        template.convertAndSend("/topic/" + topic, msg);
      }
    });
  }
}
