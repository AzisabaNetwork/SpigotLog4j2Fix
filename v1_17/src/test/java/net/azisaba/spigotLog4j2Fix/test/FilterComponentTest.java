package net.azisaba.spigotLog4j2Fix.test;

import net.azisaba.spigotLog4j2Fix.v1_17.util.VersionUtil;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import org.junit.jupiter.api.Test;

public class FilterComponentTest {
    @Test
    public void testTranslatableComponent() {
        ChatMessage chatMessage = new ChatMessage(
                "chat.type.announcement",
                new ChatComponentText("Server"),
                new ChatComponentText("jndi:ld${upper:a}${lower:p}noitisjustexample")
        );
        IChatBaseComponent filtered = VersionUtil.filterComponent(chatMessage);
        IChatBaseComponent expected = new ChatMessage("chat.type.announcement", new ChatComponentText("Server"), new ChatComponentText(""));
        assert expected.equals(filtered) : "Expected: " + expected + ", but got: " + filtered;
    }

    @Test
    public void testNotSusComponent() {
        IChatBaseComponent component = new ChatComponentText("Unknown command. Type /help for help.");
        IChatBaseComponent filtered = VersionUtil.filterComponent(component);
        assert component.equals(filtered) : "Expected: " + component + ", but got: " + filtered;
    }
}
