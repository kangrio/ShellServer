# ShellServer

ShellServer is an Android library designed to allow applications to execute shell commands or run privileged code through a dedicated server process. It helps developers bypass standard application limitations when deep system access or device control is required.

## Core Features

- **Execute Shell Commands:** Run shell commands directly and capture the output, error streams, and exit codes.
- **Remote Runnables:** Send tasks (via `ShellServerRunnable`) to be executed on the server process, supporting both one-time and scheduled execution.
- **System Services Access:** Interface with and invoke Android System Services directly from your application.
- **Pre-built Actions:** Includes ready-to-use implementations for common tasks such as UI navigation (key events), power management, and system diagnostics.

## Project Structure

- `shell_server`: The core module containing server logic, AIDL interfaces, and client-side helper utilities.
- `sample`: An example application demonstrating how to integrate and use the library in a real-world scenario.

## Getting Started

### 1. Connecting to the Server

Initialize the connection using `ShellServerHelper`:

```kotlin
ShellServerHelper.init(context) { binder ->
    if (binder != null) {
        // Connected successfully and ready for use
    }
}
```

### 2. Executing Shell Commands

```kotlin
val response = ShellServerHelper.exec("pm list packages")
println("Output: ${response.output}")
```

### 3. Running Background Tasks (Runnables)

You can create classes that implement `ShellServerRunnable` to execute complex logic on the server:

```kotlin
val taskId = ShellServerHelper.runOnce(NavigationRunnable(KeyEvent.KEYCODE_HOME))
```

## Important Note

This project requires the server process to be started with elevated privileges (e.g., via ADB or Root) to function as intended and perform privileged operations.

## Acknowledgments

This project utilizes the following libraries:

- [dadb](https://github.com/mobile-dev-inc/dadb) by [mobile-dev-inc](https://github.com/mobile-dev-inc) - Used for ADB protocol implementation.
- [HiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass) by [LSPosed](https://github.com/LSPosed) - Used for accessing restricted system APIs.

## License

Copyright (c) 2026 KangRio

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
