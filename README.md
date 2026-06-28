# AutoLogin Mod

[English](#english) | [Русский](#русский)

---

## English

A Fabric client-side mod that automatically enters your password on servers that require authentication.

### Features

- Automatically sends your login command when the server asks for a password
- Each nickname has its own separate password list
- Built-in password generator (15 characters)
- Configurable trigger word — the mod listens for it in chat to know when to log in
- Auto-login without a trigger word is also supported, with a short delay of 2–4 seconds
- ModMenu integration for a graphical settings screen
- In the password list you can press **Del** to delete an entry or **Ctrl+C** to copy it

### Commands

```
/autologin add <password>
```
Saves the password for the server you are currently on. If a password already exists for this server, it will be updated.

```
/autoreg
```
Generates a new password and automatically runs the `/register` command on the server.

**Shortened aliases:** `/al` and `/ar`

---

## Русский

Fabric клиентский мод, который автоматически вводит пароль на серверах с авторизацией.

### Возможности

- Автоматически отправляет команду входа, когда сервер запрашивает пароль
- У каждого никнейма свой отдельный список паролей
- Встроенный генератор паролей (15 символов)
- Настраиваемое триггерное слово — мод отслеживает его в чате, чтобы понять, когда нужно войти
- Поддерживается автовход без триггерного слова, с небольшой задержкой от 2 до 4 секунд
- Интеграция с ModMenu для графического экрана настроек
- В списке паролей можно нажать **Del** для удаления записи или **Ctrl+C** для копирования

### Команды

```
/autologin add <пароль>
```
Сохраняет пароль для сервера, на котором вы находитесь. Если пароль уже был задан — он обновится.

```
/autoreg
```
Генерирует новый пароль и автоматически выполняет команду `/register` на сервере.

**Сокращённые псевдонимы:** `/al` и `/ar`
