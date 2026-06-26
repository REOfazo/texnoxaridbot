package uz.technobot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.technobot.entity.BotUser;
import uz.technobot.entity.CartItem;
import uz.technobot.entity.Product;
import uz.technobot.repository.BotUserRepository;
import uz.technobot.repository.CartItemRepository;
import uz.technobot.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartRepo;
    private final BotUserRepository  userRepo;
    private final ProductRepository  productRepo;

    @Transactional(readOnly = true)
    public List<CartItem> getCart(Long telegramId) {
        return cartRepo.findByUserTelegramId(telegramId);
    }

    @Transactional(readOnly = true)
    public boolean isInCart(Long telegramId, Long productId) {
        return cartRepo.findByUserTelegramIdAndProductId(telegramId, productId).isPresent();
    }

    @Transactional
    public void addToCart(Long telegramId, Long productId) {
        Optional<CartItem> existing = cartRepo.findByUserTelegramIdAndProductId(telegramId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + 1);
            cartRepo.save(item);
        } else {
            BotUser user = userRepo.findByTelegramId(telegramId)
                    .orElseThrow(() -> new IllegalStateException("User topilmadi: " + telegramId));
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new IllegalStateException("Mahsulot topilmadi: " + productId));
            cartRepo.save(CartItem.builder()
                    .user(user).product(product).quantity(1).build());
        }
    }

    @Transactional
    public void removeItem(Long telegramId, Long productId) {
        cartRepo.findByUserTelegramIdAndProductId(telegramId, productId)
                .ifPresent(cartRepo::delete);
    }

    @Transactional
    public void clearCart(Long telegramId) {
        cartRepo.deleteByUserTelegramId(telegramId);
    }

    public double getTotal(List<CartItem> items) {
        return items.stream().mapToDouble(CartItem::subtotal).sum();
    }

    public String buildCartText(List<CartItem> items, String lang) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem it = items.get(i);
            sb.append(String.format("%d. %s × %d = %,.0f so'm\n",
                    i + 1,
                    it.getProduct().getName(lang),
                    it.getQuantity(),
                    it.subtotal()));
        }
        return sb.toString();
    }
}
