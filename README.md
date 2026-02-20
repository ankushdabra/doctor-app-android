# 🩺 VitalSync Doctor — Professional Healthcare Management

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.x-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.x-4285F4.svg?logo=jetpack-compose)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material--3-FFBB00.svg?logo=material-design)](https://m3.material.io/)

VitalSync Doctor is a cutting-edge Android application engineered specifically for healthcare professionals. It serves as a digital assistant to streamline clinical workflows, automate scheduling, and digitize patient care through high-fidelity prescription management and real-time analytics.

---

## 📸 App Experience

### Core Interface
| **Login & Identity** | **Clinical Dashboard** | **Patient Schedule** |
|:---:|:---:|:---:|
| ![Login](images/Login_VitalSync%20Doctor.jpg) | ![Dashboard](images/Dashboard_VitalSync%20Doctor.jpg) | ![Schedule](images/Patient_Schedule_VitalSync%20Doctor.jpg) |

### Consultation Workflow
| **Clinical Metrics** | **Digital Rx Creation** | **Prescription Archive** |
|:---:|:---:|:---:|
| ![Patient Detail](images/PatientDetail_VitalSync%20Doctor.jpg) | ![Create Prescription](images/CreatePrescription_VitalSync%20Doctor.jpg) | ![History](images/PrescriptionHistory_VitalSync%20Doctor.jpg) |

### Professional Profile
| **Public Profile** | **Bio & Credentials** | **Availability Engine** |
|:---:|:---:|:---:|
| ![Profile](images/Profile_VitalSync%20Doctor.jpg) | ![Signup1](images/Signup1_VitalSync%20Doctor.jpg) | ![Signup2](images/Signup2_VitalSync%20Doctor.jpg) |

---

## ✨ Enterprise-Grade Features

*   **📊 Insightful Analytics**: Real-time tracking of patient throughput, daily earnings, and appointment trends.
*   **🗓️ Advanced Scheduling**: Intelligent slot management with AM/PM support and multi-day availability configuration.
*   **📝 Smart Prescriptions**: Rapid Rx generation with built-in medicine searching and automated dosage instruction templates.
*   **📂 Patient EHR Access**: Instant access to patient vital signs, medical history, and previous clinical encounters.
*   **🔒 Secure Identity**: Robust JWT-based authentication system ensuring medical data privacy and compliance.
*   **🌓 Adaptive UI**: Fully dynamic theme engine supporting Light, Dark, and System-default modes with Material 3 aesthetics.

---

## 🏗️ Architecture & Tech Stack

The application follows the **Modern Android Development (MAD)** recommendations, utilizing a layered **MVVM** architecture for separation of concerns and testability.

*   **UI Layer**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for declarative UI components.
*   **Asynchronous**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html) for reactive stream processing.
*   **Dependency Injection**: Modularized architecture with decoupled components.
*   **Network**: [Retrofit 2](https://square.github.io/retrofit/) & [OkHttp 4](https://square.github.io/okhttp/) with custom interceptors for JWT management.
*   **Persistence**: [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for secure, asynchronous local storage.
*   **Image Pipeline**: [Coil](https://coil-kt.github.io/coil/) for optimized image loading and caching.

---

## 🚀 Getting Started

### Prerequisites
*   **Android Studio** Ladybug | 2024.2.1 or newer.
*   **JDK 17** (Java Development Kit).
*   **Android SDK 35** installed via SDK Manager.

### Local Setup
1.  **Clone the Repository**
    ```bash
    git clone https://github.com/your-repo/doctor-app-android.git
    ```
2.  **Configure Environment**
    Ensure your `local.properties` file points to the correct Android SDK location.
3.  **Build & Sync**
    Open the project in Android Studio and wait for the Gradle sync to complete.
4.  **Run Application**
    Select your target device/emulator and press `Shift + F10`.

---

## 🛠️ Project Structure

```text
com.doctor.app
├── appointments    # Appointment scheduling and Patient management
├── core            # Common UI, Network, Storage, and Utility components
├── dashboard       # Statistics and Main overview
└── login           # Authentication, Registration, and Profile settings
```

---

## 📄 License

**Copyright © 2024 VitalSync Healthcare Solutions.**  
*Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at* [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
