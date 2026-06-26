package uz.technobot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER — category har doim birga yuklanadi, LazyInit xatosi yo'q
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String nameUz;

    @Column(nullable = false)
    private String nameRu;

    private String descriptionUz;
    private String descriptionRu;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    private String imageFileId;
    private String brand;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getName(String lang) {
        return "ru".equals(lang) && nameRu != null ? nameRu : nameUz;
    }

    public String getDescription(String lang) {
        String desc = "ru".equals(lang) ? descriptionRu : descriptionUz;
        return desc != null ? desc : "";
    }

    public String formattedPrice() {
        return String.format("%,.0f so'm", price);
    }
}
