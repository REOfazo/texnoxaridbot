package uz.technobot.entity;

import jakarta.persistence.*;
import lombok.*;
import uz.technobot.enums.Language;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bot_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BotUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long telegramId;

    private String username;
    private String fullName;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Language language = Language.UZ;

    @Builder.Default
    private boolean admin = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
