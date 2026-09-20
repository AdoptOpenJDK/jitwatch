# JITWatch MCP Server

A [Model Context Protocol](https://modelcontextprotocol.io) server that exposes
[JITWatch](https://github.com/chriswhocodes/jitwatch)'s HotSpot compilation log analysis to AI
coding agents.

The JVM records every decision its JIT compiler makes, but only into a `LogCompilation` XML file
that is hostile to read: a 60 ms program produces about 10,000 lines, method ids are per-task
indirections, and C1 and C2 describe the same refusal with different words and different limits.
JITWatch already parses all of that. This server puts the result in front of an agent as answers
rather than as a log.

It is a module of the JITWatch build, sitting alongside `core` and `ui` and depending only on
`jitwatch-core`. Neither of those modules changes.

## What an answer looks like

Every tool returns two halves, and never just one:

```json
{
  "plain": "The JVM could not merge JitDemo.big into the code that calls it from JitDemo.main line 77, because it is 404 bytes of bytecode against a limit of 325. The profile recorded 181,192 calls at this site, each paying the cost of a real method call.",
  "evidence": {
    "reason": "hot method too big",
    "reason_normalised": "CALLEE_TOO_LARGE",
    "compiler": "C2", "tier": 4,
    "flag": "FreqInlineSize", "limit": 325, "actual": 404,
    "callee": "demo.JitDemo.big"
  }
}
```

`plain` is for someone who has never read the HotSpot sources. `evidence` is for someone who has.
There is no audience switch: the agent already knows who it is talking to, and the server's job is
to make sure both readings are present and consistent.

## Tools

| Tool | Question it answers |
|---|---|
| `load_log` | Here is a log, my source and my classes. Give me a session and tell me what ran. |
| `checkup` | Is anything wrong with how the JVM compiled my code? |
| `find_methods` | Which methods match this name? |
| `explain_method` | What happened to this method: compilations, sizes, what it inlined, what it did not. |
| `explain_call_site` | Was this call inlined? If not, why, and against which limit? |
| `get_inlining_tree` | What actually became part of this compiled method? |
| `top_list` | Rank the whole run: largest methods, slowest compiles, commonest refusal reasons, intrinsics, hot throws. |
| `get_deopts` | Which methods had their compiled code thrown away, and how often? |
| `list_sessions` | Which logs are loaded? |

Start with `load_log`, then `checkup`. Everything else is drill-down, and each finding carries the
arguments needed to drill into it.

## What `checkup` will and will not say

A busy log contains thousands of inlining refusals and almost all of them are normal. Four rules
keep the list short and true, and they are the reason the output is usable:

- **Only C2's verdict counts**, unless a method never reached C2. C1 refuses nearly everything over
  35 bytes while a program warms up, which says nothing about the code that ends up running.
- **Counts come from the call site**, never from the caller's own invocation count. Reporting
  "this method ran 1.5 million times" as if it were one call site overstates a finding by orders of
  magnitude.
- **Call sites that share a caller, a callee and a reason are one finding**, listing its sites.
- **Advice must be actionable by the reader.** A refusal whose callee is JDK code is reported as
  informational and never leads the list, because "split `PrintStream.println`" is not advice.

The finding types are `HOT_METHOD_TOO_LARGE`, `MEGAMORPHIC_CALL`, `SPECULATION_FAILED`,
`REPEATED_RECOMPILATION`, `INLINING_TOO_DEEP`, `CALLER_ALREADY_LARGE` and `NEVER_COMPILED`.
Anything the server cannot classify is left to the evidence-level tools rather than shown as a
finding.

A compilation log records what the compiler decided, never how long anything took. When nothing in
the log explains a slowdown, `checkup` says so and points at profilers, GC logs and lock analysis
instead.

## Build

```sh
mvn clean package
```

That produces `mcp/target/jitwatch-mcp-shaded.jar` alongside the usual `ui/target/jitwatch-ui-shaded.jar`.

### Why this module does not change how JITWatch is built

The Model Context Protocol Java SDK is compiled for Java 17, while JITWatch keeps source and
target at 1.8 so it can still be built and run on pre-JDK10 setups whose JDK bundles JavaFX.

Those two facts do not conflict, because the parent POM only adds this module to the build when
the JDK running Maven is 17 or later:

```xml
<profile>
    <id>mcp</id>
    <activation><jdk>[17,)</jdk></activation>
    <modules><module>mcp</module></modules>
</profile>
```

On an older JDK the module is not in the reactor at all and the build is exactly what it was:

```
JDK 11:     Building JITWatch Parent [1/3] … Core [2/3] … UI [3/3]
JDK 17+:    Building JITWatch Parent [1/4] … Core [2/4] … UI [3/4] … MCP Server [4/4]
```

`core` and `ui` are untouched; only this module sets `maven.compiler.release` to 17.

## Use

Produce a log from the program you want to look at:

```sh
javac -g -d classes src/demo/JitDemo.java        # -g keeps the line numbers

java -XX:+UnlockDiagnosticVMOptions \
     -XX:+LogCompilation \
     -XX:LogFile=hotspot.log \
     -cp classes demo.JitDemo
```

Then register the server with an agent host. For Claude Code:

```json
{
  "mcpServers": {
    "jitwatch": {
      "command": "java",
      "args": ["-jar", "/path/to/jitwatch-mcp-shaded.jar", "--allowed-root", "/path/to/your/project"]
    }
  }
}
```

Now ask in plain language: *"load hotspot.log with source src and classes classes, then tell me if
anything is wrong."*

### Options

| Flag | Default | Meaning |
|---|---|---|
| `--allowed-root <path>` | none | Restrict every path argument to this directory. Repeatable. With none given, any readable path is allowed. |
| `--max-sessions <n>` | 4 | How many parsed logs to keep. A parsed model can be large, so the least recently used is evicted. |
| `--max-list-items <n>` | 25 | Default cap on list length in an answer. |
| `--max-answer-chars <n>` | 32000 | Hard cap on one serialised answer. |

## Design notes

**Everything passes an answer budget.** Lists are capped, strings truncated, the whole answer
capped again as a backstop, and nothing is cut silently: a truncated list says how much was left
and which argument to raise. An agent that receives an unbounded answer has lost the context it
needed to finish the task.

**stdout is the protocol.** The server takes a private handle to the real stdout for JSON-RPC and
repoints `System.out` at stderr, so no library print or JITWatch log line can corrupt a frame.

**Flags are resolved, not recalled.** The server keeps the documented HotSpot defaults per JDK major
version and applies any `-XX:` override found on the command line the JVM recorded in the log. Every
refusal is reported against the limit that actually applied. Models are unreliable about whether
`FreqInlineSize` is 325 on a given release; this removes the question.

**Source lines need mounted classes.** `load_log` says so in a note when they are missing, and
answers degrade to bytecode indices rather than failing.

## Tests

```sh
mvn -pl mcp test
```

The tests build their own fixture on first run: they compile and run a small program with
`-XX:+LogCompilation` using whichever JDK the build is using, then assert against the log it
produces. Nothing is committed and nothing needs setting up, so a clean clone works, and the
assertions are made against what HotSpot really decided rather than against a recorded file that
could drift from the compiler's behaviour.

The demo program is written so each method provokes one specific behaviour: a tiny
method that gets inlined, a large one that does not, an allocation escape analysis removes, a
monomorphic and a megamorphic call, an intrinsic, and a branch that deoptimises. The tests assert
that the answers say what the compiler actually did, including that the finding for the oversized
callee carries `FreqInlineSize` and 325 rather than C1's `MaxInlineSize` and 35.

## Status

Early. The tools above work and are tested against real logs. Not yet implemented: comparing two
runs (`what_changed`), bytecode and assembly listings, the compilation timeline, and code cache and
compiler thread views.

Known limitation: the default flag values are the documented HotSpot defaults for x86_64 and
aarch64, and a few are release-dependent. They should be verified against each JDK's
`globals.hpp`. Values overridden on the recorded command line are always exact.

## Licence

Simplified BSD, the same as the rest of JITWatch.
