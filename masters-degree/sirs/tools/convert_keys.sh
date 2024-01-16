#!/bin/bash

# Run this script to convert binary keys to PEM format or PEM keys to binary format

# Check for correct usage
if [ $# -lt 2 ] || [ $# -gt 3 ]; then
    echo "Usage:"
    echo "$0 <user_name> PEM"
    echo "  -> To convert from DER to PEM"
    echo "$0 <user_name> DER"
    echo "  -> To convert from PEM to DER"
    exit
fi

# Check for 3rd argument
if [ "$3" != "" ]; then
    dir_keys="$3"
else
    dir_keys="../auth/keys" 
fi

# Check conversion type
if [ "$2" != "PEM" ] && [ "$2" != "DER" ]; then
    echo "Error: Conversion type must be either 'PEM' or 'DER'!"
    exit
fi

# If mode is PEM
if [ "$2" == "PEM" ]; then
    # Input and output file paths
    private_input_file="${dir_keys}/${1}_private.key"
    private_output_file="${dir_keys}/${1}_private.pem"

    public_input_file="${dir_keys}/${1}_public.key"
    public_output_file="${dir_keys}/${1}_public.pem"

    # Convert binary private key to PEM format

    # Check if input files exist
    if [ -f "$private_input_file" ]; then
        openssl rsa -inform DER -outform PEM -in $private_input_file -out $private_output_file
    fi

    if [ -f "$public_input_file" ]; then
        openssl rsa -inform DER -outform PEM -pubin -in $public_input_file -out $public_output_file
    fi

elif [ "$2" == "DER" ]; then
    # Input and output file paths
    private_input_file="${dir_keys}/${1}_private.pem"
    private_output_file="${dir_keys}/${1}_private.key"

    public_input_file="${dir_keys}/${1}_public.pem"
    public_output_file="${dir_keys}/${1}_public.key"

    # Convert PEM private key to binary format

    # Check if input files exist
    if [ -f "$private_input_file" ]; then
        openssl rsa -inform PEM -in $private_input_file -outform DER -out $private_output_file
    fi

    if [ -f "$public_input_file" ]; then
        openssl rsa -pubin -inform PEM -in $public_input_file -outform DER -out $public_output_file
    fi
    
fi

# Check if the keys were converted
if [ $? -ne 0 ]; then
    echo "Error: Keys could not be converted!"
    exit
fi

echo "Keys converted!"
