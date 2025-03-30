package org.example.s.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;

public class MessageProcessor {
    public static void processMessage(Text message, Logger logger) {
        String text = message.getString()
                .replaceAll("[^\\S\\r\\n]+", "")
                .replaceAll("[,]+$", "");

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
        if (!config.currentCheck) return;

        String serverAddress = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : null;
        if (serverAddress == null) return;

        String password = config.passwords.get(serverAddress);
        if (password == null) return;

        for (String trigger : config.triggers.split(",")) {
            if (text.contains(trigger.trim())) {
                client.getNetworkHandler().sendChatCommand("login " + password);
                config.currentCheck = false;
                JSONConfigHandler.saveConfig(config);
                return;
            }
        }
    }
}

