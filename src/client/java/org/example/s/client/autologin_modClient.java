package org.example.s.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class autologin_modClient implements ModMenuApi, ClientModInitializer {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AutoLoginConfigScreen::new;
    }

    @Override
    public void onInitializeClient() {
        CommandHandler.init();
        ServerLeaveHandler.init();
        JSONConfigHandler.getCurrentPlayerConfig();
    }
}
