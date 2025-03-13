package org.example.s.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONConfigHandler {
    private static final File CONFIG_FILE = new File(MinecraftClient.getInstance().runDirectory, "config/autologin.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class ConfigData {
        public String triggers = "login";
        public boolean check_enabled = true;
        public boolean currentCheck = true; // Новый флаг
        public Map<String, String> passwords = new HashMap<>();
    }

    public static ConfigData loadConfig() {
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.getParentFile().mkdirs();
                CONFIG_FILE.createNewFile();
                saveConfig(new ConfigData());
            } catch (IOException e) {
                System.err.println("Ошибка при создании файла конфигурации: " + e.getMessage());
            }
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            return GSON.fromJson(reader, ConfigData.class);
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке конфигурации: " + e.getMessage());
            return new ConfigData();
        }
    }

    public static void saveConfig(ConfigData config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении конфигурации: " + e.getMessage());
        }
    }
}