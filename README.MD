# redis-perf-lab
 
A hand-rolled Redis server used as a controlled sandbox for JVM performance engineering. The server itself is not the point — the experiments are.
 
The hot path (bytes in → parse → process → bytes out) is tight enough that GC pressure, false sharing, and allocation costs show up immediately and are easy to interpret in a profiler. `redis-benchmark` provides free, professional-grade load generation with real latency distributions (p50/p99/p999), and the reference Redis implementation is always there as a comparison baseline.
 
Each phase targets a specific technique: GC tuning, allocation elimination, NIO and direct ByteBuffers, mechanical sympathy (cache lines, false sharing, busy-spin), off-heap storage via `sun.misc.Unsafe`, and JMH microbenchmarks. Every experiment is recorded with a hypothesis, result, and conclusion. That log is the primary output of the project.
 