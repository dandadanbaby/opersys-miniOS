# BEAST OS — Test Matrix

> Last Updated: Phase 0 — 2026-11-14
> Status: PLANNED — Tests will be populated during implementation phases

---

## Test Strategy

- **Framework:** JUnit 5
- **Scope:** Every algorithm engine tested independently (no GUI dependency)
- **Coverage Categories:**
  1. **Normal cases** — Standard textbook examples with known results
  2. **Boundary cases** — Edge conditions (single process, zero values, maximum limits)
  3. **Exceptional cases** — Invalid inputs, impossible configurations
  4. **Small datasets** — 3-5 items (classroom examples)
  5. **Large datasets** — 1,000+ items where applicable (stress testing)

- **Verification Method:** All expected results are hand-calculated or sourced from verified textbook examples
- **Rule:** Tests are NEVER weakened to match incorrect implementations

---

## Module Test Plan

### CPU Scheduling Tests

| Test ID | Algorithm | Category | Input Description | Expected Result | Actual | P/F | Verification |
|---------|-----------|----------|-------------------|-----------------|--------|-----|-------------|
| CPU-FCFS-01 | FCFS | Normal | 4 processes, varied arrival/burst | Hand-calculated CT, TAT, WT | — | — | Phase 2 |
| CPU-FCFS-02 | FCFS | Boundary | Single process (AT=0, BT=5) | CT=5, TAT=5, WT=0 | — | — | Phase 2 |
| CPU-FCFS-03 | FCFS | Boundary | All processes arrive at time 0 | Ordered by PID | — | — | Phase 2 |
| CPU-FCFS-04 | FCFS | Normal | Processes with idle CPU gap | Idle time tracked | — | — | Phase 2 |
| CPU-FCFS-05 | FCFS | Large | 1000 processes | Correct averages | — | — | Phase 2 |
| CPU-SJF-01 | SJF | Normal | 4 processes, varied burst times | Shortest burst first (with arrival) | — | — | Phase 2 |
| CPU-SJF-02 | SJF | Boundary | Equal burst times | Tie-break by arrival → PID | — | — | Phase 2 |
| CPU-SJF-03 | SJF | Large | 1000 processes | Correct ordering | — | — | Phase 2 |
| CPU-PRI-01 | Priority | Normal | 4 processes, varied priorities | Lowest priority value first | — | — | Phase 2 |
| CPU-PRI-02 | Priority | Boundary | Equal priorities | Tie-break by arrival → PID | — | — | Phase 2 |
| CPU-PRI-03 | Priority | Large | 1000 processes | Correct ordering | — | — | Phase 2 |
| CPU-RR-01 | Round Robin | Normal | 4 processes, quantum=4 | Match design reference results | — | — | Phase 2 |
| CPU-RR-02 | Round Robin | Boundary | Quantum=1 | Each process gets 1 unit per turn | — | — | Phase 2 |
| CPU-RR-03 | Round Robin | Boundary | Quantum > max burst | Degenerates to FCFS | — | — | Phase 2 |
| CPU-RR-04 | Round Robin | Normal | Staggered arrivals | Correct ready-queue behavior | — | — | Phase 2 |
| CPU-RR-05 | Round Robin | Large | 1000 processes, quantum=5 | Correct averages | — | — | Phase 2 |
| CPU-VAL-01 | All | Exception | Negative burst time | Validation error | — | — | Phase 2 |
| CPU-VAL-02 | All | Exception | Zero burst time | Validation error | — | — | Phase 2 |
| CPU-VAL-03 | All | Exception | Negative arrival time | Validation error | — | — | Phase 2 |
| CPU-VAL-04 | RR | Exception | Zero time quantum | Validation error | — | — | Phase 2 |

### Banker's Algorithm Tests

| Test ID | Category | Input Description | Expected Result | Actual | P/F | Verification |
|---------|----------|-------------------|-----------------|--------|-----|-------------|
| BNK-01 | Normal | 5P×3R, classic safe example | SAFE, sequence exists | — | — | Phase 2 |
| BNK-02 | Normal | 5P×3R, unsafe state | UNSAFE, no safe sequence | — | — | Phase 2 |
| BNK-03 | Boundary | 1 process, 1 resource | Trivially safe | — | — | Phase 2 |
| BNK-04 | Boundary | All resources fully allocated | Check if safe with 0 available | — | — | Phase 2 |
| BNK-05 | Exception | Allocation > Maximum | Validation error | — | — | Phase 2 |
| BNK-06 | Exception | Negative resource values | Validation error | — | — | Phase 2 |
| BNK-07 | Normal | Match design reference data | P1→P3→P4→P0→P2 | — | — | Phase 2 |
| BNK-08 | Large | 100 processes, 10 resources | Correct safety determination | — | — | Phase 2 |

### Paging Tests

| Test ID | Category | Input Description | Expected Result | Actual | P/F | Verification |
|---------|----------|-------------------|-----------------|--------|-----|-------------|
| PAG-01 | Normal | Page 3, Offset 2060, Size 4096 | PA = 14348 | — | — | Phase 3 |
| PAG-02 | Boundary | Offset = 0 | PA = Frame × PageSize | — | — | Phase 3 |
| PAG-03 | Boundary | Offset = PageSize − 1 | Valid, last byte | — | — | Phase 3 |
| PAG-04 | Exception | Page number out of bounds | TRAP | — | — | Phase 3 |
| PAG-05 | Exception | Offset ≥ PageSize | Validation error | — | — | Phase 3 |
| PAG-06 | Normal | Multiple translations (design ref) | All 4 results match | — | — | Phase 3 |

### Page Replacement Tests

| Test ID | Algorithm | Category | Input | Expected | Actual | P/F | Verification |
|---------|-----------|----------|-------|----------|--------|-----|-------------|
| PR-FIFO-01 | FIFO | Normal | 7,0,1,2,0,3,0,4,2,3,0,3,2,1,2; 3 frames | 9 faults, 6 hits | — | — | Phase 3 |
| PR-LRU-01 | LRU | Normal | Same string; 3 frames | Verify against textbook | — | — | Phase 3 |
| PR-OPT-01 | Optimal | Normal | Same string; 3 frames | Fewest faults | — | — | Phase 3 |
| PR-FIFO-02 | FIFO | Boundary | All unique pages | All faults | — | — | Phase 3 |
| PR-FIFO-03 | FIFO | Boundary | Same page repeated | 1 fault, rest hits | — | — | Phase 3 |
| PR-FIFO-04 | FIFO | Boundary | Frames ≥ unique pages | No replacement needed | — | — | Phase 3 |
| PR-ALL-01 | All 3 | Large | 1000 references, 10 frames | Correct counts per alg | — | — | Phase 3 |
| PR-VAL-01 | All | Exception | 0 frames | Validation error | — | — | Phase 3 |
| PR-VAL-02 | All | Exception | Empty reference string | Validation error | — | — | Phase 3 |

### Segmentation Tests

| Test ID | Category | Input | Expected | Actual | P/F | Verification |
|---------|----------|-------|----------|--------|-----|-------------|
| SEG-01 | Normal | Seg 1, Base 6300, Limit 400, Offset 320 | PA = 6620 | — | — | Phase 4 |
| SEG-02 | Normal | Seg 1, Base 6300, Limit 400, Offset 10 | PA = 6310 | — | — | Phase 4 |
| SEG-03 | Boundary | Offset = 0 | PA = Base | — | — | Phase 4 |
| SEG-04 | Boundary | Offset = Limit − 1 | Valid (last byte) | — | — | Phase 4 |
| SEG-05 | Exception | Offset ≥ Limit | SEGMENTATION FAULT | — | — | Phase 4 |
| SEG-06 | Exception | Invalid segment number | Error | — | — | Phase 4 |
| SEG-07 | Exception | Negative offset | Validation error | — | — | Phase 4 |

### Fragmentation Tests

| Test ID | Algorithm | Category | Input | Expected | Actual | P/F | Verification |
|---------|-----------|----------|-------|----------|--------|-----|-------------|
| FRG-FF-01 | First Fit | Normal | Design ref: 5 blocks, 6 processes | Match design allocation | — | — | Phase 4 |
| FRG-BF-01 | Best Fit | Normal | Same input | Smallest adequate block | — | — | Phase 4 |
| FRG-WF-01 | Worst Fit | Normal | Same input | Largest available block | — | — | Phase 4 |
| FRG-FF-02 | First Fit | Boundary | Process exactly fits block | Internal frag = 0 | — | — | Phase 4 |
| FRG-FF-03 | First Fit | Boundary | No block large enough | Process waiting | — | — | Phase 4 |
| FRG-ALL-01 | All 3 | Normal | Verify internal frag totals | Correct per algorithm | — | — | Phase 4 |
| FRG-ALL-02 | All 3 | Large | 100 blocks, 200 processes | Correct allocation | — | — | Phase 4 |
| FRG-VAL-01 | All | Exception | Zero block size | Validation error | — | — | Phase 4 |

### Disk Scheduling Tests

| Test ID | Algorithm | Category | Input | Expected | Actual | P/F | Verification |
|---------|-----------|----------|-------|----------|--------|-----|-------------|
| DSK-FCFS-01 | FCFS | Normal | Head=53, Queue=[98,183,37,122,14,124,65,67], Max=199 | Service in order, total movement calc | — | — | Phase 5 |
| DSK-SSTF-01 | SSTF | Normal | Same input | 53→65→67→37→14→98→122→124→183, total=236 | — | — | Phase 5 |
| DSK-SCAN-01 | SCAN | Normal | Same input, direction=ascending | Serve ascending then descending | — | — | Phase 5 |
| DSK-CSCAN-01 | C-SCAN | Normal | Same input | Circular scan | — | — | Phase 5 |
| DSK-LOOK-01 | LOOK | Normal | Same input | Reverse at last request | — | — | Phase 5 |
| DSK-CLOOK-01 | C-LOOK | Normal | Same input | Circular look | — | — | Phase 5 |
| DSK-FCFS-02 | FCFS | Boundary | Single request | Movement = |head−request| | — | — | Phase 5 |
| DSK-ALL-01 | All 6 | Boundary | Head at 0 | Correct boundary behavior | — | — | Phase 5 |
| DSK-ALL-02 | All 6 | Boundary | Head at max cylinder | Correct boundary behavior | — | — | Phase 5 |
| DSK-ALL-03 | All 6 | Large | 1000 requests, max=9999 | Correct totals | — | — | Phase 5 |
| DSK-VAL-01 | All | Exception | Request > max cylinder | Validation error | — | — | Phase 5 |
| DSK-VAL-02 | All | Exception | Negative head position | Validation error | — | — | Phase 5 |

### Multiprogramming Tests

| Test ID | Category | Input | Expected | Actual | P/F | Verification |
|---------|----------|-------|----------|--------|-----|-------------|
| MPG-01 | Normal | 8 processes, 512 MB total RAM (design ref) | 6 active, 2 waiting | — | — | Phase 6 |
| MPG-02 | Boundary | Single process, fits in memory | Active, 100% allocated | — | — | Phase 6 |
| MPG-03 | Boundary | Process larger than total memory | Rejected/waiting | — | — | Phase 6 |
| MPG-04 | Normal | Process completion frees memory for waiting | Correct admission | — | — | Phase 6 |
| MPG-05 | Normal | Verify utilization calculations | Memory% = allocated/total × 100 | — | — | Phase 6 |
| MPG-06 | Large | 100 processes | Correct admission/rejection | — | — | Phase 6 |

---

## Test Evidence Requirements

For each module, the following evidence will be saved in `docs/testing/`:

1. **Small dataset test:** Screenshot or log of 3-5 item test with verified results
2. **Large dataset test:** Screenshot or log of 1,000+ item test (where applicable)
3. **JUnit test report:** Maven Surefire XML/HTML report
4. **Manual verification:** Hand-calculated expected values documented in test comments

---

## Summary

| Module | Planned Tests | Normal | Boundary | Exception | Large |
|--------|--------------|--------|----------|-----------|-------|
| CPU Scheduling | 19 | 5 | 5 | 4 | 5 |
| Banker's Algorithm | 8 | 3 | 2 | 2 | 1 |
| Paging | 6 | 2 | 2 | 2 | 0 |
| Page Replacement | 9 | 3 | 3 | 2 | 1 |
| Segmentation | 7 | 2 | 2 | 3 | 0 |
| Fragmentation | 8 | 3 | 2 | 1 | 2 |
| Disk Scheduling | 12 | 6 | 3 | 2 | 1 |
| Multiprogramming | 6 | 3 | 2 | 0 | 1 |
| **TOTAL** | **75** | **27** | **21** | **16** | **11** |
