# Dynamic Config SDK

A lightweight, framework-independent Java configuration SDK for loading, merging, caching, and dynamically refreshing application configuration from multiple sources.

The SDK provides a common configuration API across local files, application defaults, and Kubernetes ConfigMaps, with optional Spring Framework integration for `@DynamicValue` field injection and runtime updates.

---

## Modules

| Module | Artifact | Description |
|---|---|---|
| `dynamic-config-core` | `dynamic-config-core` | Framework-independent configuration engine |
| `dynamic-config-kubernetes` | `dynamic-config-kubernetes` | Kubernetes ConfigMap file + watch integration |
| `dynamic-config-spring` | `dynamic-config-spring` | Spring `@DynamicValue`, XML, lifecycle integration |
| `dynamic-config-demo` | *(not published)* | Runnable core + Spring examples (repo only) |

**Deferred (not in v1.0):** ConfigHub — planned for a future release.

Spring Boot is **not** required for Spring integration.

---

## Requirements

* Java 17+
* Maven 3.9+
* Spring Framework 6.x (Spring module only)
* Kubernetes 1.x + in-cluster config or kubeconfig (Kubernetes module only)

Set `JAVA_HOME` to JDK 17 for local builds:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

---

## Installation

### Core

```xml
<dependency>
    <groupId>io.github.chaoticsync</groupId>
    <artifactId>dynamic-config-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Kubernetes

```xml
<dependency>
    <groupId>io.github.chaoticsync</groupId>
    <artifactId>dynamic-config-kubernetes</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Spring

```xml
<dependency>
    <groupId>io.github.chaoticsync</groupId>
    <artifactId>dynamic-config-spring</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

`dynamic-config-spring` depends on `dynamic-config-core`. Add `dynamic-config-kubernetes` separately when using ConfigMap sources.

---

## Demo

The `dynamic-config-demo` module is a small runnable project in this repository. It is **not published** to Maven Central — use it to explore the SDK locally.

**Core demo** — file sources, precedence, typed getters, watchers, and `refresh()`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn -pl dynamic-config-demo -am exec:java
```

**Kubernetes file demo** — mounted ConfigMap file at `HIGHEST` priority (no cluster):

```bash
mvn -pl dynamic-config-demo -am exec:java -Dexec.mainClass=io.dynamicconfig.demo.KubernetesDemo
```

**Polling demo** — `refreshInterval` picks up on-disk file changes automatically:

```bash
mvn -pl dynamic-config-demo -am exec:java -Dexec.mainClass=io.dynamicconfig.demo.PollingDemo
```

**Spring demo (`@DynamicValue`)** — field injection and runtime updates for all supported types:

```bash
mvn -pl dynamic-config-demo -am exec:java -Dexec.mainClass=io.dynamicconfig.demo.spring.SpringDemo
```

Annotated bean: `dynamic-config-demo/src/main/java/io/dynamicconfig/demo/spring/AppSettings.java`

```java
@Component
public class AppSettings {

    @DynamicValue("server.port")
    private int serverPort;

    @DynamicValue("server.host")
    private String serverHost;
    // long, boolean, double, float supported too — see AppSettings
}
```

See `dynamic-config-demo/src/main/java/io/dynamicconfig/demo/` for all entry points.

---

## Architecture

```text
                         Application
                              |
                              v
                     +----------------+
                     |  ConfigClient  |
                     +----------------+
                              |
                              v
                  +-----------------------+
                  | DefaultConfigClient   |
                  +-----------------------+
                     |       |        |
                     v       v        v
                  Cache   Merger   Watchers
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
        File Sources    Defaults    Kubernetes
             |              |         (optional)
      +------+------+       |              |
   YAML   JSON  Properties  |      File / Watch
```

The core module does **not** depend on Spring or Kubernetes. Remote integrations live in sibling Maven modules under `io.dynamicconfig.kubernetes`.

---

## Client lifecycle

Configuration loading is split into three explicit phases:

| Phase | Method | Responsibility |
|---|---|---|
| Initial load | `refresh()` | Load all sources, merge by precedence, replace cache, notify watchers |
| Background start | `startBackgroundLifecycle()` | Start lifecycle sources → refresh once → start polling/watch |
| Shutdown | `shutdown()` | Stop refresh strategy and lifecycle sources (idempotent) |

**Non-Spring convenience** (loads config and starts background processing in one call):

```java
client.start(); // equivalent to refresh() then startBackgroundLifecycle()
```

**Spring** uses the split lifecycle — never call `start()`:

```text
FactoryBean.refresh()  →  @DynamicValue injection  →  SmartLifecycle.startBackgroundLifecycle()
```

`startBackgroundLifecycle()` ordering inside the client:

1. Start all `LifecycleConfigSource` instances (e.g. Kubernetes watch)
2. Call `refresh()` once to merge lifecycle source state
3. Start `RefreshStrategy` (scheduled polling) last

---

## Basic usage

```java
ConfigClient client = ConfigClient.builder()
        .defaults(Map.of(
                "server.port", "8080",
                "server.host", "localhost"
        ))
        .properties("application.properties")
        .yaml("application.yaml")
        .refreshInterval(Duration.ofSeconds(30))
        .build();

client.start();

String host = client.get("database.host");
Integer port = client.getInt("server.port");
Boolean enabled = client.getBoolean("feature.enabled");
```

Register a watcher:

```java
client.watch("server.port", (key, oldValue, newValue) -> {
    // handle change after refresh()
});
```

Explicit refresh:

```java
client.refresh();
```

Shutdown:

```java
client.shutdown();
```

---

## Configuration files

Supported formats: `.properties`, `.yaml` / `.yml`, `.json`

All formats are flattened into dot-separated keys:

```text
server.port=8080
server.host=localhost
database.host=mysql
```

Nested YAML/JSON:

```yaml
application:
  server:
    http:
      port: 8080
```

becomes `application.server.http.port=8080`.

Lists use indexed keys:

```yaml
servers:
  - api-1
  - api-2
```

becomes `servers[0]=api-1` and `servers[1]=api-2`.

---

## Configuration sources

Every source implements:

```java
public interface ConfigSource {
    String getName();
    Map<String, String> load();
}
```

Register custom sources with explicit priority:

```java
ConfigClient client = ConfigClient.builder()
        .addSource(new MyConfigSource(), SourcePriority.NORMAL)
        .build();
```

Lifecycle sources (e.g. Kubernetes watch) also implement:

```java
public interface LifecycleConfigSource extends ConfigSource {
    void start(RefreshTrigger refreshTrigger);
    void stop();
}
```

---

## Source precedence

Sources are merged from **low → high** priority. Higher priority overrides lower. At the **same** priority, **later registration wins**.

| Registration | Default priority |
|---|---|
| `defaults(...)` | `LOWEST` |
| `properties(...)` | `LOW` |
| `yaml(...)` / `json(...)` | `NORMAL` |
| Custom `addSource(..., priority)` | caller-defined |
| Kubernetes via `KubernetesConfigExtensions` | `HIGHEST` |

Example — effective value when all sources define `server.port`:

```text
defaults:           server.port=8000
properties:         server.port=8080
yaml:               server.port=9000
kubernetes:         server.port=10000

effective:          server.port=10000
```

Priority is stored on `SourceRegistration`, not on `ConfigSource` itself.

### Source failure behavior

When a source throws during `load()`, it contributes **nothing** for that refresh cycle. Keys it previously supplied alone may disappear unless a lower-priority source still provides them. Failures are logged; other sources continue loading.

---

## Typed configuration

```java
client.get("server.host");
client.getInt("server.port");
client.getLong("database.timeout");
client.getBoolean("feature.enabled");
client.getDouble("service.threshold");
```

All typed getters support default-value overloads:

```java
client.getInt("server.port", 8080);
```

Missing keys return `null`. Invalid conversions throw `ConfigConversionException`.

---

## Dynamic watching

Watchers receive notifications after `refresh()` when values change:

| Change | Notification |
|---|---|
| Added | `null → value` |
| Modified | `old → new` |
| Removed | `value → null` |

Watchers are invoked outside internal locks. One failing watcher does not block others. Watcher callbacks should not synchronously re-enter `refresh()` on the same client.

---

## Automatic refresh

Optional polling refresh:

```java
ConfigClient client = ConfigClient.builder()
        .yaml("application.yaml")
        .refreshInterval(Duration.ofSeconds(30))
        .build();
```

Scheduled refreshes do not overlap (guarded by a reentrant lock). Refresh failures are logged without stopping future scheduled refreshes.

---

## MutableConfigSource

Useful for tests and programmatic updates:

```java
MutableConfigSource source = new MutableConfigSource("test");
source.set("server.port", "8080");

ConfigClient client = ConfigClient.builder()
        .addSource(source)
        .build();

client.refresh();

source.set("server.port", "9090");
client.refresh();
```

`load()` returns a defensive copy on every call.

---

## Kubernetes integration

Kubernetes classes live in `io.dynamicconfig.kubernetes` — not in core.

Use `KubernetesConfigExtensions` to register sources at `HIGHEST` priority:

```java
ConfigClient client = KubernetesConfigExtensions
        .extend(ConfigClient.builder())
        .yaml("application.yaml")
        .configMapFile("/etc/config/application.yaml")
        .configMapWatch("default", "my-app-config")
        .refreshInterval(Duration.ofSeconds(30))
        .build();

client.start();
```

### ConfigMapFileSource (v1)

Reads a **single mounted file** (e.g. `/etc/config/application.yaml`). Detects format by extension and delegates to core file parsers. No Kubernetes API required.

### ConfigMapWatchSource

Implements `LifecycleConfigSource`:

* Performs synchronous ConfigMap GET on `start()`
* Watches ADDED / MODIFIED / DELETED events
* Tracks `resourceVersion`, reconnects on watch termination
* Updates internal state first, then triggers `refreshTrigger.refresh()`
* `load()` returns an unmodifiable snapshot

---

## Spring integration

### `@DynamicValue`

```java
@Component
public class ServerConfig {

    @DynamicValue("server.port")
    private Integer port;
}
```

Fields are injected at bean initialization and updated automatically when configuration changes after refresh.

Supported types: `String`, `int`/`Integer`, `long`/`Long`, `boolean`/`Boolean`, `double`/`Double`, `float`/`Float`.

### Startup ordering

```text
1. DynamicConfigClientFactoryBean.afterPropertiesSet()  →  refresh()
2. DynamicValueBeanPostProcessor                       →  inject + register watchers
3. DynamicConfigSpringLifecycle.start()                →  startBackgroundLifecycle()
4. Context close                                       →  shutdown() via SmartLifecycle.stop()
```

`DynamicConfigSpringLifecycle` uses `PHASE = SmartLifecycle.DEFAULT_PHASE + 1000`. Ordering is verified by `SpringLifecycleOrderIntegrationTest`.

Only `DynamicConfigSpringLifecycle.stop()` calls `client.shutdown()`. The FactoryBean does **not** shut down the client.

### Spring XML

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="configClient"
          class="io.dynamicconfig.spring.DynamicConfigClientFactoryBean">
        <property name="yamlLocations" value="classpath:application.yaml"/>
        <property name="refreshIntervalSeconds" value="30"/>
    </bean>

    <bean class="io.dynamicconfig.spring.processor.DynamicValueBeanPostProcessor">
        <constructor-arg ref="configClient"/>
    </bean>

    <bean class="io.dynamicconfig.spring.DynamicConfigSpringLifecycle">
        <constructor-arg ref="configClient"/>
    </bean>

</beans>
```

---

## Exception hierarchy

```text
ConfigException
├── ConfigValidationException   (missing/unreadable file)
├── ConfigParseException        (malformed YAML/JSON)
├── ConfigLoadException         (I/O failure)
└── ConfigConversionException   (typed getter conversion failure)
```

---

## Thread safety

* Concurrent reads via atomic cache snapshots
* Thread-safe watcher registration
* Watcher callbacks invoked outside refresh locks
* Refresh pipeline guarded by a single `ReentrantLock`

---

## Build

```bash
# Requires Java 17
./mvnw clean verify

# Or without wrapper:
JAVA_HOME=java-17 mvn clean verify
```

Other commands:

```bash
mvn clean test
mvn clean install
mvn clean install -DskipTests
```

JaCoCo reports are generated under `target/site/jacoco/` per module on `verify`.

---

## Publishing to Maven Central

Release publishing is automated via GitHub Actions (`.github/workflows/release.yml`).

### One-time setup

1. Ensure namespace `io.github.chaoticsync` is verified at [central.sonatype.com](https://central.sonatype.com)
2. Generate a Central Portal **user token**
3. Create a GPG key and publish the public key to a keyserver
4. Add these GitHub repository secrets:

| Secret | Value |
|---|---|
| `CENTRAL_USERNAME` | Central Portal token username |
| `CENTRAL_TOKEN` | Central Portal token password |
| `GPG_PRIVATE_KEY` | ASCII-armored private key **or** base64-encoded key (see below) |
| `GPG_PASSPHRASE` | GPG key passphrase |

**GPG private key for CI** — export and add as `GPG_PRIVATE_KEY`:

```bash
# Option A: paste armored key directly (must include BEGIN/END lines)
gpg --armor --export-secret-keys YOUR_KEY_ID

# Option B (recommended): base64-encode to avoid newline issues in GitHub Secrets
gpg --armor --export-secret-keys YOUR_KEY_ID | base64 -w0
```

If import fails in CI, re-create the secret using **Option B**.

### Release

Tag a release version (no `-SNAPSHOT` suffix):

```bash
git tag v1.0.0
git push origin v1.0.0
```

CI runs tests, signs artifacts, uploads to Central, and auto-publishes. The `dynamic-config-demo` module is excluded from deploy.

You can also trigger a release manually from the GitHub Actions tab (`workflow_dispatch`).

Local release (requires Central credentials in `~/.m2/settings.xml` and a local GPG key):

```bash
./mvnw clean verify deploy -Prelease
```

---

## Project structure

```text
dynamic-config/
├── pom.xml
├── README.md
├── LICENSE
├── dynamic-config-core/
│   └── src/main/java/io/dynamicconfig/core/
│       ├── cache/
│       ├── client/
│       ├── conversion/
│       ├── exception/
│       ├── refresh/
│       ├── source/
│       │   └── file/
│       └── watch/
├── dynamic-config-kubernetes/
│   └── src/main/java/io/dynamicconfig/kubernetes/
│       ├── builder/
│       ├── source/
│       └── watch/
├── dynamic-config-spring/
│   └── src/main/java/io/dynamicconfig/spring/
│       ├── annotation/
│       ├── conversion/
│       └── processor/
└── dynamic-config-demo/          # runnable examples (not published)
    └── src/main/java/io/dynamicconfig/demo/
```

---

## Tests

External systems are mocked where needed. A real Kubernetes cluster is not required.

**Core:** `MapFlattenerTest`, `PropertiesConfigSourceTest`, `YamlConfigSourceTest`, `JsonConfigSourceTest`, `ConfigurationMergerTest`, `ChangeDetectorTest`, `DefaultConfigClientTest`, `ConfigClientBuilderTest`, `PrecedenceTest`, `ShutdownTest`

**Spring:** `DynamicValueBeanPostProcessorTest`, `DynamicRefreshTest`, `XmlConfigurationTest`, `SpringLifecycleOrderIntegrationTest`

**Kubernetes:** `ConfigMapFileSourceTest`, `ConfigMapWatchSourceTest`, `KubernetesConfigExtensionsTest`

---

## Design principles

1. **Core stays framework-independent** — no Spring or Kubernetes compile dependencies in core
2. **Sources are pluggable** — implement `ConfigSource`; priority on registration, not on the source
3. **Uniform representation** — all sources flatten to `Map<String, String>`
4. **Reads are cheap** — application `get()` calls hit the in-memory cache
5. **Refresh is deterministic** — load → merge → replace cache → detect changes → notify
6. **Remote failures are isolated** — one failing source does not block others

---

## Public API

Primary entry points:

```text
ConfigClient
ConfigClientBuilder
ConfigSource
ConfigWatcher
RefreshTrigger
LifecycleConfigSource
KubernetesConfigExtensions   (kubernetes module)
```

---

## Roadmap

### v1.0 (implemented)

Core engine, properties/YAML/JSON, flattening, precedence, cache, typed getters, refresh, watch, mutable source, automatic refresh, shutdown, Spring `@DynamicValue` + XML + lifecycle, Kubernetes file + watch, tests, JaCoCo.

### Future

* ConfigHub integration
* Advanced type conversion (`Duration`, enums, collections)
* Configuration schema validation
* Secrets providers (Vault, AWS, GCP)
* Configuration history and rollback
* Spring Boot auto-configuration

---

## License

Copyright © Dynamic Config SDK contributors.

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
