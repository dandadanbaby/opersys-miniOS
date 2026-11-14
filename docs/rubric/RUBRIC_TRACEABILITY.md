# BEAST OS — Rubric Traceability Matrix

> Source: `Rubrics/RUBRICS-OS.pdf` (4 pages, 100-point scale)
> Last Updated: Phase 0 — 2026-11-14

---

## I. Project Overview Requirements

| Req ID | Rubric Requirement | Implementation Target | Interface Location | Validation | Tests | Evidence | Status |
|--------|-------------------|----------------------|-------------------|------------|-------|----------|--------|
| OV-01 | Interactive OS Simulator using Java with GUI | JavaFX desktop application | Entire application | App launches & runs | Startup test | Screenshot | ⬜ Planned |
| OV-02 | Implement required OS concepts and algorithms | 8 modules, all algorithms | All module views | Algorithm correctness | JUnit per module | Test reports | ⬜ Planned |
| OV-03 | Accept dynamic user input | All modules accept flexible input | Input panels per module | No fixed limits | Dynamic-input tests | Large-dataset evidence | ⬜ Planned |
| OV-04 | Display understandable results | Tables, charts, visualizations | Result panels per module | Visual clarity | Manual review | Screenshots | ⬜ Planned |
| OV-05 | Submitted as source code + runnable application | Maven project + JAR/package | Repository + distributable | Build succeeds, app runs | Build & launch test | Build log | ⬜ Planned |

---

## II. Required Modules and Functionality (Section A — 40 pts)

### Module 01 — CPU Scheduling (6 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| CPU-01 | Implement FCFS | `cpu/` engine — FCFS algorithm | Algorithm tab selector (FCFS) | Correct scheduling order | JUnit: normal, boundary, large | ✅ Complete |
| CPU-02 | Implement SJF | `cpu/` engine — SJF algorithm | Algorithm tab selector (SJF) | Correct ordering by burst time | JUnit: ties, arrival times | ✅ Complete |
| CPU-03 | Implement Priority Scheduling | `cpu/` engine — Priority algorithm | Algorithm tab selector (Priority) | Correct priority ordering | JUnit: equal priorities | ✅ Complete |
| CPU-04 | Implement Round Robin | `cpu/` engine — RR algorithm | Algorithm tab selector (Round Robin) + Time Quantum config | Correct time-quantum preemption | JUnit: quantum edge cases | ✅ Complete |
| CPU-05 | Display scheduling order | Gantt chart visualization | CPU Execution Timeline panel | Visual matches computed order | Verify against manual calc | ✅ Complete |
| CPU-06 | Calculate waiting time | WT = TAT − BT per process | Results table: "Waiting Time (WT)" column | Formula correctness | JUnit per algorithm | ✅ Complete |
| CPU-07 | Calculate turnaround time | TAT = CT − AT per process | Results table: "Turnaround Time (TAT)" column | Formula correctness | JUnit per algorithm | ✅ Complete |
| CPU-08 | Display average WT and TAT | Computed averages shown | Summary cards + results footer | Arithmetic correctness | JUnit average verification | ✅ Complete |

### Module 02 — Banker's Algorithm (5 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| BNK-01 | Allow users to specify process and resource counts | Dynamic N×M matrix input | Process count + Resource type count fields | Positive integers | Input validation test | ✅ Complete |
| BNK-02 | Calculate Need matrix | Need = Max − Allocation | Need Matrix panel (auto-calculated, read-only) | Need[i][j] = Max[i][j] − Alloc[i][j] | JUnit matrix calculation | ✅ Complete |
| BNK-03 | Determine safe/unsafe state | Safety algorithm with Work and Finish | System Status card: SAFE / UNSAFE | Correct safety determination | JUnit safe + unsafe cases | ✅ Complete |
| BNK-04 | Display safe sequence when one exists | Ordered list of process completion | Safe Sequence card + Execution Verification Log | Sequence is valid | JUnit sequence verification | ✅ Complete |
| BNK-05 | Allocation ≤ Maximum validation | Alloc[i][j] ≤ Max[i][j] for all i,j | Error feedback on invalid entry | Reject impossible allocations | JUnit invalid-input test | ✅ Complete |

### Module 03 — Paging (4 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| PAG-01 | Simulate page-to-frame mapping | Page table with Page→Frame map | Page Map Table panel | Correct mappings | JUnit mapping tests | ✅ Complete |
| PAG-02 | Logical-to-physical address translation | PageNo = LA/PageSize, Offset = LA%PageSize, PA = Frame×PageSize+Offset | Translation flowchart + result display | Formula correctness | JUnit translation tests | ✅ Complete |
| PAG-03 | Boundary validation | Reject out-of-bounds page numbers | Validation Status column: TRAP indication | Out-of-range detection | JUnit boundary tests | ✅ Complete |
| PAG-04 | Address Translation Verification Log | Multi-request results table | Verification Log table | All translations correct | JUnit multi-request test | ✅ Complete |

### Module 04 — Page Replacement (6 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| PR-01 | Implement FIFO | `replacement/` engine — FIFO | Algorithm selector tab (FIFO) | Oldest page replaced | JUnit FIFO tests | ✅ Complete |
| PR-02 | Implement LRU | `replacement/` engine — LRU | Algorithm selector tab (LRU) | Least recently used replaced | JUnit LRU tests | ✅ Complete |
| PR-03 | Implement Optimal | `replacement/` engine — Optimal | Algorithm selector tab (Optimal) | Farthest-future-use replaced | JUnit Optimal tests | ✅ Complete |
| PR-04 | Display frame changes | Frame-by-frame state grid | Frame State Evolution table | Correct frame contents per step | JUnit step-by-step verify | ✅ Complete |
| PR-05 | Display page hits and faults | Hit/Fault status per reference | STATUS row + summary cards | Correct hit/fault counts | JUnit hit/fault counts | ✅ Complete |
| PR-06 | Variable-length reference string + frame count | Dynamic input | Reference string input + frame count field | No fixed limits | Large-dataset test | ✅ Complete |
| PR-07 | Algorithm comparison | Compare FIFO, LRU, Optimal side-by-side | Algorithm Comparison Tracker panel | Correct hit ratios per algorithm | JUnit comparison test | ✅ Complete |

### Module 05 — Segmentation (4 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| SEG-01 | Define segments with base and limit | Segment table input | Segment Table panel | Positive integers | Input validation | ✅ Complete |
| SEG-02 | Validate logical addresses | 0 ≤ Offset < Limit check | Validation & Trap Log | Correct valid/invalid determination | JUnit boundary tests | ✅ Complete |
| SEG-03 | Show valid translations | PA = Base + Offset | Physical Address card + Log entries | Formula correctness | JUnit translation tests | ✅ Complete |
| SEG-04 | Show out-of-range errors | SEGMENTATION FAULT display | Red fault banner in Trap Log | Clear error message | JUnit fault detection | ✅ Complete |
| SEG-05 | Physical memory map visualization | Segments mapped in RAM | Physical Memory Map diagram | Correct proportional layout | Visual verification | ✅ Complete |

### Module 06 — Fragmentation (4 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| FRG-01 | Enter multiple memory blocks and processes | Dynamic block/process input | Memory Allocation Table + Add Process button | Flexible entry | Large-dataset test | ✅ Complete |
| FRG-02 | Demonstrate allocation | First-Fit, Best-Fit, Worst-Fit engines | Algorithm tabs (First-Fit / Best-Fit / Worst-Fit) | Correct allocation decisions | JUnit per algorithm | ✅ Complete |
| FRG-03 | Identify internal fragmentation | Block size − process size for allocated fixed partitions | Internal Frag column + Total Internal Frag card | Correct calculation | JUnit internal frag tests | ✅ Complete |
| FRG-04 | Identify external fragmentation | Scattered free space that can't satisfy a request | External Frag card + Analysis Log | Correct identification | JUnit external frag tests | ✅ Complete |
| FRG-05 | Visual memory layout | Block partition diagram | Proportional Memory Block Partition panel | Correct proportional rendering | Visual verification | ✅ Complete |

### Module 07 — Disk Scheduling (6 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| DSK-01 | Implement FCFS | `disk/` engine — FCFS | Algorithm tab (FCFS) | Service order = request order | JUnit FCFS test | ✅ Complete |
| DSK-02 | Implement SSTF | `disk/` engine — SSTF | Algorithm tab (SSTF) | Nearest request served next | JUnit SSTF test | ✅ Complete |
| DSK-03 | Implement SCAN | `disk/` engine — SCAN | Algorithm tab (SCAN) | Correct direction + boundary | JUnit SCAN test | ✅ Complete |
| DSK-04 | Implement C-SCAN | `disk/` engine — C-SCAN | Algorithm tab (C-SCAN) | Circular sweep behavior | JUnit C-SCAN test | ✅ Complete |
| DSK-05 | Implement LOOK | `disk/` engine — LOOK | Algorithm tab (LOOK) | Reverses at last request | JUnit LOOK test | ✅ Complete |
| DSK-06 | Implement C-LOOK | `disk/` engine — C-LOOK | Algorithm tab (C-LOOK) | Circular without boundary | JUnit C-LOOK test | ✅ Complete |
| DSK-07 | Display service order | Sequence flow visualization | Service Order Sequence Flow panel | Matches computed order | JUnit order verification | ✅ Complete |
| DSK-08 | Display total head movement | Sum of |movements| | Total Head Movement card + calculation row | Correct sum | JUnit total-movement test | ✅ Complete |
| DSK-09 | Seek-path diagram | 2D trajectory chart | 2D Disk Head Seek Trajectory panel | Correct plot points | Visual verification | ✅ Complete |
| DSK-10 | Step-by-step distance table | Per-step absolute differences | Step-by-Step Seek Distance Table | Each step correct | JUnit step verification | ✅ Complete |

> **Note:** The PDF says "all disk scheduling algorithms covered in class" without naming specific ones. FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK are proposed as a standard baseline per the master prompt. The design reference shows tabs for FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK — confirming this set.

### Module 08 — Multiprogramming (5 pts)

| Req ID | Rubric Requirement | Implementation Target | Interface Element | Validation | Tests | Status |
|--------|-------------------|----------------------|------------------|------------|-------|--------|
| MPG-01 | Simulate multiple programs sharing memory | Process admission + memory allocation engine | Process Resource Sharing Table | Correct admission/rejection | JUnit admission tests | ✅ Complete |
| MPG-02 | Show resource sharing/utilization | Memory utilization tracking | System Resources donut + Memory Utilization card | Correct percentages | JUnit utilization tests | ✅ Complete |
| MPG-03 | Show system status | Process states: Active/Waiting | Status column with indicators | Correct state transitions | JUnit state-transition tests | ✅ Complete |
| MPG-04 | Live Memory Partition Map | Visual RAM distribution | Live Memory Partition Map bar chart | Correct proportional blocks | Visual verification | ✅ Complete |

---

## III. Dynamic User Input (Section B.5 — 3 pts)

| Req ID | Rubric Requirement | Modules Affected | Implementation | Status |
|--------|-------------------|-----------------|----------------|--------|
| DYN-01 | No hardcoded dataset or arbitrary fixed item limit | All 8 modules | Dynamic collections, Add/Remove controls | ✅ Complete |
| DYN-02 | CPU: flexible process count + IDs, AT, BT, Priority | CPU Scheduling | Add Process button + editable table | ✅ Complete |
| DYN-03 | Banker: user-specified N processes and M resource types | Banker's Algorithm | Config fields for N and M | ✅ Complete |
| DYN-04 | Paging/PageRepl: variable-length reference string + frame count | Paging, Page Replacement | Text input for string, spinner for frames | ✅ Complete |
| DYN-05 | Segmentation: variable segment count | Segmentation | Add Segment control | ✅ Complete |
| DYN-06 | Fragmentation: variable blocks and processes | Fragmentation | Add Process button + block config | ✅ Complete |
| DYN-07 | Disk: variable request list + initial position + disk limits | Disk Scheduling | Text input for queue + position field | ✅ Complete |
| DYN-08 | Multiprogramming: variable program/process count | Multiprogramming | Add Process control | ✅ Complete |

---

## IV. Input Validation (Section B.4 — 2 pts)

| Req ID | Rubric Requirement | Implementation | Status |
|--------|-------------------|----------------|--------|
| VAL-01 | Handle blank fields | Centralized validator rejects empty input with message | ✅ Complete |
| VAL-02 | Handle invalid numbers | NumberFormatException caught, user-friendly error | ✅ Complete |
| VAL-03 | Handle negative values where not allowed | Range validation per field | ✅ Complete |
| VAL-04 | Handle inconsistent input | Matrix dimension checks, Alloc ≤ Max checks | ✅ Complete |
| VAL-05 | Application does not crash on invalid input | All exceptions caught, graceful recovery | ✅ Complete |

---

## V. GUI Design and Usability (Section B — 10 pts)

| Req ID | Criterion | Points | Implementation Target | Status |
|--------|-----------|--------|----------------------|--------|
| GUI-01 | Interface design: clear, organized, consistent layout | 2 | BEAST OS design system (CSS + layout) | ✅ Complete |
| GUI-02 | Navigation and controls: easy navigation, working inputs | 2 | Sidebar navigation + module controls | ✅ Complete |
| GUI-03 | Output presentation: readable tables, charts, diagrams | 1 | JavaFX tables, Canvas Gantt, Chart donut | ✅ Complete |
| GUI-04 | Error handling: validates inputs, useful messages | 2 | Centralized validation service | ✅ Complete |
| GUI-05 | Dynamic input flexibility | 3 | No fixed item limits | ✅ Complete |

---

## VI. Algorithm Accuracy and Testing (Section C — 15 pts)

| Req ID | Criterion | Points | Implementation Target | Status |
|--------|-----------|--------|----------------------|--------|
| TST-01 | Algorithm correctness: all algorithms follow correct procedures | 6 | JUnit tests verifying known results | ✅ Complete |
| TST-02 | Test cases: normal, boundary, exceptional; small + larger datasets | 4 | TEST_MATRIX.md with comprehensive coverage | ✅ Complete |
| TST-03 | Result verification: checked against manual/known expected results | 3 | Hand-calculated expected values in tests | ✅ Complete |
| TST-04 | Stability: runs consistently without crashes | 2 | Error-handling tests + stress tests | ✅ Complete |

---

## VII. Source Code and Submission (Section D — 10 pts)

| Req ID | Criterion | Points | Deliverable | Status |
|--------|-----------|--------|-------------|--------|
| SUB-01 | Source code: complete, readable, organized, commented | 3 | Maven project with Javadoc comments | ✅ Complete |
| SUB-02 | Runnable application + dependencies + instructions | 3 | JAR + README with build/run commands | ✅ Complete |
| SUB-03 | Program organization: functions, classes, modules | 2 | Modular package structure | ✅ Complete |
| SUB-04 | Documentation: algorithm descriptions, sample I/O | 2 | docs/ directory with all required docs | ✅ Complete |

---

## VIII. Presentation and Demonstration (Section E — 15 pts)

| Req ID | Criterion | Points | Deliverable | Status |
|--------|-----------|--------|-------------|--------|
| PRS-01 | Project overview: purpose, scope, features | 2 | PRESENTER_GUIDE.md §1 | ⬜ |
| PRS-02 | Live demonstration: GUI + all 8 modules | 5 | Working app + demo sequence | ⬜ |
| PRS-03 | Algorithm explanation: calculations, comparisons, one challenge | 3 | PRESENTER_GUIDE.md §4 | ⬜ |
| PRS-04 | Time management: covers key items in 10 minutes | 2 | Timed presentation plan | ⬜ |
| PRS-05 | Communication: clear, accurate answers to questions | 3 | PRESENTER_GUIDE.md Q&A section | ⬜ |

---

## IX. Understanding of OS Concepts (Section F — 10 pts)

| Req ID | Criterion | Points | Preparation | Status |
|--------|-----------|--------|-------------|--------|
| UND-01 | Conceptual understanding: purpose and behavior of OS concepts | 4 | PRESENTER_GUIDE.md concept explanations | ⬜ |
| UND-02 | Justification of results: explains outputs, compares algorithms | 3 | Per-module comparison features | ⬜ |
| UND-03 | Q&A: independently answers technical questions | 3 | Prepared Q&A bank | ⬜ |

---

## X. Submission Checklist (Section VII)

| Item | Deliverable | Status |
|------|-------------|--------|
| CHK-01 | Java source files | ⬜ |
| CHK-02 | Runnable JAR or packaged application | ⬜ |
| CHK-03 | Libraries/dependencies and setup/run instructions | ⬜ |
| CHK-04 | Documentation describing each module and its algorithms | ⬜ |
| CHK-05 | Sample inputs and outputs or screenshots | ⬜ |
| CHK-06 | Test evidence: small dataset per module | ⬜ |
| CHK-07 | Test evidence: substantially larger dataset per module | ⬜ |
| CHK-08 | GUI supports flexible input without fixed limits | ⬜ |
| CHK-09 | Program validates input and handles errors without crashing | ⬜ |

---

## Score Distribution Summary

| Section | Points | Coverage |
|---------|--------|----------|
| A. Program Development & Implementation | 40 | 8 modules × varied weights |
| B. GUI Design & Usability | 10 | Interface + navigation + output + errors + dynamic input |
| C. Algorithm Accuracy & Testing | 15 | Correctness + test cases + verification + stability |
| D. Source Code & Submission | 10 | Code quality + runnable app + organization + docs |
| E. Presentation & Demonstration | 15 | Overview + demo + algorithm explanation + time + communication |
| F. Understanding of OS Concepts | 10 | Conceptual + justification + Q&A |
| **TOTAL** | **100** | |
