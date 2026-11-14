# BEAST OS — Design Reference Audit

> Last Updated: Phase 0 — 2026-11-14

---

## Inventory of Supplied Design References

All files located in `Design References/` directory.

| # | File Path | Type | Dimensions | Module | Inspected | Used As |
|---|-----------|------|-----------|--------|-----------|---------|
| 1 | `Overview/overview-dashboard.png` | PNG | Full-screen | Dashboard / Overview | ✅ Yes | **Primary** visual specification for dashboard layout, sidebar, header, cards, Gantt chart, donut chart, and module navigation grid |
| 2 | `Modules/cpu-scheduling.png` | JPEG | Full-screen | CPU Scheduling | ✅ Yes | CPU module layout: 4 stat cards, process input table, algorithm tabs, Gantt chart, results table |
| 3 | `Modules/banker-algorithm.png` | JPEG | Full-screen | Banker's Algorithm | ✅ Yes | Banker module layout: stat cards, 3 matrices (Allocation/Max/Need), Available vector, execution verification log |
| 4 | `Modules/paging.png` | JPEG | Full-screen | Paging | ✅ Yes | Paging module layout: stat cards, page map table, translation flowchart, verification log |
| 5 | `Modules/page-replacement.png` | JPEG | Full-screen | Page Replacement | ✅ Yes | Page replacement layout: stat cards, frame state evolution grid, step-by-step table, algorithm comparison tracker |
| 6 | `Modules/segmentation.png` | JPEG | Full-screen | Segmentation | ✅ Yes | Segmentation layout: stat cards, segment table, physical memory map, address validation & trap log |
| 7 | `Modules/fragmentation.png` | JPEG | Full-screen | Fragmentation | ✅ Yes | Fragmentation layout: stat cards, allocation table with algorithm tabs, proportional block partition, analysis summary |
| 8 | `Modules/disk-scheduling.png` | JPEG | Full-screen | Disk Scheduling | ✅ Yes | Disk scheduling layout: stat cards, request queue with algorithm tabs, sequence flow, 2D trajectory chart, distance table |
| 9 | `Modules/multiprogramming.png` | JPEG | Full-screen | Multiprogramming | ✅ Yes | Multiprogramming layout: stat cards, process resource sharing table, system resources donut, live memory partition map |

**Total design references: 9 files (1 dashboard + 8 modules)**
**Missing files: NONE** — All 8 modules and the dashboard have complete design references.

---

## Dashboard Design Analysis (overview-dashboard.png)

### A. Left Sidebar
- **Logo:** BEAST OS dragon/beast icon (turquoise/teal), with "BEAST" in white bold and "OS" in smaller turquoise text
- **WORKSPACE section:** "Overview" item with icon, highlighted with turquoise left-border indicator and dark teal background
- **MODULES section:** 8 module items, each with unique icon:
  - CPU Scheduling (terminal/monitor icon)
  - Banker's Algorithm (person/shield icon)
  - Paging (grid/page icon)
  - Page Replacement (document icon)
  - Segmentation (grid/matrix icon)
  - Fragmentation (fragment icon)
  - Disk Scheduling (disk/circle icon)
  - Multiprogramming (gears/process icon)
- **Bottom panel:** "LOCAL WORKSPACE" with green dot "All systems operational", "BEAST OS v1.0.0"
- **Background:** Dark navy (#0d1117 to #161b22 range)

### B. Top Header Bar
- Left: "WORKSPACE / Overview" breadcrumb
- Right: "System Time: 00:45:15" in rounded pill/badge
- Far right: "OS Simulation Status: ACTIVE" in bordered pill

### C. Main Content Header
- Turquoise small-caps label: "OPERATING SYSTEM WORKBENCH"
- Large white title: "System Overview"
- Subtitle: "COMMAND CENTER"
- Right-aligned: "New Simulation" button (outlined, rounded)

### D. Four Statistics Cards (horizontal row)
1. **ACTIVE PROCESSES:** "08" (large) + "+2 this session" (green) + process icon
2. **CPU UTILIZATION:** "72.4%" + "Within normal range" (green) + chart icon
3. **MEMORY IN USE:** "61.8%" + "4.9 out of 8GB" + checkmark icon (turquoise)
4. **SIMULATION COMPLETED:** "24" + "Across 8 modules" (green) + checkmark icon (green)

Card styling: Dark blue-gray background (#1c2333 range), subtle border, rounded corners (~8-12px)

### E. Two-Panel Visualization Area
- **Left panel (≈60% width): CPU Execution Timeline**
  - "Round Robin • Time Quantum: 4ms"
  - Gantt-style horizontal bars: P1-P4 with color-coded execution blocks
  - Colors: green (P1), amber/gold (P2), teal (P3), dark-teal (P4)
  - Time axis: 0ms to 24ms with 4ms intervals
  
- **Right panel (≈40% width): System Resources**
  - "Live Utilization Overview"
  - Donut chart with center: "72% CPU LOAD"
  - Legend: CPU 72.4% (teal), Memory 61.8% (amber), Resources 38.0% (blue)

### F. Simulation Models Grid (4×2)
- Title: "Simulation Models" + "Choose a workspace module"
- 8 cards in 4-column × 2-row grid
- Each card: icon + title + description + arrow (→) indicator
- Cards:
  1. CPU Scheduling — "Scheduling prices & Gantt Charts"
  2. Banker's Algorithm — "Safety checks & resource matrices"
  3. Paging — "Page tables & address mapping"
  4. Page Replacement — "FIFO · LRU · Optimal"
  5. Segmentation — "Segment tables & translation"
  6. Fragmentation — "Memory allocation strategies"
  7. Disk Scheduling — "Seek order & head movement"
  8. Multiprogramming — "Process & resource sharing"

---

## Module Design Analysis

### CPU Scheduling (cpu-scheduling.png)
- **Header:** Same sidebar + top bar pattern. Breadcrumb: "WORKSPACE / CPU Scheduling"
- **Stat cards (4):** ACTIVE PROCESSES (05), AVG WAITING TIME (5.80 ms), AVG TURNAROUND TIME (11.40 ms), CPU UTILIZATION (92.4%)
- **Left panel:** "Process Input Table" with algorithm tabs: FCFS | SJF | **Round Robin** (active) | Priority | Config: "Time Quantum: 4ms"
  - Columns: Process ID, Arrival Time, Burst Time, Priority
  - 4 sample processes (P1-P4)
  - "Confirm" button at bottom
  - "+ Add Process" button
- **Right panel:** "CPU execution timeline — Visual Gantt Chart Scheduler"
  - Gantt chart with color-coded blocks per process
  - Below: "Scheduling Calculation Results" table with columns: Process, Arrival Time, Burst Time, Priority, Completion Time, Turnaround Time (TAT), Waiting Time (WT)
  - Footer: "Average Calculation: Average Waiting Time: 5.80 ms | Average Turnaround Time: 11.40 ms"

### Banker's Algorithm (banker-algorithm.png)
- **Stat cards (4):** PROCESSES (05), RESOURCE TYPES (03), SYSTEM STATUS (SAFE — green), SAFE SEQUENCE (P1→P3→P4→P0→P2 — green with arrows)
- **Three matrices side-by-side:**
  1. Allocation Matrix (editable cells, 5×3: P0-P4 × A,B,C)
  2. Max Demand Matrix (editable cells, highlighted in teal)
  3. Need Matrix (Max − Alloc) — displayed as computed, cells in teal
- **Bottom section:** "Available Vector & Safety Verification Log"
  - Left: Resource Available Vector [A: 3, B: 3, C: 2], System Max A: 10, B: 5, C: 7
  - Right: Execution Verification Log — step-by-step safety check showing Work comparisons and process execution
  - Green banner: "Safety check complete. System is in a SAFE STATE."

### Paging (paging.png)
- **Stat cards (4):** PAGE SIZE (4096 B), LOGICAL PAGES (08), MAPPED FRAME (Frame 3), PHYSICAL ADDRESS (14,348)
- **Left panel:** "Page Map Table (Page No. -> Frame No.)"
  - 8 rows (P0-P7) with Frame Number input fields
  - P3 row highlighted (active selection)
  - "Confirm" button
- **Right panel:** "Logical-to-Physical Address Translation — Flowchart and computational formula"
  - Input: "Page: 3" → "Offset: 2060"
  - Page Table Check visualization: Page Number 3 → Index 3 → Frame Number 3
  - Formula: (3 × 4096) + 2060 = 14,348 (Offset 2060 < Page Size 4096 VALID)
  - Result: "PHYSICAL ADDRESS: 14,348"
- **Bottom:** "Address Translation Verification Log" — table with columns: Req ID, Logical Address, Page No., Offset, Mapped Frame, Physical Address, Validation Status
  - Includes SUCCESS and TRAP examples

### Page Replacement (page-replacement.png)
- **Stat cards (4):** TOTAL REQUESTS (15), PAGE HITS (06) with green dot, PAGE FAULTS (09) with red dot, HIT RATIO (40.0%) with circular progress
- **Frame State Evolution grid:** Column-per-reference showing PAGE row, FRAME 0-2 rows, STATUS (FAULT/HIT) row
  - Reference string: 7,0,1,2,0,3,0,4,2,3,0,3,2,1,2
  - Color-coded: faults in coral/red badges, hits in teal/green badges
- **Bottom left:** "Step-by-Step State Transition Table" — Page, Frames State (array), Status
- **Bottom right:** "Algorithm Comparison Tracker"
  - FIFO: 40.0% Hit Ratio (with bar)
  - LRU: 46.7% Hit Ratio (with bar)
  - Optimal: 53.3% Hit Ratio (with bar)

### Segmentation (segmentation.png)
- **Stat cards (4):** TOTAL SEGMENTS (05), TARGET SEGMENT (Seg 1), VALIDATION (VALID — green), PHYSICAL ADDRESS (6620)
- **Left panel:** "Segment Table — Segment Table Configuration (Segment | Base Address | Limit)"
  - 5 rows (Seg 0-4) with Base Address and Limit(Length) input fields
  - Seg 1 row highlighted
- **Right panel:** "Physical Memory Map — Segments mapped proportionally in RAM"
  - Vertical bar chart showing segments at their base addresses with limits
  - Address markers on Y-axis (1400, 2000, 4300, 6300, 7200)
  - Arrow pointing to 6620 physical address
- **Bottom:** "Logical Address Validation & Trap Log"
  - Two SUCCESS translations: (Seg 1, Offset 320) → 6620, (Seg 1, Offset 10) → 6310
  - Red SEGMENTATION FAULT banner: "Offset Exceeds Limit. Attempt to access Seg 1 at Offset 450 exceeds limit (400). Execution halted."

### Fragmentation (fragmentation.png)
- **Stat cards (4):** MEMORY BLOCKS (05), ALLOCATED PROCESSES (05/06, "01 in queue"), TOTAL INTERNAL FRAG (150 KB — amber), EXTERNAL FRAG (120 KB)
- **Left panel:** "Memory Allocation Table" with tabs: **First-Fit** | Best-Fit | Worst-Fit
  - Columns: Process ID, Process Size, Status, Block Used, Internal Frag
  - 6 processes (P1-P6): P1-P4 + P6 Allocated, P5 Waiting
  - "+ Add Process" button
- **Right panel:** "Proportional Memory Block Partition"
  - Legend: Allocated Space (Teal), Internal Frag (Amber), Free Blocks (Steel Blue)
  - 5 blocks with proportional rendering showing allocated + internal frag + free
  - External Frag row at bottom
- **Bottom:** "Fragmentation Analysis Summary — ANALYSIS LOG"
  - [SUCCESS], [INFO], [ALERT] log entries
  - Right side: 91.8% Allocated Space, Internal Frag: 150 KB, External Frag: 120 KB (in stat badges)

### Disk Scheduling (disk-scheduling.png)
- **Stat cards (4):** ACTIVE PROCESS / INITIAL HEAD (53), CYLINDER LIMIT (199), TOTAL HEAD MOVEMENT (236 Cylinders), AVG SEEK DISTANCE (29.5)
- **Left panel:** "Track Request Queue & Service Order"
  - Algorithm tabs: FCFS | **SSTF** (active) | SCAN | C-SCAN | LOOK | C-LOOK
  - Initial Position: 53, Request queue: [98, 183, 37, 122, 14, 124, 65, 67]
  - "Service Order Sequence Flow" — visual chain: 53→65→67→37→14→...→98→122→124→183
  - "Restart Simulation" + "Confirm" buttons
- **Right panel:** "2D Disk Head Seek Trajectory"
  - X-axis: Cylinder Number (0-199), Y-axis: Sequence Step (0-7, DESCENDING)
  - Line chart with labeled points
- **Bottom right:** "Step-by-Step Seek Distance Calculation Table"
  - Columns: Step, Sequence Transition, Previous Cyl, Target Cyl, Calculated Distance
  - Footer: "Total Distance Calculation: 12+2+30+23+84+24+2+59 = 236 cylinders | Average Seek: 29.5 cylinders"

### Multiprogramming (multiprogramming.png)
- **Stat cards (4):** ACTIVE PROCESSES (06/08, "+2 in queue"), TOTAL SYSTEM RAM (512 MB, "Static pool size"), MEMORY ALLOCATED (496 MB), MEMORY UTILIZATION (96.8%, "High utilization" — green checkmark)
- **Left panel:** "Process Resource Sharing Table"
  - Columns: Process, Memory Needed, Status, Remaining RAM
  - 8 processes (P1-P8): P1-P6 "Active in Memory" (green dots), P7-P8 "Waiting (RAM Full)" (orange dots)
  - Remaining RAM decreasing from 448 to 16
- **Right panel top:** "System Resources" donut chart
  - Center: "96.8% RAM LOAD"
  - Legend: CPU 72.4%, Memory 96.8%, Resources 38.0%
- **Right panel bottom:** "Live Memory Partition Map — RAM distribution for active processes and free space capacity"
  - Horizontal stacked bar: P1-P6 blocks + Free (16 MB)

---

## Design Consistency Observations

### Consistent Elements Across All Screens
1. **Sidebar:** Identical structure, active module highlighted with turquoise left-border
2. **Top header:** "WORKSPACE / [Module Name]" breadcrumb, System Time (00:45:15), OS Simulation Status: ACTIVE
3. **Content header:** Turquoise "OPERATING SYSTEM WORKBENCH" label, module title, "COMMAND CENTER" subtitle, "New Simulation" button
4. **Stat cards:** Always 4 cards in a row, dark panel backgrounds, icons in top-right, value + description pattern
5. **Bottom status:** "LOCAL WORKSPACE" with green dot, "BEAST OS v1.0.0"

### Color Palette (extracted from screenshots)
| Element | Color | Approx Hex |
|---------|-------|-----------|
| Page background | Deep navy | #0d1117 |
| Sidebar background | Slightly lighter navy | #161b22 |
| Card/panel background | Dark blue-gray | #1c2333 |
| Primary accent (active nav, labels) | Turquoise/cyan | #00d4aa / #2dd4bf |
| Secondary accent (highlights) | Amber/gold | #f0a500 / #eab308 |
| Success/positive | Green | #22c55e |
| Error/fault | Coral/red | #ef4444 |
| Chart color 1 | Teal | #14b8a6 |
| Chart color 2 | Amber | #f59e0b |
| Chart color 3 | Blue | #3b82f6 |
| Primary text | White | #ffffff / #f8fafc |
| Secondary text | Muted gray | #94a3b8 |
| Border/outline | Subtle dark | #2d3748 / #334155 |
| Input field background | Dark teal-gray | #1e293b |

### Typography
- Module titles: Large bold white (≈24-28px equivalent)
- Stat card values: Extra-large bold (≈32-40px equivalent)
- Stat card labels: Small caps, muted gray (≈12px)
- Body text: Regular white (≈14px)
- Secondary descriptions: Muted gray (≈12-13px)

### Spacing & Layout
- Sidebar width: ≈200px
- Card border-radius: ≈8-12px
- Card internal padding: ≈16-20px
- Gap between cards: ≈16px
- Section vertical spacing: ≈24px

---

## Design Extensions Needed

No module designs are missing. All 8 modules + dashboard have complete visual specifications.

Minor extension decisions:
1. **Algorithm selection workflow:** Some modules show tabs for algorithm selection (CPU, Disk, Fragmentation, Page Replacement). Others show results directly. The input → compute → results flow is clear from designs.
2. **New Simulation button behavior:** Must initiate a meaningful workflow (reset/configure). Not purely decorative.
3. **Empty states:** Dashboard and modules need attractive zero/empty states before any simulation runs.
4. **Error dialogs vs. inline errors:** Segmentation design shows inline error (red banner). This pattern should be used consistently.
5. **Responsive behavior:** Designs are at desktop resolution. Must handle different window sizes gracefully.
