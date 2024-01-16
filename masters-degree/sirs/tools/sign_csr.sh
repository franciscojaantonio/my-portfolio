#!/usr/bin/env bash

# Run this script to sign a Certificate Signing Request (CSR) with the Certificate Authority (CA)

# Check for correct usage
if [ $# -ne 2 ]; then
    echo "Usage: $0 <user_name> <ca_name>"
    exit
fi

# Check if 'openssl' is installed
if ! [ -x "$(command -v openssl)" ]; then
    echo "Error: 'openssl' is not installed!" >&2
    exit
fi

# Keys directory
keys_dir="../auth/keys"
certificates_dir="../auth/certificates"

# Check if certificate directory exists
if [ ! -d "$certificates_dir" ]; then
    mkdir $certificates_dir
fi

# Check if CSR file exists
if [ ! -f "$keys_dir/${1}.csr" ]; then
    echo "Error: CSR file '$keys_dir/$1' does not exist!"
    exit
fi

# Check if .ext file should be used
read -p "Do you want to use the .ext file to add Subject Alternative Names (SANs) to the certificate? [y/n] " answer

if [ "$answer" == "y" ]; then
    # Check if CA is trying to self-sign or if it already has a certificate and is trying to sign another CSR
    if [ "$1" == "$2" ]; then
        # Self-sign the CSR
        openssl x509 -req -days 365 -in $keys_dir/${1%.*}.csr -signkey $keys_dir/${1%.*}_private.pem -out $certificates_dir/${1%.*}.crt -extfile ./configs/ca.ext

    elif [ -f "$certificates_dir/${2%.*}.crt" ]; then
        # Sign the CSR with the CA
        openssl x509 -req -days 365 -in $keys_dir/${1}.csr -CA $certificates_dir/${2%.*}.crt -CAkey $keys_dir/${2%.*}_private.pem -out $certificates_dir/${1%.*}.crt -extfile ./configs/non_ca.ext

    else
        echo "Error: CA '$2' does not have a certificate!"
        exit
    fi

elif [ "$answer" == "n" ]; then
    # Check if CA is trying to self-sign or if it already has a certificate and is trying to sign another CSR
    if [ "$1" == "$2" ]; then
        # Self-sign the CSR
        openssl x509 -req -days 365 -in $keys_dir/${1%.*}.csr -signkey $keys_dir/${1%.*}_private.pem -out $certificates_dir/${1%.*}.crt

    elif [ -f "$certificates_dir/${2%.*}.crt" ]; then
        # Sign the CSR with the CA
        openssl x509 -req -days 365 -in $keys_dir/${1}.csr -CA $certificates_dir/${2%.*}.crt -CAkey $keys_dir/${2%.*}_private.pem -out $certificates_dir/${1%.*}.crt

    else
        echo "Error: CA '$2' does not have a certificate!"
        exit
    fi
fi

# Delete the CSR after generating the certificate (if it exists)
if [ -e "$keys_dir/${1}.csr" ]; then
    rm "$keys_dir/${1}.csr"
fi

echo "Certificate Signing Request (CSR) '$1' signed with Certificate Authority (CA) '$2'!"
