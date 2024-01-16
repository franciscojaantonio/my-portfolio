#!/bin/bash

# Run this script to generate a Certificate Signing Request (CSR) for some user/organization

# Check for correct usage
if [ $# -ne 1 ]; then
    echo "Usage: $0 <name>"
    exit
fi

# Keys directory
keys_dir="../auth/keys"

# Check if key file exists
if [ ! -f "$keys_dir/${1}_private.pem" ]; then
    echo "Error: Key file '$keys_dir/${1}_private.pem' does not exist!"
    exit
fi

# Create the Certificate Signing Request (CSR)
openssl req -new -key $keys_dir/${1}_private.pem -out $keys_dir/${1%.*}.csr

echo "Certificate Signing Request (CSR) for '$1' created!"