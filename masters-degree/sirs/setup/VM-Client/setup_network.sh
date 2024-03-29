#!/bin/bash

# This script is used to setup the network configuration for the machine

# Set Netplan configuration
sudo cp ./netplan-config.yaml /etc/netplan/01-network-manager-all.yaml

sudo netplan apply

echo "Netplan configuration set successfully!"
