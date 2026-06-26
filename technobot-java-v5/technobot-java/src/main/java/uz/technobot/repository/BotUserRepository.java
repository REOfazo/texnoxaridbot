package uz.technobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.technobot.entity.BotUser;

import java.util.Optional;

public interface BotUserRepository extends JpaRepository<BotUser, Long> {
    Optional<BotUser> findByTelegramId(Long telegramId);
}
