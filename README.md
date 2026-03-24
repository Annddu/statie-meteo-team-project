# 🌿 Plant Guardian - IoT Smart Irrigation System

## 1. Project Description
Plant Guardian is an IoT solution designed to monitor soil moisture, air temperature, and humidity. It alerts the user via a mobile application and an acoustic alarm (Buzzer) when the plant needs watering.

## 2. Hardware Components
- **Arduino Uno** (Main Controller)
- **DHT11** (Temperature & Humidity Sensor)
- **Soil Moisture Sensor** (Analog)
- **7-Segment Display** (Local Data Visualization)
- **Active Buzzer** (Acoustic Alarms)

## 3. Software Architecture
- **Firmware:** C++/Arduino (Sensor reading & local logic).
- **Gateway:** Python script (Serial to Firebase bridge).
- **Backend:** Firebase Realtime Database (Cloud storage).
- **Mobile:** Kotlin Android App (Real-time monitoring, history charts, and background notifications).

## 4. Results & Screenshots
### Mobile Application
- **Live Monitoring:** Real-time updates for all sensors.
- **Interactive Charts:** Historical data with scroll and zoom capabilities.
- **Notifications:** Background alerts when soil moisture drops below 30%.




![Schema](https://github.com/Annddu/statie-meteo-team-project/blob/main/schema.png)

## 5. Project Demo
- Local display shows current stats.
- Mobile app syncs every 60 seconds or on significant change.
- Remote melody trigger ("SPECIAL" Pirates of the Caribbean theme).
