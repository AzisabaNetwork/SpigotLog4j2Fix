package net.azisaba.spigotLog4j2Fix.plugin;

import net.azisaba.spigotLog4j2Fix.common.SpigotLog4j2Fix;
import net.azisaba.spigotLog4j2Fix.plugin.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SpigotLog4j2FixPlugin extends JavaPlugin {
    private static SpigotLog4j2FixPlugin instance;

    @NotNull
    public static SpigotLog4j2FixPlugin getInstance() {
        return Objects.requireNonNull(instance, "Plugin is not initialized");
    }

    @Override
    public void onLoad() {
        instance = this;
        SpigotLog4j2FixImpl.init();
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        SpigotLog4j2Fix.getVersionDependant().registerEvents(this);
    }
}
