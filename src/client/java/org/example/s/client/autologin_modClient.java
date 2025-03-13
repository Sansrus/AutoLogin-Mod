package org.example.s.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.terraformersmc.modmenu.ModMenu.MOD_ID;

public class autologin_modClient implements ModMenuApi, ClientModInitializer {
    static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        autologin_modClient.LOGGER.info("getModConfigScreenFactory вызван");
        return AutoLoginConfigScreen::new;
    }

    @Override
    public void onInitializeClient() {
        // Клиентская инициализация, если требуется
        autologin_modClient.LOGGER.info("Клиентская инициализация AutoLoginConfig");
        CommandHandler.init();
        ServerLeaveHandler.init();
    }
}
