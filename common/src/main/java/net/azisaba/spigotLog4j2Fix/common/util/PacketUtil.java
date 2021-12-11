package net.azisaba.spigotLog4j2Fix.common.util;

import net.azisaba.spigotLog4j2Fix.common.SpigotLog4j2Fix;
import net.azisaba.spigotLog4j2Fix.common.packet.handler.ChannelHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public class PacketUtil {
    public static void inject(@NotNull Player player) {
        ChannelHandler handler = new ChannelHandler(player);
        try {
            SpigotLog4j2Fix.getVersionDependant().getChannel(player).pipeline().addBefore("packet_handler", "spigotLog4j2Fix", handler);
            SpigotLog4j2Fix.getLogger().info("Injected packet handler for " + player.getName());
        } catch (NoSuchElementException ex) {
            Bukkit.getScheduler().runTaskLater(SpigotLog4j2Fix.getPlugin(), () -> {
                if (!player.isOnline()) return;
                try {
                    SpigotLog4j2Fix.getVersionDependant().getChannel(player).pipeline().addBefore("packet_handler", "spigotLog4j2Fix", handler);
                    SpigotLog4j2Fix.getLogger().info("Injected packet handler for " + player.getName());
                } catch (NoSuchElementException ignore) {
                    SpigotLog4j2Fix.getLogger().warning("Failed to inject packet handler to " + player.getName());
                }
            }, 10);
        }
    }
}
