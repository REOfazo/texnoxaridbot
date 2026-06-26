package uz.technobot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.technobot.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Foydalanuvchi — faqat aktiv
    @Query("SELECT p FROM Product p JOIN FETCH p.category c " +
           "WHERE p.active = true AND c.id = :catId ORDER BY p.id DESC")
    List<Product> findActiveByCategoryId(@Param("catId") Long categoryId);

    Page<Product> findByCategoryIdAndActiveTrueOrderByIdDesc(Long categoryId, Pageable pageable);

    // Admin — barchasi (aktiv + o'chirilgan)
    Page<Product> findByCategoryIdOrderByIdDesc(Long categoryId, Pageable pageable);

    // Qidiruv — foydalanuvchi
    @Query("SELECT p FROM Product p JOIN FETCH p.category " +
           "WHERE p.active = true " +
           "AND (LOWER(p.nameUz) LIKE :q OR LOWER(p.nameRu) LIKE :q OR LOWER(p.brand) LIKE :q) " +
           "ORDER BY p.id DESC")
    List<Product> search(@Param("q") String query);

    // Mahsulot — category bilan birga
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);
}
