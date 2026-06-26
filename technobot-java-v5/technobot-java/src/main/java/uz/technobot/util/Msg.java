package uz.technobot.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Msg — ko'p tilli xabarlar uchun yordamchi klass.
 * Ishlatish: Msg.get("welcome", "uz", "name", "Ali")
 */
public final class Msg {

    private Msg() {}

    private static final Map<String, Map<String, String>> DATA = new HashMap<>();

    static {
        // ── UZ ──────────────────────────────────────────────────
        Map<String, String> uz = new HashMap<>();
        uz.put("choose_lang",    "🌐 Tilni tanlang / Выберите язык / Choose language:");
        uz.put("welcome",        "👋 Salom, {name}!\n\n🛒 TechnoBot — Bitavoy texnika do'koni.\n\nNimani qidiryapsiz?");
        uz.put("main_menu",      "🏠 Asosiy menyu:");
        uz.put("catalog_btn",    "📦 Katalog");
        uz.put("cart_btn",       "🛒 Savat");
        uz.put("orders_btn",     "📋 Buyurtmalarim");
        uz.put("search_btn",     "🔍 Qidirish");
        uz.put("back",           "⬅️ Orqaga");
        uz.put("cancel",         "❌ Bekor qilish");
        uz.put("confirm",        "✅ Tasdiqlash");
        uz.put("error",          "⚠️ Xatolik. Qayta urinib ko'ring.");
        uz.put("not_found",      "❌ Topilmadi.");
        // Katalog
        uz.put("choose_cat",     "📂 Kategoriyani tanlang:");
        uz.put("no_products",    "📦 Bu kategoriyada mahsulot yo'q.");
        uz.put("search_prompt",  "🔍 Qidirish so'zini kiriting:");
        uz.put("no_results",     "🔍 Hech narsa topilmadi.");
        uz.put("add_to_cart",    "🛒 Savatga qo'shish");
        uz.put("already_in_cart","✅ Savatda mavjud");
        uz.put("out_of_stock",   "❌ Mavjud emas");
        uz.put("added_to_cart",  "✅ Savatga qo'shildi!");
        uz.put("product_detail", "📱 *{name}*\n\n{desc}\n\n💰 Narx: *{price}*\n🏷️ Brend: {brand}\n📦 Mavjud: {stock} ta");
        // Savat
        uz.put("cart_empty",     "🛒 Savatingiz bo'sh.\n\nKatalogdan mahsulot qo'shing.");
        uz.put("cart_header",    "🛒 *Savat* ({count} ta mahsulot):\n\n{items}\n━━━━━━━━━━━━━\n💰 Jami: *{total}*");
        uz.put("cart_item_line", "{num}. {name} × {qty} = {sub}");
        uz.put("clear_cart",     "🗑️ Tozalash");
        uz.put("cart_cleared",   "✅ Savat tozalandi.");
        uz.put("checkout",       "✅ Buyurtma berish");
        // Buyurtma
        uz.put("enter_phone",    "📞 Telefon raqamingizni kiriting:\nMisol: +998901234567\n\nYoki kontaktni yuboring 👇");
        uz.put("share_contact",  "📞 Raqamni ulashish");
        uz.put("phone_invalid",  "❌ Noto'g'ri raqam. Qayta kiriting:");
        uz.put("enter_address",  "🏠 Yetkazib berish manzilini kiriting:");
        uz.put("choose_payment", "💳 To'lov usulini tanlang:");
        uz.put("pay_click",      "💳 Click");
        uz.put("pay_payme",      "💳 Payme");
        uz.put("pay_cash",       "💵 Naqd pul");
        uz.put("order_summary",  "📋 *Buyurtma xulosasi*\n\n{items}\n━━━━━━━━━━━━━\n💰 Jami: *{total}*\n📞 Tel: {phone}\n🏠 Manzil: {address}\n💳 To'lov: {payment}\n\nTasdiqlaysizmi?");
        uz.put("order_ok",       "🎉 Buyurtmangiz qabul qilindi!\n\n🆔 Buyurtma raqami: *#{id}*\n\nTez orada siz bilan bog'lanamiz.");
        uz.put("order_cancelled","❌ Buyurtma bekor qilindi.");
        // Mening buyurtmalarim
        uz.put("my_orders_empty","📋 Hali buyurtma yo'q.");
        uz.put("my_orders_header","📋 *Mening buyurtmalarim:*\n");
        uz.put("order_line",     "{emoji} #{id} — {total} — {status}");
        // Admin
        uz.put("admin_menu",     "👨‍💼 *Admin panel*\n\nNimani qilmoqchisiz?");
        uz.put("admin_add_prod", "➕ Mahsulot qo'shish");
        uz.put("admin_orders",   "📋 Barcha buyurtmalar");
        uz.put("admin_no_access","⛔ Ruxsat yo'q.");
        uz.put("choose_cat_4prod","📂 Mahsulot kategoriyasini tanlang:");
        uz.put("enter_name_uz",  "📝 Mahsulot nomi (o'zbekcha):");
        uz.put("enter_name_ru",  "📝 Mahsulot nomi (ruscha):");
        uz.put("enter_price",    "💰 Narxi (so'mda, faqat raqam):\nMisol: 1500000");
        uz.put("enter_stock",    "📦 Soni (dona):");
        uz.put("send_image",     "🖼️ Rasm yuboring (o'tkazib yuborish uchun - kiriting):");
        uz.put("enter_brand",    "🏷️ Brendni kiriting (o'tkazib yuborish uchun - kiriting):");
        uz.put("price_invalid",  "❌ Noto'g'ri narx. Faqat raqam kiriting:");
        uz.put("stock_invalid",  "❌ Noto'g'ri son. Faqat raqam kiriting:");
        uz.put("prod_added",     "✅ Mahsulot muvaffaqiyatli qo'shildi!\n\n*{name}*\nNarx: {price}\nSon: {stock}");
        uz.put("orders_list",    "📋 *Barcha buyurtmalar ({count} ta):*\n");
        uz.put("change_status",  "🔄 Holat o'zgartirish");
        DATA.put("uz", uz);

        // ── RU ──────────────────────────────────────────────────
        Map<String, String> ru = new HashMap<>(uz); // fallback to uz, override below
        ru.put("choose_lang",    "🌐 Tilni tanlang / Выберите язык / Choose language:");
        ru.put("welcome",        "👋 Привет, {name}!\n\n🛒 TechnoBot — Магазин бытовой техники.\n\nЧто ищете?");
        ru.put("main_menu",      "🏠 Главное меню:");
        ru.put("catalog_btn",    "📦 Каталог");
        ru.put("cart_btn",       "🛒 Корзина");
        ru.put("orders_btn",     "📋 Мои заказы");
        ru.put("search_btn",     "🔍 Поиск");
        ru.put("back",           "⬅️ Назад");
        ru.put("cancel",         "❌ Отмена");
        ru.put("confirm",        "✅ Подтвердить");
        ru.put("error",          "⚠️ Ошибка. Попробуйте снова.");
        ru.put("choose_cat",     "📂 Выберите категорию:");
        ru.put("add_to_cart",    "🛒 В корзину");
        ru.put("added_to_cart",  "✅ Добавлено в корзину!");
        ru.put("cart_empty",     "🛒 Корзина пуста.\n\nДобавьте товары из каталога.");
        ru.put("checkout",       "✅ Оформить заказ");
        ru.put("enter_phone",    "📞 Введите номер телефона:\nПример: +998901234567");
        ru.put("share_contact",  "📞 Поделиться номером");
        ru.put("enter_address",  "🏠 Введите адрес доставки:");
        ru.put("choose_payment", "💳 Выберите способ оплаты:");
        ru.put("order_ok",       "🎉 Заказ принят!\n\n🆔 Номер заказа: *#{id}*\n\nМы свяжемся с вами.");
        ru.put("search_prompt",  "🔍 Введите поисковый запрос:");
        ru.put("no_results",     "🔍 Ничего не найдено.");
        DATA.put("ru", ru);

        // ── EN ──────────────────────────────────────────────────
        Map<String, String> en = new HashMap<>(uz);
        en.put("welcome",        "👋 Hello, {name}!\n\n🛒 TechnoBot — Home appliance store.\n\nWhat are you looking for?");
        en.put("main_menu",      "🏠 Main Menu:");
        en.put("catalog_btn",    "📦 Catalog");
        en.put("cart_btn",       "🛒 Cart");
        en.put("orders_btn",     "📋 My Orders");
        en.put("search_btn",     "🔍 Search");
        en.put("back",           "⬅️ Back");
        en.put("cancel",         "❌ Cancel");
        en.put("confirm",        "✅ Confirm");
        en.put("cart_empty",     "🛒 Your cart is empty.\n\nAdd products from the catalog.");
        en.put("checkout",       "✅ Place Order");
        en.put("order_ok",       "🎉 Order placed!\n\n🆔 Order ID: *#{id}*\n\nWe will contact you shortly.");
        en.put("search_prompt",  "🔍 Enter search query:");
        DATA.put("en", en);
    }

    /** Oddiy tarjima */
    public static String get(String key, String lang) {
        Map<String, String> map = DATA.getOrDefault(lang, DATA.get("uz"));
        return map.getOrDefault(key, DATA.get("uz").getOrDefault(key, "[" + key + "]"));
    }

    /** Placeholder almashtirib tarjima: Msg.get("welcome","uz","name","Ali") */
    public static String get(String key, String lang, String... pairs) {
        String text = get(key, lang);
        if (pairs.length % 2 != 0) return text;
        for (int i = 0; i < pairs.length; i += 2) {
            text = text.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        return text;
    }
}
