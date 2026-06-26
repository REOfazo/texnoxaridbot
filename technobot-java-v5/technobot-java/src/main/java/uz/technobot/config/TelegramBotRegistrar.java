package uz.technobot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.technobot.bot.TechnoBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotRegistrar {

    private final TechnoBot technoBot;

    @EventListener(ContextRefreshedEvent.class)
    public void registerBot() {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(technoBot);
            log.info("✅ TechnoBot muvaffaqiyatli ro'yxatdan o'tdi: @{}", technoBot.getBotUsername());
        } catch (TelegramApiException e) {
            log.error("❌ TechnoBot ro'yxatdan o'tishda xato: {}", e.getMessage());
        }
    }
}
