#include "DHT.h"
#include <avr/pgmspace.h> 

// --- Setări Senzori ---
#define DHTPIN 2     
#define DHTTYPE DHT11   
DHT dht(DHTPIN, DHTTYPE);
#define PIN_SOL A0 

// --- Setări Buton și Buzzer ---
#define BUTON 4 
#define BUZZER 3

// --- Setări Alarma și Cooldown ---
unsigned long ultimulCantec = 0; 
// REPARAT: 120000UL = 2 minute (300000 era 5 minute)
unsigned long pauzaCooldown = 360000UL; 
int pragUscat = 30; 

// --- NOTE MUZICALE ---
#define NOTE_A3  220
#define NOTE_A4  440
#define NOTE_AS4 466
#define NOTE_B4  494
#define NOTE_C5  523
#define NOTE_CS5 554
#define NOTE_D5  587
#define NOTE_DS5 622
#define NOTE_E5  659
#define NOTE_F5  698
#define NOTE_FS5 740
#define NOTE_G5  784
#define NOTE_GS5 831
#define NOTE_A5  880
#define NOTE_F4  349
#define NOTE_GS4 415
#define NOTE_G4  392

// --- MELODIA SPECIALĂ: Pirații din Caraibe ---
const uint16_t melodieSpeciala[100][3] PROGMEM = {
  {NOTE_A4, 125, 50}, {NOTE_C5, 125, 50}, {NOTE_D5, 250, 50}, {NOTE_D5, 250, 50}, {NOTE_D5, 125, 50}, {NOTE_E5, 125, 50}, 
  {NOTE_F5, 250, 50}, {NOTE_F5, 250, 50}, {NOTE_F5, 125, 50}, {NOTE_G5, 125, 50}, {NOTE_E5, 250, 50}, {NOTE_E5, 250, 50},
  {NOTE_D5, 125, 50}, {NOTE_C5, 125, 50}, {NOTE_D5, 500, 100},
  {NOTE_A4, 125, 50}, {NOTE_C5, 125, 50}, {NOTE_D5, 250, 50}, {NOTE_D5, 250, 50}, {NOTE_D5, 125, 50}, {NOTE_E5, 125, 50},
  {NOTE_F5, 250, 50}, {NOTE_F5, 250, 50}, {NOTE_F5, 125, 50}, {NOTE_G5, 125, 50}, {NOTE_E5, 250, 50}, {NOTE_E5, 250, 50},
  {NOTE_D5, 125, 50}, {NOTE_C5, 125, 50}, {NOTE_A4, 500, 100}
};

// --- IMPERIAL MARCH (Vader) - Versiune lungă și ritmată ---
int melodieVader[] = {
  NOTE_A4, NOTE_A4, NOTE_A4, NOTE_F4, NOTE_C5, NOTE_A4, NOTE_F4, NOTE_C5, NOTE_A4,
  NOTE_E5, NOTE_E5, NOTE_E5, NOTE_F5, NOTE_C5, NOTE_GS4, NOTE_F4, NOTE_C5, NOTE_A4,
  NOTE_A5, NOTE_A4, NOTE_A4, NOTE_A5, NOTE_GS5, NOTE_G5, NOTE_FS5, NOTE_F5, NOTE_FS5
};
int durateVader[] = {
  500, 500, 500, 350, 150, 500, 350, 150, 650, 
  500, 500, 500, 350, 150, 500, 350, 150, 650,
  500, 350, 150, 500, 250, 250, 125, 125, 250
};

// --- Logica Afișaj ---
int modulAfisare = 0; 
bool stareButonVeche = HIGH;
int piniSegmente[] = {7, 8, 9, 10, 11, 12, 13};
byte hartaCifre[10][7] = { {1,1,1,1,1,1,0}, {0,1,1,0,0,0,0}, {1,1,0,1,1,0,1}, {1,1,1,1,0,0,1}, {0,1,1,0,0,1,1}, {1,0,1,1,0,1,1}, {1,0,1,1,1,1,1}, {1,1,1,0,0,0,0}, {1,1,1,1,1,1,1}, {1,1,1,1,0,1,1} };
byte hartaC[7] = {1,0,0,1,1,1,0}; byte hartaH[7] = {0,1,1,0,1,1,1}; byte hartaS[7] = {1,0,1,1,0,1,1};

void setup() {
  Serial.begin(9600);
  dht.begin();
  pinMode(BUTON, INPUT_PULLUP);
  pinMode(BUZZER, OUTPUT);
  for (int i = 0; i < 7; i++) pinMode(piniSegmente[i], OUTPUT);
  
  // REPARAT: Nu setăm ultimulCantec la millis(), lăsăm 0 
  // pentru ca prima alarmă să poată suna imediat dacă e nevoie.
  ultimulCantec = 0; 
}

void cantaAlarma() {
  stingeTot();
  for (int i = 0; i < 27; i++) { // Am mărit numărul de note
    tone(BUZZER, melodieVader[i], durateVader[i]);
    delay(durateVader[i] * 1.2); 
    noTone(BUZZER);
  }
}

void cantaMelodieDinFlash() {
  stingeTot();
  Serial.println(">>> REPLAY: PIRATES OF THE CARIBBEAN <<<");
  for (int i = 0; i < 45; i++) {
    uint16_t frecv = pgm_read_word(&(melodieSpeciala[i][0]));
    uint16_t dur = pgm_read_word(&(melodieSpeciala[i][1]));
    uint16_t pauza = pgm_read_word(&(melodieSpeciala[i][2]));
    if (frecv == 0) noTone(BUZZER);
    else tone(BUZZER, frecv, dur);
    delay(dur + pauza);
    noTone(BUZZER);
  }
}

void cantaMelodieCustom(String textMelodie) {
  if(textMelodie == "SPECIAL") {
    cantaMelodieDinFlash();
    return;
  }
  stingeTot();
  while (textMelodie.length() > 0) {
    int iv = textMelodie.indexOf(',');
    if (iv == -1) break;
    int frecv = textMelodie.substring(0, iv).toInt();
    textMelodie.remove(0, iv + 1);
    iv = textMelodie.indexOf(',');
    int dur = (iv == -1) ? textMelodie.toInt() : textMelodie.substring(0, iv).toInt();
    if (iv != -1) textMelodie.remove(0, iv + 1);
    else textMelodie = "";
    if (frecv == 0) noTone(BUZZER);
    else tone(BUZZER, frecv, dur);
    delay(dur + 50);
    noTone(BUZZER);
  }
}

void ascultaComenziPC() {
  if (Serial.available() > 0) {
    String comanda = Serial.readStringUntil('\n');
    comanda.trim();
    if (comanda.startsWith("M:")) {
      comanda.remove(0, 2);
      cantaMelodieCustom(comanda);
    }
  }
}

void stingeTot() { for (int i = 0; i < 7; i++) digitalWrite(piniSegmente[i], LOW); }
void deseneazaCifra(int c) { for (int i = 0; i < 7; i++) digitalWrite(piniSegmente[i], hartaCifre[c][i]); }
void deseneazaLitera(char l) {
  for (int i = 0; i < 7; i++) {
    if (l == 'C') digitalWrite(piniSegmente[i], hartaC[i]);
    else if (l == 'H') digitalWrite(piniSegmente[i], hartaH[i]);
    else if (l == 'S') digitalWrite(piniSegmente[i], hartaS[i]);
  }
}

void verificaButon() {
  bool stare = digitalRead(BUTON);
  if (stare == LOW && stareButonVeche == HIGH) {
    delay(50);
    modulAfisare++;
    if (modulAfisare > 2) modulAfisare = 0;
  }
  stareButonVeche = stare;
}

void asteapta(int ms) {
  unsigned long start = millis();
  while (millis() - start < ms) {
    verificaButon();
    ascultaComenziPC();
  }
}

void afiseazaSecventa(int nr, char lit) {
  deseneazaCifra(nr / 10); asteapta(1000); stingeTot(); asteapta(200);
  deseneazaCifra(nr % 10); asteapta(1000); stingeTot(); asteapta(200);
  deseneazaLitera(lit); asteapta(1000); stingeTot(); asteapta(1500);
}

void loop() {
  int t = dht.readTemperature();
  int h = dht.readHumidity();
  
  long sumaSol = 0;
  for(int i=0; i<10; i++) { 
    sumaSol += analogRead(PIN_SOL);
    delay(10);
  }
  int medie = sumaSol / 10; 
  int s = map(medie, 1023, 0, 0, 99);
  if (s > 99) s = 99; if (s < 0) s = 0;

  Serial.print("DATA:"); Serial.print(t); Serial.print(",");
  Serial.print(h); Serial.print(","); Serial.println(s);

  // REPARAT: Logica de timp
  if (s < pragUscat) {
    if (ultimulCantec == 0 || (millis() - ultimulCantec > pauzaCooldown)) {
      cantaAlarma();
      ultimulCantec = millis(); 
    }
  }

  if (modulAfisare == 0) afiseazaSecventa(t, 'C');
  else if (modulAfisare == 1) afiseazaSecventa(h, 'H');
  else if (modulAfisare == 2) afiseazaSecventa(s, 'S');
}