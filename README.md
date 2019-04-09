# Description
Real-time home monitoring application using raspberry pi 3B+ and AWS

# Before To Use
This Project is Personal Toy Project \
All Information is stored in plain text in database of server \
Project is terminated.

# Client
- Anrdoid Level : min level - 26 , target - 27

# Raspberry Pi 3
- Kits
 > DHT11, HCSR04, PIR, LCD \
DHT11 : To check humidity & Temperature -> Detecting Fire \
HCSR04 : To detect intruders \
PIR : To detect intruders with HCSR04 \
LCD : To show recognized information in Raspberry Pi 3
 
- On Raspberry Pi 3
 > Camera
Will take picture or video to show real-time states of client's home

# How to Use - Client
- Raspberry Pi 
> Just save /Raspberry Pi Source Code/Main.py and run \
python3 Main.py \
> If you want to auto run, You will be change Main.py path in AutoRun.sh and register Crontab

- Anrdoid \
Just Install Application

# Server
> Need to Apache tomcat 8 \
Use Sqllite3, But Using Another DB is OK
- Will Save Personal Information, Picture and Video to make credential or to send client

# CopyRight
 Copyright 2018. Girlfriend_Yerin, Ali rights resvered.
