package org.example.s.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ServerLeaveHandler {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getCurrentServerEntry() == null) {
                JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
                if (!config.currentCheck) {
                    config.currentCheck = true;
                    JSONConfigHandler.saveCurrentPlayerConfig(config);
                }
            }
        });
    }
}