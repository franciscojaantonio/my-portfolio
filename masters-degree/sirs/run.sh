#!/bin/bash

mvn install
mvn compile

# Get the current working directory
current_dir=$PWD

# Set the relative path to appassembler/bin
relative_path="/target/appassembler/bin"

# Create the full path by combining the current directory and the relative path
full_path="$current_dir$relative_path"
export PATH=$PATH:$full_path
