package org.example.s.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.example.s.PasswordGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoLoginConfigScreen extends Screen {
    public static JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
    private int scrollOffset = 0;
    private final Screen parent;
    private TextFieldWidget ipField;
    private TextFieldWidget passwordField;
    private ButtonWidget addButton;
    private ButtonWidget generateButton;
    private ButtonWidget deleteButton;
    private String selectedServer = null;
    private Map<String, String> servers = new HashMap<>();
    private TextFieldWidget loginCommandField;
    private ButtonWidget toggleCheckButton;
    private ButtonWidget saveTriggerButton;

    public AutoLoginConfigScreen(Screen parent) {
        super(Text.translatable("config.autologin_mod.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        JSONConfigHandler.PlayerConfig config = JSONConfigHandler.getCurrentPlayerConfig();
        // Поля ввода
        ipField = new TextFieldWidget(
                textRenderer,
                width / 4,
                height / 4 + 60,
                200,
                20,
                Text.translatable("config.autologin_mod.ip")
        );
        passwordField = new TextFieldWidget(
                textRenderer,
                width / 4,
                height / 4 + 100,
                200,
                20,
                Text.translatable("config.autologin_mod.password")
        );

        // Кнопки
        addButton = ButtonWidget.builder(
                Text.translatable("config.autologin_mod.add"),
                button -> addServer()
        ).dimensions(
                width / 4 + 210,
                height / 4 + 60,
                100,
                20
        ).build();

        generateButton = ButtonWidget.builder(
                Text.translatable("config.autologin_mod.generate"),
                button -> generatePassword()
        ).dimensions(
                width / 4 + 210,
                height / 4 + 100,
                100,
                20
        ).build();

        deleteButton = ButtonWidget.builder(
                Text.translatable("config.autologin_mod.delete"),
                button -> deleteServer()
        ).dimensions(
                width / 4 * 3,
                (int) (height * 0.74),
                100,
                20
        ).build();

        // Поле для ввода команды проверки логина
        loginCommandField = new TextFieldWidget(
                textRenderer,
                width / 4,
                height / 4 + 140,
                200,
                20,
                Text.translatable("config.autologin_mod.login_command")
        );


        toggleCheckButton = ButtonWidget.builder(
                Text.translatable(config.check_enabled ? "config.autologin_mod.enabled" : "config.autologin_mod.disabled"),
                button -> toggleLoginCheck()
        ).dimensions(
                width / 2 - 50,
                10,
                100,
                20
        ).build();


        saveTriggerButton = ButtonWidget.builder(
                Text.translatable("config.autologin_mod.save_trigger"),
                button -> saveConfig()
        ).dimensions(
                width / 4 + 210,
                height / 4 + 140,
                100,
                20
        ).build();


        addDrawableChild(loginCommandField);
        addDrawableChild(saveTriggerButton);
        addDrawableChild(toggleCheckButton);
        addDrawableChild(ipField);
        addDrawableChild(passwordField);
        addDrawableChild(addButton);
        addDrawableChild(generateButton);
        addDrawableChild(deleteButton);

        // Кнопка "Назад"
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.back"),
                button -> {
                    assert client != null;
                    client.setScreen(parent);
                }
        ).dimensions(5, 5, 60, 20).build());

        loadConfig();
    }

//    @Override
//    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.render(context, mouseX, mouseY, delta);
//
//            context.drawCenteredTextWithShadow(
//                    this.textRenderer,
//                    Text.literal("Opaque Text"),
//                    this.width / 2,
//                    this.height / 2,
//                    0xFFFFFFFF // 0xFF - альфа-канал (не прозрачный)
//            );
//    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // Позволяет закрыть экран по нажатию Escape
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Отрисовка списка сохраненных паролей с прокруткой
        int listX = width / 4 * 3;
        int listY = height / 4 + 20;
        int listWidth = width / 4;
        int listHeight = height / 2 - 40;

        context.fill(listX - 1, listY - 1, listX + listWidth + 1, listY + listHeight + 1, 0x80000000);
        String username = MinecraftClient.getInstance().getSession().getUsername();
        context.drawText(this.textRenderer, Text.translatable("config.autologin_mod.saved_servers", username), listX, listY - 20, 0xFFFFFFFF, false);

        int listXPol = width / 4 * 3;
        int listYPol = height / 4 + 20;
        int listWidthPol = width / 4;
        int listHeightPol = height / 2 - 40;
        int entryHeight = 20;
        int visibleEntries = listHeightPol / entryHeight;
        int totalEntries = servers.size();
        int maxScroll = Math.max(0, totalEntries - visibleEntries);

        int entryCount = 0;
        int renderedEntries = 0;

        for (Map.Entry<String, String> entry : servers.entrySet()) {
            if (renderedEntries >= visibleEntries) break;
            if (entryCount < scrollOffset) {
                entryCount++;
                continue;
            }

            String ip = entry.getKey();
            String password = entry.getValue();

            String displayText = ip + "=" + password;
            context.drawText(this.textRenderer, Text.literal(displayText), listX, listY + renderedEntries * entryHeight, 0xFFFFFFFF, false);

            if (ip.equals(selectedServer)) {
                context.fill(listX, listYPol + renderedEntries * entryHeight, listXPol + listWidthPol, listYPol + (renderedEntries + 1) * entryHeight, 0x30FFFFFF);
            }

            renderedEntries++;
        }

        if (maxScroll > 0) {
            int scrollBarX = listX + listWidth - 5;
            int scrollBarHeight = listHeight * visibleEntries / totalEntries;
            int scrollBarY = listY + (int)((listHeight - scrollBarHeight) * ((float)scrollOffset / maxScroll));

            context.fill(scrollBarX, scrollBarY, scrollBarX + 5, scrollBarY + scrollBarHeight, 0x808080);
        }

        // Отрисовка заголовков полей по центру
        context.drawText(this.textRenderer, Text.translatable("config.autologin_mod.ip"), width / 4, height / 4 + 45, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, Text.translatable("config.autologin_mod.password"), width / 4, height / 4 + 85, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, Text.translatable("config.autologin_mod.login_command"), width / 4, height / 4 + 125, 0xFFFFFFFF, false);
        String statusText = Text.translatable("config.autologin_mod.status", config.check_enabled ? "Включен" : "Выключен").getString();
        int statusX = width / 2 - textRenderer.getWidth(statusText) / 2;
        context.drawText(this.textRenderer, statusText, statusX, 40, 0xFFFFFFFF, false);

        // Отрисовка текущего триггер-слова
        context.drawText(this.textRenderer, Text.translatable("config.autologin_mod.current_trigger", loginCommandField.getText()), width / 4, height / 4 + 165, 0xFFFFFFFF, false);


    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double delta) {
        int listX = width / 4 * 3;
        int listY = height / 4 + 20;
        int listWidth = width / 4;
        int listHeight = height / 2 - 40;

        if (mouseX >= listX && mouseX < listX + listWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)amount, servers.size() - (listHeight / 20)));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount, delta);
    }

    // Добавьте метод для переключения проверки
    private void toggleLoginCheck() {
        config.check_enabled = !config.check_enabled;
        toggleCheckButton.setMessage(Text.translatable(config.check_enabled ? "config.autologin_mod.enabled" : "config.autologin_mod.disabled"));
        JSONConfigHandler.saveCurrentPlayerConfig(config);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Сначала проверяем, кликнули ли по какому-либо элементу интерфейса (кнопкам, полям ввода)
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        // Если событие не было обработано другими элементами, проверяем клик по списку серверов
        if (!handled && button == 0) {
            int listX = width / 4 * 3;
            int listY = height / 4 + 20;
            int listWidth = width / 4;
            int listHeight = height / 2 - 40;
            int entryHeight = 20;

            if (mouseX >= listX && mouseX < listX + listWidth &&
                    mouseY >= listY && mouseY < listY + listHeight) {
                int entryIndex = (int) ((mouseY - listY) / entryHeight);
                int entryCount = 0;

                for (Map.Entry<String, String> entry : servers.entrySet()) {
                    if (entryIndex == entryCount) {
                        selectedServer = entry.getKey();
                        break;
                    }
                    entryCount++;
                }
            } else {
                selectedServer = null;
            }
        }

        return true; // Возвращаем true, так как событие было обработано
    }

    private void addServer() {
        String ip = ipField.getText().trim();
        String password = passwordField.getText().trim();

        if (!ip.isEmpty() && !password.isEmpty()) {
            servers.put(ip, password);
            saveConfig();
            ipField.setText("");
            passwordField.setText("");
        }
    }

    private void generatePassword() {
        String ip = ipField.getText().trim();
        if (!ip.isEmpty()) {
            String password = PasswordGenerator.generate(15);
            passwordField.setText(password);
            servers.put(ip, password);
            saveConfig();
        }
    }

    private void deleteServer() {
        if (selectedServer != null && servers.containsKey(selectedServer)) {
            servers.remove(selectedServer);
            saveConfig();
            selectedServer = null; // Сброс выбранного сервера
        }
    }

    //Метод загрузки конфига
    private void loadConfig() {
        config = JSONConfigHandler.getCurrentPlayerConfig();
        servers.putAll(config.passwords);
        // Устанавливаем значение триггер-слова в поле ввода
        if (loginCommandField != null) {
            loginCommandField.setText(config.triggers);
        }
    }

    // Метод сохранения конфига
    private void saveConfig() {
        if (config != null && loginCommandField != null) {
            config.triggers = loginCommandField.getText();
            config.passwords.clear();
            config.passwords.putAll(servers);
            JSONConfigHandler.saveCurrentPlayerConfig(config);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ipField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (passwordField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (loginCommandField.keyPressed(keyCode, scanCode, modifiers)) {
            if (keyCode == 256) { // Код клавиши Enter
                saveConfig();
            }
            return true;
        }

        // Удаление сервера по клавише Delete
        if (keyCode == 261) { // Код клавиши Delete
            if (selectedServer != null) {
                deleteServer();
                return true;
            }
        }

        // Копирование пароля в буфер обмена при Ctrl + C
        if (keyCode == 67 && (modifiers & 2) != 0) { // 67 = C, 2 = Ctrl
            if (selectedServer != null && servers.containsKey(selectedServer)) {
                String password = servers.get(selectedServer);
                if (password != null) {
                    client.keyboard.setClipboard(password);
                }
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (ipField.charTyped(chr, modifiers)) {
            return true;
        }
        if (passwordField.charTyped(chr, modifiers)) {
            return true;
        }
        if (loginCommandField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}