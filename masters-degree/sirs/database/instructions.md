# MongoDB

## Install

### 1 - Import the public key used by the package management system

```sh
sudo apt-get install gnupg curl
```

```sh
curl -fsSL https://pgp.mongodb.com/server-7.0.asc | \
   sudo gpg -o /usr/share/keyrings/mongodb-server-7.0.gpg \
   --dearmor
```

### 2 - Create a list file for MongoDB

```sh
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
```

### 3 - Reload local package database

```sh
sudo apt-get update
```

### 4 - Install the MongoDB packages

```sh
sudo apt-get install -y mongodb-org
```

```sh
echo "mongodb-org hold" | sudo dpkg --set-selections
echo "mongodb-org-database hold" | sudo dpkg --set-selections
echo "mongodb-org-server hold" | sudo dpkg --set-selections
echo "mongodb-mongosh hold" | sudo dpkg --set-selections
echo "mongodb-org-mongos hold" | sudo dpkg --set-selections
echo "mongodb-org-tools hold" | sudo dpkg --set-selections
```

## Run

### 1 - Start MongoDB

```sh
sudo systemctl start mongod
```

### 2 - Verify that MongoDB has started successfully

```sh
sudo systemctl status mongod
```

### 3 - Stop MongoDB

```sh
sudo systemctl stop mongod
```

### 4 - Restart MongoDB

```sh
sudo systemctl restart mongod
```

### 5 - Begin using MongoDB

```sh
mongosh
```

> You can follow the state of the process for errors or important messages by watching the output in the **/var/log/mongodb/mongod.log** file.

## Setup for the project

### 1 - Open Mongo Shell

```sh
mongosh
```

### 2 - Create Database

```sh
use bombAppetit
```

### 3 - Create Collections

```sh
db.createCollection("restaurantInfo")
```

```sh
db.createCollection("review")
```

```sh
db.createCollection("user")
```

```sh
db.createCollection("voucher")
```
