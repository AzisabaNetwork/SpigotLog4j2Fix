package net.azisaba.spigotLog4j2Fix.common;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

public class SpigotLog4j2Fix {
    private static ISpigotLog4j2Fix api;

    public static void setAPI(ISpigotLog4j2Fix api) {
        if (SpigotLog4j2Fix.api != null) throw new IllegalStateException("Cannot redefine API singleton");
        SpigotLog4j2Fix.api = api;
    }

    @NotNull
    public static ISpigotLog4j2Fix getAPI() {
        return Objects.requireNonNull(api, "API isn't defined yet");
    }

    @NotNull
    public static VersionDependant getVersionDependant() {
        return getAPI().getVersionDependant();
    }

    @NotNull
    public static Logger getLogger() {
        return getAPI().getLogger();
    }

    @NotNull
    public static Plugin getPlugin() {
        return getAPI().getPlugin();
    }
}
