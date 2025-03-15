package org.example.s.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import org.example.s.client.JSONConfigHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        Text message = packet.content();
        String text = message.getString().replaceAll("[^\\S\\r\\n]+", "").replaceAll("[,]+$", "");

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
                client.getNetworkHandler().sendChatCommand(trigger.trim() + " " + password);
                config.currentCheck = false;
                JSONConfigHandler.saveConfig(config);
                return;
            }
        }
    }
}


