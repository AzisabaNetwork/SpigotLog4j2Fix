package net.azisaba.spigotLog4j2Fix.plugin.listener;

import net.azisaba.spigotLog4j2Fix.common.util.PacketUtil;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        PacketUtil.inject(e.getPlayer());
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        for (int i = 0; i < e.getLines().length; i++) {
            if (Util.isTaintedString(e.getLine(i))) {
                e.setLine(i, Util.sanitizeString(e.getLine(i)));
            }
        }
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        if (Util.isTaintedItemMeta(e.getNewBookMeta())) {
            e.setNewBookMeta(Util.sanitizeItemMeta(e.getNewBookMeta()));
        }
    }
}
