### AlphaTrainer multiplatform

<img align="right" alt="logo" src="/composeApp/src/commonMain/composeResources/drawable/icon_ico.ico">

This is a Kotlin Multiplatform project targeting Android, Web (pending) and Desktop (JVM).

The application allows for quick and playful consolidation of foreign language knowledge that you've
already started learning. Begin with choosing the language you want to work with, then choose a
desired alphabet (for languages where there are several ones), adjust preferences if desired and
start training.

Other modes yet to come:

- pronunciation mode to verify your spelling skills
- dictionary mode to learn new words
- translation mode to train your memory

Should you experience any issues while using the app or in case you find a bug, please contact me
over the Issues section on GitHub. With any questions or ideas, please don't hesitate to open a thread in Discussions section

### Training sets customization

The app database is open for customization that means that you may modify contents of any section shown in the app by adjusting appropriate table contents in the Training.db SQLite database.

> [!IMPORTANT]
Please run the app at least once before you can modify the database file

#### Android

Database file location: `/data/data/ru.coolsoft.alphatrainer/databases/Trainer.db`

> [!IMPORTANT]
In order to be able to gain access to the database file on a non-rooted Android device, a `debug` version of the app has to be installed rather than a `release` one.

There are many ways to download the database file to your PC or get it modified right on the device. Here are the steps to accomplish this task using Android Studio:

- connect the Android device to your Android Studio in debug mode
- open Device Explorer and navigate to the database file location
- download the file to your desktop (right-click the file name and select `Save As...`)
- alter the database according to your plans, make sure that the `Trainer.db` file is actually updated and marked with new timestamp
- upload the modified file to the device (right-click `databases` folder in Device Explorer, select `Upload...` and choose the modified database file)

#### Desktop (JVM)

Database file `Trainer.db` is located in the root folder of the app next to `Alphatrainer.exe` file. Any changes to the file will be visible with the very nex app start.

### Source code structure

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform
  applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the
      folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part,
      the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.
* [/nonAndroid](./nonAndroid/src) is for code that shares implementation among platforms not related
  to Android
    - [commonMain](./nonAndroid/src/commonMain/kotlin) is for code that’s common for all such
      targets
* [/nonWasm](./nonWasm/src) is for code that shares implementation among platforms not related to
  Web Assembly
    - [commonMain](./nonWasm/src/commonMain/kotlin) is for code that’s common for all such targets
    - [jvmMain](./nonWasm/src/jvmMain/kotlin) and [androidMain](./nonWasm/src/androidMain/kotlin)
      are for Kotlin code that will be compiled for only the platform indicated in the folder name

### Build and run

To build and run the development version of the app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

| Host platform   | Android                                   | Desktop (JVM)                   | Web Application                                         |
|-----------------|-------------------------------------------|---------------------------------|---------------------------------------------------------|
| **macOS/Linux** | `./gradlew :composeApp:assembleDebug`     | `./gradlew :composeApp:run`     | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`     |
| **Windows**     | `.\gradlew.bat :composeApp:assembleDebug` | `.\gradlew.bat :composeApp:run` | `.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun` |

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them
on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).