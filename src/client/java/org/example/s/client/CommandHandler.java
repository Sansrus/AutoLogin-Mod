package org.example.s.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.example.s.PasswordGenerator;

public class CommandHandler {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            registerCommands(dispatcher, null, null);
        });
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                        CommandRegistryAccess registryAccess,
                                        CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(ClientCommandManager.literal("autoreg")
                .executes(context -> {
                    String password = PasswordGenerator.generate(15);
                    MinecraftClient client = MinecraftClient.getInstance();
                    ServerInfo server = client.getCurrentServerEntry();

                    if (server != null) {
                        JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
                        config.passwords.put(server.address, password);
                        JSONConfigHandler.saveConfig(config);

                        // Отправляем команду на регистрацию
                        client.getNetworkHandler().sendChatCommand("register " + password + " " + password);

                        // Выводим сообщение в чат
                        client.player.sendMessage(Text.translatable("message.autoreg", server.address, password), false);
                    }
                    return 1;
                }));

        dispatcher.register(ClientCommandManager.literal("autologin")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("password", StringArgumentType.string())
                                .executes(context -> {
                                    String password = StringArgumentType.getString(context, "password");
                                    MinecraftClient client = MinecraftClient.getInstance();
                                    ServerInfo server = client.getCurrentServerEntry();

                                    if (server != null) {
                                        JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
                                        config.passwords.put(server.address, password);
                                        JSONConfigHandler.saveConfig(config);

                                        // Выводим сообщение в чат
                                        client.player.sendMessage(Text.translatable("message.autologin", server.address, password), false);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}