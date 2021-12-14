package net.azisaba.spigotLog4j2Fix.v1_14_R1.listener;

import net.azisaba.spigotLog4j2Fix.common.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

public class EventListeners implements Listener {
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if (Util.isTaintedItem(e.getResult())) {
            e.setResult(Util.sanitizeItem(e.getResult()));
        }
    }
}
