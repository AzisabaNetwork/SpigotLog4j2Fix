package net.azisaba.spigotLog4j2Fix.common;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public interface ISpigotLog4j2Fix {
    @NotNull
    VersionDependant getVersionDependant();

    @NotNull
    Logger getLogger();

    @NotNull
    Plugin getPlugin();

    boolean shouldDoLazy();
}
