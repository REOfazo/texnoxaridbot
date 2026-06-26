package uz.technobot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.technobot.bot.handler.*;
import uz.technobot.config.AppConfig;
import uz.technobot.config.BotConfig;
import uz.technobot.enums.UserState;
import uz.technobot.service.SessionService;
import uz.technobot.util.Msg;

@Slf4j
@Component
@RequiredArgsConstructor
public class TechnoBot extends TelegramLongPollingBot {

    private final BotConfig      botConfig;
    private final AppConfig      appConfig;
    private final SessionService session;

    private final StartHandler   startHandler;
    private final CatalogHandler catalogHandler;
    private final CartHandler    cartHandler;
    private final OrderHandler   orderHandler;
    private final AdminHandler   adminHandler;

    @Override public String getBotUsername() { return botConfig.getUsername(); }
    @Override public String getBotToken()    { return botConfig.getToken(); }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if      (update.hasCallbackQuery()) handleCallback(update);
            else if (update.hasMessage())       handleMessage(update);
        } catch (Exception e) {
            log.error("Dispatcher xatosi: {}", e.getMessage(), e);
        }
    }

    // ─── CALLBACK ─────────────────────────────────────────────

    private void handleCallback(Update update) {
        String    data   = update.getCallbackQuery().getData();
        Long      userId = update.getCallbackQuery().getFrom().getId();
        UserState state  = session.getState(userId);

        if ("noop".equals(data)) { ack(update); return; }

        // ── Til ──────────────────────────────────────────────
        if (data.startsWith("lang:")) {
            startHandler.handleLanguageSelect(update, this);

        // ── Admin asosiy ─────────────────────────────────────
        } else if (data.startsWith("admin:")) {
            adminHandler.handleCallback(update, this);

        // ── Admin buyurtmalar ─────────────────────────────────
        } else if (data.startsWith("adminorder:")) {
            adminHandler.handleOrderView(update, this);
        } else if (data.startsWith("adminorders:")) {
            adminHandler.handleOrdersPage(update, this);
        } else if (data.startsWith("setstatus:")) {
            adminHandler.handleSetStatus(update, this);

        // ── Admin kategoriya ──────────────────────────────────
        } else if (data.startsWith("admincat_detail:")) {
            adminHandler.handleCategoryDetail(update, this);
        } else if (data.startsWith("editcat_")) {
            adminHandler.handleCategoryEdit(update, this);

        // ── Admin mahsulot ────────────────────────────────────
        } else if (data.startsWith("adminprod_detail:")) {
            adminHandler.handleProductDetail(update, this);
        } else if (data.startsWith("adminprods:")) {
            adminHandler.handleProductsPage(update, this);
        } else if (data.startsWith("editprod_")) {
            adminHandler.handleProductEdit(update, this);
        } else if (data.startsWith("back_adminprods:")) {
            adminHandler.handleBackToProducts(update, this);

        // ── admcat — ikki holat: qo'shish yoki ko'rish ────────
        } else if (data.startsWith("admcat:")) {
            adminHandler.handleAdmCat(update, this);

        // ── Katalog ───────────────────────────────────────────
        } else if (data.startsWith("cat:")) {
            catalogHandler.showProducts(update, this);
        } else if (data.startsWith("page:")) {
            catalogHandler.handlePagination(update, this);
        } else if (data.startsWith("prod:")) {
            catalogHandler.showProductDetail(update, this);

        // ── Savat ─────────────────────────────────────────────
        } else if (data.startsWith("addcart:")) {
            cartHandler.addToCart(update, this);
        } else if (data.startsWith("cart:")) {
            cartHandler.handleCartAction(update, this);
        } else if ("goto:cart".equals(data)) {
            cartHandler.showCart(update, this);

        // ── Buyurtma ──────────────────────────────────────────
        } else if ("goto:checkout".equals(data)) {
            orderHandler.startCheckout(update, this);
        } else if (data.startsWith("pay:")) {
            orderHandler.receivePayment(update, this);
        } else if (data.startsWith("order:")) {
            orderHandler.handleOrderAction(update, this);

        // ── Orqaga ────────────────────────────────────────────
        } else if (data.startsWith("back:")) {
            handleBack(update, data.substring(5));
        }
    }

    // ─── MESSAGE ──────────────────────────────────────────────

    private void handleMessage(Update update) {
        var   msg    = update.getMessage();
        Long  userId = msg.getFrom().getId();
        UserState state  = session.getState(userId);
        String    lang   = session.getLang(userId);

        // Commands
        if (msg.hasText()) {
            String text = msg.getText();
            if (text.startsWith("/start")) { startHandler.handle(update, this); return; }
            if (text.startsWith("/admin")) { adminHandler.showMenu(update, this); return; }
        }

        // Contact
        if (msg.hasContact() && state == UserState.WAITING_PHONE) {
            orderHandler.receivePhone(update, this); return;
        }

        // Photo — qo'shish yoki tahrirlash
        if (msg.hasPhoto()) {
            if (state == UserState.ADMIN_WAITING_IMAGE) {
                adminHandler.receiveImage(update, this); return;
            }
            if (state == UserState.ADMIN_EDIT_PROD_IMAGE) {
                adminHandler.receiveEditProdImage(update, this); return;
            }
        }

        if (!msg.hasText()) return;
        String text = msg.getText().trim();

        // State machine
        switch (state) {
            // Foydalanuvchi
            case WAITING_SEARCH_QUERY      -> catalogHandler.handleSearch(update, this);
            case WAITING_PHONE             -> orderHandler.receivePhoneText(update, this);
            case WAITING_ADDRESS           -> orderHandler.receiveAddress(update, this);

            // Admin — mahsulot qo'shish
            case ADMIN_WAITING_NAME_UZ     -> adminHandler.receiveNameUz(update, this);
            case ADMIN_WAITING_NAME_RU     -> adminHandler.receiveNameRu(update, this);
            case ADMIN_WAITING_PRICE       -> adminHandler.receivePrice(update, this);
            case ADMIN_WAITING_STOCK       -> adminHandler.receiveStock(update, this);
            case ADMIN_WAITING_BRAND       -> adminHandler.receiveBrand(update, this);
            case ADMIN_WAITING_IMAGE       -> {
                if ("-".equals(text)) adminHandler.receiveImage(update, this);
            }

            // Admin — mahsulot tahrirlash
            case ADMIN_EDIT_PROD_NAME_UZ   -> adminHandler.receiveEditProdNameUz(update, this);
            case ADMIN_EDIT_PROD_NAME_RU   -> adminHandler.receiveEditProdNameRu(update, this);
            case ADMIN_EDIT_PROD_PRICE     -> adminHandler.receiveEditProdPrice(update, this);
            case ADMIN_EDIT_PROD_STOCK     -> adminHandler.receiveEditProdStock(update, this);
            case ADMIN_EDIT_PROD_BRAND     -> adminHandler.receiveEditProdBrand(update, this);
            case ADMIN_EDIT_PROD_IMAGE     -> {
                if ("-".equals(text)) adminHandler.receiveEditProdImage(update, this);
            }

            // Admin — kategoriya qo'shish
            case ADMIN_CAT_WAITING_NAME_UZ -> adminHandler.receiveCatNameUz(update, this);
            case ADMIN_CAT_WAITING_NAME_RU -> adminHandler.receiveCatNameRu(update, this);
            case ADMIN_CAT_WAITING_EMOJI   -> adminHandler.receiveCatEmoji(update, this);

            // Admin — kategoriya tahrirlash
            case ADMIN_EDIT_CAT_NAME_UZ    -> adminHandler.receiveCatEditNameUz(update, this);
            case ADMIN_EDIT_CAT_NAME_RU    -> adminHandler.receiveCatEditNameRu(update, this);
            case ADMIN_EDIT_CAT_EMOJI      -> adminHandler.receiveCatEditEmoji(update, this);

            default -> handleMenuButtons(update, text, lang);
        }
    }

    // ─── MENU BUTTONS ─────────────────────────────────────────

    private void handleMenuButtons(Update update, String text, String lang) {
        if      (isBtn(text, "catalog_btn")) catalogHandler.showCategories(update, this);
        else if (isBtn(text, "cart_btn"))    cartHandler.showCart(update, this);
        else if (isBtn(text, "search_btn"))  catalogHandler.promptSearch(update, this);
        else if (isBtn(text, "orders_btn"))  orderHandler.showMyOrders(update, this);
    }

    private boolean isBtn(String text, String key) {
        return text.equalsIgnoreCase(Msg.get(key, "uz"))
            || text.equalsIgnoreCase(Msg.get(key, "ru"))
            || text.equalsIgnoreCase(Msg.get(key, "en"));
    }

    // ─── BACK ─────────────────────────────────────────────────

    private void handleBack(Update update, String target) {
        switch (target) {
            case "main"       -> startHandler.showMainMenu(update, this);
            case "categories" -> catalogHandler.showCategories(update, this);
            case "products"   -> catalogHandler.showCurrentCategoryProducts(update, this);
        }
    }

    private void ack(Update update) {
        try {
            AnswerCallbackQuery a = new AnswerCallbackQuery();
            a.setCallbackQueryId(update.getCallbackQuery().getId());
            execute(a);
        } catch (Exception ignore) {}
    }
}
