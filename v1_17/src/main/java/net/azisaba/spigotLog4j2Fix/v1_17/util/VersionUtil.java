package net.azisaba.spigotLog4j2Fix.v1_17.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class VersionUtil {
    /**
     * @return true indicates the packet should be cancelled; false indicates the packet should not be cancelled
     */
    public static boolean processOutgoingPacket(@NotNull Packet<?> packet) {
        System.err.println(ToStringBuilder.reflectionToString(packet));
        if (packet instanceof PacketPlayOutChat chatPacket) {
            List<BaseComponent> components = new ArrayList<>(Arrays.asList(chatPacket.components));
            components.removeIf(component -> component.toPlainText().toLowerCase(Locale.ROOT).contains("jndi:ldap"));
            chatPacket.components = components.toArray(new BaseComponent[0]);
        }
        return false;
    }
}
