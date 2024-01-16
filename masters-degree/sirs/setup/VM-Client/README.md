# Virtual Machine Setup: Client

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
        - 192.168.1.1/24
      routes:
        - to: 192.168.2.0/24
          via: 192.168.1.2
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]
```

### Network adapters

- Adapter 1: Internal network

  - Name: bombappetit.internet

## Installation steps

### Frontend

```sh
./init-vm-client.sh
```

> At this point, the network adapters must be configured

### Network

```sh
./setup_network.sh
```
