package org.example.s.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.example.s.PasswordGenerator;

import java.util.HashMap;
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
        addSelectableChild(ipField);
        addSelectableChild(passwordField);
        addSelectableChild(loginCommandField);

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

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Параметры списка серверов
        int listX = width / 4 * 3;
        int listY = height / 4 + 20;
        int listWidth = width / 4;
        int listHeight = height / 2 - 40;
        int entryHeight = 20;
        int visibleEntries = listHeight / entryHeight;
        int totalEntries = servers.size();

        // Рамка списка
        context.fill(listX - 1, listY - 1, listX + listWidth + 1, listY + listHeight + 1, 0x80000000);

        String username = MinecraftClient.getInstance().getSession().getUsername();
        context.drawText(this.textRenderer, Text.translatable("config.autologin_mod.saved_servers", username), listX, listY - 20, 0xFFFFFFFF, false);

        // Отрисовка элементов списка с учетом прокрутки
        int renderedCount = 0;
        int entryIndex = 0;

        for (Map.Entry<String, String> entry : servers.entrySet()) {
            if (entryIndex < scrollOffset) {
                entryIndex++;
                continue;
            }

            if (renderedCount >= visibleEntries) {
                break;
            }

            String ip = entry.getKey();
            String password = entry.getValue();
            String displayText = ip + "=" + password;

            int entryY = listY + renderedCount * entryHeight;

            // Подсветка выбранного элемента
            if (ip.equals(selectedServer)) {
                context.fill(listX, entryY, listX + listWidth, entryY + entryHeight, 0x30FFFFFF);
            }

            // Обрезка текста по ширине
            String truncatedText = textRenderer.trimToWidth(displayText, listWidth - 10);
            context.drawText(this.textRenderer, Text.literal(truncatedText), listX + 2, entryY + 5, 0xFFFFFFFF, false);

            renderedCount++;
            entryIndex++;
        }

        // Полоса прокрутки
        if (totalEntries > visibleEntries) {
            int scrollBarX = listX + listWidth - 6;
            int scrollBarWidth = 4;
            int maxScroll = Math.max(0, totalEntries - visibleEntries);

            // Фон полосы прокрутки
            context.fill(scrollBarX, listY, scrollBarX + scrollBarWidth, listY + listHeight, 0x40FFFFFF);

            if (maxScroll > 0) {
                int scrollBarHeight = Math.max(10, listHeight * visibleEntries / totalEntries);
                int scrollBarY = listY + (int)((float)(listHeight - scrollBarHeight) * scrollOffset / maxScroll);

                // Полоса прокрутки
                context.fill(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight, 0xFFFFFFFF);
            }
        }

        // Отрисовка заголовков полей
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listX = width / 4 * 3;
        int listY = height / 4 + 20;
        int listWidth = width / 4;
        int listHeight = height / 2 - 40;
        int entryHeight = 20;
        int visibleEntries = listHeight / entryHeight;

        // Проверяем, находится ли курсор над списком
        if (mouseX >= listX && mouseX < listX + listWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {

            int maxScroll = Math.max(0, servers.size() - visibleEntries);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)verticalAmount));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Сначала проверяем стандартные элементы
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        // Если не обработано и левая кнопка мыши
        if (!handled && button == 0) {
            int listX = width / 4 * 3;
            int listY = height / 4 + 20;
            int listWidth = width / 4;
            int listHeight = height / 2 - 40;
            int entryHeight = 20;
            int visibleEntries = listHeight / entryHeight;

            // Проверяем клик по списку
            if (mouseX >= listX && mouseX < listX + listWidth &&
                    mouseY >= listY && mouseY < listY + listHeight) {

                int clickedIndex = (int)((mouseY - listY) / entryHeight);
                int actualIndex = scrollOffset + clickedIndex;

                // Проверяем, что индекс в допустимых пределах
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
                // Клик вне списка - сбрасываем выбор
                selectedServer = null;
            }
        }

        return handled;
    }

    private void toggleLoginCheck() {
        config.check_enabled = !config.check_enabled;
        toggleCheckButton.setMessage(Text.translatable(config.check_enabled ? "config.autologin_mod.enabled" : "config.autologin_mod.disabled"));
        JSONConfigHandler.saveCurrentPlayerConfig(config);
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
            selectedServer = null;

            // Корректируем прокрутку после удаления
            int entryHeight = 20;
            int visibleEntries = (height / 2 - 40) / entryHeight;
            int maxScroll = Math.max(0, servers.size() - visibleEntries);
            scrollOffset = Math.min(scrollOffset, maxScroll);
        }
    }

    private void loadConfig() {
        config = JSONConfigHandler.getCurrentPlayerConfig();
        servers.putAll(config.passwords);
        if (loginCommandField != null) {
            loginCommandField.setText(config.triggers);
        }
    }

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
            if (keyCode == 256) { // Enter
                saveConfig();
            }
            return true;
        }

        // Удаление сервера по Delete
        if (keyCode == 261) { // Delete
            if (selectedServer != null) {
                deleteServer();
                return true;
            }
        }

        // Копирование пароля Ctrl+C
        if (keyCode == 67 && (modifiers & 2) != 0) { // C + Ctrl
            if (selectedServer != null && servers.containsKey(selectedServer)) {
                String password = servers.get(selectedServer);
                if (password != null) {
                    assert client != null;
                    client.keyboard.setClipboard(password);
                }
                return true;
            }
        }

        // Навигация стрелками
        if (selectedServer != null) {
            if (keyCode == 264) { // Стрелка вниз
                selectNextServer(1);
                return true;
            } else if (keyCode == 265) { // Стрелка вверх
                selectNextServer(-1);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void selectNextServer(int direction) {
        if (servers.isEmpty()) return;

        String[] serverKeys = servers.keySet().toArray(new String[0]);
        int currentIndex = -1;

        // Найти текущий индекс
        for (int i = 0; i < serverKeys.length; i++) {
            if (serverKeys[i].equals(selectedServer)) {
                currentIndex = i;
                break;
            }
        }

        // Вычислить новый индекс
        int newIndex = Math.max(0, Math.min(serverKeys.length - 1, currentIndex + direction));
        selectedServer = serverKeys[newIndex];

        // Автопрокрутка к выбранному элементу
        int entryHeight = 20;
        int visibleEntries = (height / 2 - 40) / entryHeight;

        if (newIndex < scrollOffset) {
            scrollOffset = newIndex;
        } else if (newIndex >= scrollOffset + visibleEntries) {
            scrollOffset = newIndex - visibleEntries + 1;
        }
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