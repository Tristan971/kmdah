# kmdah

A distributed mangadex@home (MDAH) client implementation

## Design

An operator node serves as heartbeat for the mangadex API to keep the client registered, and dynamically update the client settings of the distributed client.

This operator node can then be used as a soft load-balancer for the worker nodes to register against.

This is achieved either through a load-balancer (once the MDAH backend supports host ip overriding), or by issuing temporary redirect responses to incoming
requests, effectively acting as a "soft" load-balancer.

---

Rest of the documentation is tbd until done.
