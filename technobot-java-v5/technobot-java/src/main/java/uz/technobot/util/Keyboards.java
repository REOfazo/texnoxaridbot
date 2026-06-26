package uz.technobot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.technobot.entity.Category;
import uz.technobot.entity.Order;
import uz.technobot.entity.Product;
import uz.technobot.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public final class Keyboards {

    private Keyboards() {}

    // ── helpers ───────────────────────────────────────────────

    private static InlineKeyboardButton btn(String text, String data) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text); b.setCallbackData(data); return b;
    }

    private static InlineKeyboardMarkup inline(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(rows); return m;
    }

    private static List<InlineKeyboardButton> row(InlineKeyboardButton... btns) {
        return List.of(btns);
    }

    // ── Til tanlash ───────────────────────────────────────────

    public static InlineKeyboardMarkup language() {
        return inline(List.of(row(
            btn("🇺🇿 O'zbek", "lang:uz"),
            btn("🇷🇺 Русский", "lang:ru"),
            btn("🇬🇧 English", "lang:en")
        )));
    }

    // ── Asosiy menyu ─────────────────────────────────────────

    public static ReplyKeyboardMarkup mainMenu(String lang) {
        ReplyKeyboardMarkup m = new ReplyKeyboardMarkup();
        m.setResizeKeyboard(true);
        KeyboardRow r1 = new KeyboardRow();
        r1.add(Msg.get("catalog_btn", lang)); r1.add(Msg.get("cart_btn", lang));
        KeyboardRow r2 = new KeyboardRow();
        r2.add(Msg.get("orders_btn", lang));  r2.add(Msg.get("search_btn", lang));
        m.setKeyboard(List.of(r1, r2));
        return m;
    }

    // ── Kategoriyalar ─────────────────────────────────────────

    public static InlineKeyboardMarkup categories(List<Category> cats, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category c : cats)
            rows.add(row(btn(c.getEmoji() + " " + c.getName(lang), "cat:" + c.getId())));
        return inline(rows);
    }

    // ── Mahsulot ro'yxati ─────────────────────────────────────

    public static InlineKeyboardMarkup productList(List<Product> products,
                                                    int page, int totalPages,
                                                    Long categoryId, String lang) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Product p : products)
            rows.add(row(btn(p.getName(lang) + " — " + p.formattedPrice(), "prod:" + p.getId())));
        if (totalPages > 1) {
            List<InlineKeyboardButton> nav = new ArrayList<>();
            if (page > 1)          nav.add(btn("◀️", "page:" + categoryId + ":" + (page-1)));
            nav.add(btn(page + "/" + totalPages, "noop"));
            if (page < totalPages) nav.add(btn("▶️", "page:" + categoryId + ":" + (page+1)));
            rows.add(nav);
        }
        rows.add(row(btn(Msg.get("back", lang), "back:categories")));
        return inline(rows);
    }

    // ── Mahsulot detail ───────────────────────────────────────

    public static InlineKeyboardMarkup productDetail(Long productId, boolean inCart, String lang) {
        String label = inCart ? "✅ " + Msg.get("already_in_cart", lang)
                              : "🛒 " + Msg.get("add_to_cart", lang);
        return inline(List.of(
            row(btn(label, "addcart:" + productId)),
            row(btn("🛒 " + Msg.get("cart_btn", lang), "goto:cart"),
                btn(Msg.get("back", lang), "back:products"))
        ));
    }

    // ── Savat ─────────────────────────────────────────────────

    public static InlineKeyboardMarkup cart(String lang) {
        return inline(List.of(
            row(btn("✅ " + Msg.get("checkout", lang), "goto:checkout")),
            row(btn("🗑 " + Msg.get("clear_cart", lang), "cart:clear")),
            row(btn(Msg.get("back", lang), "back:main"))
        ));
    }

    // ── Telefon ───────────────────────────────────────────────

    public static ReplyKeyboardMarkup phone(String lang) {
        KeyboardButton c = new KeyboardButton();
        c.setText("📞 " + Msg.get("share_contact", lang));
        c.setRequestContact(true);
        KeyboardRow r1 = new KeyboardRow(); r1.add(c);
        KeyboardRow r2 = new KeyboardRow(); r2.add(Msg.get("cancel", lang));
        ReplyKeyboardMarkup m = new ReplyKeyboardMarkup();
        m.setResizeKeyboard(true); m.setOneTimeKeyboard(true);
        m.setKeyboard(List.of(r1, r2)); return m;
    }

    // ── To'lov ────────────────────────────────────────────────

    public static InlineKeyboardMarkup payment(String lang) {
        return inline(List.of(
            row(btn("💳 Click",    "pay:CLICK")),
            row(btn("💳 Payme",   "pay:PAYME")),
            row(btn("💵 Naqd",    "pay:CASH")),
            row(btn(Msg.get("back", lang), "back:address"))
        ));
    }

    // ── Buyurtma tasdiqlash ───────────────────────────────────

    public static InlineKeyboardMarkup orderConfirm(String lang) {
        return inline(List.of(row(
            btn("✅ " + Msg.get("confirm", lang), "order:confirm"),
            btn("❌ " + Msg.get("cancel",  lang), "order:cancel")
        )));
    }

    // ═════════════════════════════════════════════════════════
    // ADMIN
    // ═════════════════════════════════════════════════════════

    public static InlineKeyboardMarkup adminMenu() {
        return inline(List.of(
            row(btn("➕ Mahsulot qo'shish",   "admin:add_product"),
                btn("📂 Kategoriya qo'shish", "admin:add_category")),
            row(btn("📦 Mahsulotlar",         "admin:products"),
                btn("🗂 Kategoriyalar",        "admin:categories")),
            row(btn("📋 Buyurtmalar",         "admin:orders"),
                btn("📊 Statistika",          "admin:stats")),
            row(btn("🏠 Asosiy menyu",        "admin:exit"))
        ));
    }

    // ── Admin: kategoriyalar ro'yxati ─────────────────────────

    public static InlineKeyboardMarkup adminCategoryList(List<Category> cats) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category c : cats) {
            String status = c.isActive() ? "" : " ❌";
            rows.add(row(btn(c.getEmoji() + " " + c.getNameUz() + status,
                             "admincat_detail:" + c.getId())));
        }
        rows.add(row(btn("⬅️ Orqaga", "admin:menu")));
        return inline(rows);
    }

    // ── Admin: kategoriya detail (tahrirlash tugmalari) ───────

    public static InlineKeyboardMarkup adminCategoryDetail(Category cat) {
        String toggleLabel = cat.isActive() ? "🚫 O'chirish" : "✅ Faollashtirish";
        return inline(List.of(
            row(btn("✏️ Nom (Uz)",   "editcat_nameuz:" + cat.getId()),
                btn("✏️ Nom (Ru)",   "editcat_nameru:" + cat.getId())),
            row(btn("😀 Emoji",       "editcat_emoji:"  + cat.getId())),
            row(btn(toggleLabel,      "editcat_toggle:" + cat.getId())),
            row(btn("⬅️ Orqaga",     "admin:categories"))
        ));
    }

    // ── Admin: kategoriya tanlash (mahsulot qo'shish uchun) ───

    public static InlineKeyboardMarkup categoriesForAdmin(List<Category> cats) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Category c : cats)
            rows.add(row(btn(c.getEmoji() + " " + c.getNameUz(), "admcat:" + c.getId())));
        rows.add(row(btn("⬅️ Bekor qilish", "admin:menu")));
        return inline(rows);
    }

    // ── Admin: mahsulotlar ro'yxati ───────────────────────────

    public static InlineKeyboardMarkup adminProductList(List<Product> products,
                                                         int page, int totalPages,
                                                         Long catId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Product p : products) {
            String status = p.isActive() ? "" : " ❌";
            String label  = p.getNameUz() + " — " + p.formattedPrice() + status;
            rows.add(row(btn(label, "adminprod_detail:" + p.getId())));
        }
        if (totalPages > 1) {
            List<InlineKeyboardButton> nav = new ArrayList<>();
            if (page > 1)          nav.add(btn("◀️", "adminprods:" + catId + ":" + (page-1)));
            nav.add(btn(page + "/" + totalPages, "noop"));
            if (page < totalPages) nav.add(btn("▶️", "adminprods:" + catId + ":" + (page+1)));
            rows.add(nav);
        }
        rows.add(row(btn("⬅️ Kategoriyalarga", "admin:categories")));
        return inline(rows);
    }

    // ── Admin: mahsulot detail (tahrirlash tugmalari) ─────────

    public static InlineKeyboardMarkup adminProductDetail(Product p) {
        String toggleLabel = p.isActive() ? "🚫 O'chirish" : "✅ Faollashtirish";
        Long id = p.getId();
        return inline(List.of(
            row(btn("✏️ Nom (Uz)",  "editprod_nameuz:" + id),
                btn("✏️ Nom (Ru)",  "editprod_nameru:" + id)),
            row(btn("💰 Narx",      "editprod_price:"  + id),
                btn("📦 Son",       "editprod_stock:"  + id)),
            row(btn("🏷 Brend",     "editprod_brand:"  + id),
                btn("🖼 Rasm",      "editprod_image:"  + id)),
            row(btn(toggleLabel,    "editprod_toggle:" + id)),
            row(btn("⬅️ Orqaga",   "back_adminprods:" + p.getCategory().getId()))
        ));
    }

    // ── Admin: buyurtmalar ────────────────────────────────────

    public static InlineKeyboardMarkup adminOrders(List<Order> orders, int page, int total) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Order o : orders) {
            String label = String.format("%s #%d — %s — %,.0f so'm",
                o.getStatus().emoji(), o.getId(),
                o.getUser().getFullName(), o.getTotalAmount());
            rows.add(row(btn(label, "adminorder:" + o.getId())));
        }
        if (total > 1) {
            List<InlineKeyboardButton> nav = new ArrayList<>();
            if (page > 1)     nav.add(btn("◀️", "adminorders:" + (page-1)));
            nav.add(btn(page + "/" + total, "noop"));
            if (page < total) nav.add(btn("▶️", "adminorders:" + (page+1)));
            rows.add(nav);
        }
        rows.add(row(btn("⬅️ Orqaga", "admin:menu")));
        return inline(rows);
    }

    public static InlineKeyboardMarkup adminOrderDetail(Long orderId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (OrderStatus s : OrderStatus.values())
            rows.add(row(btn(s.emoji() + " " + s.labelUz(), "setstatus:" + orderId + ":" + s.name())));
        rows.add(row(btn("⬅️ Buyurtmalarga", "admin:orders")));
        return inline(rows);
    }

    // ── Klaviaturani olib tashlash ────────────────────────────

    public static ReplyKeyboardRemove remove() {
        ReplyKeyboardRemove r = new ReplyKeyboardRemove();
        r.setRemoveKeyboard(true); return r;
    }
}
