package uz.technobot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.technobot.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderByIdAsc();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.active = true ORDER BY c.id ASC")
    List<Category> findAllWithProducts();
}
