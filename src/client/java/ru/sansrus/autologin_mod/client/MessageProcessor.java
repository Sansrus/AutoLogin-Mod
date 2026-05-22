package ru.sansrus.autologin_mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageProcessor {
    private static JSONConfigHandler.PlayerConfig config;
    private static final Pattern WS_PATTERN = Pattern.compile("[^\\S\\r\\n]+");
    private static final Pattern TRAIL_COMMAS = Pattern.compile("[,]+$");
    public static List<String> triggers;
    private static final Logger LOGGER = LoggerFactory.getLogger("AutoLogin/MessageProcessor");

    static {
        reloadConfig();
    }

    public static void reloadConfig() {
        config = JSONConfigHandler.getCurrentPlayerConfig();
        triggers = Arrays.stream(config.triggers.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static void processMessage(Component message, Logger logger) {
        String text = TRAIL_COMMAS.matcher(
                WS_PATTERN.matcher(message.getString())
                        .replaceAll("")
                        .replace("/", "")
        ).replaceAll("");
        reloadConfig();

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || config == null) return;

        if (!config.currentCheck) return;

        String serverAddress = client.getCurrentServer() != null ? client.getCurrentServer().ip : null;
        if (serverAddress == null) return;

        String password = config.passwords.get(serverAddress);
        if (password == null) return;

        boolean isAutoSend = config.autosend;
        if (isAutoSend) return;

        for (String trigger : triggers) {
            if (text.contains(trigger.trim())) {
                Objects.requireNonNull(client.getConnection()).sendCommand(trigger + " " + password);
                reloadConfig();
                config.currentCheck = false;
                JSONConfigHandler.saveCurrentPlayerConfig(config);
                return;
            }
        }
    }
}
