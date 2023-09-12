@echo off
start "Server db1" /MIN mongod --config \sensorSet\server1\server1.conf
start "Server db2" /MIN mongod --config \sensorSet\server2\server2.conf
start "Server db3" /MIN mongod --config \sensorSet\server3\server3.conf