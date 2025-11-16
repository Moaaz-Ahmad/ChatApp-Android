# StudyConnect

Jetpack Compose app that connects university students via authenticated chat rooms, backed by Firebase Auth and Cloud Firestore.

## Features by Roadmap Stage

1. **Setup & Firebase** – Compose project namespace `com.studyconnect.app`, Kotlin + Material 3 theme, Google services plugin, Firebase Auth/Firestore dependencies, and `android.permission.INTERNET` granted.
2. **Authentication** – Email/password auth restricted to `@myuniversity.edu`, Login and Sign Up screens, shared `AuthViewModel` with `StateFlow`, navigation from Splash based on `Firebase.auth.currentUser`.
3. **Core UI & Models** – Data classes for `User`, `ChatRoom`, `Message`; chat list with lazy column of rooms; chat room screen with top bar, message list, and composer using `MessageItem` styling per sender.
4. **Firestore Messaging** – `FirestoreRepository` streaming chat rooms/messages, membership enforcement, message sending updates `lastMessage`; `ChatListViewModel` and `ChatRoomViewModel` expose UI state + flows.
5. **Refinement** – `ProfileScreen` for editing `displayName`, Material 3 styling, snackbars for errors, inline loading indicators, and auto-scroll when new messages arrive.
6. **Room Management** – Floating action button on the chat list opens a Create Room form where validated `@myuniversity.edu` emails can be invited before the room goes live.

## Getting Started

1. **Android Studio** – Arctic Fox (or newer) with Kotlin 2.x Compose tooling.
2. **Firebase console**
   - Create a project (e.g., `studyconnect`).
   - Register the Android app with package `com.studyconnect.app` and SHA-1 (optional for now).
   - Download `google-services.json` and replace the placeholder at `app/google-services.json`.
   - Enable **Email/Password** in Authentication.
   - Create Firestore in *Native* mode.
3. **Gradle Sync** – From the repo root run:

```powershell
.\gradlew.bat tasks
```

(Or open the project in Android Studio and let it sync automatically.)

## Firestore Security Rules

Deploy rules similar to the snippet below, which enforces authentication and restricts writes to room members:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    match /chatRooms/{roomId} {
      allow read: if request.auth != null &&
        (getResource().data.memberIds.size() == 0 || request.auth.uid in getResource().data.memberIds);

      allow write: if request.auth != null && request.auth.uid in request.resource.data.memberIds;

      match /messages/{messageId} {
        allow read, write: if request.auth != null &&
          request.auth.uid in get(/databases/$(database)/documents/chatRooms/$(roomId)).data.memberIds;
      }
    }
  }
}
```

## Running & Testing

Build (and trigger Compose preview validation) via:

```powershell
.\gradlew.bat assembleDebug
```

For UI tests (requires emulator/device):

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Once the build succeeds, install `app/build/outputs/apk/debug/app-debug.apk` on a device that has network access and Google Play Services.

## Creating Chat Rooms

- Open the chat list and tap the floating `+` button to launch the **Create chat room** screen.
- Pick a descriptive title and list member emails separated by commas, spaces, or new lines (only `@myuniversity.edu` accounts that have already signed in at least once can be added).
- After you submit, the app creates the Firestore document, updates membership, and returns you to the chat list with the new room ready to use.

## Notes

- Replace the placeholder `google-services.json` before shipping.
- The repository expects Material 3; theme overrides live under `app/src/main/java/com/studyconnect/app/ui/theme`.
- Repository classes rely on `kotlinx-coroutines-play-services` for `Tasks.await` support—ensure Gradle sync grabs the dependency.
