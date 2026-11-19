package com.beastos.multiprogramming;

import com.beastos.multiprogramming.engine.MultiprogrammingEngine;
import com.beastos.multiprogramming.model.MultiprogrammingOutput;
import com.beastos.multiprogramming.model.ProgramProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiprogrammingEngineTest {

    private MultiprogrammingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MultiprogrammingEngine();
    }

    @Test
    void testSpreadsheetProcessorSharingScenario() {
        List<ProgramProcess> jobs = List.of(
                new ProgramProcess("j1", 10.0, 0.3),
                new ProgramProcess("j2", 10.2, 0.5),
                new ProgramProcess("j3", 10.4, 0.1),
                new ProgramProcess("j4", 10.5, 0.4),
                new ProgramProcess("j5", 10.8, 0.1)
        );

        MultiprogrammingOutput output = engine.simulate(jobs);

        assertEquals(5, output.getTotalJobs());
        assertEquals(3, output.getMaxConcurrency());
        assertEquals(10.0, output.getStartTime(), 0.001);
        assertEquals(11.4, output.getEndTime(), 0.001);
        assertEquals(100.0, output.getCpuUtilization(), 0.01);

        List<String[]> trace = output.getTraceRows();
        assertEquals("10", trace.get(0)[0]);
        assertEquals("j1 a/s", trace.get(0)[1]);
        assertEquals("11.4", trace.get(trace.size() - 1)[0]);
        assertEquals("j4 t", trace.get(trace.size() - 1)[1]);
    }

    @Test
    void testValidation() {
        assertFalse(engine.validate(List.of()).isEmpty());
        assertFalse(engine.validate(List.of(new ProgramProcess("j1", -1.0, 0.3))).isEmpty());
        assertFalse(engine.validate(List.of(new ProgramProcess("j1", 10.0, 0.0))).isEmpty());
        assertTrue(engine.validate(List.of(new ProgramProcess("j1", 10.0, 0.3))).isEmpty());
    }
}
