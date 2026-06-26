package uz.technobot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfig {
    private String token;
    private String username;
}
