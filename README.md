# Juggle

Juggle is a task app for groups, so you can make a group, throw tasks at it, assign people, and set deadlines while it keeps track of everything. On top of that there's a calendar, a notification feed, and one  fun part where you save a place like home or campus and Juggle pings you when you walk in and something's due.

It's an Android app written in Kotlin with Jetpack Compose, talking to a separate FastAPI backend.

## The setup

The app is hardwired to a local dev stack where it expects the backend on your machine and Firebase's auth emulator running next to it. That means getting a working app requires three things going at once, which are the backend, the auth emulator, and an emulator with the app installed.

You'll need JDK 21, Android Studio (or the SDK plus an emulator), and an SDK platform for `compileSdk = 37`, while `minSdk` is 33.

You can clone the repo using this command:


```bash
git clone https://github.com/ISIS3510-Team12/Kotlin.git
cd Kotlin
```

Then create a `local.properties` at the root, which is gitignored so it won't exist yet.

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=your_maps_api_key_here
```

The maps key gets baked into the manifest, and if it isn't authorized for the app's package name and your signing SHA-1 then the map comes up blank, it's important for you to note that.

Firebase is already handled since `app/google-services.json` is committed for project `juggle-65ff0`.

For the backend, grab [ISIS3510-Team12/Back-end](https://github.com/ISIS3510-Team12/Back-end), which needs Postgres and S3 that its Docker setup takes care of, and then the API runs on port 8000.

```bash
docker compose up -d
uv run uvicorn app.main:app --reload --proxy-headers --forwarded-allow-ips=*
```

And the auth emulator comes from `firebase.json`, with auth on 9099 and its UI on 4000.

```bash
firebase emulators:start --only auth
```

One emulator quirk worth knowing is that from inside the Android emulator `10.0.2.2` is your host machine, and the app points there for both the backend and the auth emulator, which is why debug builds allow cleartext traffic and ask for the local network permission that Android 17 otherwise uses to block local addresses. 

## Running it

```bash
./gradlew assembleDebug
```

Or just hit Run in Android Studio, where you'll land on onboarding and sign-in needs the backend and auth emulator up.

If you need to point it somewhere else, the URLs and flags are hardcoded in `app/build.gradle.kts` covering `BASE_URL`, `USE_FIREBASE_EMULATOR`, the emulator host and port, and `GOOGLE_WEB_CLIENT_ID`, which are not in `local.properties` so edit the build file directly.

## How it's put together

Most of the code lives under `com.team12kotlin.juggle`, where `AppRoot.kt` holds the nav graph and the auth routing, `data/` is the Retrofit API plus repositories wired by hand in `Dependencies.kt`, `ui/` is the screens and ViewModels and shared components, `reminders/` is the geofencing and notification components, and `utils/` are small date helpers.

## The demo
You may find the app demo here: [Demo](https://youtu.be/LFA7VD-WpQc)

---

Built for ISIS-3510 - Team 12-kotlin :)
