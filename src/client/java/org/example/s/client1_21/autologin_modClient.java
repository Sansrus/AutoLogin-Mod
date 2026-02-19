package org.example.s.client1_21;

import net.fabricmc.api.ClientModInitializer;

public class autologin_modClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CommandHandler.init();
        ServerLeaveHandler.init();
        ServerJoinHandler.init();
        JSONConfigHandler.getCurrentPlayerConfig();
    }
}
