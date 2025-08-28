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

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandHandler {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            registerCommands(dispatcher, null, null);
        });
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                        CommandRegistryAccess registryAccess,
                                        CommandManager.RegistrationEnvironment environment) {
        // 1) Определяем основную команду autologin
        LiteralArgumentBuilder<FabricClientCommandSource> autologinCommand = literal("autologin")
                .then(literal("add")
                        .then(ClientCommandManager.argument("password", StringArgumentType.string())
                                .executes(CommandHandler::handleAddPassword)
                        )
                )
                .executes(CommandHandler::handleAutoLogin);

        // Регистрируем основную команду и сохраняем её узел
        CommandNode<FabricClientCommandSource> autologinNode = dispatcher.register(autologinCommand);

        // 2) Регистрируем алиас "al" и перенаправляем туда всё из autologin,
        dispatcher.register(
                ClientCommandManager.literal("al")
                        // без аргументов → /autologin
                        .executes(ctx -> dispatcher.execute("autologin", ctx.getSource()))
                        // подкоманда add → перенаправляем именно на ноду autologin.add
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("password", StringArgumentType.greedyString())
                                        .redirect(autologinNode.getChild("add"))
                                )
                        )
        );

        // 3) Определяем и регистрируем основную команду autoreg
        LiteralArgumentBuilder<FabricClientCommandSource> autoreg = literal("autoreg")
                .executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ServerInfo server = client.getCurrentServerEntry();
                    if (server == null) {
                        return 0;
                    }
                    String address = server.address;
                    JSONConfigHandler.PlayerConfig cfg = JSONConfigHandler.getCurrentPlayerConfig();

                    // Если пароль уже есть — сообщаем и выходим
                    if (cfg.passwords.containsKey(address)) {
                        client.player.sendMessage(Text.translatable("error.already_password", address), false);
                        return 0;
                    }

                    // Иначе — генерируем, сохраняем и отправляем в чат
                    String password = PasswordGenerator.generate(15);
                    cfg.passwords.put(address, password);
                    JSONConfigHandler.saveCurrentPlayerConfig(cfg);

                    if (client.getNetworkHandler() != null) {
                        client.getNetworkHandler().sendChatCommand("register " + password + " " + password);
                    }
                    client.player.sendMessage(Text.translatable("message.autoreg", address), false);
                    return 1;
                });

        // Регистрируем autoreg и сохраняем его узел
        CommandNode<FabricClientCommandSource> autoregNode = dispatcher.register(autoreg);

        // 4) Регистрируем алиас "ar" для autoreg (без дополнительных подкоманд)
        dispatcher.register(
                ClientCommandManager.literal("ar")
                        // без аргументов → /autoreg
                        .executes(ctx -> dispatcher.execute("autoreg", ctx.getSource()))
        );

    }

    private static int handleAddPassword(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String password = StringArgumentType.getString(context, "password");
        MinecraftClient client = MinecraftClient.getInstance();
        ServerInfo server = client.getCurrentServerEntry();

        if (server != null) {
            JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
            config.passwords.put(server.address, password);
            JSONConfigHandler.saveCurrentPlayerConfig(config);

            assert client.player != null;
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

        JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
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
            config = JSONConfigHandler.getCurrentPlayerConfig();
            config.currentCheck = true;
            JSONConfigHandler.saveCurrentPlayerConfig(config);
        }

        return 1;
    }
}