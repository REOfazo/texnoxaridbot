# TechnoBot — Telegram savdo boti (Java/Spring Boot)

## Ishga tushirish

### 1. .env fayl yarating
```bash
cp .env.example .env
# .env faylni o'zgartiring: BOT_TOKEN, BOT_USERNAME, ADMIN_IDS
```

### Telegram ID ni qanday bilish?
@userinfobot ga /start yuboring — u sizning ID ni ko'rsatadi.

### 2. Ishga tushirish
```bash
# Windows
set BOT_TOKEN=tokeningiz&& set BOT_USERNAME=botusername&& set ADMIN_IDS=telegramid&& mvn spring-boot:run

# Linux/Mac
source .env && mvn spring-boot:run
```

### IntelliJ IDEA orqali
Run → Edit Configurations → Environment Variables ga qo'shing:
```
BOT_TOKEN=tokeningiz
BOT_USERNAME=botusername
ADMIN_IDS=telegramid
```

## Admin panel
- `/admin` buyrug'ini yuboring
- Mahsulot qo'shish, kategoriya, buyurtmalar boshqaruvi

## Bot funksiyalari
✅ Katalog, kategoriyalar, pagination
✅ Mahsulot rasmi (Telegram file_id)
✅ Qidiruv
✅ Savat + buyurtma berish
✅ Click / Payme / Naqd to'lov
✅ Admin panel (bot ichida /admin)
✅ Kategoriya qo'shish
✅ Mahsulot qo'shish + rasm yuklash
✅ Buyurtmalar boshqaruvi + holat o'zgartirish
✅ Statistika

## H2 Console (dev)
http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/technobotdb`
- Username: sa, Password: (bo'sh)
