# Сборка и запуск приложения (APK)

Ниже команды показаны для macOS / Linux (`zsh`/`bash`).  
В Windows используйте `gradlew.bat` вместо `./gradlew`.

---

## 1. Требования

- Установленная **Android Studio** (вместе с Android SDK).
- **JDK 11+** (обычно идёт внутри Android Studio как Embedded JDK).

Проверка в терминале (из корня проекта):

```bash
java -version
./gradlew -version
```

---

## 2. Сборка Debug APK (для тестирования)

Debug‑сборка предназначена для разработки и локального теста.

Из корня проекта:

```bash
cd /Users/fedor/AndroidStudioProjects/lab2
./gradlew :app:assembleDebug
```

Готовый файл:

- `app/build/outputs/apk/debug/app-debug.apk`

### Установка Debug‑APK на устройство/эмулятор (опционально)

При наличии `adb`:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```


## 4. Запускать приложения удобнее всего через Android Studio



