package com.beastos.cpu;

import com.beastos.common.SimulationState;
import com.beastos.common.SimulationSession;
import com.beastos.common.FormatUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1 smoke test — verifies the core infrastructure compiles and works.
 */
class SmokeTest {

    @Test
    void simulationSession_initialState() {
        SimulationSession session = SimulationSession.getInstance();
        assertNotNull(session);
        assertEquals(SimulationState.IDLE, session.getSimulationState());
        assertEquals(0, session.getActiveProcesses());
        assertEquals(0, session.getSimulationsCompleted());
        assertEquals(0.0, session.getCpuUtilization());
    }

    @Test
    void simulationSession_incrementCompleted() {
        SimulationSession session = SimulationSession.getInstance();
        int before = session.getSimulationsCompleted();
        session.incrementSimulationsCompleted();
        assertEquals(before + 1, session.getSimulationsCompleted());
    }

    @Test
    void simulationState_values() {
        assertEquals("IDLE", SimulationState.IDLE.getDisplayName());
        assertEquals("RUNNING", SimulationState.RUNNING.getDisplayName());
        assertEquals("COMPLETED", SimulationState.COMPLETED.getDisplayName());
        assertEquals("PAUSED", SimulationState.PAUSED.getDisplayName());
        assertEquals("READY", SimulationState.READY.getDisplayName());
    }

    @Test
    void formatUtils_percent() {
        assertEquals("72.4%", FormatUtils.formatPercent(72.4));
        assertEquals("0.0%", FormatUtils.formatPercent(0.0));
        assertEquals("100.0%", FormatUtils.formatPercent(100.0));
    }

    @Test
    void formatUtils_zeroPad() {
        assertEquals("08", FormatUtils.zeroPad(8, 2));
        assertEquals("00", FormatUtils.zeroPad(0, 2));
        assertEquals("123", FormatUtils.zeroPad(123, 2));
    }
}
