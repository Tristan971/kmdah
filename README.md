# kmdah

A distributed-first mangadex@home (MD@H) client implementation.

## Architecture overview

To run multiple instances of a single client, we need to:

1. Regularly ping the MD@H backend without causing compromission or sending inconsistent requests

   For this, we use gossipping and distributed elections via [Redis](#redis)

2. Support multiple read-write consumers at the storage level

   For this, we have an array of persistent [storage](#storage) options available

3. Handle SSL termination at the cluster level

   For this, we use a [dynamically updated](#ssl-termination) Ingress Certificate Secret

![Architecture](docs/architecture.svg)

## Introduction

Additionally, this document, and kmdah in general, assume prior knowledge of Kubernetes. If that is not the case, feel free to ask for help in the
`#support-md-at-home` channel.

You can also jump straight into the [examples](docs/examples) and only refer to this document passively if that is your preference.

## Getting started

The configuration can be done either via environment variables or with a yaml configuration file (from a ConfigMap for example).

The order of precedence is as follows (the lower down the list, the higher priority for overriding):

- Default value
- Environment variable
- Explicit yaml configuration key

The default values are in [application.yml](src/main/resources/application.yml#L77) under `kmdah`.

## Redis

First, we need to install and start [Redis](https://redis.io/) somewhere. It could be in the cluster, or outside it. The latter is probably preferable.

As we do not store any actual data in it, a few megabytes of RAM and a very small bit of CPU is enough for our purposes.

Redis Sentinel is currently not officially supported, mostly because it hasn't been tested.

### Relevant default configuration

```yaml
kmdah:
  gossip:
    id-generation-strategy: ${KMDAH_GOSSIP_ID_GENERATION_METHOD:random_uuid}
    redis:
      host: ${KMDAH_GOSSIP_REDIS_HOST:localhost}
      port: ${KMDAH_GOSSIP_REDIS_PORT:6379}
      gossip-topic: ${KMDAH_GOSSIP_REDIS_TOPIC:kmdah}
      lock-registry-key: ${KMDAH_GOSSIP_REDIS_LOCK_REGISTRY_KEY:kmdah-leadership}
```

---

If your redis has authentication enabled, you can add the following **at the root** of your configuration file:

```yaml
spring:
  redis:
    username: myuser
    password: some-password
```

#### `kmdah.gossip.id-generation-strategy`

Each instance must have a unique identity.

You can use any of the [identity generation strategies](src/main/java/moe/tristan/kmdah/service/gossip/InstanceId.java):

- `random_uuid` to generate one on startup
- `hostname` to use the container's hostname

#### `kmdah.gossip.redis.{host, port, gossip-topic, lock-registry-key}`

The `host` and `port` define where the instances should find the shared redis to connect to.

The `gossip-topic` is the [Redis PubSub topic/channel](https://redis.io/topics/pubsub) that instances will use for communications between each other.

The `lock-registry-key` is the name of the [Redis DistLock](https://redis.io/topics/distlock) that will serve for elections of the cluster leader instance.

## Storage

To save cost and use storage optimally, it is ideal to share storage amongst instances. While it is technically possible to have completely different storage
pools for each instance, that incurs a most likely unreasonable cost.

Thankfully, kmdah is designed to avoid needing this.

There are many options available. Some that I have experimented with are listed hereafter along with the performance observed and some notes.

Reported performance characteristics, unless indicated otherwise, are always:

- in a LAN environment with 10Gbps full-duplex connectivity between kmdah instances and storage servers
- with real live traffic, and thus without much benefit from the Linux kernel's file caching

### NFSv4

**Backend type:** `filesystem`

The first solution is the ubiquitous standard for shared filesystems, NFS.

#### Pros

- Simple to set up

#### Cons

- Poor support in Kubernetes
- Poor performance
- High CPU usage

#### Performance

Unfortunately, NFS has consistently poor performance along with very high CPU consumption for the client. At 200 req/s (~1Gbps):

- 24% of CPU time was lost to the NFS client,
- latency regularly spiked to multiple 100s of milliseconds.

In testing, it became clear that NFS is not suitable above the 150rqps range.

**Recommended:** No, unless you expect less than 100 req/s.

#### Example Kubernetes manifest

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: kmdah-pv
  namespace: kmdah
spec:
  capacity:
    storage: 2Ti
  accessModes:
    - ReadWriteMany
    - ReadWriteOnce
    - ReadOnlyMany
  nfs:
    server: 192.168.2.3
    path: "/nfs/cache"
  mountOptions:
    - noatime       # strongly recommended
    - nodiratime    # strongly recommended
    - async         # recommended
    - noacl         # recommended
    - nocto         # recommended
    - rsize=32768   # optional, if present it must match the server side
    - wsize=32768   # optional, if present it must match the server side
```

### CephFS

**Backend type:** `filesystem`

[Ceph](https://ceph.io/) is an infinitely scalable solution for distributed storage. CephFS is able to sustain concurrent reads and writes by multiple clients
to a single filesystem.

#### Pros

- Can be incredibly fast
- Strong concurrency guarantees
- You're literally using the same storage tech as CERN

#### Cons

- Requires a pre-existing Ceph cluster, which is either costly or at least more involved to set up
- Requires MTU 9000 for best performance

#### Performance

CephFS has shown it is capable of handling at least 600 req/s without noticeable CPU overhead nor latency spikes.

We have not yet tested it for MDAH yet above these speeds at the time of writing,
but [institutional users have pushed it to multiple 100s of Gbps and millions of iops](https://www.vi4io.org/io500/list/20-11/10node).

**Recommended: Yes, if you want to achieve extremely high performance.**

## SSL Termination

On start, the client receives an SSL certificate and domain on which to listen from the backend.

It then needs a way to configure the cluster with it.

To be more precise, the domain is made of 3 parts:

- The current temporary client-specific subdomain
- A never-changing MangaDex account subdomain
- `.mangadex.network` as TLD

Your url is thus in the form: `<random>.<account specific>.mangadex.network`

The certificate is a wildcard certificate for the subject `*.<MangaDex user account specific>.mangadex.network`

### Relevant default configuration

```yaml
kmdah:
  tls:
    backend: ${KMDAH_TLS_BACKEND:unset}
    file:
      certificate-output-file: ${KMDAH_TLS_FILE_CERTIFICATE_OUTPUT_FILE:tls.crt}
      private-key-output-file: ${KMDAH_TLS_FILE_PRIVATE_KEY_OUTPUT_FILE:tls.key}
    k8s:
      secret:
        auto-update: ${KMDAH_TLS_K8S_SECRET_AUTO_UPDATE:false}
        name: ${KMDAH_TLS_K8S_SECRET_NAME:secret-name}
        namespace: ${KMDAH_TLS_K8S_SECRET_NAMESPACE:default}
```

#### `kmdah.tls.backend`

There are 2 ["TLS Backends"](src/main/java/moe/tristan/kmdah/service/tls/TlsBackend.java) available to configure SSL:

1. The [`k8s` backend](#k8s-backend-configuration)

   When the leader of the cluster receives the certificate, it updates a Certificate's Secret resource, which is picked up by your Ingress controller:

   a. Create a [ServiceAccount](docs/examples/kubernetes/serviceaccount.yml).

   b. Grant this ServiceAccount the [RBAC permissions](docs/examples/kubernetes/rbac.yml) to `UPDATE` (and optionally `CREATE`)
   the Secret resource. This might require editing the RBAC or pre-creating the Secret with
   the [sample fake cert resource](docs/examples/kubernetes/initial-tls-secret.yaml)
   depending on your cluster's RBAC configuration.

   c. Then, ensure your [Ingress](docs/examples/kubernetes/ingress.yml) is configured to use that secret for TLS.

2. The [`file` backend](#file-backend-configuration)

   If you prefer to handle setting these certificates manually for some reason (if you are using nginx as a reverse proxy outside k8s for example), this backend
   simply outputs the content of the certificate and private key in a directory.

   Note: **only the current leader outputs the certificate** files, **not** all instances.

##### `k8s` backend configuration

```yaml
kmdah:
  tls:
    backend: k8s
    k8s:
      secret:
        auto-update: ${KMDAH_TLS_K8S_SECRET_AUTO_UPDATE:false}
        namespace: ${KMDAH_TLS_K8S_SECRET_NAMESPACE:default}
        name: ${KMDAH_TLS_K8S_SECRET_NAME:secret-name}
```

To avoid mistakenly breaking your production configuration, `auto-update`, the automatic update of the certificate, is set to `false` by default.

This effectively means that this backend is completely disabled, and you must set it to `true` when you are ready to go to production.

Then, `namespace` and `name` are essentially a reference to the secret. Note that the secret **must** be in the same namespace as the Ingress that uses it,
otherwise the Ingress cannot access it.

With the sample manifests, you will want the `kmdah` namespace and `mangadex-at-home-tls-secret` name.

##### `file` backend configuration

```yaml
kmdah:
  tls:
    backend: file
    file:
      certificate-output-file: ${KMDAH_TLS_FILE_CERTIFICATE_OUTPUT_FILE:tls.crt}
      private-key-output-file: ${KMDAH_TLS_FILE_PRIVATE_KEY_OUTPUT_FILE:tls.key}
```

You may use either a relative (to the process' working directory) or absolute path.

Notes:

- Intermediate directories are **not** automatically created
- The ownership and permissions will be those of the Java process

