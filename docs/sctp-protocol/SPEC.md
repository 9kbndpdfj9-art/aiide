# SCTP Protocol Specification

**Semantic Compression Token Protocol**

Version: 1.0.0  
Status: Draft

---

## Overview

SCTP (Semantic Compression Token Protocol) is a standardized protocol for AI code generation that reduces token consumption by 80-95% without sacrificing generation quality.

**Core Principle**: Only tell the model "what the boundaries are", not "what the current state is".

---

## Design Principles

1. **Boundary-First**: Transmit only behavioral boundaries, not implementation details
2. **Lossy-but-Safe**: Compress implementation details; preserve all constraints
3. **Language-Agnostic**: Works with any programming language
4. **Extensible**: New constraint types can be added without breaking existing implementations
5. **Minimal Overhead**: SBDL encoding is 4-6x more compact than JSON

---

## Protocol Structure

### SCTP Message Types

#### 1. INSTRUCTION

Tells the model what to do.

```
## INSTRUCTION
Intent: {natural language description}
Targets: {comma-separated function names}
Scope: {comma-separated file:function boundaries}
```

#### 2. BOUNDARY

Provides compressed semantic fingerprints.

```
## BOUNDARY
FUNC: {function signature}
INPUT: {compressed input contract}
OUTPUT: {compressed output contract}
EFFECTS: {compressed side effects}
DEPS: {compressed dependencies}
CONSTRAINTS: {compressed constraints}
---
```

#### 3. RESPONSE

The model's generated code skeleton.

```
## RESPONSE
SKELETON: {generated code}
AFFECTED: {comma-separated file paths}
CONFIDENCE: {0.0-1.0}
```

---

## SBDL: Semantic Boundary Description Language

SBDL is the compact encoding format for semantic boundaries.

### Input Contract Format

```
INPUT: params=[name1,name2],opt=[name3],pre=[precondition text]
```

### Output Contract Format

```
OUTPUT: type=ReturnType,errors=[ErrorType],inv=[invariant text]
```

### Side Effects Format

```
EFFECTS: state_mutation,io_operation,network_call
```

---

## Compression Mechanisms

### Mechanism 1: Semantic Boundary Extraction

Instead of sending full code, SCTP sends semantic boundaries:
- 60 tokens per function (vs 156 tokens full code)
- 62% reduction

### Mechanism 2: Dependency Graph Compression

Instead of sending full import statements, SCTP sends dependency edges.

### Mechanism 3: Constraint-Only Transmission

SCTP only transmits constraints, not implementation.

### Mechanism 4: Incremental Updates

When code changes, SCTP only transmits the diff of semantic boundaries.

### Mechanism 5: Local Zero-Token Engine

For simple tasks (complexity < 0.7), use local rule-based engine.

---

## Versioning

SCTP uses semantic versioning: `major.minor.patch`

- **Major**: Breaking changes to protocol structure
- **Minor**: New constraint types, new SBDL fields
- **Patch**: Bug fixes, performance improvements

---

## License

SCTP Protocol is released under the Apache 2.0 License.
