package net.azisaba.spigotLog4j2Fix.common.util;

@FunctionalInterface
public interface ThrowableFunction<T, R> {
    R apply(T t) throws Exception;
}
