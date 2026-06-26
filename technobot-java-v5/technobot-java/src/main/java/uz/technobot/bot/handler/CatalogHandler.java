package uz.technobot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import uz.technobot.entity.Category;
import uz.technobot.entity.Product;
import uz.technobot.enums.UserState;
import uz.technobot.service.CartService;
import uz.technobot.service.ProductService;
import uz.technobot.service.SessionService;
import uz.technobot.util.Keyboards;
import uz.technobot.util.Msg;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogHandler extends BotHelper {

    private final ProductService productService;
    private final CartService    cartService;
    private final SessionService session;

    // ── Kategoriyalar ─────────────────────────────────────────

    public void showCategories(Update update, AbsSender bot) {
        Long userId = uid(update);
        Long chatId = cid(update);
        String lang = session.getLang(userId);
        if (update.hasCallbackQuery()) answerCallback(bot, update.getCallbackQuery().getId(), null);

        List<Category> cats = productService.getActiveCategories();
        if (cats.isEmpty()) {
            send(bot, chatId, "📦 Hozircha kategoriya yo'q.", Keyboards.mainMenu(lang));
            return;
        }
        session.setState(userId, UserState.BROWSING_CATEGORIES);
        send(bot, chatId, Msg.get("choose_cat", lang), Keyboards.categories(cats, lang));
    }

    // ── Mahsulotlar ───────────────────────────────────────────

    public void showProducts(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        answerCallback(bot, query.getId(), null);

        Long catId = Long.parseLong(query.getData().split(":")[1]);
        session.setData(userId, "catId", catId);
        session.setData(userId, "page", 1);
        session.setState(userId, UserState.BROWSING_PRODUCTS);

        renderProductList(bot, chatId, userId, catId, 1, lang,
                query.getMessage().getMessageId());
    }

    public void handlePagination(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        answerCallback(bot, query.getId(), null);

        // "page:catId:pageNum"
        String[] parts = query.getData().split(":");
        Long catId = Long.parseLong(parts[1]);
        int  page  = Integer.parseInt(parts[2]);
        session.setData(userId, "catId", catId);
        session.setData(userId, "page", page);

        renderProductList(bot, chatId, userId, catId, page, lang,
                query.getMessage().getMessageId());
    }

    public void showCurrentCategoryProducts(Update update, AbsSender bot) {
        Long userId = uid(update);
        Long chatId = cid(update);
        String lang = session.getLang(userId);
        if (update.hasCallbackQuery()) answerCallback(bot, update.getCallbackQuery().getId(), null);

        Long catId = session.getData(userId, "catId");
        Integer page = session.getData(userId, "page");
        if (catId == null) { showCategories(update, bot); return; }

        Integer msgId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getMessage().getMessageId() : null;
        renderProductList(bot, chatId, userId, catId, page != null ? page : 1, lang, msgId);
    }

    private void renderProductList(AbsSender bot, Long chatId, Long userId,
                                    Long catId, int page, String lang, Integer editMsgId) {
        Page<Product> productPage = productService.getProductsByCategory(catId, page);
        Category cat = productService.getCategory(catId).orElse(null);
        String catName = cat != null ? cat.getEmoji() + " " + cat.getName(lang) : "";

        if (productPage.isEmpty()) {
            send(bot, chatId, "📦 Bu kategoriyada mahsulot yo'q.");
            return;
        }

        String text = "*" + catName + "*\n" + productPage.getTotalElements() + " ta mahsulot";
        var keyboard = Keyboards.productList(productPage.getContent(), page,
                productPage.getTotalPages(), catId, lang);

        if (editMsgId != null) {
            editText(bot, chatId, editMsgId, text, keyboard);
        } else {
            send(bot, chatId, text, keyboard);
        }
    }

    // ── Mahsulot detail ───────────────────────────────────────

    public void showProductDetail(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = session.getLang(userId);
        answerCallback(bot, query.getId(), null);

        Long productId = Long.parseLong(query.getData().split(":")[1]);
        session.setData(userId, "productId", productId);

        productService.getProduct(productId).ifPresentOrElse(p -> {
            boolean inCart = cartService.isInCart(userId, productId);

            String text = String.format(
                    "📱 *%s*\n\n%s\n\n" +
                    "💰 Narx: *%s*\n" +
                    "🏷 Brend: %s\n" +
                    "📦 Mavjud: %d ta",
                    p.getName(lang),
                    p.getDescription(lang).isBlank() ? "" : p.getDescription(lang) + "\n",
                    p.formattedPrice(),
                    p.getBrand() != null ? p.getBrand() : "—",
                    p.getStock()
            );

            var keyboard = Keyboards.productDetail(productId, inCart, lang);

            // Eski xabarni o'chirish
            deleteMsg(bot, chatId, query.getMessage().getMessageId());

            if (p.getImageFileId() != null && !p.getImageFileId().isBlank()) {
                // Rasmli xabar yuborish
                sendPhoto(bot, chatId, p.getImageFileId(), text, keyboard);
            } else {
                send(bot, chatId, text, keyboard);
            }

        }, () -> send(bot, chatId, "❌ Mahsulot topilmadi."));
    }

    // ── Qidiruv ───────────────────────────────────────────────

    public void promptSearch(Update update, AbsSender bot) {
        Long userId = uid(update);
        Long chatId = cid(update);
        String lang = session.getLang(userId);
        session.setState(userId, UserState.WAITING_SEARCH_QUERY);
        send(bot, chatId, Msg.get("search_prompt", lang), Keyboards.remove());
    }

    public void handleSearch(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String lang = session.getLang(userId);
        String query = update.getMessage().getText().trim();

        if (query.length() < 2) {
            send(bot, chatId, "❌ Kamida 2 ta harf kiriting:");
            return;
        }

        List<Product> results = productService.search(query);
        session.setState(userId, UserState.IDLE);

        if (results.isEmpty()) {
            send(bot, chatId, "🔍 *" + query + "* — topilmadi.", Keyboards.mainMenu(lang));
            return;
        }

        var keyboard = Keyboards.productList(results, 1, 1, null, lang);
        send(bot, chatId, "🔍 *" + query + "* — " + results.size() + " ta natija:", keyboard);
    }

    private Long uid(Update u) {
        return u.hasCallbackQuery() ? u.getCallbackQuery().getFrom().getId()
                                    : u.getMessage().getFrom().getId();
    }
    private Long cid(Update u) {
        return u.hasCallbackQuery() ? u.getCallbackQuery().getMessage().getChatId()
                                    : u.getMessage().getChatId();
    }
}
