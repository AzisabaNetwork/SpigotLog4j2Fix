package net.azisaba.spigotLog4j2Fix.v1_12_R1.util;

import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.minecraft.server.v1_12_R1.ChatBaseComponent;
import net.minecraft.server.v1_12_R1.ChatComponentKeybind;
import net.minecraft.server.v1_12_R1.ChatComponentScore;
import net.minecraft.server.v1_12_R1.ChatComponentSelector;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutCombatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class VersionUtil {
    public static List<Object> processOutgoingPacket(@NotNull PacketData packetData) {
        Packet<?> packet = packetData.getPacket();
        if (packet instanceof PacketPlayOutChat) {
            PacketPlayOutChat p = (PacketPlayOutChat) packet;
            if (packetData.getField("a") != null) {
                if (packetData.modifyField("a", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList();
                }
            }
            if (p.components != null) {
                if (packetData.modifyField("components", Util::filterBaseComponentArray) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutCombatEvent) {
            if (packetData.modifyField("e", VersionUtil::filterComponent) == null) {
                return Collections.emptyList();
            }
        }
        return Collections.singletonList(packet);
    }

    @Nullable
    public static IChatBaseComponent filterComponent(@Nullable IChatBaseComponent component) {
        if (component == null) return null;
        if (component instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) component;
            List<Object> args = new ArrayList<>();
            for (Object o : chatMessage.j()) {
                if (o instanceof IChatBaseComponent) {
                    o = filterComponent((IChatBaseComponent) o);
                    if (o != null) args.add(o);
                }
            }
            component = new ChatMessage(chatMessage.i().replaceAll("(?i)jndi:ldap", ""), args.toArray());
        }
        if (component instanceof ChatBaseComponent) {
            ChatBaseComponent chatBaseComponent = (ChatBaseComponent) component;
            List<IChatBaseComponent> list = component.a().stream().map(VersionUtil::filterComponent).filter(Objects::nonNull).collect(Collectors.toList());
            if (!Util.listEquals(list, chatBaseComponent.a())) {
                component = copyWithoutSiblings(chatBaseComponent);
                // Add siblings
                for (IChatBaseComponent c : list) component.addSibling(c);
            }
        }
        if (component instanceof ChatComponentText) {
            ChatComponentText text = (ChatComponentText) component;
            if (Util.isTaintedString(text.g())) {
                component = new ChatComponentText(Util.sanitizeString(text.g())).setChatModifier(text.getChatModifier());
            }
        }
        if (Util.isTaintedString(component.getText()) || Util.isTaintedString(component.toPlainText()) || Util.isTaintedString(component.toString())) {
            try {
                List<IChatBaseComponent> s = component.a();
                List<IChatBaseComponent> list = new ArrayList<>();
                for (IChatBaseComponent iChatBaseComponent : s) {
                    list.add(filterComponent(iChatBaseComponent));
                }
                int i = 0;
                for (IChatBaseComponent c : list) {
                    if (c == null || list.get(i) == null) continue;
                    if (!Objects.equals(c, list.get(i))) {
                        list.set(i, list.get(i));
                    }
                    i++;
                }
            } catch (UnsupportedOperationException e) {
                return null;
            }
            return null;
        } else {
            return component;
        }
    }

    @NotNull
    public static ChatBaseComponent newInstance(@NotNull ChatBaseComponent component) {
        if (component instanceof ChatComponentKeybind) {
            ChatComponentKeybind c = (ChatComponentKeybind) component;
            return new ChatComponentKeybind(c.h().replaceAll("(?i)jndi:ldap", ""));
        } else if (component instanceof ChatComponentText) {
            ChatComponentText c = (ChatComponentText) component;
            return new ChatComponentText(c.g().replaceAll("(?i)jndi:ldap", ""));
        } else if (component instanceof ChatComponentScore) {
            ChatComponentScore c = (ChatComponentScore) component;
            return new ChatComponentScore(c.g().replaceAll("(?i)jndi:ldap", ""), c.h().replaceAll("(?i)jndi:ldap", ""));
        } else if (component instanceof ChatComponentSelector) {
            ChatComponentSelector c = (ChatComponentSelector) component;
            return new ChatComponentSelector(c.g().replaceAll("(?i)jndi:ldap", ""));
        } else if (component instanceof ChatMessage) {
            ChatMessage c = (ChatMessage) component;
            return new ChatMessage(c.i().replaceAll("(?i)jndi:ldap", ""), c.j());
        } else {
            throw new RuntimeException("Unsupported ChatBaseComponent type: " + component.getClass().getTypeName());
        }
    }

    @NotNull
    public static ChatBaseComponent copy(@NotNull ChatBaseComponent component) {
        ChatBaseComponent newComponent = newInstance(component);
        for (IChatBaseComponent c : component.a()) newComponent.addSibling(c);
        newComponent.setChatModifier(component.getChatModifier());
        return newComponent;
    }

    @NotNull
    public static ChatBaseComponent copyWithoutSiblings(@NotNull ChatBaseComponent component) {
        ChatBaseComponent newComponent = newInstance(component);
        newComponent.setChatModifier(component.getChatModifier());
        return newComponent;
    }
}
