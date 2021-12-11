package net.azisaba.spigotLog4j2Fix.common.packet;

import net.azisaba.spigotLog4j2Fix.common.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.function.Function;

public class PacketData {
    private boolean cancelled = false;
    private final Player player;
    private final Object packet;

    public PacketData(@NotNull Player player, @NotNull Object packet) {
        this.player = player;
        this.packet = packet;
    }

    public boolean isCancelled() { return cancelled; }

    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @NotNull
    public Player getPlayer() { return player; }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getPacket() {
        return (T) packet;
    }

    @NotNull
    public String getPacketName() {
        return packet.getClass().getSimpleName();
    }

    @Contract(pure = true)
    public <T> T getField(@NotNull String name) {
        return Util.getField(packet.getClass(), name, packet);
    }

    @Contract(value = "_, _ -> param2", pure = true)
    public <T> T setField(@NotNull String name, T value) {
        return Util.setField(packet.getClass(), name, packet, value);
    }

    @Contract
    public <T, R> R modifyField(@NotNull String name, @NotNull Function<T, R> function) {
        T t = getField(name);
        R r = function.apply(t);
        setField(name, r);
        return r;
    }
}
