#!/bin/bash

echo "Updating package lists..."

# Update package lists
sudo apt update

echo "Upgrading installed packages..."

# Upgrade installed packages
sudo apt upgrade -y

sudo apt install python3.11-venv

sudo apt-get install -y maven

mvn clean compile
mvn install

# Get the current working directory
current_dir=$PWD

# Set the relative path to appassembler/bin
relative_path="/target/appassembler/bin"

# Create the full path by combining the current directory and the relative path
full_path="$current_dir$relative_path"
export PATH=$full_path:$PATH

cd frontend

python3 -m venv .client-venv

pip install requests

python3 cli.py
