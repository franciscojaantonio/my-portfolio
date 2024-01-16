# Virtual Machine Setup: serverToDatabase Gateway

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
        - 192.168.2.3/24
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]
    enp0s8:
      addresses:
        - 192.168.3.2/24
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]
```

### Network adapters

- Adapter 1: Internal network

  - Name: bombappetit.dmz

- Adapter 2: Internal network

  - Name: bombappetit.intra

## Installation steps

> At this point, the network adapters must be configured

### Network

```sh
./setup_network.sh
```
