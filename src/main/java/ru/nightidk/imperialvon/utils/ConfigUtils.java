package ru.nightidk.imperialvon.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.nightidk.imperialvon.ImperialVon;
import ru.nightidk.imperialvon.configuration.AuthVariables;
import ru.nightidk.imperialvon.configuration.ConfigVariables;

import java.io.*;
import java.util.Properties;

public class ConfigUtils {
    public static void loadAuth(File file) {
        try {
            if (!file.exists() || !file.canRead())
                saveAuth(file);
            FileReader fis = new FileReader(file);;
            AuthVariables.JSON_AUTH = (JsonObject) JsonParser.parseReader(fis);
            fis.close();
        }  catch (Exception e) {
            e.printStackTrace();
            ImperialVon.LOG.info("Something goes wrong when loading auth file.");
        }
    }

    public static void saveAuth(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        if (AuthVariables.JSON_AUTH.size() == 0)
            AuthVariables.JSON_AUTH.add("users", new JsonArray());
        fos.write(AuthVariables.JSON_AUTH.toString().getBytes());
        fos.close();
    }

    public static void loadConfig(File file) {
        try {
            if (!file.exists() || !file.canRead())
                saveConfig(file);
            FileInputStream fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            fis.close();
            ConfigVariables.TICK_FOR_CLEAN = Integer.parseInt((String) properties.computeIfAbsent("tick_for_clean", vr -> "36000"));
            ConfigVariables.MAINTANCE = Boolean.parseBoolean((String) properties.computeIfAbsent("maintains", vr -> "false"));
            ConfigVariables.RESTART_TIME = Integer.parseInt((String) properties.computeIfAbsent("restart_time", vr -> "432000"));
            ConfigVariables.AUTORESTART = Boolean.parseBoolean((String) properties.computeIfAbsent("auto_restart", vr -> "true"));
            ConfigVariables.DISCORD_AUTH = (String) properties.computeIfAbsent("discord", vr -> "");
            ConfigVariables.DISCORD_STATUS_MESSAGE = (String) properties.computeIfAbsent("status_message", vr -> "");
            ConfigVariables.MAX_CHUNK_REGION = Integer.parseInt((String) properties.computeIfAbsent("region_size", vr -> "3"));
        } catch (Exception e) {
            ImperialVon.LOG.error(e);
            ConfigVariables.TICK_FOR_CLEAN = 36000;
            ConfigVariables.MAINTANCE = false;
            ConfigVariables.RESTART_TIME = 432000;
            ConfigVariables.AUTORESTART = true;
            ConfigVariables.DISCORD_AUTH = "";
            ConfigVariables.DISCORD_STATUS_MESSAGE = "";
            ConfigVariables.MAX_CHUNK_REGION = 3;
        }
    }

    public static void saveConfig(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write("# ImperialVon Config".getBytes());
        fos.write("\n".getBytes());
        fos.write(("tick_for_clean=" + ConfigVariables.TICK_FOR_CLEAN).getBytes());
        fos.write("\n".getBytes());
        fos.write(("maintains=" + ConfigVariables.MAINTANCE).getBytes());
        fos.write("\n".getBytes());
        fos.write(("restart_time=" + ConfigVariables.RESTART_TIME).getBytes());
        fos.write("\n".getBytes());
        fos.write(("auto_restart=" + ConfigVariables.AUTORESTART).getBytes());
        fos.write("\n".getBytes());
        fos.write(("region_size=" + ConfigVariables.MAX_CHUNK_REGION).getBytes());
        fos.write("\n".getBytes());
        fos.write(("discord=" + ConfigVariables.DISCORD_AUTH).getBytes());
        fos.write("\n".getBytes());
        fos.write(("status_message=" + ConfigVariables.DISCORD_STATUS_MESSAGE).getBytes());
        fos.close();
    }
}