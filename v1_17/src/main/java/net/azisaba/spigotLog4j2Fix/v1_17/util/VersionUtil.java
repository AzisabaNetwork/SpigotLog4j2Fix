package net.azisaba.spigotLog4j2Fix.v1_17.util;

import com.mojang.datafixers.util.Pair;
import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.ChatBaseComponent;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.PacketPlayInChat;
import net.minecraft.network.protocol.game.PacketPlayInItemName;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutNBTQuery;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindowMerchant;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantRecipe;
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
        } else if (packet instanceof ClientboundSetTitleTextPacket p) {
            if (p.b() != null) {
                if (packetData.modifyField("a", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutWindowItems p) {
            for (ItemStack stack : p.c()) {
                filterItemStack(stack);
            }
        } else if (packet instanceof PacketPlayOutOpenWindow p) {
            if (p.d() != null) {
                if (packetData.modifyField("c", VersionUtil::filterComponent) == null) {
                    return Collections.emptyList();
                }
            }
        } else if (packet instanceof PacketPlayOutOpenWindowMerchant p) {
            for (MerchantRecipe merchantRecipe : p.c()) {
                filterItemStack(merchantRecipe.a);
                filterItemStack(merchantRecipe.b);
                filterItemStack(merchantRecipe.c);
            }
        } else if (packet instanceof PacketPlayOutNBTQuery) {
            filterNBTTagCompound(packetData.getField("b"));
        } else if (packet instanceof PacketPlayOutEntityEquipment p) {
            if (p.c() != null) {
                for (Pair<EnumItemSlot, ItemStack> pair : p.c()) {
                    filterItemStack(pair.getSecond());
                }
            }
        } else if (packet instanceof PacketPlayOutSetSlot) {
            filterItemStack(packetData.getField("f"));
        } else if (packet instanceof PacketPlayOutTileEntityData p) {
            filterNBTTagCompound(p.d());
        } else if (packet instanceof PacketPlayOutMapChunk p) {
            filterNBTTagCompound(p.f());
            if (p.g() != null) {
                for (NBTTagCompound tag : p.g()) {
                    filterNBTTagCompound(tag);
                }
            }
        }
        return Collections.singletonList(packet);
    }

    public static List<Object> processIncomingPacket(@NotNull PacketData packetData) {
        Packet<?> packet = packetData.getPacket();
        if (packet instanceof PacketPlayInChat) {
            packetData.modifyField("b", Util::sanitizeString);
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
        Map<String, NBTBase> map = new HashMap<>(Util.getField(NBTTagCompound.class, "x", tag));
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
        if (value instanceof NBTTagString stringTag) {
            if (Util.isTaintedString(stringTag.asString())) {
                return NBTTagString.a(Util.sanitizeString(stringTag.asString()));
            }
        } else if (value instanceof NBTTagCompound compound) {
            return filterNBTTagCompound(compound);
        } else if (value instanceof NBTTagList list) {
            NBTTagList newList = new NBTTagList();
            for (NBTBase entry : new ArrayList<>(list)) {
                newList.add(filterNBTBase(entry));
            }
            return newList;
        }
        return value;
    }

    @Nullable
    public static IChatBaseComponent filterComponent(@Nullable IChatBaseComponent component) {
        if (component == null) return null;
        if (component instanceof ChatMessage chatMessage) {
            List<Object> args = new ArrayList<>();
            for (Object o : chatMessage.getArgs()) {
                if (o instanceof IChatBaseComponent) {
                    o = filterComponent((IChatBaseComponent) o);
                    //noinspection ReplaceNullCheck
                    if (o != null) {
                        args.add(o);
                    } else {
                        args.add(new ChatComponentText("?"));
                    }
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
