#!/bin/bash
cd /home/ubuntu/webapp
sudo rm -rf logs/*
source /etc/profile.d/envvariable.sh
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/home/ubuntu/webapp/cloudwatch-config.json -s
cd target
sudo chmod +x ROOT.jar
kill -9 $(ps -ef|grep java | grep -v grep)
nohup java -jar ROOT.jar > /home/ubuntu/webapp/output.txt 2> /home/ubuntu/webapp/output.txt < /home/ubuntu/webapp/output.txt &