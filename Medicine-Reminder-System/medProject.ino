// main - desktop

#include <Servo.h>
#define servo1Pin 2
Servo servo1;
// ThingSpeak -------------------------------------------------------


#include <ESP8266WiFi.h>
//#include "secrets.h"
#include "ThingSpeak.h" // always include thingspeak header file after other header files and custom macros

char ssid[] = "imran";   // your network SSID (name) 
char pass[] = "imran123";   // your network password
int keyIndex = 0;            // your network key Index number (needed only for WEP)
WiFiClient  client;

unsigned long myChannelNumber = 2524933;
const char * myWriteAPIKey = "3YCGLPOOWFT3CHTJ";

// ThingSpeak -------------------------------------------------------


#include <RtcDS3231.h>

#include <Wire.h> 
RtcDS3231<TwoWire> Rtc(Wire);
#include <LiquidCrystal_I2C.h>
#define buzzer D3
#define stopAlarm D5
#define medTakenButton D6
#define medUploadButton D7
#define alarmLedPin D0
// Set the LCD address to 0x27 for a 16 chars and 2 line display
LiquidCrystal_I2C lcd(0x27, 16, 2);

int hr1 = 0, minut1 = 32;
int morningH = 3,morningM = 25;
int noonH = 3,noonM = 27;
int nightH = 3,nightM = 29;
int medA,medB,medC;
int alarm_len = 1;
int medFrequency = 3;
bool medTaken = 0;

void setup()
{
  servo1.attach(servo1Pin);
  servo1.write(0);
  // not working
//  Serial.println("Program started");
//  Rtc.SetDateTime(RtcDateTime(2024, 4, 22, 2, 55, 50));
//
//  RtcDateTime nTime = Rtc.GetDateTime();
//  Serial.print("CurrTime = ");
//  Serial.println(nTime.Minute());

  //delay(1000);
  
  Serial.begin(9600);
  pinMode(buzzer, OUTPUT);
  pinMode(alarmLedPin, OUTPUT);
  pinMode(stopAlarm, INPUT);
  //pinMode(A0, OUTPUT);
	// initialize the LCD
	lcd.begin();

	// Turn on the blacklight and print a message.
	lcd.backlight();
	//lcd.print("Hello, world!");
  //rtc.setDOW(WEDNESDAY);     // Set Day-of-Week to SUNDAY
//  Rtc.SetDateTime(2024,4,20,11, 5, 0);     // Set the time to 12:00:00 (24hr format)
  //rtc.setDate(1, 1, 2014);   // Set the date to January 1st, 2014

  medA = medB = medC = 10;

  // ThingSpeak -------------------------------------------------------

  WiFi.mode(WIFI_STA); 
  ThingSpeak.begin(client);  // Initialize ThingSpeak
  
  // ThingSpeak -------------------------------------------------------

}

void loop()
{
    //Rtc.SetDateTime(RtcDateTime(2024, 5, 6, 1, 8, 0)); // upload once, then comment it and upload another time

    // ThingSpeak -------------------------------------------------------

    // Connect or reconnect to WiFi
    if(WiFi.status() != 7){
//    if(WiFi.status() != WL_CONNECTED){
      Serial.print("Attempting to connect to SSID: ");
      //Serial.println(SECRET_SSID);
      while(WiFi.status() != WL_CONNECTED){
        WiFi.begin(ssid, pass);  // Connect to WPA/WPA2 network. Change this line if using open or WEP network
        Serial.print("Connecting..... Wifi Status = ");
        Serial.println(WiFi.status());
        delay(5000);     
        if(WiFi.status() == 7)
          break;
        //break; // temporary
      } 
      Serial.println("\nConnected.");
    }

    // set the fields with the values
    ThingSpeak.setField(1, medA);
    ThingSpeak.setField(2, medB);
    //ThingSpeak.setField(3, number3);
    //ThingSpeak.setField(4, number4);

    // missing the status 

    int x = ThingSpeak.writeFields(myChannelNumber, myWriteAPIKey);
    if(x == 200){
      Serial.println("Channel update successful.");
    }
    else{
      Serial.println("Problem updating channel. HTTP error code " + String(x));
    }
    
    // ThingSpeak -------------------------------------------------------

    RtcDateTime now = Rtc.GetDateTime();
    
    lcd.setCursor(0,0);
    lcd.print("Time:");
    //lcd.setCursor(5,0);
    if(now.Hour() < 10)
      lcd.print('0');
    lcd.print(now.Hour(), DEC);
    lcd.print(':');
    if(now.Minute() < 10)
      lcd.print('0');
    lcd.print(now.Minute(), DEC);
    lcd.print(':');
    if(now.Second() < 10)
      lcd.print('0');
    lcd.print(now.Second(), DEC);


    if(medFrequency == 1)
    {
      morning();
    }
    else if(medFrequency == 2)
    {
      morning();
      night();
    }
    else if(medFrequency == 3)
    {
      morning();
      noon();
      night();
    }

    isMedTaken();
    isMedUploaded();
   // clearBit(4,1);
//    bool st = digitalRead(stopAlarm);
//    Serial.print("Switch value = ");
//    Serial.println(st);
    Serial.print("Med A balance = ");
    Serial.print(medA);
    Serial.print(" , Med B balance = ");
    Serial.println(medB);

}

void clearBit(int c, int r)
{
  lcd.setCursor(r,c);
  lcd.write('I');
}

void isMedTaken()
{
  bool val = digitalRead(medTakenButton);
  if(medTaken == 0 && val == HIGH)
  {
    medA--,medB--;
    medTaken = 1;
    servo1.write(0);
    ThingSpeak.setField(1, medA);
    ThingSpeak.setField(2, medB);
    int x = ThingSpeak.writeFields(myChannelNumber, myWriteAPIKey);
    if(x == 200){
      Serial.println("Channel update successful.");
    }
    else{
      Serial.println("Problem updating channel. HTTP error code " + String(x));
    }
    //Serial.println("Updated --------------------- ");
  }
  //Serial.print("MedTakenButton = ");
  //Serial.println(val);
}
void isMedUploaded()
{
  bool val = digitalRead(medUploadButton);
  if(val == HIGH)
  {
    medA += 10;
    medB += 10;
    medC += 10;
    delay(200);
  }
}

void openBox()
{
  for(int i = 2; i <= 180; i++)
  {
    servo1.write(i);
    delay(2);
  }
}

void Buzzer()
    {
    digitalWrite(buzzer,HIGH);
    delay(500);
    digitalWrite(buzzer, LOW);
    delay(500);
}
