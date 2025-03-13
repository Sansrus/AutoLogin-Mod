package org.example.s.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import org.example.s.client.JSONConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void onChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();

        if (!config.currentCheck) return;

        String text = message.getContent().getString();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

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
