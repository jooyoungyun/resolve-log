package com.resolvelog.common;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class TimeConfig {
  @Bean
  Clock clock(@Value("${app.timezone}") String timezone) {
    return Clock.system(ZoneId.of(timezone));
  }
}
