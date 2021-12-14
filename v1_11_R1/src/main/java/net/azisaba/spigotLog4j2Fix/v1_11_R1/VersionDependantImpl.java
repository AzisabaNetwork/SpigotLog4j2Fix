package net.azisaba.spigotLog4j2Fix.v1_11_R1;

import io.netty.channel.Channel;
import net.azisaba.spigotLog4j2Fix.common.VersionDependant;
import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.v1_11_R1.listener.EventListeners;
import net.azisaba.spigotLog4j2Fix.v1_11_R1.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
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
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
    }

    @Override
    public List<Object> processOutgoingPacket(@NotNull PacketData packetData) {
        return VersionUtil.processOutgoingPacket(packetData);
    }

    @Override
    public List<Object> processIncomingPacket(@NotNull PacketData packetData) {
        return VersionUtil.processIncomingPacket(packetData);
    }
}
