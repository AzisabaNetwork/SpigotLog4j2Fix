package net.azisaba.spigotLog4j2Fix.v1_16_R3.util;

import com.mojang.datafixers.util.Pair;
import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.minecraft.server.v1_16_R3.ChatBaseComponent;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.ChatModifier;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IChatMutableComponent;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MerchantRecipe;
import net.minecraft.server.v1_16_R3.MerchantRecipeList;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.NBTTagString;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayInChat;
import net.minecraft.server.v1_16_R3.PacketPlayInItemName;
import net.minecraft.server.v1_16_R3.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import net.minecraft.server.v1_16_R3.PacketPlayOutCombatEvent;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R3.PacketPlayOutNBTQuery;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenWindowMerchant;
import net.minecraft.server.v1_16_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_16_R3.PacketPlayOutWindowItems;
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
                    return Collections.emptyList(); // cancel packet
                }
            }
            if (p.components != null) {
                if (packetData.modifyField("components", Util::filterBaseComponentArray) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutCombatEvent) {
            if (packetData.modifyField("e", VersionUtil::filterComponent) == null) {
                return Collections.emptyList(); // cancel packet
            }
            System.err.println((Object) packetData.getField("e"));
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
            if (packetData.getField("c") != null) {
                if (packetData.modifyField("c", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutOpenWindowMerchant) {
            for (MerchantRecipe merchantRecipe : packetData.<MerchantRecipeList>getField("b")) {
                filterItemStack(merchantRecipe.buyingItem1);
                filterItemStack(merchantRecipe.buyingItem2);
                filterItemStack(merchantRecipe.sellingItem);
            }
        } else if (packet instanceof PacketPlayOutNBTQuery) {
            filterNBTTagCompound(packetData.getField("b"));
        } else if (packet instanceof PacketPlayOutEntityEquipment) {
            if (packetData.getField("b") != null) {
                for (Pair<EnumItemSlot, ItemStack> pair : packetData.<List<Pair<EnumItemSlot, ItemStack>>>getField("b")) {
                    filterItemStack(pair.getSecond());
                }
            }
        } else if (packet instanceof PacketPlayOutSetSlot) {
            filterItemStack(packetData.getField("c"));
        } else if (packet instanceof PacketPlayOutTileEntityData) {
            filterNBTTagCompound(packetData.getField("c"));
        } else if (packet instanceof PacketPlayOutMapChunk) {
            filterNBTTagCompound(packetData.getField("d"));
            if (packetData.getField("g") != null) {
                for (NBTTagCompound tag : packetData.<List<NBTTagCompound>>getField("g")) {
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
        } else if (packet instanceof PacketPlayInSetCreativeSlot) {
            filterItemStack(((PacketPlayInSetCreativeSlot) packet).getItemStack());
        } else if (packet instanceof PacketPlayInItemName) {
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
        Util.setField(NBTTagCompound.class, "map", tag, newMap);
        return tag;
    }

    @Contract(value = "_ -> param1", mutates = "param1")
    @Nullable
    public static NBTBase filterNBTBase(@Nullable NBTBase value) {
        if (value instanceof NBTTagString) {
            if (Util.isTaintedString(value.asString())) {
                return NBTTagString.a(Util.sanitizeString(value.asString()));
            }
        } else if (value instanceof NBTTagCompound) {
            return filterNBTTagCompound((NBTTagCompound) value);
        } else if (value instanceof NBTTagList) {
            NBTTagList newList = new NBTTagList();
            for (NBTBase entry : (NBTTagList) value) {
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
}
