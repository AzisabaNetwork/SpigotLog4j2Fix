package net.azisaba.spigotLog4j2Fix.v1_12_R1.util;

import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.minecraft.server.v1_12_R1.ChatBaseComponent;
import net.minecraft.server.v1_12_R1.ChatComponentKeybind;
import net.minecraft.server.v1_12_R1.ChatComponentScore;
import net.minecraft.server.v1_12_R1.ChatComponentSelector;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.ChatModifier;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayInChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutCombatEvent;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_12_R1.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWindowItems;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        } else if (packet instanceof PacketPlayOutTitle) {
            if (packetData.getField("b") != null) {
                if (packetData.modifyField("b", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutWindowItems) {
            for (ItemStack stack : packetData.<List<ItemStack>>getField("b")) {
                filterItemStack(stack);
            }
        } else if (packet instanceof PacketPlayOutOpenWindow) {
            packetData.modifyField("b", Util::sanitizeString);
            if (packetData.getField("c") != null) {
                if (packetData.modifyField("c", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutEntityEquipment) {
            filterItemStack(packetData.getField("c"));
        } else if (packet instanceof PacketPlayOutSetSlot) {
            filterItemStack(packetData.getField("c"));
        } else if (packet instanceof PacketPlayOutTileEntityData) {
            filterNBTTagCompound(packetData.getField("c"));
        } else if (packet instanceof PacketPlayOutMapChunk) {
            if (packetData.getField("e") != null) {
                for (NBTTagCompound tag : packetData.<List<NBTTagCompound>>getField("e")) {
                    filterNBTTagCompound(tag);
                }
            }
        }
        return Collections.singletonList(packet);
    }

    public static List<Object> processIncomingPacket(@NotNull PacketData packetData) {
        Packet<?> packet = packetData.getPacket();
        if (packet instanceof PacketPlayInChat) {
            packetData.modifyField("a", Util::sanitizeString);
        }
        return Collections.singletonList(packet);
    }

    @Contract(value = "_ -> param1", mutates = "param1")
    @Nullable
    public static ItemStack filterItemStack(@Nullable ItemStack item) {
        if (item == null) return null;
        item.setTag(filterNBTTagCompound(item.getTag()));
        return item;
    }

    @Contract(value = "_ -> param1", mutates = "param1")
    @Nullable
    public static NBTTagCompound filterNBTTagCompound(@Nullable NBTTagCompound tag) {
        if (tag == null) return null;
        Map<String, NBTBase> map = new HashMap<>(Util.getField(NBTTagCompound.class, "map", tag));
        Map<String, NBTBase> newMap = new HashMap<>();
        map.forEach((key, value) -> {
            if (!Util.isTaintedString(key)) {
                newMap.put(key, filterNBTBase(value));
            }
        });
        map.clear();
        map.putAll(newMap);
        return tag;
    }

    @Contract(value = "_ -> param1", mutates = "param1")
    @Nullable
    public static NBTBase filterNBTBase(@Nullable NBTBase value) {
        if (value instanceof NBTTagString) {
            if (Util.isTaintedString(((NBTTagString) value).c_())) {
                return new NBTTagString(Util.sanitizeString(((NBTTagString) value).c_()));
            }
        } else if (value instanceof NBTTagCompound) {
            return filterNBTTagCompound((NBTTagCompound) value);
        } else if (value instanceof NBTTagList) {
            NBTTagList newList = new NBTTagList();
            for (NBTBase entry : new ArrayList<>(Util.<List<NBTBase>>getField(NBTTagList.class, "list", value))) {
                newList.add(filterNBTBase(entry));
            }
            return newList;
        }
        return value;
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
                    if (o != null) {
                        args.add(o);
                    } else {
                        args.add(new ChatComponentText("?"));
                    }
                } else {
                    args.add(o);
                }
            }
            ChatModifier chatModifier = component.getChatModifier();
            component = new ChatMessage(Util.sanitizeString(chatMessage.i()), args.toArray());
            component.setChatModifier(chatModifier);
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
                        s.set(i, list.get(i));
                    }
                    i++;
                }
                return component;
            } catch (UnsupportedOperationException e) {
                return null;
            }
        } else {
            return component;
        }
    }

    @NotNull
    public static ChatBaseComponent newInstance(@NotNull ChatBaseComponent component) {
        if (component instanceof ChatComponentKeybind) {
            ChatComponentKeybind c = (ChatComponentKeybind) component;
            return new ChatComponentKeybind(Util.sanitizeString(c.h()));
        } else if (component instanceof ChatComponentText) {
            ChatComponentText c = (ChatComponentText) component;
            return new ChatComponentText(Util.sanitizeString(c.g()));
        } else if (component instanceof ChatComponentScore) {
            ChatComponentScore c = (ChatComponentScore) component;
            return new ChatComponentScore(Util.sanitizeString(c.g()), Util.sanitizeString(c.h()));
        } else if (component instanceof ChatComponentSelector) {
            ChatComponentSelector c = (ChatComponentSelector) component;
            return new ChatComponentSelector(Util.sanitizeString(c.g()));
        } else if (component instanceof ChatMessage) {
            ChatMessage c = (ChatMessage) component;
            return new ChatMessage(Util.sanitizeString(c.i()), c.j());
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
