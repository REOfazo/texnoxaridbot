package uz.technobot.enums;

public enum PaymentMethod {
    CLICK, PAYME, CASH;

    public String label() {
        return switch (this) {
            case CLICK  -> "💳 Click";
            case PAYME  -> "💳 Payme";
            case CASH   -> "💵 Naqd pul";
        };
    }
}
