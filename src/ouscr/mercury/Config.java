package ouscr.mercury;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private static final Logger LOGGER = Logger.getLogger( Config.class.getName() );

    public static String ip;
    public static int port;
    public static String password;
    public static int fps;

    public static void readConfig() {
        Properties prop = new Properties();
        String fileName = "mercury.config";
        InputStream is = null;

        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Could not find mercury.config! mercury.config has been created.");
            try {
                FileOutputStream out = new FileOutputStream(fileName);
                prop.setProperty("server.ip","");
                prop.setProperty("server.port","6372");
                prop.setProperty("password","");
                prop.setProperty("fps","10");
                prop.store(out, "Top Secwet owo");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.log(Level.WARNING, "Could not create mercury.config!");
            }
            System.exit(0);
        }
        try {
            prop.load(is);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not read mercury.config!");
            System.exit(0);
        }

        ip = prop.getProperty("server.ip");
        port = Integer.parseInt(prop.getProperty("server.port"));
        password = prop.getProperty("password");
        fps = Integer.parseInt(prop.getProperty("fps"));
    }
}
