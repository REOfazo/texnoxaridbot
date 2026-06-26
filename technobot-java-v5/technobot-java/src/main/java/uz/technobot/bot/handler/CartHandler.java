package uz.technobot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import uz.technobot.entity.CartItem;
import uz.technobot.service.CartService;
import uz.technobot.service.SessionService;
import uz.technobot.util.Keyboards;
import uz.technobot.util.Msg;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartHandler extends BotHelper {

    private final CartService cartService;
    private final SessionService session;

    public void showCart(Update update, AbsSender bot) {
        Long userId = getUserId(update);
        Long chatId = getChatId(update);
        String lang = session.getLang(userId);

        if (update.hasCallbackQuery()) {
            answerCallback(bot, update.getCallbackQuery().getId(), null);
        }

        List<CartItem> items = cartService.getCart(userId);

        if (items.isEmpty()) {
            send(bot, chatId, Msg.get("cart_empty", lang), Keyboards.mainMenu(lang));
            return;
        }

        String itemsText = cartService.buildCartText(items, lang);
        double total = cartService.getTotal(items);

        String text = Msg.get("cart_header", lang,
                "count", String.valueOf(items.size()),
                "items", itemsText,
                "total", String.format("%,.0f so'm", total));

        send(bot, chatId, text, Keyboards.cart(lang));
    }

    public void addToCart(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);

        Long productId = Long.parseLong(query.getData().split(":")[1]);

        try {
            cartService.addToCart(userId, productId);
            answerCallback(bot, query.getId(), Msg.get("added_to_cart", lang));
        } catch (Exception e) {
            answerCallback(bot, query.getId(), Msg.get("error", lang));
        }
    }

    public void handleCartAction(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        String action = query.getData().split(":")[1]; // "cart:clear"

        answerCallback(bot, query.getId(), null);

        if ("clear".equals(action)) {
            cartService.clearCart(userId);
            send(bot, chatId, Msg.get("cart_cleared", lang), Keyboards.mainMenu(lang));
        }
    }

    private Long getUserId(Update u) {
        return u.hasCallbackQuery() ? u.getCallbackQuery().getFrom().getId()
                                    : u.getMessage().getFrom().getId();
    }

    private Long getChatId(Update u) {
        return u.hasCallbackQuery() ? u.getCallbackQuery().getMessage().getChatId()
                                    : u.getMessage().getChatId();
    }
}
