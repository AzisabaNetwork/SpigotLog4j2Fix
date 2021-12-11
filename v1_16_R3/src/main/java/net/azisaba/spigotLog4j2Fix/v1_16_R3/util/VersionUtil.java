package net.azisaba.spigotLog4j2Fix.v1_16_R3.util;

import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.minecraft.server.v1_16_R3.ChatBaseComponent;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.ChatModifier;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChatMutableComponent;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import net.minecraft.server.v1_16_R3.PacketPlayOutCombatEvent;
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
                    return Collections.emptyList(); // cancel packet
                }
            }
            if (p.components != null) {
                if (packetData.modifyField("components", Util::filterBaseComponentArray) == null) {
                    return Collections.emptyList();
                }
            }
            System.err.println((Object) packetData.getField("a"));
            System.err.println(java.util.Arrays.toString(p.components));
        } else if (packet instanceof PacketPlayOutCombatEvent) {
            if (packetData.modifyField("e", VersionUtil::filterComponent) == null) {
                return Collections.emptyList(); // cancel packet
            }
            System.err.println((Object) packetData.getField("e"));
        }
        return Collections.singletonList(packet);
    }

    @Nullable
    public static IChatBaseComponent filterComponent(@Nullable IChatBaseComponent component) {
        if (component == null) return null;
        if (component instanceof ChatMessage) {
            ChatMessage chatMessage = (ChatMessage) component;
            List<Object> args = new ArrayList<>();
            for (Object o : chatMessage.getArgs()) {
                if (o instanceof IChatBaseComponent) {
                    o = filterComponent((IChatBaseComponent) o);
                    if (o != null) args.add(o);
                } else {
                    args.add(o);
                }
            }
            ChatModifier cm = component.getChatModifier();
            component = new ChatMessage(Util.sanitizeString(chatMessage.getKey()), args.toArray());
            ((ChatMessage) component).setChatModifier(cm);
        }
        if (component instanceof ChatBaseComponent) {
            ChatBaseComponent chatBaseComponent = (ChatBaseComponent) component;
            List<IChatBaseComponent> list = component.getSiblings().stream().map(VersionUtil::filterComponent).filter(Objects::nonNull).collect(Collectors.toList());
            if (!Util.listEquals(list, chatBaseComponent.getSiblings())) {
                component = chatBaseComponent.g();
                // Add siblings
                Util.<List<IChatBaseComponent>>getField(ChatBaseComponent.class, "siblings", component).clear();
                for (IChatBaseComponent c : list) ((ChatBaseComponent) component).addSibling(c);
            }
        }
        if (component instanceof ChatComponentText) {
            ChatComponentText text = (ChatComponentText) component;
            if (Util.isTaintedString(text.h())) {
                ChatModifier cm = component.getChatModifier();
                component = new ChatComponentText(Util.sanitizeString(text.h())).setChatModifier(text.getChatModifier());
                ((IChatMutableComponent) component).setChatModifier(cm);
            }
        }
        if (Util.isTaintedString(component.getText()) || Util.isTaintedString(component.getString()) || Util.isTaintedString(component.toString())) {
            return null;
        } else {
            try {
                List<IChatBaseComponent> s = component.getSiblings();
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
            return component;
        }
    }
}
