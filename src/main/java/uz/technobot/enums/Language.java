package uz.technobot.enums;

public enum Language {
    UZ, RU, EN;

    public static Language from(String code) {
        return switch (code.toLowerCase()) {
            case "ru" -> RU;
            case "en" -> EN;
            default -> UZ;
        };
    }

    public String code() {
        return name().toLowerCase();
    }
}
