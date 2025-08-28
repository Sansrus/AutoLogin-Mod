package org.example.s.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageProcessor {
    private static JSONConfigHandler.PlayerConfig config;
    private static final Pattern WS_PATTERN = Pattern.compile("[^\\S\\r\\n]+");
    private static final Pattern TRAIL_COMMAS = Pattern.compile("[,]+$");
    public static List<String> triggers;

    static {
        reloadConfig();  // и там же заполнить triggers из config.triggers
    }

    public static void reloadConfig() {
        config = JSONConfigHandler.getCurrentPlayerConfig();
        triggers = Arrays.stream(config.triggers.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static void processMessage(Text message, Logger logger) {
        String text = TRAIL_COMMAS.matcher(
                WS_PATTERN.matcher(message.getString())
                        .replaceAll("")
                        .replace("/", "")
        ).replaceAll("");
        reloadConfig();


        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        if (!config.currentCheck) return;

        String serverAddress = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : null;
        if (serverAddress == null) return;

        String password = config.passwords.get(serverAddress);
        if (password == null) return;

        for (String trigger : triggers) {
            if (text.contains(trigger.trim())) {
                client.getNetworkHandler().sendChatCommand("login " + password);
                reloadConfig();
                config.currentCheck = false;
                JSONConfigHandler.saveCurrentPlayerConfig(config);
                return;
            }
        }
    }
}

