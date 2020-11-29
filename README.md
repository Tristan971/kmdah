# kmdah

A distributed mangadex@home (MDAH) client implementation

## Architecture and prerequisites

![Architecture](docs/architecture.svg)

kmdah is a distributed-first implementation which ensures that instances can always start, stop or lose connectivity.

To achieve this, we ensure they all have a uniquely identifying name. Then they use Redis both for cross-instance
leadership elections, discovery and synchronisation.

### Discovery, leadership and synchronisation

On start-up each instance broadcasts its identifier through redis PubSub, and keeps doing so for its whole lifetime.
All instances listen for such events and keep a local registry of known peers. This ensures that each instance is 
always ready to pick up leadership if it ever needs to.

On the topic of leadership, elections happen constantly, using Redis. It relies on Spring Integration's
RedisLockRegistry, and thus effectively is an expiring lock that is acquired on a first-come first-served
basis. Because it is expirable, a "dead" leader that didn't release it properly will not cause the cluster
to deadlock.
