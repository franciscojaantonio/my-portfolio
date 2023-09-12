#!/bin/bash
rm -r ./out

mkdir ./out

# Compile all .java files in dir
javac -d ./out/ ./src/*.java

mkdir ./out/src/ServerKeys
mkdir ./out/src/AccountKeys

# Clear config file
> ./config.txt

# Compile all .java files in 'sources.txt'
    # javac -d ./out/ @sources.txt

# Beware of the package name
cd ./out/

read -p "How many servers do you want to run? " numServers

# Generate config file and launch server(s)
for i in `seq 1 $numServers`
do
    ((port = 1230 + i))
    echo "$i 127.0.0.1:$port" >> ../config.txt
    read -p $'Select intended behavior:\n1)Normal Behavior\n2)Sends Random Values\n3)Sends Multiple Requests\n4)Does Not Respond\n' serverBehavior[$i]
done

# Launch Server(s)
for i in `seq 1 $numServers`
do
    ((port = 1230 + i))
    gnome-terminal -- bash -c "java src/Server $i 127.0.0.1 $port ${serverBehavior[$i]}; bash"
done

# Launch Client(s)
gnome-terminal -- bash -c "java src/Client 0 127.0.0.1 1230; bash"
