## Token Exchange System

### About
This project was made for the subject of *High Dependability Systems*. It is part of the Major in Cybersecurity of the Master in Computer Science and Engineering @IST.
In this project me and my group implemented a simplified version (no round changes) of the Instanbul-BFT Consensus Algorithm Protocol, which is known in the blockchain community as QBFT.

1. The Token Exchange System holds a set of accounts.
2. The servers are responsible for maintaining the set of TES accounts.
3. A client of the system can perform transfers between a pair of accounts provided it owns the corresponding private key of the crediting account.
4. System supports up to (N-1)/3 byzantine servers, N being total number of servers.

### How to run (only tested on linux)
1. *./run.sh*
2. Insert number of servers
3. Choose desired behavior
