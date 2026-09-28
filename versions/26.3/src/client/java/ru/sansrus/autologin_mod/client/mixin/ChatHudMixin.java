package ru.sansrus.autologin_mod.client.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sansrus.autologin_mod.client.MessageProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChatHudMixin");
    @Inject(at = @At("HEAD"),
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            cancellable = true)
    private void onAddMessage(Component message, MessageSignature signatureData, GuiMessageSource source, GuiMessageTag indicator, CallbackInfo ci) {
        MessageProcessor.processMessage(message, LOGGER);
    }
}
