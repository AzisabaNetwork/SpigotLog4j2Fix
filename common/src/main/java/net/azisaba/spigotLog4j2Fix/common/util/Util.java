package net.azisaba.spigotLog4j2Fix.common.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class Util {
    @NotNull
    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    @NotNull
    public static String getImplVersion() {
        String v = getServerVersion();
        if (v.equals("v1_12_R1")) return v;
        if (v.equals("v1_15_R1")) return v;
        if (v.equals("v1_16_R3")) return v;
        if (v.equals("v1_17_R1")) return "v1_17";
        throw new RuntimeException("Unsupported version: " + v);
    }

    @Nullable
    public static Field findField(@NotNull Class<?> clazz, @NotNull String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (ReflectiveOperationException ignore) {}
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            Field field = findField(superClass, name);
            if (field != null) return field;
        }
        for (Class<?> c : clazz.getInterfaces()) {
            Field field = findField(c, name);
            if (field != null) return field;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(@Nullable Class<?> clazz, @NotNull String name, Object instance) {
        if (clazz == null) clazz = instance.getClass();
        try {
            Field f = findField(clazz, name);
            if (f == null) throw new RuntimeException("Could not find field '" + name + "' in " + clazz.getTypeName() + " and its superclass/interfaces");
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract("_, _, _, _ -> param4")
    public static <T> T setField(@Nullable Class<?> clazz, @NotNull String name, Object instance, T value) {
        if (clazz == null) clazz = instance.getClass();
        try {
            Field f = findField(clazz, name);
            if (f == null) throw new RuntimeException("Could not find field '" + name + "' in " + clazz.getTypeName() + " and its superclass/interfaces");
            f.setAccessible(true);
            f.set(instance, value);
            return value;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract
    public static <T, R> R modifyField(@Nullable Class<?> clazz, @NotNull String name, Object instance, @NotNull Function<T, R> function) {
        if (clazz == null) clazz = instance.getClass();
        T t = getField(clazz, name, instance);
        R r = function.apply(t);
        if (Objects.equals(t, r)) return r;
        setField(clazz, name, instance, r);
        return r;
    }

    @Contract
    public static <T> List<T> modifyList(@Nullable Class<?> clazz, @NotNull String name, Object instance, @NotNull Function<T, T> mapFunction) {
        return modifyList(clazz, name, instance, mapFunction, false);
    }

    @Contract
    public static <T> List<T> modifyList(@Nullable Class<?> clazz, @NotNull String name, Object instance, @NotNull Function<T, T> mapFunction, boolean includeNulls) {
        if (clazz == null) clazz = instance.getClass();
        List<T> list = getField(clazz, name, instance);
        if (list == null) return null;
        list = new ArrayList<>(list);
        List<T> toAdd = new ArrayList<>();
        for (T t : list) toAdd.add(mapFunction.apply(t));
        list.clear();
        list.addAll(toAdd);
        return list;
    }

    public static <T> boolean listEquals(@NotNull List<T> list, @NotNull List<T> another) {
        if (list.size() != another.size()) return false;
        if (list.isEmpty()) return true;
        for (int i = 0; i < list.size(); i++) {
            T t1 = list.get(i);
            T t2 = another.get(i);
            if (!Objects.equals(t1, t2)) return false;
        }
        return true;
    }

    @Contract(pure = true)
    @NotNull
    public static Class<?> getClass(@NotNull String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract("null -> null")
    public static BaseComponent[] filterBaseComponentArray(@Nullable BaseComponent[] components) {
        if (components == null) return null;
        return filterBaseComponents(Arrays.asList(components)).toArray(new BaseComponent[0]);
    }

    @NotNull
    public static List<BaseComponent> filterBaseComponents(@Nullable List<BaseComponent> component) {
        if (component == null) {
            return Collections.singletonList(new TextComponent(""));
        }
        List<BaseComponent> newList = new ArrayList<>();
        for (BaseComponent baseComponent : component) newList.add(filterBaseComponent(baseComponent));
        return newList;
    }

    @Nullable
    public static BaseComponent filterBaseComponent(@Nullable BaseComponent component) {
        if (component == null) return null;
        modifyList(BaseComponent.class, "extra", component, Util::filterBaseComponent, false);
        modifyField(BaseComponent.class, "insertion", component, Util::sanitizeString);
        if (component instanceof TextComponent) {
            modifyField(TextComponent.class, "text", component, Util::sanitizeString);
        }
        if (component instanceof TranslatableComponent) {
            modifyField(TranslatableComponent.class, "translate", component, Util::sanitizeString);
            modifyList(TranslatableComponent.class, "with", component, Util::filterBaseComponent, false);
        }
        if (component.getClass().getTypeName().equals("net.md_5.bungee.api.chat.ScoreComponent")) {
            modifyField(null, "name", component, Util::sanitizeString);
            modifyField(null, "objective", component, Util::sanitizeString);
            modifyField(null, "value", component, Util::sanitizeString);
        }
        // SelectorComponent and more
        return component;
    }

    @Contract(value = "null -> null; !null -> new", pure = true)
    @Nullable
    public static List<String> sanitizeStringList(@Nullable List<String> list) {
        if (list == null) return null;
        List<String> newList = new ArrayList<>();
        for (String s : list) newList.add(sanitizeString(s));
        return newList;
    }

    @Contract(value = "null -> null; !null -> !null", pure = true)
    @Nullable
    public static String sanitizeString(@Nullable String input) {
        if (input == null) return null;
        return removeSomething(input).replaceAll("(?i)(\u00a7[0-9abcdefklmnorx])?j(\u00a7[0-9abcdefklmnorx])?n(\u00a7[0-9abcdefklmnorx])?d(\u00a7[0-9abcdefklmnorx])?i(\u00a7[0-9abcdefklmnorx])?:(\u00a7[0-9abcdefklmnorx])?l(\u00a7[0-9abcdefklmnorx])?d(\u00a7[0-9abcdefklmnorx])?a(\u00a7[0-9abcdefklmnorx])?p.*", "");
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static String removeSomething(@Nullable String input) {
        if (input == null) return null;
        return input.replaceAll("(?i)\\$\\{(.*):(.*)}", "");
    }

    @Contract("null -> false")
    public static boolean isTaintedStringList(@Nullable List<String> input) {
        if (input == null) return false;
        for (String s : input) {
            if (isTaintedString(s)) return true;
        }
        return false;
    }

    @Contract("null -> false")
    public static boolean isTaintedString(@Nullable String input) {
        if (input == null) return false;
        String s = ChatColor.stripColor(removeSomething(input));
        if (s.length() != input.length()) return true;
        return s.toLowerCase(Locale.ROOT).contains("jndi:ldap");
    }

    @Contract(value = "null -> null; !null -> param1", mutates = "param1")
    @Nullable
    public static ItemStack sanitizeItem(@Nullable ItemStack itemStack) {
        if (itemStack == null) return null;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            sanitizeItemMeta(meta);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @Contract(value = "null -> null; !null -> param1", mutates = "param1")
    @Nullable
    public static <T extends ItemMeta> T sanitizeItemMeta(@Nullable T meta) {
        if (meta == null) return null;
        meta.setDisplayName(sanitizeString(meta.getDisplayName()));
        meta.setLore(sanitizeStringList(meta.getLore()));
        if (meta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) meta;
            bookMeta.setAuthor(sanitizeString(bookMeta.getAuthor()));
            bookMeta.setTitle(sanitizeString(bookMeta.getTitle()));
            bookMeta.setPages(sanitizeStringList(bookMeta.getPages()));
        }
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwner(sanitizeString(skullMeta.getOwner()));
        }
        return meta;
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean isTaintedItem(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        ItemMeta meta = itemStack.getItemMeta();
        return isTaintedItemMeta(meta);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Contract(value = "null -> false", pure = true)
    public static boolean isTaintedItemMeta(@Nullable ItemMeta meta) {
        if (meta == null) return false;
        if (isTaintedString(meta.getDisplayName())) return true;
        if (isTaintedStringList(meta.getLore())) return true;
        if (meta instanceof BookMeta) {
            BookMeta bookMeta = (BookMeta) meta;
            if (isTaintedString(bookMeta.getAuthor())) return true;
            if (isTaintedString(bookMeta.getTitle())) return true;
            if (isTaintedStringList(bookMeta.getPages())) return true;
        }
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            if (isTaintedString(skullMeta.getOwner())) return true;
        }
        return false;
    }
}
