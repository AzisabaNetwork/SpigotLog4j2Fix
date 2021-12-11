package net.azisaba.spigotLog4j2Fix.plugin;

import net.azisaba.spigotLog4j2Fix.common.ISpigotLog4j2Fix;
import net.azisaba.spigotLog4j2Fix.common.SpigotLog4j2Fix;
import net.azisaba.spigotLog4j2Fix.common.VersionDependant;
import net.azisaba.spigotLog4j2Fix.common.util.Util;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class SpigotLog4j2FixImpl implements ISpigotLog4j2Fix {
    private final VersionDependant versionDependant;

    private SpigotLog4j2FixImpl() {
        try {
            this.versionDependant = (VersionDependant) Class.forName("net.azisaba.spigotLog4j2Fix." + Util.getImplVersion() + ".VersionDependantImpl").getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public VersionDependant getVersionDependant() {
        return versionDependant;
    }

    @NotNull
    @Override
    public Logger getLogger() {
        return getPlugin().getLogger();
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return SpigotLog4j2FixPlugin.getInstance();
    }

    @Override
    public boolean shouldDoLazy() {
        return getPlugin().getConfig().getBoolean("lazy", true);
    }

    static void init() {}

    static {
        SpigotLog4j2Fix.setAPI(new SpigotLog4j2FixImpl());
    }
}
