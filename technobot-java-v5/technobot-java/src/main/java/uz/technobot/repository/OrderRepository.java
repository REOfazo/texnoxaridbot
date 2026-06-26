package uz.technobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.technobot.entity.Order;
import uz.technobot.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.telegramId = :tid ORDER BY o.createdAt DESC")
    List<Order> findByUserTelegramId(@Param("tid") Long telegramId);

    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC")
    List<Order> findAllWithUser();

    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.items i JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    long countByStatus(OrderStatus status);
}
