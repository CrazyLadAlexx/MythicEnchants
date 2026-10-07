package me.alex.mythicenchants.book;

public enum BookText {
    NAME("%s%s %s"), SUCCESS("&a%d%% success chance"), DESTROY("&c%d%% destroy chance"),
    DESCRIPTION("&e%s"), SEPARATOR(""),
    HINT_FIRST("&7Hint: Drag n' drop onto the peice of gear you want to"),
    HINT_SECOND("&7apply this book to!");

    private final String template;
    BookText(String template) { this.template = template; }
    public String format(Object... arguments) { return template.formatted(arguments); }
}
