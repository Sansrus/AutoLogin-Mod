package ru.sansrus.autologin_mod.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import ru.sansrus.autologin_mod.PasswordGenerator;

public class CommandHandler {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            registerCommands(dispatcher);
        });
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> autologinCommand = ClientCommands.literal("autologin")
                .then(ClientCommands.literal("add")
                        .then(ClientCommands.argument("password", StringArgumentType.string())
                                .executes(CommandHandler::handleAddPassword)
                        )
                )
                .executes(CommandHandler::handleAutoLogin);

        CommandNode<FabricClientCommandSource> autologinNode = dispatcher.register(autologinCommand);

        dispatcher.register(
                ClientCommands.literal("al")
                        .executes(ctx -> dispatcher.execute("autologin", ctx.getSource()))
                        .then(ClientCommands.literal("add")
                                .then(ClientCommands.argument("password", StringArgumentType.greedyString())
                                        .redirect(autologinNode.getChild("add"))
                                )
                        )
        );

        LiteralArgumentBuilder<FabricClientCommandSource> autoreg = ClientCommands.literal("autoreg")
                .executes(ctx -> {
                    Minecraft client = Minecraft.getInstance();
                    ServerData server = client.getCurrentServer();
                    if (server == null) {
                        client.player.sendSystemMessage(Component.translatable("error.not_connected"));
                        return 0;
                    }
                    String address = server.ip;
                    JSONConfigHandler.PlayerConfig cfg = JSONConfigHandler.getCurrentPlayerConfig();

                    if (cfg.passwords.containsKey(address)) {
                        client.player.sendSystemMessage(Component.translatable("error.already_password", address));
                        return 0;
                    }

                    String password = PasswordGenerator.generate(15);
                    cfg.passwords.put(address, password);
                    JSONConfigHandler.saveCurrentPlayerConfig(cfg);

                    if (client.getConnection() != null) {
                        client.getConnection().sendCommand("register " + password + " " + password);
                    }
                    client.player.sendSystemMessage(Component.translatable("message.autoreg", address));
                    return 1;
                });

        CommandNode<FabricClientCommandSource> autoregNode = dispatcher.register(autoreg);

        dispatcher.register(
                ClientCommands.literal("ar")
                        .executes(ctx -> dispatcher.execute("autoreg", ctx.getSource()))
        );

    }

    private static int handleAddPassword(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String password = StringArgumentType.getString(context, "password");
        Minecraft client = Minecraft.getInstance();
        ServerData server = client.getCurrentServer();

        if (server != null) {
            JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
            config.passwords.put(server.ip, password);
            JSONConfigHandler.saveCurrentPlayerConfig(config);

            assert client.player != null;
            client.player.sendSystemMessage(
                    Component.translatable("message.autologin", server.ip, password)
            );
        }
        return 1;
    }

    private static int handleAutoLogin(CommandContext<FabricClientCommandSource> context) {
        Minecraft client = Minecraft.getInstance();
        String serverAddress = client.getCurrentServer() != null
                ? client.getCurrentServer().ip
                : null;

        if (serverAddress == null) {
            client.player.sendSystemMessage(Component.translatable("error.not_connected"));
            return 0;
        }

        JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
        String password = config.passwords.get(serverAddress);

        if (password == null || password.isEmpty()) {
            client.player.sendSystemMessage(Component.translatable("error.no_password"));
            return 0;
        }

        String[] triggers = config.triggers.split(",");
        if (triggers.length == 0 || triggers[0].trim().isEmpty()) {
            client.player.sendSystemMessage(Component.translatable("error.no_triggers"));
            return 0;
        }

        String command = String.format("%s %s", triggers[0].trim(), password);

        if (client.getConnection() != null) {
            client.getConnection().sendCommand(command);
            config = JSONConfigHandler.getCurrentPlayerConfig();
            config.currentCheck = true;
            JSONConfigHandler.saveCurrentPlayerConfig(config);
        }

        return 1;
    }
}
