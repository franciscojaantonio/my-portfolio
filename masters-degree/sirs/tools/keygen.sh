#!/bin/bash

# Run this script to generate a KeyPair for some user/organization and store both keys in the 'keys_dir' directory

# Check for correct usage
if [ $# -ne 1 ]; then
    echo "Usage: $0 <name>"
    exit
fi

# Keys directory
keys_dir="../auth/keys"

# Create keys directory if it doesn't exist
if [ ! -d "$keys_dir" ]; then
    mkdir $keys_dir
fi

# Run java 'AsymmetricKeys' to generate the KeyPair
java_file="../src/pt/tecnico/AsymmetricKeys"
class_name="pt.tecnico.AsymmetricKeys"

javac -d . $java_file.java

java -cp . $class_name $keys_dir/${1}_public.key $keys_dir/${1}_private.key

# Convert keys to PEM format
./convert_keys.sh $1 "PEM"

echo "Generated keypair for '$1'!"

# Remove compiled files
rm -rf pt