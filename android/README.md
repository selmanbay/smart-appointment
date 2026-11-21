# android

Kotlin customer app. Planned for after the web MVP (ADR-0005). No code yet.

## Planned stack

- Kotlin 2.x, Jetpack Compose, Gradle wrapper + `gradle/libs.versions.toml`
- Retrofit + kotlinx.serialization, Hilt
- Firebase Cloud Messaging (push)

## Scope (first release)

Sign in / sign up, book appointment (UC-3), my appointments, cancel and reschedule (UC-4, 5, 6), feedback (UC-7), push notifications.

## Folder layout (ADR-0009)

```text
app/src/
  main/java/com/smartappointment/android/<feature>/         code
  test/java/com/smartappointment/android/<feature>/         local unit tests (JUnit)
  androidTest/java/com/smartappointment/android/<feature>/  instrumented UI tests (Compose testing)
```

## Notes

- Backend address from the emulator: `http://10.0.2.2:8080`.
- `app/google-services.json` is never committed; get it from the team.
- Android does not run in Docker; open it in Android Studio.
