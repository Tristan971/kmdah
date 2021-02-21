# kmdah

A distributed-first mangadex@home (MD@H) client implementation.

## Architecture overview

To run multiple instances of a single client, we need a few things:

- A consistent heartbeat to the MD@H backend to avoid compromission/inconsistent queries: Redis locks and Redis PubSub
- A way to handle SSL termination at the cluster level: see [SSL termination](#ssl-termination)
- A storage backend supporting multiple RW consumers: see [Storage backends](#storage-backends)

This is an example of configuration result:

![Architecture](docs/architecture.svg)

## Getting started

While you probably should read this document to get on, you can also jump straight into the [examples](docs/examples) if you prefer.

The configuration can be done either via environment variables or with a yaml configuration file (from a ConfigMap for example).

The order of precedence is as follows (the lower down the list, the higher priority for overriding):

- Default value
- Environment variable
- Explicit yaml configuration key

The default values are in [application.yml](src/main/resources/application.yml) under `kmdah`.

## Redis

First, we need to install and start [Redis](https://redis.io/) somewhere. It could be in the cluster, or outside it. The latter is probably preferable.

As we do not store any actual data in it, a few megabytes of RAM and a very small bit of CPU is enough for our purposes.

You can of course use Sentinel to achieve high-availability of Redis itself, but it isn't usually necessary as short downtimes (<2 minutes) are acceptable. If
you do however, make sure that your configuration supports distributed locks during normal operation (loss of the lock during failover is acceptable)

### Configuration

In the configuration, all of these keys are under

```yaml
kmdah.gossip:
```

---

```yaml
    id-generation-strategy: ${KMDAH_GOSSIP_ID_GENERATION_METHOD:random_uuid}
```

Each instance must have a unique identity.

You can use any of the [identity generation strategies](src/main/java/moe/tristan/kmdah/service/gossip/InstanceId.java):

- `random_uuid` to generate one on startup
- `hostname` to use the container's hostname

---

```yaml
    redis:
      host: ${KMDAH_GOSSIP_REDIS_HOST:localhost}
      port: ${KMDAH_GOSSIP_REDIS_PORT:6379}
      gossip-topic: ${KMDAH_GOSSIP_REDIS_TOPIC:kmdah}
      lock-registry-key: ${KMDAH_GOSSIP_REDIS_LOCK_REGISTRY_KEY:kmdah-leadership}
```

All of these properties **must** be the same across all instances.

As implied by the name, `host` and `port` define where the instances should find the shared redis to connect to.

The `gossip-topic` is the [Redis PubSub topic/channel](https://redis.io/topics/pubsub) that instances will use for communications between each other. The
default is fine to keep.

The `lock-registry-key` is the name of the [Redis DistLock](https://redis.io/topics/distlock) that will serve for elections of the cluster leader instance. That
is, the one that will actually communicate with the MangaDex backend.

---

If your redis has authentication enabled, you can add the following at the root of your configuration file (so not under `kmdah.gossip`):

```yaml
spring.redis:
  username: myuser
  password: some-password
```

---

If you are using [Redis Sentinel](https://redis.io/topics/sentinel), add:
```yaml
spring.redis:
  sentinel:
    master: master-name
    nodes:
      - 1.2.3.4:6379
      - 3.4.5.6:6379
      - ...
    password: my-sentinel-password 
```

Note: Using Redis Sentinel hasn't been well-tested and may not work correctly. It should be fine, but... y'know.

## SSL Termination

Relevant configuration snippet:

```yaml
kmdah:
  ...

  tls:
    backend: k8s
    k8s:
      secret:
        auto-update: true
        namespace: kmdah
        name: mangadex-at-home-tls-secret
```

On start, the client receives its SSL certificate from the backend.

There following options are there to configure its usage.

### a. Kubernetes Ingress controller

Assuming a well-configured Kubernetes cluster, you should have an ingress controller already installed.

When the leader of the cluster receives the certificate, it will try to update the
secret [`<namespace>/<secret name>`](docs/examples/kubernetes/tls-secret.yaml) accordingly.

If you configured your [ingress](docs/examples/kubernetes/ingress.yml) to use it for TLS, you should now have the correct certificate.

### b. Certificate and private key file output

If you prefer to handle setting these certificates up manually for some reason (if you are using nginx as a reverse proxy outside k8s for example).

## Storage backends
