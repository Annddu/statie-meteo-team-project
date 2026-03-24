import serial
import time
import firebase_admin
from firebase_admin import credentials
from firebase_admin import db

# --- SETARI FIREBASE ---
DATABASE_URL = "https://statie-meteo-12345-default-rtdb.firebaseio.com/"

print("[*] Conectare la Firebase...")
cred = credentials.Certificate("cheie.json")
firebase_admin.initialize_app(cred, {'databaseURL': DATABASE_URL})
print("[*] Firebase conectat cu succes!")

PORT = 'COM8'
BAUD_RATE = 9600
arduino = None # Initializam variabila

def asculta_comenzi(event):
    global arduino
    if event.data and arduino and arduino.is_open:
        comanda = str(event.data)
        print(f"\n🎵 [COMANDA] Trimitem la Arduino: {comanda}")
        arduino.write(f"M:{comanda}\n".encode('utf-8'))
        db.reference('statie_meteo/comenzi/melodie').delete()

try:
    print(f"[*] Incercam sa ne conectam la Arduino pe {PORT}...")
    arduino = serial.Serial(PORT, BAUD_RATE, timeout=1)
    time.sleep(2) 
    print("[*] Arduino conectat!")

    # ABIA ACUM incepem sa ascultam comenzile de pe telefon
    db.reference('statie_meteo/comenzi/melodie').listen(asculta_comenzi)

    ultima_temp, ultima_uaer, ultima_usol = 0, 0, 0
    ultimul_istoric = 0

    while True:
        if arduino.in_waiting > 0:
            linie = arduino.readline().decode('utf-8', errors='ignore').strip()
            if linie.startswith("DATA:"):
                valori = linie.replace("DATA:", "").split(",")
                if len(valori) == 3:
                    temp, uaer, usol = float(valori[0]), float(valori[1]), float(valori[2])
                    
                    # Update Live
                    db.reference('statie_meteo/curent').set({
                        'temperatura': temp, 'umiditate_aer': uaer, 'umiditate_sol': usol
                    })

                    # Salvare Istoric (la 60 secunde sau schimbare mare)
                    acum = time.time()
                    if (acum - ultimul_istoric > 60) or (abs(temp-ultima_temp) > 0.5):
                        db.reference('statie_meteo/istoric').push({
                            'temperatura': temp, 'umiditate_aer': uaer, 'umiditate_sol': usol, 'timestamp': int(acum)
                        })
                        ultimul_istoric = acum
                        ultima_temp, ultima_uaer, ultima_usol = temp, uaer, usol
                        print(f"\n📊 Istoric salvat: {temp}°C")
                    
                    print(f"⚡ Live -> T:{temp}°C A:{uaer}% S:{usol}%", end="\r")

except Exception as e:
    print(f"\n[!] EROARE: {e}")