package uz.technobot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.technobot.entity.Category;
import uz.technobot.entity.Product;
import uz.technobot.repository.CategoryRepository;
import uz.technobot.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE       = 5;
    private static final int ADMIN_PAGE_SIZE = 6;

    private final ProductRepository  productRepo;
    private final CategoryRepository categoryRepo;

    // ── Kategoriyalar ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepo.findByActiveTrueOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepo.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategory(Long id) {
        return categoryRepo.findById(id);
    }

    // ── Mahsulotlar ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategory(Long categoryId, int page) {
        return productRepo.findByCategoryIdAndActiveTrueOrderByIdDesc(
                categoryId, PageRequest.of(page - 1, PAGE_SIZE));
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByCategoryAdmin(Long categoryId, int page) {
        return productRepo.findByCategoryIdOrderByIdDesc(
                categoryId, PageRequest.of(page - 1, ADMIN_PAGE_SIZE));
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProduct(Long id) {
        // category bilan birga yuklaymiz
        return productRepo.findByIdWithCategory(id);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String query) {
        return productRepo.search("%" + query.toLowerCase() + "%");
    }

    // ── Qo'shish ──────────────────────────────────────────────

    @Transactional
    public Category addCategory(String nameUz, String nameRu, String emoji) {
        return categoryRepo.save(Category.builder()
                .nameUz(nameUz).nameRu(nameRu).nameEn(nameUz).emoji(emoji).build());
    }

    @Transactional
    public Product addProduct(Long categoryId, String nameUz, String nameRu,
                              Double price, Integer stock, String imageFileId, String brand) {
        Category cat = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Kategoriya topilmadi"));
        Product saved = productRepo.save(Product.builder()
                .category(cat).nameUz(nameUz)
                .nameRu(nameRu != null ? nameRu : nameUz)
                .price(price).stock(stock)
                .imageFileId(imageFileId).brand(brand).build());
        // category bilan birga qaytarish
        return productRepo.findByIdWithCategory(saved.getId()).orElse(saved);
    }

    // ── Tahrirlash — Mahsulot ─────────────────────────────────

    @Transactional
    public Product updateProductNameUz(Long id, String nameUz) {
        Product p = getOrThrow(id); p.setNameUz(nameUz);
        return save(p);
    }

    @Transactional
    public Product updateProductNameRu(Long id, String nameRu) {
        Product p = getOrThrow(id); p.setNameRu(nameRu);
        return save(p);
    }

    @Transactional
    public Product updateProductPrice(Long id, Double price) {
        Product p = getOrThrow(id); p.setPrice(price);
        return save(p);
    }

    @Transactional
    public Product updateProductStock(Long id, Integer stock) {
        Product p = getOrThrow(id); p.setStock(stock);
        return save(p);
    }

    @Transactional
    public Product updateProductBrand(Long id, String brand) {
        Product p = getOrThrow(id); p.setBrand(brand);
        return save(p);
    }

    @Transactional
    public Product updateProductImage(Long id, String fileId) {
        Product p = getOrThrow(id); p.setImageFileId(fileId);
        return save(p);
    }

    @Transactional
    public Product toggleProductActive(Long id) {
        Product p = getOrThrow(id);
        p.setActive(!p.isActive());
        return save(p);
    }

    // ── Tahrirlash — Kategoriya ───────────────────────────────

    @Transactional
    public Category updateCategoryNameUz(Long id, String nameUz) {
        Category c = catOrThrow(id); c.setNameUz(nameUz);
        return categoryRepo.save(c);
    }

    @Transactional
    public Category updateCategoryNameRu(Long id, String nameRu) {
        Category c = catOrThrow(id); c.setNameRu(nameRu);
        return categoryRepo.save(c);
    }

    @Transactional
    public Category updateCategoryEmoji(Long id, String emoji) {
        Category c = catOrThrow(id); c.setEmoji(emoji);
        return categoryRepo.save(c);
    }

    @Transactional
    public Category toggleCategoryActive(Long id) {
        Category c = catOrThrow(id);
        c.setActive(!c.isActive());
        return categoryRepo.save(c);
    }

    // ── Yordamchi ─────────────────────────────────────────────

    @Transactional
    public void decreaseStock(Long productId, int qty) {
        Product p = getOrThrow(productId);
        p.setStock(Math.max(0, p.getStock() - qty));
        productRepo.save(p);
    }

    private Product getOrThrow(Long id) {
        return productRepo.findByIdWithCategory(id)
                .orElseThrow(() -> new IllegalArgumentException("Mahsulot topilmadi: " + id));
    }

    private Category catOrThrow(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kategoriya topilmadi: " + id));
    }

    // category bilan birga saqlash
    private Product save(Product p) {
        Product saved = productRepo.save(p);
        return productRepo.findByIdWithCategory(saved.getId()).orElse(saved);
    }
}
