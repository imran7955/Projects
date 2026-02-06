void give_alarm(int callingM, int len)
{
  //servo1.write(180);
  openBox(); // new added
  //Serial.println("give_alarm is called");
  medTaken = 0;
  digitalWrite(buzzer, HIGH);
  lcd.clear();    
  //lcd.print("Alarm ON");
  bool alarmStoped = 0,buzzerVoltage = HIGH;
  while(1)
  {

    if(alarmStoped == 0)
    {
      lcd.setCursor(0,1);
      lcd.print("Alarming");
    }
    else
    {
      lcd.setCursor(0,1);
      lcd.print("Alarm end"); // clearing the "Alarming" message
    }
    RtcDateTime curTime = Rtc.GetDateTime();
//    Serial.println("CurrTime = ");
//    Serial.println(curTime.Minute());
//    Serial.println("  callingM+len = ");
//    Serial.println(callingM+len);


    lcd.setCursor(0,0);
    lcd.print("Time:");
    if(curTime.Hour() < 10)
      lcd.print('0');
    lcd.print(curTime.Hour(), DEC);
    lcd.print(':');
    if(curTime.Minute() < 10)
      lcd.print(0);
    lcd.print(curTime.Minute(), DEC);
    lcd.print(':');
    if(curTime.Second() < 10)
      lcd.print(0);
    lcd.print(curTime.Second(), DEC);

    
    // isMedTaken(); // new added
    bool stopRun = digitalRead(stopAlarm);
    //Serial.print("Digital read(button) = ");
    //Serial.println(stopRun);
    //if(curTime.Minute() >= callingM+len)
    if(curTime.Minute() >= callingM+len || stopRun == HIGH)
    {
      buzzerVoltage = LOW;
      alarmStoped = 1;
      //delay(5000);
      //lcd.clear();
      //break;
    }
    if(curTime.Minute() >= callingM+len){
      digitalWrite(buzzer,LOW);
      digitalWrite(alarmLedPin,LOW);
      lcd.clear(); 
      break;
    }

    digitalWrite(buzzer,LOW);
    digitalWrite(alarmLedPin,LOW);
    delay(250);
    digitalWrite(buzzer,buzzerVoltage);
    digitalWrite(alarmLedPin,buzzerVoltage);
    delay(250);

    isMedTaken();
  }
  //Serial.println("Alarm end");
}

void morning()
{
  RtcDateTime curTime = Rtc.GetDateTime();;
  if( curTime.Hour() == morningH && curTime.Minute() == morningM) //Comparing the current time with the Alarm time
  {
    give_alarm(morningM,alarm_len);
  } 
  else
  {
    Serial.println("Not yet");
    //Serial.println(curTime.Minute());
  }
}
void noon()
{
  RtcDateTime curTime = Rtc.GetDateTime();
  if( curTime.Hour() == noonH && curTime.Minute() == noonM) //Comparing the current time with the Alarm time
  {
    give_alarm(noonM,alarm_len);
  } 
}
void night()
{
  RtcDateTime curTime= Rtc.GetDateTime();
  if( curTime.Hour() == nightH && curTime.Minute() == nightM) //Comparing the current time with the Alarm time
  {
    give_alarm(nightM,alarm_len);
  } 
}
