package com.beastos.multiprogramming;

import com.beastos.multiprogramming.engine.MultiprogrammingEngine;
import com.beastos.multiprogramming.model.MultiprogrammingOutput;
import com.beastos.multiprogramming.model.ProcessStatus;
import com.beastos.multiprogramming.model.ProgramProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MultiprogrammingEngine.
 */
class MultiprogrammingEngineTest {

    private MultiprogrammingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MultiprogrammingEngine();
    }

    @Test
    void testDesignReferenceScenario() {
        // Design reference: 512 MB total RAM, 8 processes
        // P1=64, P2=128, P3=96, P4=128, P5=48, P6=32, P7=64, P8=128
        List<ProgramProcess> processes = List.of(
                new ProgramProcess("P1", 64),
                new ProgramProcess("P2", 128),
                new ProgramProcess("P3", 96),
                new ProgramProcess("P4", 128),
                new ProgramProcess("P5", 48),
                new ProgramProcess("P6", 32),
                new ProgramProcess("P7", 64),
                new ProgramProcess("P8", 128)
        );

        MultiprogrammingOutput output = engine.allocate(512, processes);

        assertEquals(512, output.getTotalMemory());
        assertEquals(496, output.getAllocatedMemory());
        assertEquals(16, output.getFreeMemory());
        assertEquals(96.875, output.getMemoryUtilization(), 0.01);
        assertEquals(6, output.getDegreeOfMultiprogramming());
        assertEquals(2, output.getWaitingCount());
        assertEquals(8, output.getTotalProcesses());

        // Check individual process statuses
        List<ProgramProcess> res = output.getProcesses();
        assertEquals(ProcessStatus.ACTIVE, res.get(0).getStatus());
        assertEquals(448, res.get(0).getRemainingRam());

        assertEquals(ProcessStatus.ACTIVE, res.get(1).getStatus());
        assertEquals(320, res.get(1).getRemainingRam());

        assertEquals(ProcessStatus.ACTIVE, res.get(2).getStatus());
        assertEquals(224, res.get(2).getRemainingRam());

        assertEquals(ProcessStatus.ACTIVE, res.get(3).getStatus());
        assertEquals(96, res.get(3).getRemainingRam());

        assertEquals(ProcessStatus.ACTIVE, res.get(4).getStatus());
        assertEquals(48, res.get(4).getRemainingRam());

        assertEquals(ProcessStatus.ACTIVE, res.get(5).getStatus());
        assertEquals(16, res.get(5).getRemainingRam());

        // P7 and P8 should be WAITING
        assertEquals(ProcessStatus.WAITING, res.get(6).getStatus());
        assertEquals(16, res.get(6).getRemainingRam());

        assertEquals(ProcessStatus.WAITING, res.get(7).getStatus());
        assertEquals(16, res.get(7).getRemainingRam());

        // Verify calibrated CPU & Resource metrics
        assertEquals(72.4, output.getCpuLoad(), 0.5);
        assertEquals(38.0, output.getResourceLoad(), 0.5);
    }

    @Test
    void testAllProcessesFit() {
        List<ProgramProcess> processes = List.of(
                new ProgramProcess("P1", 100),
                new ProgramProcess("P2", 150),
                new ProgramProcess("P3", 200)
        );
        MultiprogrammingOutput output = engine.allocate(1000, processes);

        assertEquals(450, output.getAllocatedMemory());
        assertEquals(550, output.getFreeMemory());
        assertEquals(3, output.getDegreeOfMultiprogramming());
        assertEquals(0, output.getWaitingCount());
        assertTrue(output.getWaitingProcesses().isEmpty());
    }

    @Test
    void testNoneFit() {
        List<ProgramProcess> processes = List.of(
                new ProgramProcess("P1", 200),
                new ProgramProcess("P2", 300)
        );
        MultiprogrammingOutput output = engine.allocate(100, processes);

        assertEquals(0, output.getAllocatedMemory());
        assertEquals(100, output.getFreeMemory());
        assertEquals(0, output.getDegreeOfMultiprogramming());
        assertEquals(2, output.getWaitingCount());
        assertEquals(0.0, output.getMemoryUtilization());
        assertEquals(0.0, output.getCpuLoad());
        assertEquals(0.0, output.getResourceLoad());
    }

    @Test
    void testExactFit100Percent() {
        List<ProgramProcess> processes = List.of(
                new ProgramProcess("P1", 128),
                new ProgramProcess("P2", 128)
        );
        MultiprogrammingOutput output = engine.allocate(256, processes);

        assertEquals(256, output.getAllocatedMemory());
        assertEquals(0, output.getFreeMemory());
        assertEquals(100.0, output.getMemoryUtilization());
        assertEquals(2, output.getDegreeOfMultiprogramming());
        assertEquals(0, output.getWaitingCount());
    }

    @Test
    void testTerminateAndAdmitWaitingProcess() {
        List<ProgramProcess> processes = List.of(
                new ProgramProcess("P1", 64),
                new ProgramProcess("P2", 128),
                new ProgramProcess("P3", 96),
                new ProgramProcess("P4", 128),
                new ProgramProcess("P5", 48),
                new ProgramProcess("P6", 32),
                new ProgramProcess("P7", 64), // was waiting in 512 MB pool
                new ProgramProcess("P8", 128)  // was waiting
        );

        // Terminating P2 frees 128 MB -> active processes are P1 (64), P3 (96), P4 (128), P5 (48), P6 (32) = 368 MB
        // 512 - 368 = 144 MB available -> P7 (64 MB) fits and becomes ACTIVE!
        // Remaining = 144 - 64 = 80 MB. P8 (128 MB) still doesn't fit -> WAITING.
        MultiprogrammingOutput output = engine.terminateAndCompact(512, processes, "P2");

        assertEquals(7, output.getTotalProcesses()); // P2 was removed
        assertEquals(6, output.getDegreeOfMultiprogramming()); // P1, P3, P4, P5, P6, P7
        assertEquals(1, output.getWaitingCount()); // P8

        // Verify P7 is now active
        ProgramProcess p7 = output.getProcesses().stream()
                .filter(p -> p.getPid().equals("P7"))
                .findFirst().orElseThrow();
        assertEquals(ProcessStatus.ACTIVE, p7.getStatus());

        // Verify P8 is still waiting
        ProgramProcess p8 = output.getProcesses().stream()
                .filter(p -> p.getPid().equals("P8"))
                .findFirst().orElseThrow();
        assertEquals(ProcessStatus.WAITING, p8.getStatus());
    }

    @Test
    void testValidation() {
        // Negative RAM
        List<String> err1 = engine.validate(-512, List.of(64, 128));
        assertFalse(err1.isEmpty());

        // Zero RAM
        List<String> err2 = engine.validate(0, List.of(64));
        assertFalse(err2.isEmpty());

        // Empty processes
        List<String> err3 = engine.validate(512, List.of());
        assertFalse(err3.isEmpty());

        // Negative process size
        List<String> err4 = engine.validate(512, List.of(64, -32));
        assertFalse(err4.isEmpty());

        // Valid
        List<String> valid = engine.validate(512, List.of(64, 128, 96));
        assertTrue(valid.isEmpty());
    }

    @Test
    void testLargeWorkload() {
        List<ProgramProcess> largeList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            largeList.add(new ProgramProcess("P" + i, 50));
        }
        // 50 * 50 = 2500 MB needed. In a 1000 MB RAM pool, exactly 20 processes fit.
        MultiprogrammingOutput output = engine.allocate(1000, largeList);

        assertEquals(20, output.getDegreeOfMultiprogramming());
        assertEquals(30, output.getWaitingCount());
        assertEquals(1000, output.getAllocatedMemory());
        assertEquals(0, output.getFreeMemory());
        assertEquals(100.0, output.getMemoryUtilization());
    }
}
