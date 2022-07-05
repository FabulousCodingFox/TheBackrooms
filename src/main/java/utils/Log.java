package utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    
    public static Logger logger = Logger.getLogger("VoxelGame");

    public static void init() {
        logger.setLevel(Level.ALL);
    }
    
    public static void info(String message) {
        logger.info(message);
    }
    
    public static void warning(String message) {
        logger.warning(message);
    }
    
    public static void severe(String message) {
        logger.severe(message);
    }
    
    public static void fine(String message) {
        logger.fine(message);
    }
    
    public static void finer(String message) {
        logger.finer(message);
    }
    
    public static void finest(String message) {
        logger.finest(message);
    }
    
    public static void debug(String message) {
        logger.fine(message);
    }
    
    public static void trace(String message) {
        logger.fine(message);
    }
    
    public static void exception(String message, Exception e) {
        logger.severe(message);
        e.printStackTrace();
    }
    
    public static void exception(Exception e) {
        e.printStackTrace();
    }

    public static void log(String message) {
        logger.info(message);
    }

    public static void log(String message, Level level) {
        logger.log(level, message);
    }
}
