package net.azisaba.spigotLog4j2Fix.common;

import io.netty.channel.Channel;
import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface VersionDependant {
    void registerEvents(@NotNull Plugin plugin);

    @NotNull
    Channel getChannel(@NotNull Player player);

    /**
     * @return packets which would be sent to a player. Returning empty list will effectively cancel the packet.
     */
    List<Object> processOutgoingPacket(@NotNull PacketData packetData);
}
