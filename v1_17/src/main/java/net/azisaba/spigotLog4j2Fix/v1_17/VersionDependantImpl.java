package net.azisaba.spigotLog4j2Fix.v1_17;

import io.netty.channel.Channel;
import net.azisaba.spigotLog4j2Fix.common.VersionDependant;
import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.v1_17.listener.EventListeners;
import net.azisaba.spigotLog4j2Fix.v1_17.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VersionDependantImpl implements VersionDependant {
    @Override
    public void registerEvents(@NotNull Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new EventListeners(), plugin);
    }

    @Override
    public @NotNull Channel getChannel(@NotNull Player player) {
        return ((CraftPlayer) player).getHandle().b.a.k;
    }

    @Override
    public List<Object> processOutgoingPacket(@NotNull PacketData packet) {
        return VersionUtil.processOutgoingPacket(packet);
    }
}
