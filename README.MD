# redis-perf-lab

A hand-rolled Redis server used as a controlled sandbox for JVM performance engineering. The server itself is not the point — the experiments are.

The hot path (bytes in → parse → process → bytes out) is tight enough that GC pressure, false sharing, and allocation costs show up immediately and are easy to interpret in a profiler. `redis-benchmark` provides free, professional-grade load generation with real latency distributions (p50/p99/p999), and the reference Redis implementation is always there as a comparison baseline.

Each phase targets a specific technique: GC tuning, allocation elimination, NIO and direct ByteBuffers, mechanical sympathy (cache lines, false sharing, busy-spin), off-heap storage via `sun.misc.Unsafe`, and JMH microbenchmarks. Every experiment is recorded with a hypothesis, result, and conclusion. That log is the primary output of the project.

## Phases

### Phase 0 — Minimal working server (done)

Single-threaded, blocking, String-based RESP implementation. Supports `PING`, `ECHO`, `GET`, `SET`, `CONFIG`, both RESP array and inline command formats, and a tagged test suite that exercises the full parse → dispatch → encode pipeline.

Baseline on a MacBook Air (loopback, `redis-benchmark -c 1 -n 100000 -t set,get -q`):

- SET: ~24.9k req/s, p50 ≈ 0.039 ms
- GET: ~28.3k req/s, p50 ≈ 0.039 ms

That's the number every subsequent phase has to beat.

### Phase 1 — GC tuning (weeks 2–3)

Turn on GC logging, run async-profiler in allocation mode to see what you're allocating, then swap GC algorithms (G1GC → ZGC → Shenandoah) and record p50/p99/p999 in a comparison table. Then tune G1GC knobs in isolation and experiment with heap sizing, including `-XX:+AlwaysPreTouch`.

### Phase 2 — Allocation elimination (weeks 3–4)

Drive allocations in the hot path toward zero. Object pooling for response buffers (`ThreadLocal` first), zero-allocation RESP parser using index arithmetic on `ByteBuffer`s instead of `String.split()`, and pre-computed `byte[]` for common responses like `+OK\r\n` and `+PONG\r\n`.

### Phase 3 — NIO and direct ByteBuffers (weeks 4–5)

Replace the blocking one-thread-per-connection model with an NIO selector loop. Then compare heap vs direct `ByteBuffer`s and experiment with buffer sizing to find the inflection point for your workload.

### Phase 4 — Mechanical sympathy (weeks 5–6)

Add per-command counters, demonstrate false sharing, then fix it with `@Contended`. Compare `LongAdder` vs `AtomicLong` under contention. Use JOL to inspect object layout. Benchmark busy-spin vs yield vs sleep for p999 impact.

### Phase 5 — Off-heap with `sun.misc.Unsafe` (weeks 6–7)

Implement a simple off-heap string store manually using `allocateMemory`/`putByte`/`getByte` with an on-heap key→address index. Observe GC pressure drop in `gc.log`. Then swap in Chronicle Map as the production-safe equivalent and compare.

### Phase 6 — JMH microbenchmarks (ongoing)

Runs in parallel with everything — write JMH benchmarks for the RESP parser, response serialiser, map implementations, and counter types as you go. The critical rules: always warm up, never run inside a unit test, use `Blackhole.consume()` to prevent dead-code elimination, and run with `-prof gc`.
