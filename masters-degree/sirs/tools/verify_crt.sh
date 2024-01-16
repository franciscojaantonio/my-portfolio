#!/bin/bash

# Run this script to verify the certificate chain of the server

# Check for correct usage
if [ $# -ne 2 ]; then
    echo "Usage: $0 <user_name> <ca_name>"
    exit
fi

certificates_dir="../auth/certificates"

# Check if both certificates exist
if [ ! -f "$certificates_dir/${1%.*}.crt" ]; then
    echo "Error: Certificate '$certificates_dir/${1%.*}.crt' does not exist!"
    exit
fi

if [ ! -f "$certificates_dir/${2%.*}.crt" ]; then
    echo "Error: Certificate '$certificates_dir/${2%.*}.crt' does not exist!"
    exit
fi

# Verify the certificate chain
openssl verify -CAfile $certificates_dir/${2%.*}.crt $certificates_dir/${1%.*}.crt

# Check if the certificate was verified
if [ $? -ne 0 ]; then
    echo "Certificate issued by Certificate Authority (CA) '$2' could not be verified!"
    exit
fi

echo "Successfully verified!"