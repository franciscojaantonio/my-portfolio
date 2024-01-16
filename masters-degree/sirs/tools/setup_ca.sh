#!/bin/bash

# Run this script to setup a Certificate Authority (CA) in this machine

# Check for correct usage
if [ $# -ne 1 ]; then
    echo "Usage: $0 <ca_name>"
    exit
fi

# Check if 'openssl' is installed
if ! [ -x "$(command -v openssl)" ]; then
    echo "Error: 'openssl' is not installed!" >&2
    exit
fi

# Keys directorys
keys_dir="../auth/keys"

# Generate CA KeyPair
./keygen.sh $1

# Create a Certificate Signing Request (CSR) for the CA
./create_csr.sh $1

# Self-sign the CSR
./sign_csr.sh $1 $1

# TODO: Distribute the CA certificate to all machines

echo "Certificate Authority (CA) setup complete!"