package uz.technobot.bot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import uz.technobot.config.AppConfig;
import uz.technobot.entity.Category;
import uz.technobot.entity.Order;
import uz.technobot.entity.OrderItem;
import uz.technobot.entity.Product;
import uz.technobot.enums.OrderStatus;
import uz.technobot.enums.UserState;
import uz.technobot.service.OrderService;
import uz.technobot.service.ProductService;
import uz.technobot.service.SessionService;
import uz.technobot.util.Keyboards;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminHandler extends BotHelper {

    private static final int ORDERS_PAGE = 8;

    private final ProductService productService;
    private final OrderService   orderService;
    private final SessionService session;
    private final AppConfig      appConfig;

    // ═════════════════════════════════════════════════════════
    // KIRISH TEKSHIRUVI
    // ═════════════════════════════════════════════════════════

    private boolean guard(Long userId, Long chatId, AbsSender bot) {
        if (appConfig.isAdmin(userId)) return true;
        send(bot, chatId, "⛔ Ruxsat yo'q.\n\n`ADMIN_IDS` ni `.env` ga qo'shing.");
        return false;
    }

    // ═════════════════════════════════════════════════════════
    // ASOSIY MENYU
    // ═════════════════════════════════════════════════════════

    public void showMenu(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        if (update.hasCallbackQuery()) ack(bot, update);
        session.setState(userId, UserState.IDLE);
        session.clearData(userId);
        send(bot, chatId,
            "👨‍💼 *Admin panel*\n\nMahsulotlar, kategoriyalar va buyurtmalarni boshqaring.",
            Keyboards.adminMenu());
    }

    // ═════════════════════════════════════════════════════════
    // CALLBACK ROUTER
    // ═════════════════════════════════════════════════════════

    public void handleCallback(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        String action = update.getCallbackQuery().getData().split(":")[1];
        switch (action) {
            case "add_product"  -> startAddProduct(bot, userId, chatId);
            case "add_category" -> startAddCategory(bot, userId, chatId);
            case "products"     -> showCategoryPickForProducts(bot, userId, chatId);
            case "categories"   -> showCategoryList(bot, chatId);
            case "orders"       -> showOrders(bot, userId, chatId, 1);
            case "stats"        -> showStats(bot, chatId);
            case "menu"         -> showMenu(update, bot);
            case "exit"         -> {
                session.setState(userId, UserState.IDLE);
                send(bot, chatId, "🏠 Asosiy menyu.", Keyboards.mainMenu(session.getLang(userId)));
            }
        }
    }

    // ═════════════════════════════════════════════════════════
    // KATEGORIYALAR BOSHQARUVI
    // ═════════════════════════════════════════════════════════

    private void showCategoryList(AbsSender bot, Long chatId) {
        List<Category> all = productService.getAllCategories();
        if (all.isEmpty()) {
            send(bot, chatId, "📂 Kategoriya yo'q.", Keyboards.adminMenu());
            return;
        }
        send(bot, chatId,
            "🗂 *Kategoriyalar* (" + all.size() + " ta)\n\n" +
            "❌ — o'chirilgan, bosing tafsilotni ko'rish:",
            Keyboards.adminCategoryList(all));
    }

    /** Callback: admincat_detail:ID */
    public void handleCategoryDetail(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        productService.getCategory(catId).ifPresentOrElse(cat -> {
            session.setData(userId, "editCatId", catId);
            send(bot, chatId, buildCatInfo(cat), Keyboards.adminCategoryDetail(cat));
        }, () -> send(bot, chatId, "❌ Topilmadi."));
    }

    private String buildCatInfo(Category cat) {
        return String.format("""
            %s *%s* / %s
            ID: `%d`
            Holat: %s
            """,
            cat.getEmoji(), cat.getNameUz(), cat.getNameRu(),
            cat.getId(),
            cat.isActive() ? "✅ Faol" : "❌ O'chirilgan");
    }

    /** editcat_nameuz / editcat_nameru / editcat_emoji / editcat_toggle */
    public void handleCategoryEdit(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);

        String data  = update.getCallbackQuery().getData(); // editcat_nameuz:5
        String[] p   = data.split(":");
        String action = p[0]; // editcat_nameuz
        Long catId    = Long.parseLong(p[1]);
        session.setData(userId, "editCatId", catId);

        switch (action) {
            case "editcat_nameuz" -> {
                session.setState(userId, UserState.ADMIN_EDIT_CAT_NAME_UZ);
                send(bot, chatId, "✏️ Yangi nom *(o'zbekcha)* kiriting:", Keyboards.remove());
            }
            case "editcat_nameru" -> {
                session.setState(userId, UserState.ADMIN_EDIT_CAT_NAME_RU);
                send(bot, chatId, "✏️ Yangi nom *(ruscha)* kiriting:", Keyboards.remove());
            }
            case "editcat_emoji" -> {
                session.setState(userId, UserState.ADMIN_EDIT_CAT_EMOJI);
                send(bot, chatId, "😀 Yangi emoji kiriting:", Keyboards.remove());
            }
            case "editcat_toggle" -> {
                Category cat = productService.toggleCategoryActive(catId);
                send(bot, chatId,
                    "✅ Holat: " + (cat.isActive() ? "Faol ✅" : "O'chirilgan ❌"),
                    Keyboards.adminCategoryDetail(cat));
            }
        }
    }

    public void receiveCatEditNameUz(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long catId  = session.getData(userId, "editCatId");
        Category cat = productService.updateCategoryNameUz(catId, update.getMessage().getText().trim());
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "✅ Nom yangilandi: *" + cat.getNameUz() + "*",
             Keyboards.adminCategoryDetail(cat));
    }

    public void receiveCatEditNameRu(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long catId  = session.getData(userId, "editCatId");
        Category cat = productService.updateCategoryNameRu(catId, update.getMessage().getText().trim());
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "✅ Nom yangilandi: *" + cat.getNameRu() + "*",
             Keyboards.adminCategoryDetail(cat));
    }

    public void receiveCatEditEmoji(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long catId  = session.getData(userId, "editCatId");
        Category cat = productService.updateCategoryEmoji(catId, update.getMessage().getText().trim());
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "✅ Emoji yangilandi: " + cat.getEmoji(),
             Keyboards.adminCategoryDetail(cat));
    }

    // ═════════════════════════════════════════════════════════
    // MAHSULOTLAR BOSHQARUVI
    // ═════════════════════════════════════════════════════════

    private void showCategoryPickForProducts(AbsSender bot, Long userId, Long chatId) {
        List<Category> all = productService.getAllCategories();
        if (all.isEmpty()) { send(bot, chatId, "📂 Kategoriya yo'q.", Keyboards.adminMenu()); return; }
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "📦 Qaysi kategoriya mahsulotlarini ko'rmoqchisiz?",
             Keyboards.categoriesForAdmin(all));
    }

    /** Callback: admcat:ID — ikki holat uchun (qo'shish yoki ko'rish) */
    public void handleAdmCat(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);

        UserState state = session.getState(userId);
        if (state == UserState.ADMIN_WAITING_CATEGORY) {
            // Mahsulot qo'shish flow
            session.setData(userId, "p_catId", catId);
            session.setState(userId, UserState.ADMIN_WAITING_NAME_UZ);
            send(bot, chatId, "📝 Mahsulot nomi *(o'zbekcha)*:", Keyboards.remove());
        } else {
            // Mahsulotlar ro'yxatini ko'rish
            session.setData(userId, "adminCatId", catId);
            showProductList(bot, chatId, catId, 1);
        }
    }

    private void showProductList(AbsSender bot, Long chatId, Long catId, int page) {
        Page<Product> pg = productService.getProductsByCategoryAdmin(catId, page);
        productService.getCategory(catId).ifPresent(cat -> {
            if (pg.isEmpty()) {
                send(bot, chatId, "📦 Bu kategoriyada mahsulot yo'q.", Keyboards.adminMenu());
                return;
            }
            send(bot, chatId,
                "📦 *" + cat.getNameUz() + "* — " + pg.getTotalElements() + " ta:\n\n" +
                "❌ — o'chirilgan mahsulotlar",
                Keyboards.adminProductList(pg.getContent(), page, pg.getTotalPages(), catId));
        });
    }

    /** Callback: adminprods:catId:page — pagination */
    public void handleProductsPage(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        String[] p = update.getCallbackQuery().getData().split(":");
        Long catId = Long.parseLong(p[1]);
        int  page  = Integer.parseInt(p[2]);
        showProductList(bot, chatId, catId, page);
    }

    /** Callback: adminprod_detail:ID */
    public void handleProductDetail(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);

        Long prodId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        productService.getProduct(prodId).ifPresentOrElse(p -> {
            session.setData(userId, "editProdId", prodId);
            String info = buildProdInfo(p);
            if (p.getImageFileId() != null) {
                sendPhoto(bot, chatId, p.getImageFileId(), info, Keyboards.adminProductDetail(p));
            } else {
                send(bot, chatId, info, Keyboards.adminProductDetail(p));
            }
        }, () -> send(bot, chatId, "❌ Mahsulot topilmadi."));
    }

    private String buildProdInfo(Product p) {
        return String.format("""
            📦 *%s*
            RU: %s
            💰 Narx: *%s*
            🔢 Son: *%d* dona
            🏷 Brend: %s
            📂 Kategoriya: %s
            🖼 Rasm: %s
            Holat: %s
            ID: `%d`
            """,
            p.getNameUz(),
            p.getNameRu() != null ? p.getNameRu() : "—",
            p.formattedPrice(),
            p.getStock(),
            p.getBrand() != null ? p.getBrand() : "—",
            p.getCategory().getNameUz(),
            p.getImageFileId() != null ? "✅ Bor" : "❌ Yo'q",
            p.isActive() ? "✅ Faol" : "❌ O'chirilgan",
            p.getId());
    }

    /** editprod_* callback'lar */
    public void handleProductEdit(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);

        String data   = update.getCallbackQuery().getData();
        String[] p    = data.split(":");
        String action = p[0];
        Long prodId   = Long.parseLong(p[1]);
        session.setData(userId, "editProdId", prodId);

        switch (action) {
            case "editprod_nameuz" -> {
                session.setState(userId, UserState.ADMIN_EDIT_PROD_NAME_UZ);
                send(bot, chatId, "✏️ Yangi nom *(o'zbekcha)*:", Keyboards.remove());
            }
            case "editprod_nameru" -> {
                session.setState(userId, UserState.ADMIN_EDIT_PROD_NAME_RU);
                send(bot, chatId, "✏️ Yangi nom *(ruscha)*:", Keyboards.remove());
            }
            case "editprod_price" -> {
                session.setState(userId, UserState.ADMIN_EDIT_PROD_PRICE);
                send(bot, chatId, "💰 Yangi narx *(so'mda)*:", Keyboards.remove());
            }
            case "editprod_stock" -> {
                session.setState(userId, UserState.ADMIN_EDIT_PROD_STOCK);
                send(bot, chatId, "📦 Yangi son *(dona)*:", Keyboards.remove());
            }
            case "editprod_brand" -> {
                session.setState(userId, UserState.ADMIN_EDIT_PROD_BRAND);
                send(bot, chatId, "🏷 Yangi brend *(yoki `-` tozalash)*:", Keyboards.remove());
            }
            case "editprod_image" -> {
                session.setState(userId, UserState.ADMIN_EDIT_PROD_IMAGE);
                send(bot, chatId, "🖼 Yangi rasm yuboring *(yoki `-` tozalash)*:", Keyboards.remove());
            }
            case "editprod_toggle" -> {
                Product prod = productService.toggleProductActive(prodId);
                send(bot, chatId,
                    "✅ Holat: " + (prod.isActive() ? "Faol ✅" : "O'chirilgan ❌"),
                    Keyboards.adminProductDetail(prod));
            }
        }
    }

    /** Callback: back_adminprods:catId */
    public void handleBackToProducts(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        showProductList(bot, chatId, catId, 1);
    }

    // ── Tahrirlash — matn qabul qilish ────────────────────────

    public void receiveEditProdNameUz(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long prodId = session.getData(userId, "editProdId");
        Product p = productService.updateProductNameUz(prodId, update.getMessage().getText().trim());
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "✅ Nom yangilandi: *" + p.getNameUz() + "*",
             Keyboards.adminProductDetail(p));
    }

    public void receiveEditProdNameRu(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long prodId = session.getData(userId, "editProdId");
        Product p = productService.updateProductNameRu(prodId, update.getMessage().getText().trim());
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "✅ Nom yangilandi: *" + p.getNameRu() + "*",
             Keyboards.adminProductDetail(p));
    }

    public void receiveEditProdPrice(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long prodId = session.getData(userId, "editProdId");
        try {
            double price = Double.parseDouble(
                update.getMessage().getText().trim().replace(" ","").replace(",",""));
            if (price <= 0) throw new NumberFormatException();
            Product p = productService.updateProductPrice(prodId, price);
            session.setState(userId, UserState.IDLE);
            send(bot, chatId, "✅ Narx yangilandi: *" + p.formattedPrice() + "*",
                 Keyboards.adminProductDetail(p));
        } catch (NumberFormatException e) {
            send(bot, chatId, "❌ Noto'g'ri narx. Qayta kiriting:");
        }
    }

    public void receiveEditProdStock(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long prodId = session.getData(userId, "editProdId");
        try {
            int stock = Integer.parseInt(update.getMessage().getText().trim());
            if (stock < 0) throw new NumberFormatException();
            Product p = productService.updateProductStock(prodId, stock);
            session.setState(userId, UserState.IDLE);
            send(bot, chatId, "✅ Son yangilandi: *" + p.getStock() + "* dona",
                 Keyboards.adminProductDetail(p));
        } catch (NumberFormatException e) {
            send(bot, chatId, "❌ Noto'g'ri son. Qayta kiriting:");
        }
    }

    public void receiveEditProdBrand(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        Long prodId = session.getData(userId, "editProdId");
        String t = update.getMessage().getText().trim();
        Product p = productService.updateProductBrand(prodId, "-".equals(t) ? null : t);
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, "✅ Brend yangilandi: *" + (p.getBrand() != null ? p.getBrand() : "—") + "*",
             Keyboards.adminProductDetail(p));
    }

    public void receiveEditProdImage(Update update, AbsSender bot) {
        Long userId = uid(update);
        Long chatId = cid(update);
        Long prodId = session.getData(userId, "editProdId");

        String fileId = null;
        if (update.getMessage() != null) {
            if (update.getMessage().hasPhoto()) {
                var photos = update.getMessage().getPhoto();
                fileId = photos.get(photos.size() - 1).getFileId();
            } else if ("-".equals(update.getMessage().getText())) {
                fileId = null; // Rasmni tozalash
            } else {
                send(bot, chatId, "🖼 Rasm yuboring yoki `-` yozing:");
                return;
            }
        }

        Product p = productService.updateProductImage(prodId, fileId);
        session.setState(userId, UserState.IDLE);

        String msg = fileId != null ? "✅ Rasm yangilandi!" : "✅ Rasm o'chirildi.";
        if (fileId != null) {
            sendPhoto(bot, chatId, fileId, msg + "\n\n" + buildProdInfo(p),
                      Keyboards.adminProductDetail(p));
        } else {
            send(bot, chatId, msg, Keyboards.adminProductDetail(p));
        }
    }

    // ═════════════════════════════════════════════════════════
    // MAHSULOT QO'SHISH (step-by-step)
    // ═════════════════════════════════════════════════════════

    private void startAddProduct(AbsSender bot, Long userId, Long chatId) {
        session.clearData(userId);
        List<Category> cats = productService.getActiveCategories();
        if (cats.isEmpty()) {
            send(bot, chatId, "⚠️ Avval kategoriya qo'shing!", Keyboards.adminMenu()); return;
        }
        session.setState(userId, UserState.ADMIN_WAITING_CATEGORY);
        send(bot, chatId, "➕ *Yangi mahsulot*\n\n📂 Kategoriyani tanlang:",
             Keyboards.categoriesForAdmin(cats));
    }

    public void receiveNameUz(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        session.setData(userId, "p_nameUz", update.getMessage().getText().trim());
        session.setState(userId, UserState.ADMIN_WAITING_NAME_RU);
        send(bot, chatId, "📝 Nom *(ruscha)*, yoki `-` o'tkazib yuborish:");
    }

    public void receiveNameRu(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String t = update.getMessage().getText().trim();
        session.setData(userId, "p_nameRu", "-".equals(t) ? session.getData(userId, "p_nameUz") : t);
        session.setState(userId, UserState.ADMIN_WAITING_PRICE);
        send(bot, chatId, "💰 Narxi *(so'mda)*:\nMisol: `2500000`");
    }

    public void receivePrice(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        try {
            double price = Double.parseDouble(
                update.getMessage().getText().trim().replace(" ","").replace(",",""));
            if (price <= 0) throw new NumberFormatException();
            session.setData(userId, "p_price", price);
            session.setState(userId, UserState.ADMIN_WAITING_STOCK);
            send(bot, chatId, "📦 Soni *(dona)*:");
        } catch (NumberFormatException e) {
            send(bot, chatId, "❌ Noto'g'ri narx. Musbat raqam kiriting:");
        }
    }

    public void receiveStock(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        try {
            int stock = Integer.parseInt(update.getMessage().getText().trim());
            if (stock < 0) throw new NumberFormatException();
            session.setData(userId, "p_stock", stock);
            session.setState(userId, UserState.ADMIN_WAITING_BRAND);
            send(bot, chatId, "🏷 Brend *(Misol: Samsung)*, yoki `-`:");
        } catch (NumberFormatException e) {
            send(bot, chatId, "❌ Noto'g'ri son. Kiriting:");
        }
    }

    public void receiveBrand(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String t = update.getMessage().getText().trim();
        session.setData(userId, "p_brand", "-".equals(t) ? null : t);
        session.setState(userId, UserState.ADMIN_WAITING_IMAGE);
        send(bot, chatId, "🖼 *Rasm yuboring* yoki `-` rasmsiz saqlash:");
    }

    public void receiveImage(Update update, AbsSender bot) {
        Long userId = uid(update);
        Long chatId = cid(update);
        String fileId = null;
        if (update.getMessage() != null && update.getMessage().hasPhoto()) {
            var photos = update.getMessage().getPhoto();
            fileId = photos.get(photos.size() - 1).getFileId();
        }
        saveNewProduct(bot, userId, chatId, fileId);
    }

    private void saveNewProduct(AbsSender bot, Long userId, Long chatId, String imageFileId) {
        try {
            Long    catId  = session.getData(userId, "p_catId");
            String  nameUz = session.getData(userId, "p_nameUz");
            String  nameRu = session.getData(userId, "p_nameRu");
            Double  price  = session.getData(userId, "p_price");
            Integer stock  = session.getData(userId, "p_stock");
            String  brand  = session.getData(userId, "p_brand");

            Product p = productService.addProduct(catId, nameUz, nameRu, price, stock, imageFileId, brand);
            session.clearData(userId);
            session.setState(userId, UserState.IDLE);

            String msg = String.format("✅ *Mahsulot qo'shildi!*\n\n📦 %s\n💰 %s\n🔢 %d dona\n🖼 %s",
                p.getNameUz(), p.formattedPrice(), p.getStock(),
                imageFileId != null ? "Rasm yuklandi ✅" : "Rasm yo'q");

            if (imageFileId != null) {
                sendPhoto(bot, chatId, imageFileId, msg, null);
                send(bot, chatId, "Boshqa nima?", Keyboards.adminMenu());
            } else {
                send(bot, chatId, msg, Keyboards.adminMenu());
            }
        } catch (Exception e) {
            log.error("Mahsulot saqlashda xato: {}", e.getMessage(), e);
            send(bot, chatId, "❌ Xatolik: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════
    // KATEGORIYA QO'SHISH
    // ═════════════════════════════════════════════════════════

    private void startAddCategory(AbsSender bot, Long userId, Long chatId) {
        session.clearData(userId);
        session.setState(userId, UserState.ADMIN_CAT_WAITING_NAME_UZ);
        send(bot, chatId, "📂 *Yangi kategoriya*\n\nNomi *(o'zbekcha)*:", Keyboards.remove());
    }

    public void receiveCatNameUz(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        session.setData(userId, "c_nameUz", update.getMessage().getText().trim());
        session.setState(userId, UserState.ADMIN_CAT_WAITING_NAME_RU);
        send(bot, chatId, "📂 Nomi *(ruscha)*, yoki `-`:");
    }

    public void receiveCatNameRu(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String t = update.getMessage().getText().trim();
        session.setData(userId, "c_nameRu", "-".equals(t) ? session.getData(userId, "c_nameUz") : t);
        session.setState(userId, UserState.ADMIN_CAT_WAITING_EMOJI);
        send(bot, chatId, "😀 Emoji *(Misol: 📱 💻)*, yoki `-` default uchun:");
    }

    public void receiveCatEmoji(Update update, AbsSender bot) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String t = update.getMessage().getText().trim();
        try {
            Category cat = productService.addCategory(
                session.getData(userId, "c_nameUz"),
                session.getData(userId, "c_nameRu"),
                "-".equals(t) ? "📦" : t);
            session.clearData(userId);
            session.setState(userId, UserState.IDLE);
            send(bot, chatId, "✅ Kategoriya qo'shildi: " + cat.getEmoji() + " *" + cat.getNameUz() + "*",
                 Keyboards.adminMenu());
        } catch (Exception e) {
            send(bot, chatId, "❌ Xatolik: " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════
    // BUYURTMALAR
    // ═════════════════════════════════════════════════════════

    public void handleOrderView(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        Long orderId = Long.parseLong(update.getCallbackQuery().getData().split(":")[1]);
        orderService.getOrderWithDetails(orderId).ifPresentOrElse(
            o -> send(bot, chatId, buildOrderDetail(o), Keyboards.adminOrderDetail(orderId)),
            () -> send(bot, chatId, "❌ Buyurtma topilmadi."));
    }

    public void handleOrdersPage(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        int page = Integer.parseInt(update.getCallbackQuery().getData().split(":")[1]);
        showOrders(bot, userId, chatId, page);
    }

    public void handleSetStatus(Update update, AbsSender bot) {
        Long userId = uid(update), chatId = cid(update);
        if (!guard(userId, chatId, bot)) return;
        ack(bot, update);
        String[] p = update.getCallbackQuery().getData().split(":");
        Long orderId = Long.parseLong(p[1]);
        OrderStatus status = OrderStatus.valueOf(p[2]);
        orderService.updateStatus(orderId, status);
        send(bot, chatId,
            "✅ Buyurtma *#" + orderId + "* — " + status.emoji() + " *" + status.labelUz() + "*",
            Keyboards.adminMenu());
    }

    private void showOrders(AbsSender bot, Long userId, Long chatId, int page) {
        List<Order> all = orderService.getAllOrders();
        if (all.isEmpty()) { send(bot, chatId, "📋 Hali buyurtma yo'q.", Keyboards.adminMenu()); return; }
        int totalPages = (int) Math.ceil((double) all.size() / ORDERS_PAGE);
        int from = (page - 1) * ORDERS_PAGE;
        int to   = Math.min(from + ORDERS_PAGE, all.size());
        send(bot, chatId, "📋 *Buyurtmalar* — " + all.size() + " ta:",
             Keyboards.adminOrders(all.subList(from, to), page, totalPages));
    }

    private String buildOrderDetail(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📋 *Buyurtma #%d*\n%s *%s*\n\n",
            order.getId(), order.getStatus().emoji(), order.getStatus().labelUz()));
        sb.append("👤 ").append(order.getUser().getFullName()).append("\n");
        sb.append("📞 ").append(order.getPhone()).append("\n");
        sb.append("🏠 ").append(order.getDeliveryAddress()).append("\n");
        if (order.getPaymentMethod() != null)
            sb.append("💳 ").append(order.getPaymentMethod().label()).append("\n");
        sb.append("🕐 ").append(order.getCreatedAt().toString().substring(0, 16)).append("\n\n");
        sb.append("━━━━━━━━━━━━━━\n");
        for (OrderItem item : order.getItems())
            sb.append(String.format("• %s × %d = %,.0f so'm\n",
                item.getProduct().getNameUz(), item.getQuantity(), item.subtotal()));
        sb.append(String.format("\n💰 *Jami: %,.0f so'm*", order.getTotalAmount()));
        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════
    // STATISTIKA
    // ═════════════════════════════════════════════════════════

    private void showStats(AbsSender bot, Long chatId) {
        List<Order> all = orderService.getAllOrders();
        double revenue = all.stream()
            .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.DELIVERED)
            .mapToDouble(Order::getTotalAmount).sum();
        send(bot, chatId, String.format("""
            📊 *Statistika*

            📦 Jami buyurtma: *%d*
            ⏳ Kutilmoqda: *%d*
            ✅ Tasdiqlangan: *%d*
            💳 To'landi: *%d*
            🚚 Yo'lda: *%d*
            🎉 Yetkazildi: *%d*
            ❌ Bekor: *%d*

            💰 Umumiy tushum: *%,.0f so'm*
            """,
            all.size(),
            cnt(all, OrderStatus.PENDING), cnt(all, OrderStatus.CONFIRMED),
            cnt(all, OrderStatus.PAID),    cnt(all, OrderStatus.SHIPPED),
            cnt(all, OrderStatus.DELIVERED), cnt(all, OrderStatus.CANCELLED),
            revenue), Keyboards.adminMenu());
    }

    private long cnt(List<Order> list, OrderStatus s) {
        return list.stream().filter(o -> o.getStatus() == s).count();
    }

    // ═════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════

    private Long uid(Update u) {
        return u.hasCallbackQuery() ? u.getCallbackQuery().getFrom().getId()
                                    : u.getMessage().getFrom().getId();
    }
    private Long cid(Update u) {
        return u.hasCallbackQuery() ? u.getCallbackQuery().getMessage().getChatId()
                                    : u.getMessage().getChatId();
    }
    private void ack(AbsSender bot, Update u) {
        if (u.hasCallbackQuery()) answerCallback(bot, u.getCallbackQuery().getId(), null);
    }
}
