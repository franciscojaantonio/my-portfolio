#!/bin/bash

# Run this script to setup user's certificate in this machine

# Check for correct usage
if [ $# -ne 2 ]; then
    echo "Usage: $0 <user_name> <ca_name>"
    exit
fi

# Keys directorys
keys_dir="../auth/keys"

# Generate CA KeyPair
./keygen.sh $1

# Create a Certificate Signing Request (CSR) for the CA
./create_csr.sh $1

# Self-sign the CSR
./sign_csr.sh $1 $2

# TODO: Distribute the CA certificate to all machines

echo "Certificate Authority (CA) setup complete for user '$1'!"