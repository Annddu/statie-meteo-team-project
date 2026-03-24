# 🌿 Plant Guardian - Smart IoT Monitoring System

Plant Guardian is a comprehensive Internet of Things (IoT) solution designed for advanced plant care. The system monitors environmental conditions in real-time and triggers acoustic alarms and mobile notifications when the plant requires immediate attention.

---

## 🚀 Key Features

### 📱 Companion Mobile Application (Android)
Developed in **Kotlin**, the app provides an intuitive interface for managing your weather station:

* **Real-time Data:** Instant visualization of temperature, air humidity, and soil moisture, synchronized via **Firebase Realtime Database**.
* **Interactive Charts (History):** Powered by the *MPAndroidChart* library, the chart features:
    * **Time-based X-Axis:** Displays the exact hour and minute data points were recorded.
    * **Scroll & Zoom:** Seamlessly navigate through historical data and zoom into specific time intervals.
    * **Multi-Series Display:** Monitor all 3 parameters simultaneously on a single, clean graph.
* **24/7 Background Notifications:** Utilizes a *Foreground Service* to monitor soil moisture even when the app is closed. If moisture drops below **30%**, a critical alert is sent to your device.
* **Remote Control:** Dedicated button to manually trigger the "SPECIAL" Pirates of the Caribbean theme on the Arduino buzzer.

### 🤖 Hardware Logic (Arduino & Python)
* **Local "Vader" Alarm:** When soil moisture hits critical levels, the Arduino triggers a buzzer playing the *Imperial March* (Star Wars) in a high-tempo rhythm.
* **Local Display:** A 7-segment display cycles through temperature, humidity, and soil levels directly at the physical station.
* **Python Gateway:** A secure script bridges the Serial (USB) communication to the Cloud, ensuring data integrity and instant message delivery from the phone to the Arduino.

---

## 📸 Screenshots (Demo)

| APP | CIRCUIT |
| :---: | :---: |
| ![APP](https://github.com/Annddu/statie-meteo-team-project/blob/main/aplicatiess.jpeg) | ![CIRCUIT](https://github.com/Annddu/statie-meteo-team-project/blob/main/schema.png) |

> *Note: Replace the placeholders above with actual screenshots from your app saved in the `images` folder.*

---

## 🛠️ System Architecture

1. **Sensors (DHT11 + Soil Moisture)** ➔ **Arduino** (Local Processing)
2. **Arduino** ➔ (Serial USB) ➔ **Python Gateway**
3. **Python Gateway** ➔ **Firebase Cloud** (Storage & Sync)
4. **Firebase** ➔ **Android App** (Notifications & Charts)

---

## 🎥 Video Presentation
The full project demo, including hardware connections and alarm functionality (music + notifications), is available in the Teams assignment folder.

---

## 👨‍💻 Team
Grades and individual contributions are documented in the `teamGrades.txt` file located in this repository.
