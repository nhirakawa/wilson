# wilson-server

## Startup
The class that starts everything is `WilsonRunner`. First, it parses command line arguments and loads properties from a configuration file (optional).
Then, it constructs an injector with `WilsonConfigModule` and `WilsonTransportModule` modules. The `WilsonConfigModule` provides the original parsed command line arguments,
as well as the canonical `Configuration`. The `WilsonTransportModule` provides low-level Netty classes depending on the cluster mode; running in local mode allows the entire cluster to run in a single JVM,
while normal mode runs a single cluster member.
 
Each cluster member in the JVM gets its own injector. This injector is created from the parent injector described above, with the addition of a `WilsonRaftModule`. The `WilsonRaftModule` provides
the canonical state machine context (`WilsonState`) for each member. If running in local mode, an injector will be created for each member, and each member will have its own isolated `WilsonState` reference.
If running in normal mode, a single child injector will be created.
