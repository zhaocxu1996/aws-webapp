#!/bin/bash
cd /home/centos/webapp
sudo rm -rf logs/*
sudo chown -R /home/ubuntu/webapp
sudo chmod +x ROOT.jar
source /etc/profile.d/envvariable.sh
kill -9 $(ps -ef|grep java | grep -v grep)
nohup java -jar ROOT.jar