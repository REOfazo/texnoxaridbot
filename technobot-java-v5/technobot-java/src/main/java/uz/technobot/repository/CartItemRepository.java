package uz.technobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.technobot.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c JOIN FETCH c.product WHERE c.user.telegramId = :tid")
    List<CartItem> findByUserTelegramId(@Param("tid") Long telegramId);

    @Query("SELECT c FROM CartItem c WHERE c.user.telegramId = :tid AND c.product.id = :pid")
    Optional<CartItem> findByUserTelegramIdAndProductId(@Param("tid") Long telegramId,
                                                        @Param("pid") Long productId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.telegramId = :tid")
    void deleteByUserTelegramId(@Param("tid") Long telegramId);
}
