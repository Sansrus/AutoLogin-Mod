package org.example.s.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
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
        // Общий обработчик для autologin и al
        LiteralArgumentBuilder<FabricClientCommandSource> autologinCommand = ClientCommandManager.literal("autologin")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("password", StringArgumentType.string())
                                .executes(context -> handleAddPassword(context))
                        )
                )
                .executes(context -> handleAutoLogin(context));

        // Регистрация основной команды
        CommandNode<FabricClientCommandSource> autologinNode = dispatcher.register(autologinCommand);

        // Регистрируем алиас al с полным перенаправлением
        dispatcher.register(ClientCommandManager.literal("al")
                // Перенаправляем основной вызов
                .executes(context -> dispatcher.execute("autologin", context.getSource()))
                // Перенаправляем подкоманду add
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("password", StringArgumentType.string())
                                .redirect(autologinNode.getChild("add"))
                        )
                )
        );

        // Команда autoreg
        dispatcher.register(ClientCommandManager.literal("autoreg")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ServerInfo server = client.getCurrentServerEntry();

                    if (server != null) {
                        String password = PasswordGenerator.generate(15);
                        JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
                        config.passwords.put(server.address, password);
                        JSONConfigHandler.saveConfig(config);

                        // Отправка команды регистрации
                        if (client.getNetworkHandler() != null) {
                            client.getNetworkHandler().sendChatCommand("register " + password + " " + password);
                        }

                        client.player.sendMessage(
                                Text.translatable("message.autoreg", server.address),
                                false
                        );
                    }
                    return 1;
                })
        );
    }

    private static int handleAddPassword(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String password = StringArgumentType.getString(context, "password");
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo server = client.getCurrentServerEntry();

        if (server != null) {
            JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
            config.passwords.put(server.address, password);
            JSONConfigHandler.saveConfig(config);

            client.player.sendMessage(
                    Text.translatable("message.autologin", server.address, password),
                    false
            );
        }
        return 1;
    }

    private static int handleAutoLogin(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();
        String serverAddress = client.getCurrentServerEntry() != null
                ? client.getCurrentServerEntry().address
                : null;

        if (serverAddress == null) {
            client.player.sendMessage(Text.translatable("error.not_connected"), false);
            return 0;
        }

        JSONConfigHandler.ConfigData config = JSONConfigHandler.loadConfig();
        String password = config.passwords.get(serverAddress);

        if (password == null || password.isEmpty()) {
            client.player.sendMessage(Text.translatable("error.no_password"), false);
            return 0;
        }

        String[] triggers = config.triggers.split(",");
        if (triggers.length == 0 || triggers[0].trim().isEmpty()) {
            client.player.sendMessage(Text.translatable("error.no_triggers"), false);
            return 0;
        }

        // Формируем и отправляем команду БЕЗ кавычек
        String command = String.format("%s %s", triggers[0].trim(), password);

        // Правильный способ отправки команды
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand(command);
        }

        return 1;
    }
}