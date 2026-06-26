package uz.technobot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.technobot.entity.Category;
import uz.technobot.entity.Product;
import uz.technobot.repository.CategoryRepository;
import uz.technobot.repository.ProductRepository;

/**
 * DataSeeder — ilk ishga tushganda test ma'lumotlari qo'shadi.
 * Faqat H2 (dev) rejimida ishlaydi, productda o'chiring.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository catRepo;
    private final ProductRepository prodRepo;

    @Override
    public void run(String... args) {
        if (catRepo.count() > 0) return; // Allaqachon ma'lumot bor

        log.info("Test ma'lumotlari qo'shilmoqda...");

        Category phones = catRepo.save(Category.builder()
                .nameUz("Telefonlar").nameRu("Телефоны").nameEn("Phones")
                .emoji("📱").build());

        Category tvs = catRepo.save(Category.builder()
                .nameUz("Televizorlar").nameRu("Телевизоры").nameEn("TVs")
                .emoji("📺").build());

        Category laptops = catRepo.save(Category.builder()
                .nameUz("Noutbuklar").nameRu("Ноутбуки").nameEn("Laptops")
                .emoji("💻").build());

        Category fridges = catRepo.save(Category.builder()
                .nameUz("Muzlatgichlar").nameRu("Холодильники").nameEn("Fridges")
                .emoji("🧊").build());

        // Telefonlar
        prodRepo.save(Product.builder().category(phones)
                .nameUz("Samsung Galaxy A55").nameRu("Samsung Galaxy A55")
                .descriptionUz("6.6\" AMOLED, 8GB RAM, 256GB xotira, 50MP kamera")
                .descriptionRu("6.6\" AMOLED, 8GB RAM, 256GB памяти, камера 50MP")
                .price(3_990_000.0).stock(15).brand("Samsung").build());

        prodRepo.save(Product.builder().category(phones)
                .nameUz("iPhone 15").nameRu("iPhone 15")
                .descriptionUz("6.1\", A16 chip, 128GB, Dynamic Island")
                .descriptionRu("6.1\", чип A16, 128GB, Dynamic Island")
                .price(12_500_000.0).stock(8).brand("Apple").build());

        prodRepo.save(Product.builder().category(phones)
                .nameUz("Xiaomi Redmi Note 13").nameRu("Xiaomi Redmi Note 13")
                .descriptionUz("6.67\" AMOLED, 8GB RAM, 256GB, 108MP kamera")
                .price(2_490_000.0).stock(20).brand("Xiaomi").build());

        // Televizorlar
        prodRepo.save(Product.builder().category(tvs)
                .nameUz("Samsung 55\" QLED 4K").nameRu("Samsung 55\" QLED 4K")
                .descriptionUz("55 dyuym, 4K UHD, Smart TV, WiFi, Bluetooth")
                .price(8_900_000.0).stock(5).brand("Samsung").build());

        prodRepo.save(Product.builder().category(tvs)
                .nameUz("LG 43\" Full HD Smart").nameRu("LG 43\" Full HD Smart")
                .descriptionUz("43 dyuym, Full HD, WebOS, Netflix, YouTube")
                .price(4_200_000.0).stock(10).brand("LG").build());

        // Noutbuklar
        prodRepo.save(Product.builder().category(laptops)
                .nameUz("Lenovo IdeaPad 3").nameRu("Lenovo IdeaPad 3")
                .descriptionUz("Intel Core i5, 8GB RAM, 512GB SSD, 15.6\" FHD")
                .price(6_500_000.0).stock(7).brand("Lenovo").build());

        prodRepo.save(Product.builder().category(laptops)
                .nameUz("ASUS VivoBook 15").nameRu("ASUS VivoBook 15")
                .descriptionUz("AMD Ryzen 5, 16GB RAM, 512GB SSD, OLED ekran")
                .price(7_200_000.0).stock(4).brand("ASUS").build());

        // Muzlatgichlar
        prodRepo.save(Product.builder().category(fridges)
                .nameUz("Artel 270L No-Frost").nameRu("Artel 270L No-Frost")
                .descriptionUz("270 litr, No-Frost texnologiya, 2 eshik, A+ sinf")
                .price(4_800_000.0).stock(6).brand("Artel").build());

        prodRepo.save(Product.builder().category(fridges)
                .nameUz("Samsung 350L Side-by-Side").nameRu("Samsung 350L Side-by-Side")
                .descriptionUz("350 litr, Side-by-Side, dispenser, A++ sinf")
                .price(11_000_000.0).stock(3).brand("Samsung").build());

        log.info("✅ {} kategoriya, {} mahsulot qo'shildi",
                catRepo.count(), prodRepo.count());
    }
}
