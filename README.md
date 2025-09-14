### AlphaTrainer multiplatform
This is a Kotlin Multiplatform project targeting Android, Web (pending) and Desktop (JVM).

The application allows for quick and playful consolidation of foreign language knowledge that you're already started learning. Begin with choosing the language you want to work with, then choose a desired alphabet (for languages where there are several ones), adjust preferences if desired and start training.

Other modes yet to come:
- pronunciation mode to verify your spelling skills
- dictionary mode to learn new words
- translation mode to train your memory

Should you experience any issues while using the app or in case you find a bug, please contact me over the Issues section on GitHub.

### Source code structure
* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.
* [/nonAndroid](./nonAndroid/src) is for code that shares implementation among platforms not related to Android
  - [commonMain](./nonAndroid/src/commonMain/kotlin) is for code that’s common for all such targets
* [/nonWasm](./nonWasm/src) is for code that shares implementation among platforms not related to Web Assembly
  - [commonMain](./nonWasm/src/commonMain/kotlin) is for code that’s common for all such targets
  - [jvmMain](./nonWasm/src/jvmMain/kotlin) and [androidMain](./nonWasm/src/androidMain/kotlin) are for Kotlin code that will be compiled for only the platform indicated in the folder name
 
### Build and run

To build and run the development version of the app, use the run configuration from the run widget in your IDE’s toolbar or build it directly from the terminal:

| Host platform | Android                                   | Desktop (JVM)               | Web Application                                     |
|---------------|-------------------------------------------|-----------------------------|-----------------------------------------------------|
| **macOS/Linux**   | `./gradlew :composeApp:assembleDebug`     | `./gradlew :composeApp:run` | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| **Windows**       | `.\gradlew.bat :composeApp:assembleDebug` | `.\gradlew.bat :composeApp:run` | `.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun`                                                    |

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).