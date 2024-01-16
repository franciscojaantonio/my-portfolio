# Virtual Machine Setup: Database

## Network configuration

### Netplan file

```yaml
# Let NetworkManager manage all devices on this system
network:
  version: 2
  renderer: NetworkManager
  ethernets:
    enp0s3:
      addresses:
        - 192.168.3.1/24
      routes:
        - to: 192.168.2.0/24
          via: 192.168.3.2
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]
```

### Network adapters

- Adapter 1: Internal network

  - Name: bombappetit.intra

## Installation steps

### MongoDB

```sh
./setup_dependencies.sh
```

> At this point, the network adapters must be configured

### Network

```sh
./setup_network.sh
```

## Run MongoDB

```sh
./start.sh
```
