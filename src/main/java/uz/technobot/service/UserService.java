package uz.technobot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.technobot.entity.BotUser;
import uz.technobot.enums.Language;
import uz.technobot.repository.BotUserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BotUserRepository repo;

    @Transactional
    public BotUser getOrCreate(Long telegramId, String username, String fullName) {
        return repo.findByTelegramId(telegramId).orElseGet(() -> {
            BotUser u = BotUser.builder()
                    .telegramId(telegramId)
                    .username(username)
                    .fullName(fullName)
                    .build();
            return repo.save(u);
        });
    }

    @Transactional
    public void updateLanguage(Long telegramId, String langCode) {
        repo.findByTelegramId(telegramId).ifPresent(u -> {
            u.setLanguage(Language.from(langCode));
            repo.save(u);
        });
    }

    @Transactional
    public void updatePhone(Long telegramId, String phone) {
        repo.findByTelegramId(telegramId).ifPresent(u -> {
            u.setPhone(phone);
            repo.save(u);
        });
    }

    public BotUser findByTelegramId(Long telegramId) {
        return repo.findByTelegramId(telegramId).orElse(null);
    }
}
