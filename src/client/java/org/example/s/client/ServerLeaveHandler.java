package org.example.s.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ServerLeaveHandler {
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getCurrentServerEntry() == null) {
                JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
                if (!config.currentCheck) {
                    config.currentCheck = true;
                    JSONConfigHandler.saveConfig(config);
                }
            }
        });
    }
}