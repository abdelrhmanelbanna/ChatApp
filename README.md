# Chat App (Clean Architecture - Kotlin)

A modern real-time chat application built using **Kotlin**, **Clean Architecture**, **Hilt**, **WorkManager**, and **Supabase** as the backend.  
The app follows best practices in Android development, ensuring a clean separation of concerns between layers.

---

##  Project Architecture

This project is based on **Clean Architecture**, divided into three main layers:

```
Presentation (UI)
↑
Domain (Use Cases)
↑
Data (Repositories & Data Sources)
```

### **Data Layer**
Responsible for communicating with external data sources such as Supabase and managing data operations.

- **MessageDataSource / MessageDataSourceImpl**
  - Handles network operations (CRUD) for messages.
  - Uses Supabase client for:
    - `sendMessage()`
    - `updateMessageStatus()`
    - `observeMessages()` via real-time subscription.

- **UserDataSource / UserDataSourceImpl**
  - Uploads profile images to Supabase Storage.
  - Manages user-related operations (get, save, etc).

---

### **Domain Layer**
Contains **business logic** (Use Cases) and **core entities**.

#### 🧩 Entities
- `Message`
- `MessageStatus`
- `User`

####  Use Cases
Each use case performs a single business action:
- `SendMessageUseCase`
- `RetryMessageUseCase`
- `ObserveMessagesUseCase`
- `UpdateMessageStatusUseCase`
- `GetUserUseCase`
- `SaveUserUseCase`
- `UploadProfileImageUseCase`

####  Repository Interfaces
- `MessageRepository`
- `UserRepository`

These are implemented in the **data layer** to decouple logic from implementation details.

---

### **Presentation Layer (UI)**
Implemented using **Jetpack Compose** and **MVVM** architecture.

####  `ChatViewModel`
Handles:
- Sending messages (`sendMessage()`)
- Retrying failed messages
- Observing messages from `ObserveMessagesUseCase`
- Managing UI state via `StateFlow`
- Triggering side effects (e.g., image picker, toast messages)

####  `ChatState`
Holds UI data such as:
- List of messages
- Composing text
- Selected images
- Current user ID

####  `ChatIntent` & `ChatEffect`
Used to represent user actions (intents) and one-time events (effects).

---

##  Background Sending (WorkManager)

The app uses **WorkManager** via a class called `MessageSendManager` that:
- Enqueues a background worker (`SendMessageWorker`)
- Handles message uploading (text/media)
- Calls `SendMessageUseCase` to send the message
- Calls `UpdateMessageStatusUseCase` to update the message status after success/failure
- Automatically retries failed messages when connected again

---

##  DataStore

Used for **persisting user information locally** (userId, username, etc).

**`DataStoreManager`**:
- `saveUserId(userId: String)`
- `getUserId(): Flow<String?>`
- `saveUsername(username: String)`
- `getUsername(): Flow<String?>`

`ChatViewModel` loads user data from `DataStoreManager` during initialization.

---

##  Supabase Integration

Supabase acts as the backend for:
- **Authentication (optional)**
- **Database (PostgreSQL)**
- **Realtime subscription** (for instant message updates)
- **Storage** (for image & media uploads)

### Key Tables:
- `messages`
  - `id`
  - `userId`
  - `username`
  - `profileImage`
  - `content`
  - `mediaUrls`
  - `createdAt`
  - `status` (SENDING, SENT, FAILED)

### Common Issues Solved:
 Message status not updating  
 Username saved as "You" only → fixed via DataStore  
 Background message upload with correct status sync  
 Worker Hilt injection setup  
 Supabase client setup across modules

---

## Dependency Injection (Hilt)

All UseCases and Repositories are provided via Hilt modules.

Hilt simplifies dependency management between layers (e.g., injecting `SendMessageUseCase` into `ChatViewModel`).

---

## Domain Exception Handling

All domain operations return `ResultWrapper`.

This ensures consistent handling of errors (network, validation, unknown).

---

##  Tech Stack

| Layer | Technology |
|-------|-------------|
| UI | Jetpack Compose, StateFlow, ViewModel |
| Background | WorkManager |
| DI | Hilt |
| Storage | DataStore Preferences |
| Network | Supabase |
| Language | Kotlin (Coroutines + Flows) |
| Architecture | Clean Architecture + MVVM |

---

## Features Summary

✅ Real-time messaging using Supabase  
✅ Send text and multiple images (up to 10)  
✅ Message retry system  
✅ Offline-safe sending with WorkManager  
✅ Message status updates (SENDING → SENT → FAILED)  
✅ Profile image upload  
✅ Persistent user identity via DataStore  
✅ Clean modular code organization  

---

## Folder Structure

```
com.example.chatapp/
│
├── chatScreen/
│   ├── ChatViewModel.kt
│   ├── ChatState.kt
│   ├── ChatIntent.kt
│   ├── ChatEffect.kt
│   └── MessageSendManager.kt
│
├── core/
│   └── datastore/
│       └── DataStoreManager.kt
│
├── di/
│   ├── UseCaseModule.kt
│   ├── RepositoryModule.kt
│   └── DataSourceModule.kt
│
├── worker/
│   └── SendMessageWorker.kt
│
└── MainActivity.kt
```

---

## Common Fixes and Notes

| Issue | Cause | Solution |
|-------|--------|-----------|
| Message status stays “sending” | `updateMessageStatus()` not updating in Supabase | Added correct `.eq("id", messageId)` filter |
| Username always “You” | Missing username persistence | Added `DataStoreManager.saveUsername()` |
| Hilt Worker error | Missing `@AssistedInject` constructor | Fixed by proper WorkerFactory setup |
| Supabase URI invalid | Wrong file handling in `uploadProfileImage()` | Used `context.contentResolver.openInputStream()` properly |

---

## Author

**Abdelrahman Elbanna**  
Android Developer & Software Instructor  
Specializing in Kotlin, Clean Architecture, and Android Development.

---

## License

This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.
