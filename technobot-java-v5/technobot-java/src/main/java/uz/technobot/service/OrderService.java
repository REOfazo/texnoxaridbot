package uz.technobot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.technobot.entity.*;
import uz.technobot.enums.OrderStatus;
import uz.technobot.enums.PaymentMethod;
import uz.technobot.repository.BotUserRepository;
import uz.technobot.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository    orderRepo;
    private final BotUserRepository  userRepo;
    private final CartService        cartService;
    private final ProductService     productService;

    @Transactional
    public Order createOrder(Long telegramId, String phone,
                             String address, PaymentMethod paymentMethod) {
        BotUser user = userRepo.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException("User topilmadi"));

        List<CartItem> cartItems = cartService.getCart(telegramId);
        if (cartItems.isEmpty()) throw new IllegalStateException("Savat bo'sh");

        Order order = Order.builder()
                .user(user).phone(phone)
                .deliveryAddress(address)
                .paymentMethod(paymentMethod)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        double total = 0;
        for (CartItem ci : cartItems) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(ci.getProduct())
                    .quantity(ci.getQuantity())
                    .priceAtOrder(ci.getProduct().getPrice())
                    .build();
            order.getItems().add(oi);
            total += oi.subtotal();
            productService.decreaseStock(ci.getProduct().getId(), ci.getQuantity());
        }
        order.setTotalAmount(total);
        Order saved = orderRepo.save(order);
        cartService.clearCart(telegramId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long telegramId) {
        return orderRepo.findByUserTelegramId(telegramId);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepo.findAllWithUser();
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderWithDetails(Long orderId) {
        return orderRepo.findByIdWithDetails(orderId);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setStatus(status);
            orderRepo.save(o);
        });
    }

    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepo.countByStatus(status);
    }
}
