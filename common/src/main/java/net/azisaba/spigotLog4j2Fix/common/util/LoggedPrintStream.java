package net.azisaba.spigotLog4j2Fix.common.util;

import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

public class LoggedPrintStream extends PrintStream {
    private final Logger logger;

    public LoggedPrintStream(Logger logger, OutputStream outputStream) {
        super(outputStream);
        this.logger = logger;
    }

    public void println(@Nullable String s) {
        this.logLine(s);
    }

    public void println(Object object) {
        this.logLine(String.valueOf(object));
    }

    protected void logLine(@Nullable String s) {
        if (logger == null) {
            System.out.println(s);
        } else {
            logger.info(s);
        }
    }
}
