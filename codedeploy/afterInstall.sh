#!/bin/bash
cd /home/ubuntu
sudo rm -rf logs/*
sudo chmod +x ROOT.jar
source /etc/profile.d/envvariable.sh
kill -9 $(ps -ef|grep java | grep -v grep)
nohup java -jar ROOT.jar > /home/ubuntu/output.txt 2> /home/ubuntu/output.txt < /home/ubuntu/output.txt &