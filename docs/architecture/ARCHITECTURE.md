# BEAST OS — Architecture Document

> Last Updated: Phase 0 — 2026-11-14

---

## 1. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 25 LTS |
| GUI Framework | JavaFX | 25 LTS |
| Build System | Apache Maven | 3.9.x + Maven Wrapper |
| Testing | JUnit 5 | 5.11.x |
| Styling | JavaFX CSS | (bundled with JavaFX) |
| Visualization | JavaFX Canvas, Charts, Controls | (bundled with JavaFX) |

---

## 2. Project Structure

```
opersys-miniOS/
│
├── README.md
├── .gitignore
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/com/beastos/
│   │   │   ├── app/                    # Application entry point, main controller
│   │   │   │   ├── BeastOSApp.java            # JavaFX Application subclass
│   │   │   │   ├── MainController.java        # Root scene & navigation controller
│   │   │   │   └── NavigationService.java     # Module view switching
│   │   │   │
│   │   │   ├── common/                 # Shared domain models and utilities
│   │   │   │   ├── SimulationState.java       # Global simulation state (IDLE/READY/RUNNING/etc.)
│   │   │   │   ├── SimulationSession.java     # Session tracking (stats for dashboard)
│   │   │   │   └── FormatUtils.java           # Number/string formatting helpers
│   │   │   │
│   │   │   ├── validation/             # Centralized input validation
│   │   │   │   ├── ValidationResult.java      # Validation outcome model
│   │   │   │   ├── InputValidator.java        # Core validation methods
│   │   │   │   └── ValidationMessages.java    # User-friendly error message constants
│   │   │   │
│   │   │   ├── dashboard/              # Dashboard/Overview module
│   │   │   │   ├── DashboardView.java         # Dashboard UI layout
│   │   │   │   └── DashboardController.java   # Dashboard data binding
│   │   │   │
│   │   │   ├── cpu/                    # CPU Scheduling module
│   │   │   │   ├── model/
│   │   │   │   │   ├── Process.java           # Process domain model
│   │   │   │   │   ├── SchedulingResult.java  # Per-process result (CT, TAT, WT, RT)
│   │   │   │   │   ├── GanttEntry.java        # Gantt chart entry (process, start, end)
│   │   │   │   │   └── SchedulingOutput.java  # Complete algorithm output
│   │   │   │   ├── engine/
│   │   │   │   │   ├── CpuScheduler.java      # Interface for all CPU schedulers
│   │   │   │   │   ├── FcfsScheduler.java     # FCFS implementation
│   │   │   │   │   ├── SjfScheduler.java      # SJF implementation
│   │   │   │   │   ├── PriorityScheduler.java # Priority implementation
│   │   │   │   │   └── RoundRobinScheduler.java # Round Robin implementation
│   │   │   │   └── view/
│   │   │   │       ├── CpuSchedulingView.java # CPU module UI
│   │   │   │       └── GanttChartRenderer.java # Canvas-based Gantt chart
│   │   │   │
│   │   │   ├── banker/                 # Banker's Algorithm module
│   │   │   │   ├── model/
│   │   │   │   │   ├── BankerState.java       # Allocation, Max, Need, Available
│   │   │   │   │   └── SafetyResult.java      # Safe/unsafe + sequence + steps
│   │   │   │   ├── engine/
│   │   │   │   │   └── BankerEngine.java      # Safety algorithm implementation
│   │   │   │   └── view/
│   │   │   │       └── BankerView.java        # Banker module UI
│   │   │   │
│   │   │   ├── paging/                 # Paging module
│   │   │   │   ├── model/
│   │   │   │   │   ├── PageTable.java         # Page table model
│   │   │   │   │   └── TranslationResult.java # Translation outcome
│   │   │   │   ├── engine/
│   │   │   │   │   └── PagingEngine.java      # Address translation logic
│   │   │   │   └── view/
│   │   │   │       └── PagingView.java        # Paging module UI
│   │   │   │
│   │   │   ├── replacement/            # Page Replacement module
│   │   │   │   ├── model/
│   │   │   │   │   ├── FrameState.java        # Frame contents at each step
│   │   │   │   │   └── ReplacementResult.java # Hits, faults, per-step details
│   │   │   │   ├── engine/
│   │   │   │   │   ├── PageReplacer.java      # Interface
│   │   │   │   │   ├── FifoReplacer.java      # FIFO implementation
│   │   │   │   │   ├── LruReplacer.java       # LRU implementation
│   │   │   │   │   └── OptimalReplacer.java   # Optimal implementation
│   │   │   │   └── view/
│   │   │   │       └── PageReplacementView.java
│   │   │   │
│   │   │   ├── segmentation/           # Segmentation module
│   │   │   │   ├── model/
│   │   │   │   │   ├── Segment.java           # Segment (base, limit)
│   │   │   │   │   └── SegmentResult.java     # Translation or fault result
│   │   │   │   ├── engine/
│   │   │   │   │   └── SegmentationEngine.java
│   │   │   │   └── view/
│   │   │   │       └── SegmentationView.java
│   │   │   │
│   │   │   ├── fragmentation/          # Fragmentation module
│   │   │   │   ├── model/
│   │   │   │   │   ├── MemoryBlock.java       # Block model
│   │   │   │   │   └── AllocationResult.java  # Allocation outcome + fragmentation
│   │   │   │   ├── engine/
│   │   │   │   │   ├── MemoryAllocator.java   # Interface
│   │   │   │   │   ├── FirstFitAllocator.java
│   │   │   │   │   ├── BestFitAllocator.java
│   │   │   │   │   └── WorstFitAllocator.java
│   │   │   │   └── view/
│   │   │   │       └── FragmentationView.java
│   │   │   │
│   │   │   ├── disk/                   # Disk Scheduling module
│   │   │   │   ├── model/
│   │   │   │   │   ├── DiskRequest.java       # Request model
│   │   │   │   │   └── DiskResult.java        # Service order + movements
│   │   │   │   ├── engine/
│   │   │   │   │   ├── DiskScheduler.java     # Interface
│   │   │   │   │   ├── FcfsDiskScheduler.java
│   │   │   │   │   ├── SstfDiskScheduler.java
│   │   │   │   │   ├── ScanDiskScheduler.java
│   │   │   │   │   ├── CScanDiskScheduler.java
│   │   │   │   │   ├── LookDiskScheduler.java
│   │   │   │   │   └── CLookDiskScheduler.java
│   │   │   │   └── view/
│   │   │   │       ├── DiskSchedulingView.java
│   │   │   │       └── SeekTrajectoryRenderer.java
│   │   │   │
│   │   │   └── multiprogramming/       # Multiprogramming module
│   │   │       ├── model/
│   │   │       │   ├── MpProcess.java         # Process with memory/resource needs
│   │   │       │   ├── MpState.java           # Process state enum
│   │   │       │   └── MpSimulationResult.java
│   │   │       ├── engine/
│   │   │       │   └── MultiprogrammingEngine.java
│   │   │       └── view/
│   │   │           └── MultiprogrammingView.java
│   │   │
│   │   └── resources/
│   │       ├── css/
│   │       │   ├── beast-os-theme.css         # Master design system
│   │       │   └── module-common.css          # Shared module styles
│   │       ├── images/
│   │       │   └── beast-os-logo.png          # Application logo
│   │       └── icons/                         # Module and UI icons
│   │
│   └── test/java/com/beastos/
│       ├── cpu/
│       │   ├── FcfsSchedulerTest.java
│       │   ├── SjfSchedulerTest.java
│       │   ├── PrioritySchedulerTest.java
│       │   └── RoundRobinSchedulerTest.java
│       ├── banker/
│       │   └── BankerEngineTest.java
│       ├── paging/
│       │   └── PagingEngineTest.java
│       ├── replacement/
│       │   ├── FifoReplacerTest.java
│       │   ├── LruReplacerTest.java
│       │   └── OptimalReplacerTest.java
│       ├── segmentation/
│       │   └── SegmentationEngineTest.java
│       ├── fragmentation/
│       │   ├── FirstFitAllocatorTest.java
│       │   ├── BestFitAllocatorTest.java
│       │   └── WorstFitAllocatorTest.java
│       ├── disk/
│       │   ├── FcfsDiskSchedulerTest.java
│       │   ├── SstfDiskSchedulerTest.java
│       │   ├── ScanDiskSchedulerTest.java
│       │   ├── CScanDiskSchedulerTest.java
│       │   ├── LookDiskSchedulerTest.java
│       │   └── CLookDiskSchedulerTest.java
│       └── multiprogramming/
│           └── MultiprogrammingEngineTest.java
│
├── docs/
│   ├── PROGRESS.md
│   ├── rubric/
│   │   ├── RUBRIC_TRACEABILITY.md
│   │   └── (RUBRICS-OS.pdf — reference, in Rubrics/)
│   ├── architecture/
│   │   ├── ARCHITECTURE.md
│   │   ├── DESIGN_REFERENCE_AUDIT.md
│   │   └── ASSUMPTIONS.md
│   ├── testing/
│   │   └── TEST_MATRIX.md
│   └── presentation/
│       └── PRESENTER_GUIDE.md
│
├── Design References/                  # Original user-supplied designs (preserved)
├── Java FIles/                         # Original Java implementations (preserved, reference only)
└── Rubrics/
    └── RUBRICS-OS.pdf                  # Official professor rubric
```

---

## 3. Architectural Principles

### 3.1 Separation of Concerns

```
┌─────────────────────────────────────────────────────────────┐
│                    JavaFX Presentation Layer                  │
│  (Views, Controllers, CSS, FXML)                             │
│  - Receives user input                                       │
│  - Delegates to engine layer                                 │
│  - Displays results via bindings/updates                     │
├─────────────────────────────────────────────────────────────┤
│                    Service / Coordination Layer               │
│  (NavigationService, SimulationSession, ValidationService)   │
│  - Coordinates between UI and engines                        │
│  - Manages simulation state                                  │
│  - Centralizes validation                                    │
├─────────────────────────────────────────────────────────────┤
│                    Algorithm Engine Layer                     │
│  (CpuScheduler, BankerEngine, PagingEngine, etc.)           │
│  - Pure Java — NO JavaFX dependency                          │
│  - Testable without GUI                                      │
│  - Stateless: input → output                                 │
├─────────────────────────────────────────────────────────────┤
│                    Domain Model Layer                         │
│  (Process, Segment, MemoryBlock, GanttEntry, etc.)          │
│  - Plain Java objects                                        │
│  - Immutable where appropriate                               │
└─────────────────────────────────────────────────────────────┘
```

**Critical rule:** Algorithm engines must NOT import any `javafx.*` packages. They must be independently testable via JUnit without launching the JavaFX application.

### 3.2 Design Patterns Used

| Pattern | Usage |
|---------|-------|
| **Strategy** | Algorithm selection (CpuScheduler interface with FCFS/SJF/Priority/RR implementations; DiskScheduler interface with 6 implementations; MemoryAllocator with 3 implementations; PageReplacer with 3 implementations) |
| **MVC** | Each module has Model (domain), Engine (logic), View (JavaFX UI) |
| **Observer** | SimulationState observable for dashboard stat updates |
| **Factory Method** | Algorithm instantiation based on user selection |
| **Singleton** | SimulationSession (application-wide state) |

### 3.3 Navigation Architecture

```
BeastOSApp (Application)
  └── MainController (root BorderPane)
       ├── LEFT:   SidebarView (fixed, always visible)
       ├── TOP:    HeaderBar (breadcrumb, clock, status)
       └── CENTER: ContentArea (swapped per navigation)
                    ├── DashboardView (default)
                    ├── CpuSchedulingView
                    ├── BankerView
                    ├── PagingView
                    ├── PageReplacementView
                    ├── SegmentationView
                    ├── FragmentationView
                    ├── DiskSchedulingView
                    └── MultiprogrammingView
```

Navigation is handled by `NavigationService` which:
1. Maintains a reference to the center content area
2. Lazily creates or caches module views
3. Updates sidebar highlighting
4. Updates header breadcrumb
5. Updates simulation state

### 3.4 CSS Design System

All styling centralized in `beast-os-theme.css`:

```css
/* Core variables (as looked-up colors in JavaFX CSS) */
-fx-beast-bg-primary:     #0d1117;    /* Page background */
-fx-beast-bg-secondary:   #161b22;    /* Sidebar background */
-fx-beast-bg-card:        #1c2333;    /* Card/panel background */
-fx-beast-accent-primary: #2dd4bf;    /* Turquoise accent */
-fx-beast-accent-amber:   #f59e0b;    /* Amber accent */
-fx-beast-accent-green:   #22c55e;    /* Success/green */
-fx-beast-accent-red:     #ef4444;    /* Error/red */
-fx-beast-accent-blue:    #3b82f6;    /* Info/blue */
-fx-beast-text-primary:   #f8fafc;    /* Primary text */
-fx-beast-text-secondary: #94a3b8;    /* Secondary text */
-fx-beast-border:         #334155;    /* Subtle borders */
-fx-beast-input-bg:       #1e293b;    /* Input field background */
```

---

## 4. Build Configuration (Maven)

### Key Dependencies
- `org.openjfx:javafx-controls:25`
- `org.openjfx:javafx-fxml:25`
- `org.junit.jupiter:junit-jupiter:5.11.x`

### Key Plugins
- `org.openjfx:javafx-maven-plugin` — Run with `mvnw javafx:run`
- `maven-compiler-plugin` — Java 25 source/target
- `maven-surefire-plugin` — JUnit 5 test execution
- `maven-jar-plugin` — Executable JAR
- `maven-shade-plugin` or `jlink` — Self-contained distributable

---

## 5. Threading Model

- **JavaFX Application Thread:** All UI updates
- **Background threads (via `Task<>`):** Long-running computations (large datasets)
- **Platform.runLater():** Post-computation UI updates
- Rule: Never block the JavaFX thread with algorithm computation for large inputs

---

## 6. Data Flow Per Module

```
User Input (JavaFX Controls)
    ↓
InputValidator.validate(input)
    ↓ (if valid)
AlgorithmEngine.execute(validatedInput)
    ↓
Result (domain model)
    ↓
View.displayResults(result)
    ↓
SimulationSession.recordCompletion()
    ↓
Dashboard.updateStats()
```
