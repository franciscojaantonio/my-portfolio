#!/bin/bash

echo "Updating package lists..."

# Update package lists
sudo apt update

echo "Upgrading installed packages..."

# Upgrade installed packages
sudo apt upgrade -y

sudo apt install python3.11-venv

cd backend

# create venv

python3 -m venv .venv

source .venv/bin/activate

pip install -r requirements.txt

pip uninstall -y bson

pip uninstall -y pymongo

pip install pymongo
