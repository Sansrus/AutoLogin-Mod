package ru.sansrus.autologin_mod.client;

import net.fabricmc.api.ClientModInitializer;

public class AutologinmodClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CommandHandler.init();
        ServerLeaveHandler.init();
        ServerJoinHandler.init();
        JSONConfigHandler.getCurrentPlayerConfig();

    }
}
