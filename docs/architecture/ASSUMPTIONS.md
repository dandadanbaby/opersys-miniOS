# BEAST OS — Assumptions Document

> Last Updated: Phase 0 — 2026-11-14

---

## 1. Academic Scope Assumptions

### 1.1 CPU Scheduling
| # | Assumption | Rationale |
|---|-----------|-----------|
| A1 | Core algorithms: FCFS, SJF (non-preemptive), Priority (non-preemptive), Round Robin | Explicitly listed in PDF as examples of "algorithms covered in class" |
| A2 | Lower numerical priority value = higher priority (default convention) | Standard OS textbook convention; will be documented in UI |
| A3 | Tie-breaking: earliest arrival time first; if still tied, lowest PID first | Deterministic behavior required for testing |
| A4 | Round Robin uses arrival-aware ready queue | Processes enter ready queue only upon arrival |
| A5 | CPU idle time is tracked when no process is available | Required for accurate utilization and Gantt chart |
| A6 | Additional variants (SRTF, Preemptive Priority) may be added as extensions | PDF says "all covered in class"; these are commonly taught |

### 1.2 Banker's Algorithm
| # | Assumption | Rationale |
|---|-----------|-----------|
| B1 | Need matrix is always auto-calculated (Need = Max − Allocation) | PDF explicitly says "Calculate Need"; design shows Need as computed |
| B2 | System Max resources = sum of all allocated + available | Standard Banker's definition |
| B3 | When multiple processes can be selected in safety check, choose lowest index first | Deterministic tie-breaking for testability |
| B4 | Zero allocation is valid for a process | A process may not have any resources yet |

### 1.3 Paging
| # | Assumption | Rationale |
|---|-----------|-----------|
| C1 | Page size is user-configurable (in bytes) | Design shows "4096 B" as user-set value |
| C2 | Frame numbers are user-provided via the page table | Design shows editable Frame Number fields |
| C3 | Multiple translation requests can be submitted in one session | Design shows a Verification Log with multiple entries |
| C4 | Out-of-bounds page numbers generate a TRAP | Design shows "TRAP: Out of bounds" |

### 1.4 Page Replacement
| # | Assumption | Rationale |
|---|-----------|-----------|
| D1 | All three algorithms (FIFO, LRU, Optimal) run on the same reference string for comparison | Design shows Algorithm Comparison Tracker |
| D2 | Frames are initially empty (−1) | Standard assumption; first references always fault |
| D3 | FIFO tie-breaking: replace the first frame loaded (by order) | Standard FIFO definition |
| D4 | Optimal tie-breaking: if multiple pages have equal future distance, replace the one in the lowest-indexed frame | Documented convention |

### 1.5 Segmentation
| # | Assumption | Rationale |
|---|-----------|-----------|
| E1 | Segments can have overlapping address ranges in physical memory | The user defines base/limit independently |
| E2 | Offset validity: 0 ≤ offset < limit (strict less-than) | Standard: limit is the segment length |
| E3 | Multiple translation requests per session | Design shows multiple log entries |

### 1.6 Fragmentation
| # | Assumption | Rationale |
|---|-----------|-----------|
| F1 | Default mode: fixed-partition allocation (internal fragmentation) | Design shows fixed blocks with internal fragmentation |
| F2 | Internal fragmentation = block size − process size (for allocated processes) | Standard definition |
| F3 | External fragmentation = total scattered free space when a process can't fit contiguously | Standard definition |
| F4 | Processes are allocated in order (P1, P2, P3...) | Design shows sequential allocation |
| F5 | A block can hold only one process | Fixed partitioning assumption |
| F6 | Variable-partition allocation may be added as an extension | Master prompt suggests both fixed and variable |

### 1.7 Disk Scheduling
| # | Assumption | Rationale |
|---|-----------|-----------|
| G1 | Six algorithms: FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK | Design confirms all 6 tabs; PDF says "all covered in class" |
| G2 | SCAN moves to disk boundary (0 or max cylinder) before reversing | Standard SCAN definition |
| G3 | C-SCAN: moves to max boundary, then jumps to 0, serves remaining | Standard C-SCAN |
| G4 | LOOK reverses at the last request in current direction (not at boundary) | Standard LOOK |
| G5 | C-LOOK: moves to highest request, then jumps to lowest request | Standard C-LOOK |
| G6 | Initial direction: default is ascending (toward higher cylinders); user-configurable where needed | Design does not show explicit direction selector; SSTF doesn't need one |
| G7 | Duplicate requests are served (counted in movement) | Conservative handling |
| G8 | Head movement at boundary jumps (C-SCAN jump from max→0, C-LOOK jump) is NOT counted in total distance | Common textbook convention; will be documented |

### 1.8 Multiprogramming
| # | Assumption | Rationale |
|---|-----------|-----------|
| H1 | Simulation model: memory-limited process admission with FIFO ready queue | Design shows processes admitted until RAM full, then waiting |
| H2 | CPU execution uses Round Robin with configurable time quantum | Master prompt suggests RR; design doesn't show explicit quantum but it's implied |
| H3 | Process states: NEW → READY → RUNNING → COMPLETED or WAITING (if insufficient memory) | Design shows "Active in Memory" and "Waiting (RAM Full)" |
| H4 | Resource sharing demonstration: primarily memory sharing | Design focuses on memory allocation/utilization |
| H5 | Process completion releases memory for waiting processes | Standard multiprogramming behavior |

---

## 2. Technical Assumptions

| # | Assumption | Rationale |
|---|-----------|-----------|
| T1 | Java 25 LTS and JavaFX 25 LTS are available and compatible | Master prompt specification |
| T2 | Maven Wrapper will bundle Maven 3.9.x | Standard Maven Wrapper version |
| T3 | Application window default size: ≈1440×800px (based on design proportions) | Design reference screenshots |
| T4 | Minimum usable window size: ≈1024×700px | Responsive design target |
| T5 | The application operates entirely offline | No external services or APIs |
| T6 | Dashboard values start at zero/empty before any simulation | No fabricated initial data |
| T7 | "System Time" in header is a running clock showing actual wall time | Design shows 00:45:15 format |
| T8 | "OS Simulation Status" reflects actual app state: IDLE → RUNNING → COMPLETED cycle | Must not be permanently ACTIVE |

---

## 3. Design Interpretation Assumptions

| # | Assumption | Rationale |
|---|-----------|-----------|
| D1 | The BEAST OS logo will be recreated programmatically (SVG path or icon font) since no vector source is available | Only raster PNG in screenshots |
| D2 | Colors extracted from screenshots are approximate; exact hex values may vary ±5% | Screenshot compression artifacts |
| D3 | Card icons will use Unicode symbols or simple JavaFX shapes rather than an external icon library | Minimize dependencies |
| D4 | The "New Simulation" button resets the current module for a fresh input session | Consistent across all module screens |

---

## 4. Assumptions Requiring User Confirmation

| # | Question | Default Assumption | Impact |
|---|----------|-------------------|--------|
| Q1 | Should SRTF and Preemptive Priority be included as additional CPU scheduling variants? | Yes, if "covered in class" | Additional 2 algorithms in CPU module |
| Q2 | For Disk Scheduling SCAN/C-SCAN, should the jump from boundary to opposite end count as head movement? | No (jump distance not counted) | Affects total movement calculations |
| Q3 | Should the multiprogramming module include a CPU burst simulation with time quantum, or focus on memory admission only? | Include basic RR CPU execution | Complexity of module |
| Q4 | For Fragmentation, should variable-partition allocation be implemented alongside fixed-partition? | Start with fixed; add variable if time permits | Scope of fragmentation module |
