import sys
import os
import RPi.GPIO as GPIO
import Adafruit_DHT
import time
import threading
from picamera import PiCamera
from gpiozero import MotionSensor
from RPLCD import CharLCD
import socket
from datetime import datetime

filePath = "/home/pi/Desktop/IOT_Term_Project/"
PicturePath = "Picture/"
VideoPath = "Video/"
host = '52.34.74.63'
port = 8111

# Tag
machine_id = 'AAAAA'
DATA_TYPE_INFO = 'Info'
DATA_TYPE_PICTURE = 'Picture'
DATA_TYPE_VIDEO = 'Video'

# Camera
camera = PiCamera()

# Ultrasonic GPIO Sensor
GPIO_TRIGGER = 2
GPIO_ECHO = 3

# Humidity & Temperature Sensor
DHT_sensor = 11
GPIO_HnT = 4

# Motion Sensor
GPIO_Motion = 17
PIR = MotionSensor(GPIO_Motion)
captured = False
recorded = False
capturedTime = ''

# OnOff LED
GPIO_Red = 27

# LCD
lcd = CharLCD(cols=16, rows=2, pin_rs=5, pin_e=6,
              pins_data=[13, 19, 26, 9, 11, 16, 20, 21], numbering_mode=GPIO.BCM)

# URL
url = 'http://52.34.74.63:8111'

# Data
last_humidity = 0
last_temperature = 0

#Flag
isFinish = False

GPIO.setmode(GPIO.BCM)
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
GPIO.setup(GPIO_ECHO,GPIO.IN)
GPIO.setup(GPIO_Red,GPIO.OUT)

def distance():
    while(isFinish is False):
        GPIO.output(GPIO_TRIGGER, True)
        
        time.sleep(0.0001)
        GPIO.output(GPIO_TRIGGER,False)
        
        StartTime = time.time()
        StopTime = time.time()
        
        while GPIO.input(GPIO_ECHO) == 0:
            StartTime = time.time()
            
        while GPIO.input(GPIO_ECHO) == 1:
            StopTime = time.time()
            
        TimeElapsed = StopTime - StartTime
            
        distance = (TimeElapsed * 34300) / 2
            
        #print("Measured Distance = %.1f cm" %distance)
        time.sleep(0.7)
    print('Stop Distance')

def humidityNTemperature():
    while(isFinish is False):
        global last_humidity, last_temperature
        humidity, temperature = Adafruit_DHT.read_retry(DHT_sensor, GPIO_HnT)

        if humidity is not None and temperature is not None:
            last_humidity = humidity
            last_temperature = temperature
            #print('Temp : {0:0.1f}   Humidity {1:0.1f}'.format(temperature, humidity))
            
        time.sleep(1)
    print('Stop HNT')

def motionDetect():
    print('Start Motion Detect')
    global captured, capturedTime
    while isFinish is False:
        now = datetime.now()
        capturedTime = ('%s-%s-%s %s:%s:%s' %(now.year, now.month, now.day, now.hour, now.minute, now.second))
        PIR.wait_for_motion()
        print('Motion Detected')
        GPIO.output(GPIO_Red,True)
        camera.capture(filePath+ PicturePath +capturedTime +".jpg")
        captured = True
        time.sleep(5)     
        PIR.wait_for_no_motion()
        print('Motion UnDetected')
        GPIO.output(GPIO_Red,False)
        time.sleep(5)
    print('Stop Motion')

def LCD_Logo():
    time.sleep(5)
    while(isFinish is False):
        lcd.cursor_pos = (0,0)
        lcd.write_string("%s" %time.strftime("%Y / %m / %d"))
        lcd.cursor_pos = (1, 0)
        lcd.write_string("%s" %time.strftime("%p %l : %M : %S"))
        time.sleep(0.1)
    print('Stop LCD')

if __name__ == '__main__':
    print('Start Project')
    lcd.write_string("Hi ! '^'")
    motionThread = threading.Thread(target=motionDetect,daemon = True)
    LCDThread = threading.Thread(target=LCD_Logo, daemon = True)
    humidityThread = threading.Thread(target=humidityNTemperature, daemon = True)
    distanceThread = threading.Thread(target=distance, daemon = True)
    try:
        while True:
            clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)   
            try :
                clientSocket.connect((host, port))
                if LCDThread.isAlive() is False:
                    motionThread.start()
                    LCDThread.start()
                    humidityThread.start()
                    distanceThread.start()
                dataType = ''
                          
                if captured is False and recorded is False :
                    dataType = DATA_TYPE_INFO  
                else :
                    if captured is True :
                        dataType = DATA_TYPE_PICTURE
                    else :
                        dataType = DATA_TYPE_VIDEO

                header = ''.join(['data_type :' + dataType, '\r\n'])
                header = header.encode()
                header = bytearray(header)
                machineID = ''.join(['data_type :' + machine_id, '\r\n'])
                machineID = machineID.encode()
                machineID = bytearray(machineID)
                clientSocket.send(header)
                clientSocket.send(machineID)

                if dataType is DATA_TYPE_INFO:
                    data = 'temp :' + str(last_temperature) + '\r\n'
                    data += 'humidity :' + str(last_humidity) + '\r\n'
                    print (data)
                    data = data.encode()
                    data = bytearray(data)
                    clientSocket.sendall(data)
                elif dataType is DATA_TYPE_PICTURE :
                    fileName = capturedTime + '.jpg'
                    imageFile = filePath + PicturePath+ fileName
                    #print(imageFile)
                    image = open(imageFile, 'rb')
                    data = 'filename :' + fileName +'\r\n' + 'fileSize :' + str(os.path.getsize(imageFile)) + '\r\n'
                    #print (data)
                    data = data.encode()
                    data = bytearray(data)
                    #print (image)
                    clientSocket.sendall(data)
                    clientSocket.sendfile(image)
                    captured = False

                recvData = b''
                while (b'Over' not in recvData) :
                    recvData = clientSocket.recv(1024)
            except ConnectionRefusedError:
                lcd.cursor_pos = (0,0)
                lcd.write_string('     Server')
                lcd.cursor_pos = (1,0)
                lcd.write_string('     Closed')
                print ('Server Closed')
            except IOError as e:
                print(e)
                break
            except OSError:
                print('alreay Connect')
            finally :
                clientSocket.close()
                time.sleep(1)
    except KeyboardInterrupt:
        print("Measurement stopped by User")
    finally :
        isFinish = True
        if LCDThread.isAlive() is True :
            motionThread.join()
            LCDThread.join()
            humidityThread.join()
            distanceThread.join()
        while motionThread.isAlive() or LCDThread.isAlive() or humidityThread.isAlive() or distanceThread.isAlive():
            time.sleep(1)
        lcd.cursor_pos = (0,0)
        lcd.write_string('   Good Bye   ')
        lcd.cursor_pos = (1,0)
        lcd.write_string('   TT . TT   ')
        print('Good bye')
        GPIO.cleanup()
