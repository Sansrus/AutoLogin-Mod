package org.example.s;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class autologin_mod implements ModInitializer {
    public static final String MOD_ID = "autologin_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Загрузка конфигурации при инициализации мода
        loadConfig();
    }

    public static void loadConfig() {
        // Реализация загрузки из Properties
    }

    public static void saveConfig() {
        // Реализация сохранения в Properties
    }
}