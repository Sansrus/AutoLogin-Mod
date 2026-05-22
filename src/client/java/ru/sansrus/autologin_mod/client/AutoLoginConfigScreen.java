package ru.sansrus.autologin_mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.sansrus.autologin_mod.PasswordGenerator;

import java.util.HashMap;
import java.util.Map;

public class AutoLoginConfigScreen extends Screen {
    public static JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();

    private int scrollOffset = 0;
    private final Screen parent;

    private EditBox ipField;
    private EditBox passwordField;
    private Button addButton;
    private Button generateButton;
    private Button deleteButton;
    private String selectedServer = null;
    private final Map<String, String> servers = new HashMap<>();
    private EditBox loginCommandField;
    private Checkbox autosendToggle;
    private Button toggleCheckButton;
    private Button saveTriggerButton;

    public AutoLoginConfigScreen(Screen parent) {
        super(Component.translatable("config.autologin_mod.title"));
        this.parent = parent;
    }


    @Override
    protected void init() {
        config = JSONConfigHandler.getCurrentPlayerConfig();

        int fieldLeft = width / 4;
        int fieldWidth = 200;

        ipField = new EditBox(
                font,
                fieldLeft,
                height / 4 + 60,
                fieldWidth,
                20,
                Component.translatable("config.autologin_mod.ip")
        );

        passwordField = new EditBox(
                font,
                fieldLeft,
                height / 4 + 100,
                fieldWidth,
                20,
                Component.translatable("config.autologin_mod.password")
        );

        loginCommandField = new EditBox(
                font,
                fieldLeft,
                height / 4 + 140,
                fieldWidth,
                20,
                Component.translatable("config.autologin_mod.login_command")
        );

        addButton = Button.builder(
                Component.translatable("config.autologin_mod.add"),
                button -> addServer()
        ).bounds(fieldLeft + fieldWidth + 10, height / 4 + 60, 100, 20).build();

        generateButton = Button.builder(
                Component.translatable("config.autologin_mod.generate"),
                button -> generatePassword()
        ).bounds(fieldLeft + fieldWidth + 10, height / 4 + 100, 100, 20).build();

        saveTriggerButton = Button.builder(
                Component.translatable("config.autologin_mod.save_trigger"),
                button -> saveConfig()
        ).bounds(fieldLeft + fieldWidth + 10, height / 4 + 140, 100, 20).build();

        toggleCheckButton = Button.builder(
                Component.translatable(config.check_enabled ? "config.autologin_mod.enabled" : "config.autologin_mod.disabled"),
                button -> toggleLoginCheck()
        ).bounds(width / 2 - 50, 10, 100, 20).build();

        deleteButton = Button.builder(
                Component.translatable("config.autologin_mod.delete"),
                button -> deleteServer()
        ).bounds(width / 4 * 3, (int) (height * 0.74), 100, 20).build();

        int checkboxY = height / 4 + 165;
        autosendToggle = Checkbox.builder(
                        Component.translatable("config.autologin_mod.autosend"),
                        font
                )
                .pos(fieldLeft, checkboxY)
                .selected(config != null && config.autosend)
                .onValueChange((cb, val) -> {
                    if (config == null) config = JSONConfigHandler.getCurrentPlayerConfig();
                    config.autosend = val;
                    JSONConfigHandler.saveCurrentPlayerConfig(config);
                })
                .build();

        addRenderableWidget(ipField);
        addRenderableWidget(passwordField);
        addRenderableWidget(loginCommandField);
        addRenderableWidget(autosendToggle);

        addRenderableWidget(addButton);
        addRenderableWidget(generateButton);
        addRenderableWidget(saveTriggerButton);
        addRenderableWidget(toggleCheckButton);
        addRenderableWidget(deleteButton);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> {
                    assert minecraft != null;
                    minecraft.setScreen(parent);
                }
        ).bounds(5, 5, 60, 20).build());

        loadConfig();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        int listX = width / 4 * 3;
        int listY = height / 4 + 20;
        int listWidth = width / 4;
        int listHeight = height / 2 - 40;
        int entryHeight = 20;
        int visibleEntries = listHeight / entryHeight;
        int totalEntries = servers.size();

        context.fill(listX - 1, listY - 1, listX + listWidth + 1, listY + listHeight + 1, 0x80000000);

        String username = Minecraft.getInstance().getUser().getName();
        context.centeredText(this.font, Component.translatable("config.autologin_mod.saved_servers", username), listX + listWidth / 2, listY - 20, 0xFFFFFFFF);

        int renderedCount = 0;
        int entryIndex = 0;
        for (Map.Entry<String, String> entry : servers.entrySet()) {
            if (entryIndex < scrollOffset) {
                entryIndex++;
                continue;
            }
            if (renderedCount >= visibleEntries) break;

            String ip = entry.getKey();
            String password = entry.getValue();
            String displayText = ip + "=" + password;

            int entryY = listY + renderedCount * entryHeight;

            if (ip.equals(selectedServer)) {
                context.fill(listX, entryY, listX + listWidth, entryY + entryHeight, 0x30FFFFFF);
            }

            String truncatedText = font.plainSubstrByWidth(displayText, listWidth - 10);
            int centerX = listX + listWidth / 2;
            int textY = entryY + (entryHeight - font.lineHeight) / 2;
            context.centeredText(this.font, Component.literal(truncatedText), centerX, textY, 0xFFFFFFFF);

            renderedCount++;
            entryIndex++;
        }

        if (totalEntries > visibleEntries) {
            int scrollBarX = listX + listWidth - 6;
            int scrollBarWidth = 4;
            int maxScroll = Math.max(0, totalEntries - visibleEntries);

            context.fill(scrollBarX, listY, scrollBarX + scrollBarWidth, listY + listHeight, 0x40FFFFFF);

            if (maxScroll > 0) {
                int scrollBarHeight = Math.max(10, listHeight * visibleEntries / totalEntries);
                int scrollBarY = listY + (int)((float)(listHeight - scrollBarHeight) * scrollOffset / maxScroll);
                context.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
            }
        }

        int fieldLeft = width / 4;
        int fieldWidth = 200;
        int fieldCenter = fieldLeft + fieldWidth / 2;

        context.centeredText(this.font, Component.translatable("config.autologin_mod.ip"), fieldCenter, height / 4 + 45, 0xFFFFFFFF);
        context.centeredText(this.font, Component.translatable("config.autologin_mod.password"), fieldCenter, height / 4 + 85, 0xFFFFFFFF);
        context.centeredText(this.font, Component.translatable("config.autologin_mod.login_command"), fieldCenter, height / 4 + 125, 0xFFFFFFFF);

        String statusText = Component.translatable("config.autologin_mod.status", config.check_enabled ? Component.translatable("config.autologin_mod.enabled") : Component.translatable("config.autologin_mod.disabled")).getString();
        context.centeredText(this.font, Component.literal(statusText), width / 2, 40, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listX = width / 4 * 3;
        int listY = height / 4 + 20;
        int listWidth = width / 4;
        int listHeight = height / 2 - 40;
        int entryHeight = 20;
        int visibleEntries = listHeight / entryHeight;

        if (mouseX >= listX && mouseX < listX + listWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {

            int maxScroll = Math.max(0, servers.size() - visibleEntries);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)verticalAmount));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        boolean handled = super.mouseClicked(event, doubleClicked);

        if (!handled && event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            int listX = width / 4 * 3;
            int listY = height / 4 + 20;
            int listWidth = width / 4;
            int listHeight = height / 2 - 40;
            int entryHeight = 20;

            if (mouseX >= listX && mouseX < listX + listWidth &&
                    mouseY >= listY && mouseY < listY + listHeight) {
                int clickedIndex = (int)((mouseY - listY) / entryHeight);
                int actualIndex = scrollOffset + clickedIndex;
                if (actualIndex >= 0 && actualIndex < servers.size()) {
                    int currentIndex = 0;
                    for (Map.Entry<String, String> entry : servers.entrySet()) {
                        if (currentIndex == actualIndex) {
                            selectedServer = entry.getKey();
                            return true;
                        }
                        currentIndex++;
                    }
                }
            } else {
                selectedServer = null;
            }
        }
        return handled;
    }



    private void toggleLoginCheck() {
        config.check_enabled = !config.check_enabled;
        toggleCheckButton.setMessage(Component.translatable(config.check_enabled ? "config.autologin_mod.enabled" : "config.autologin_mod.disabled"));
        JSONConfigHandler.saveCurrentPlayerConfig(config);
    }

    private void addServer() {
        String ip = ipField.getValue().trim();
        String password = passwordField.getValue().trim();

        if (!ip.isEmpty() && !password.isEmpty()) {
            servers.put(ip, password);
            saveConfig();
            ipField.setValue("");
            passwordField.setValue("");
        }
    }

    private void generatePassword() {
        String ip = ipField.getValue().trim();
        if (!ip.isEmpty()) {
            String password = PasswordGenerator.generate(15);
            passwordField.setValue(password);
            servers.put(ip, password);
            saveConfig();
        }
    }

    private void deleteServer() {
        if (selectedServer != null && servers.containsKey(selectedServer)) {
            servers.remove(selectedServer);
            saveConfig();
            selectedServer = null;

            int entryHeight = 20;
            int visibleEntries = (height / 2 - 40) / entryHeight;
            int maxScroll = Math.max(0, servers.size() - visibleEntries);
            scrollOffset = Math.min(scrollOffset, maxScroll);
        }
    }

    private void loadConfig() {
        config = JSONConfigHandler.getCurrentPlayerConfig();
        servers.clear();
        if (config != null && config.passwords != null) {
            servers.putAll(config.passwords);
        }
        if (loginCommandField != null && config != null) {
            loginCommandField.setValue(config.triggers != null ? config.triggers : "");
        }
        if (autosendToggle != null && config != null) {
            autosendToggle.setFocused(config.autosend);
        }
    }

    private void saveConfig() {
        if (config != null && loginCommandField != null) {
            config.triggers = loginCommandField.getValue();
            if (autosendToggle != null) {
                config.autosend = autosendToggle.selected();
            }
            config.passwords.clear();
            config.passwords.putAll(servers);
            JSONConfigHandler.saveCurrentPlayerConfig(config);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int modifiers = event.modifiers();
        if (ipField.keyPressed(event)) return true;
        if (passwordField.keyPressed(event)) return true;
        if (loginCommandField.keyPressed(event)) {
            if (keyCode == 257) saveConfig();
            return true;
        }

        if (keyCode == 261 && selectedServer != null) {
            deleteServer();
            return true;
        }
        if (keyCode == 67 && (modifiers & 2) != 0 && selectedServer != null) {
            String password = servers.get(selectedServer);
            if (password != null) {
                assert minecraft != null;
                minecraft.keyboardHandler.setClipboard(password);
            }
            return true;
        }
        if (selectedServer != null) {
            if (keyCode == 264) { selectNextServer(1); return true; }
            if (keyCode == 265) { selectNextServer(-1); return true; }
        }

        return super.keyPressed(event);
    }



    private void selectNextServer(int direction) {
        if (servers.isEmpty()) return;

        String[] serverKeys = servers.keySet().toArray(new String[0]);
        int currentIndex = -1;

        for (int i = 0; i < serverKeys.length; i++) {
            if (serverKeys[i].equals(selectedServer)) {
                currentIndex = i;
                break;
            }
        }

        int newIndex = Math.max(0, Math.min(serverKeys.length - 1, currentIndex + direction));
        selectedServer = serverKeys[newIndex];

        int entryHeight = 20;
        int visibleEntries = (height / 2 - 40) / entryHeight;

        if (newIndex < scrollOffset) {
            scrollOffset = newIndex;
        } else if (newIndex >= scrollOffset + visibleEntries) {
            scrollOffset = newIndex - visibleEntries + 1;
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (ipField.charTyped(event)) return true;
        if (passwordField.charTyped(event)) return true;
        if (loginCommandField.charTyped(event)) return true;
        return super.charTyped(event);
    }

}
