package me.alex.mythicenchants.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {
    private Text() {}
    public static Component colour(String text) {
        return noItalic(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
    }
    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false)
            .children(component.children().stream().map(Text::noItalic).toList());
    }
}
