#!/bin/bash

# Run this script to extract the public key from a certificate

# Check for correct usage
if [ $# -ne 2 ]; then
    echo "Usage: $0 <user_name> <ca_name>"
    exit
fi

certificates_dir="../auth/certificates"
keys_from_certificates_dir="../auth/certificates/trusted_keys"

# Check if the certificate exists
if [ ! -f "$certificates_dir/${1%.*}.crt" ]; then
    echo "Error: Certificate '$certificates_dir/${1%.*}.crt' does not exist!"
    exit
fi

# Verify the certificate
status=$(./verify_crt.sh $1 $2)

# Check if the certificate was verified
if [[ $status != *"Successfully verified!"* ]]; then
    echo "Certificate '$certificates_dir/${1%.*}.crt' could not be verified!"
    exit
fi

# Create the keys_from_certificates_dir directory if it does not exist
if [ ! -d "$keys_from_certificates_dir" ]; then
    mkdir $keys_from_certificates_dir
fi

# Extract the public key from the certificate
openssl x509 -pubkey -noout -in $certificates_dir/${1%.*}.crt > $keys_from_certificates_dir/${1%.*}_public.pem

# Check if the public key was extracted
if [ $? -ne 0 ]; then
    echo "Public key could not be extracted from certificate '$certificates_dir/${1%.*}.crt'!"
    exit
fi

# Write the public key in DER format
./convert_keys.sh $1 "DER" $keys_from_certificates_dir

# Check if the public key was converted
if [ $? -ne 0 ]; then
    echo "Public key could not be converted from PEM to DER format!"
    exit
fi

echo "Public key extracted from certificate '$certificates_dir/${1%.*}.crt'!"
