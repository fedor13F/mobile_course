# Сборка и запуск приложения (APK)

Документ описывает, как:

- собрать **Debug APK** через терминал;
- собрать **Release APK**;
- **запустить** приложение через Android Studio (самый простой вариант).

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

Если `./gradlew` ругается на Java Runtime, в Android Studio выберите:

> Settings → Build, Execution, Deployment → Gradle → Gradle JDK → Embedded JDK

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

---

## 3. Сборка Release APK

Release‑сборка нужна для публикации / финальной сдачи (обычно с подписью).

Без настройки подписи (unsigned):

```bash
./gradlew :app:assembleRelease
```

Результат:

- `app/build/outputs/apk/release/app-release-unsigned.apk`

После добавления `signingConfigs` в `app/build.gradle.kts` и привязки к `buildTypes.release`
можно собирать уже **подписанный** `release` той же командой:

```bash
./gradlew :app:assembleRelease
```

Файл будет в той же папке `app/build/outputs/apk/release/`,
имя зависит от настроек Gradle.

---

## 4. Запуск приложения через Android Studio (рекомендуемый способ)

1. **Открыть проект**
   - File → Open… → выбрать папку `lab2`.

2. **Создать или выбрать устройство**
   - Меню: `Tools → Device Manager`.
   - Нажать **Create device…**, выбрать, например, `Medium Phone API 36.1`, создать.
   - В Device Manager нажать **▶** (Play) — эмулятор запустится в отдельном окне.

3. **Настроить конфигурацию запуска**
   - В верхней панели Android Studio в выпадающем списке конфигураций выбрать **`app`**.
   - Рядом в списке устройств выбрать запущенный эмулятор
     (например, `Medium Phone API 36.1`).

4. **Собрать и запустить приложение**
   - Нажать зелёный треугольник **Run** или сочетание `Shift + F10`.
   - Gradle соберёт debug‑версию и установит её на выбранное устройство.
   - Через несколько секунд откроется главный экран приложения.

5. **Где лежит APK после запуска через Studio**
   - Физически Studio тоже собирает **`app-debug.apk`**:
     `app/build/outputs/apk/debug/app-debug.apk`.

