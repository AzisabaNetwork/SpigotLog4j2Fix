package net.azisaba.spigotLog4j2Fix.v1_17.util;

import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.minecraft.network.chat.ChatBaseComponent;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
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
        if (packet instanceof PacketPlayOutChat p) {
            if (p.b() != null) {
                if (packetData.modifyField("a", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList(); // cancel packet
                }
            }
            if (p.components != null) {
                if (packetData.modifyField("components", Util::filterBaseComponentArray) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof ClientboundPlayerCombatKillPacket) {
            if (packetData.modifyField("c", VersionUtil::filterComponent) == null) {
                return Collections.emptyList(); // cancel packet
            }
        }
        return Collections.singletonList(packet);
    }

    @Nullable
    public static IChatBaseComponent filterComponent(@Nullable IChatBaseComponent component) {
        if (component == null) return null;
        if (component instanceof ChatMessage chatMessage) {
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
        if (component instanceof ChatBaseComponent chatBaseComponent) {
            List<IChatBaseComponent> list = component.getSiblings().stream().map(VersionUtil::filterComponent).filter(Objects::nonNull).collect(Collectors.toList());
            if (!Util.listEquals(list, chatBaseComponent.getSiblings())) {
                component = chatBaseComponent.g();
                // Add siblings
                Util.<List<IChatBaseComponent>>getField(ChatBaseComponent.class, "a", component).clear();
                for (IChatBaseComponent c : list) ((ChatBaseComponent) component).addSibling(c);
            }
        }
        if (component instanceof ChatComponentText text) {
            if (Util.isTaintedString(text.h())) {
                ChatModifier cm = component.getChatModifier();
                component = new ChatComponentText(Util.sanitizeString(text.h())).setChatModifier(text.getChatModifier());
                ((IChatMutableComponent) component).setChatModifier(cm);
            }
        }
        if (Util.isTaintedString(component.getText()) || Util.isTaintedString(component.getString()) || Util.isTaintedString(component.toString())) {
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
            return null;
        } else {
            return component;
        }
    }
}
