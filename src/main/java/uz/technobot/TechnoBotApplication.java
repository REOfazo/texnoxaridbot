package uz.technobot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uz.technobot.config.BotConfig;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(BotConfig.class)
public class TechnoBotApplication {

    public static void main(String[] args) {
        log.info("🤖 TechnoBot ishga tushmoqda...");
        SpringApplication.run(TechnoBotApplication.class, args);
    }
}
