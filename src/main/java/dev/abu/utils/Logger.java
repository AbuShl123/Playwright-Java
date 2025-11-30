package dev.abu.utils;

import org.slf4j.LoggerFactory;

public class Logger {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);

    public static void log(Status status, String msg) {
        String stat = status == Status.INFO ? "INFO" : "ERROR";
        Logger.log.info("{} - {}", stat, msg);
    }

    public static void log(String msg) {
        Logger.log.info(msg);
    }
}
