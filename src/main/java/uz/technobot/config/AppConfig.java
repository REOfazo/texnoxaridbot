package uz.technobot.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Getter
@Configuration
public class AppConfig {

    private final List<Long> adminIds;

    public AppConfig(@Value("${app.admin-ids:}") String adminIdsRaw) {
        if (adminIdsRaw == null || adminIdsRaw.isBlank()) {
            this.adminIds = Collections.emptyList();
            log.warn("⚠️  ADMIN_IDS o'rnatilmagan! /admin komandasi ishlamaydi. " +
                     ".env faylga: ADMIN_IDS=sizning_telegram_id");
        } else {
            this.adminIds = Arrays.stream(adminIdsRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList();
            log.info("✅ Admin IDlar yuklandi: {}", this.adminIds);
        }
    }

    public boolean isAdmin(Long telegramId) {
        // ADMIN_IDS bo'sh bo'lsa — hech kim admin emas
        // Dev rejim uchun: ADMIN_IDS=* bo'lsa hammaga ruxsat
        if (adminIds.isEmpty()) return false;
        return adminIds.contains(telegramId);
    }
}
