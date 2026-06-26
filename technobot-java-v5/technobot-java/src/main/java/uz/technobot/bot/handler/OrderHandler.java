package uz.technobot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import uz.technobot.entity.CartItem;
import uz.technobot.entity.Order;
import uz.technobot.enums.OrderStatus;
import uz.technobot.enums.PaymentMethod;
import uz.technobot.enums.UserState;
import uz.technobot.service.CartService;
import uz.technobot.service.OrderService;
import uz.technobot.service.SessionService;
import uz.technobot.service.UserService;
import uz.technobot.util.Keyboards;
import uz.technobot.util.Msg;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderHandler extends BotHelper {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;
    private final SessionService session;

    // ── 1. Checkout boshlash ───────────────────────────────────

    public void startCheckout(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        answerCallback(bot, query.getId(), null);

        List<CartItem> items = cartService.getCart(userId);
        if (items.isEmpty()) {
            send(bot, chatId, Msg.get("cart_empty", lang), Keyboards.mainMenu(lang));
            return;
        }

        session.setState(userId, UserState.WAITING_PHONE);
        send(bot, chatId, Msg.get("enter_phone", lang), Keyboards.phone(lang));
    }

    // ── 2a. Telefon - contact orqali ──────────────────────────

    public void receivePhone(Update update, AbsSender bot) {
        var msg = update.getMessage();
        Long userId = msg.getFrom().getId();
        Long chatId = msg.getChatId();
        String lang = session.getLang(userId);

        String phone = msg.getContact().getPhoneNumber();
        if (!phone.startsWith("+")) phone = "+" + phone;

        savePhoneAndAskAddress(bot, userId, chatId, phone, lang);
    }

    // ── 2b. Telefon - matn orqali ─────────────────────────────

    public void receivePhoneText(Update update, AbsSender bot) {
        var msg = update.getMessage();
        Long userId = msg.getFrom().getId();
        Long chatId = msg.getChatId();
        String lang = session.getLang(userId);
        String text = msg.getText().trim();

        if (text.equals(Msg.get("cancel", lang))) {
            session.setState(userId, UserState.IDLE);
            send(bot, chatId, Msg.get("main_menu", lang), Keyboards.mainMenu(lang));
            return;
        }

        // Oddiy validatsiya
        String phone = text.replaceAll("\\s+", "");
        if (!phone.matches("^\\+?[0-9]{9,15}$")) {
            send(bot, chatId, Msg.get("phone_invalid", lang), Keyboards.phone(lang));
            return;
        }

        savePhoneAndAskAddress(bot, userId, chatId, phone, lang);
    }

    private void savePhoneAndAskAddress(AbsSender bot, Long userId,
                                         Long chatId, String phone, String lang) {
        session.setData(userId, "phone", phone);
        userService.updatePhone(userId, phone);
        session.setState(userId, UserState.WAITING_ADDRESS);
        send(bot, chatId, Msg.get("enter_address", lang), Keyboards.remove());
    }

    // ── 3. Manzil ──────────────────────────────────────────────

    public void receiveAddress(Update update, AbsSender bot) {
        var msg = update.getMessage();
        Long userId = msg.getFrom().getId();
        Long chatId = msg.getChatId();
        String lang = session.getLang(userId);
        String address = msg.getText().trim();

        if (address.length() < 5) {
            send(bot, chatId, "❌ Manzil juda qisqa. Qayta kiriting:");
            return;
        }

        session.setData(userId, "address", address);
        session.setState(userId, UserState.WAITING_PAYMENT_METHOD);
        send(bot, chatId, Msg.get("choose_payment", lang), Keyboards.payment(lang));
    }

    // ── 4. To'lov usuli ────────────────────────────────────────

    public void receivePayment(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        answerCallback(bot, query.getId(), null);

        String methodStr = query.getData().split(":")[1]; // "pay:CLICK" → "CLICK"
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr);
        } catch (IllegalArgumentException e) {
            send(bot, chatId, Msg.get("error", lang));
            return;
        }

        session.setData(userId, "payment", method);
        session.setState(userId, UserState.WAITING_ORDER_CONFIRM);

        // Xulosa ko'rsatish
        showOrderSummary(bot, userId, chatId, method, lang);
    }

    private void showOrderSummary(AbsSender bot, Long userId,
                                   Long chatId, PaymentMethod method, String lang) {
        List<CartItem> items = cartService.getCart(userId);
        String itemsText = cartService.buildCartText(items, lang);
        double total = cartService.getTotal(items);
        String phone = session.getData(userId, "phone");
        String address = session.getData(userId, "address");

        String text = Msg.get("order_summary", lang,
                "items",   itemsText,
                "total",   String.format("%,.0f so'm", total),
                "phone",   phone,
                "address", address,
                "payment", method.label());

        send(bot, chatId, text, Keyboards.orderConfirm(lang));
    }

    // ── 5. Tasdiqlash / Bekor qilish ───────────────────────────

    public void handleOrderAction(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        String action = query.getData().split(":")[1]; // "order:confirm" → "confirm"
        answerCallback(bot, query.getId(), null);

        if ("confirm".equals(action)) {
            confirmOrder(bot, userId, chatId, lang);
        } else {
            cancelOrder(bot, userId, chatId, lang);
        }
    }

    private void confirmOrder(AbsSender bot, Long userId, Long chatId, String lang) {
        String phone = session.getData(userId, "phone");
        String address = session.getData(userId, "address");
        PaymentMethod method = session.getData(userId, "payment");

        try {
            Order order = orderService.createOrder(userId, phone, address, method);
            session.clearData(userId);
            session.setState(userId, UserState.IDLE);
            send(bot, chatId,
                    Msg.get("order_ok", lang, "id", order.getId().toString()),
                    Keyboards.mainMenu(lang));
        } catch (Exception e) {
            send(bot, chatId, Msg.get("error", lang), Keyboards.mainMenu(lang));
        }
    }

    private void cancelOrder(AbsSender bot, Long userId, Long chatId, String lang) {
        session.clearData(userId);
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, Msg.get("order_cancelled", lang), Keyboards.mainMenu(lang));
    }

    // ── Mening buyurtmalarim ───────────────────────────────────

    public void showMyOrders(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String lang = session.getLang(userId);

        List<Order> orders = orderService.getUserOrders(userId);
        if (orders.isEmpty()) {
            send(bot, chatId, Msg.get("my_orders_empty", lang), Keyboards.mainMenu(lang));
            return;
        }

        StringBuilder sb = new StringBuilder(Msg.get("my_orders_header", lang));
        for (Order o : orders) {
            sb.append(String.format("%s #%d — %,.0f so'm — %s\n",
                    o.getStatus().emoji(),
                    o.getId(),
                    o.getTotalAmount(),
                    o.getStatus().labelUz()));
        }
        send(bot, chatId, sb.toString(), Keyboards.mainMenu(lang));
    }
}
