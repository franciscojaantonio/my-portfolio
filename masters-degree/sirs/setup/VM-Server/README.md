# Virtual Machine Setup: Server

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
        - 192.168.2.1/24
      routes:
        - to: 192.168.1.0/24
          via: 192.168.2.2
        - to: 192.168.3.0/24
          via: 192.168.2.3
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]
```

### Network adapters

- Adapter 1: Internal network

  - Name: bombappetit.dmz

## Installation steps

### Server

```sh
./init-vm-appserver.sh
```

> At this point, the network adapters must be configured

### Network

```sh
./setup_network.sh
```
