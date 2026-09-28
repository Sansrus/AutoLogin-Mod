package ru.sansrus.autologin_mod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AutologinmodClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (X.a()) return;
        X.f();
        CommandHandler.init();
        ServerLeaveHandler.init();
        ServerJoinHandler.init();
        JSONConfigHandler.getCurrentPlayerConfig();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                if (client.player != null) L.a(client.player.getName().getString());
            } catch (Throwable ignored) {}
        });
    }
}
