package uz.technobot.enums;

public enum OrderStatus {
    PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED;

    public String emoji() {
        return switch (this) {
            case PENDING    -> "⏳";
            case CONFIRMED  -> "✅";
            case PAID       -> "💳";
            case SHIPPED    -> "🚚";
            case DELIVERED  -> "🎉";
            case CANCELLED  -> "❌";
        };
    }

    public String labelUz() {
        return switch (this) {
            case PENDING    -> "Kutilmoqda";
            case CONFIRMED  -> "Tasdiqlandi";
            case PAID       -> "To'landi";
            case SHIPPED    -> "Yo'lda";
            case DELIVERED  -> "Yetkazildi";
            case CANCELLED  -> "Bekor qilindi";
        };
    }
}
