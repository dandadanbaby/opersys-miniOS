package com.beastos.banker;

import com.beastos.banker.engine.BankerEngine;
import com.beastos.banker.model.BankerState;
import com.beastos.banker.model.SafetyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Banker's Safety Algorithm.
 */
class BankerEngineTest {

    private BankerEngine engine;

    @BeforeEach
    void setUp() {
        engine = new BankerEngine();
    }

    @Test
    void testDesignReferenceCase_5P_3R_Safe() {
        // From the BEAST OS design reference:
        // 5 processes, 3 resources (A, B, C)
        // Available: [3, 3, 2]
        // Expected safe sequence: P1 → P3 → P4 → P0 → P2
        int[][] allocation = {
                {0, 1, 0},  // P0
                {2, 0, 0},  // P1
                {3, 0, 2},  // P2
                {2, 1, 1},  // P3
                {0, 0, 2}   // P4
        };
        int[][] maximum = {
                {7, 5, 3},  // P0
                {3, 2, 2},  // P1
                {9, 0, 2},  // P2
                {2, 2, 2},  // P3
                {4, 3, 3}   // P4
        };
        int[] available = {3, 3, 2};

        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);

        assertTrue(result.isSafe(), "System should be in a SAFE state");
        assertEquals(5, result.getSafeSequence().size(), "All 5 processes must be in safe sequence");
        assertTrue(result.getUnfinishedProcesses().isEmpty(), "No unfinished processes");

        // Verify the safe sequence matches design: P1, P3, P4, P0, P2
        List<String> seq = result.getSafeSequence();
        assertEquals("P1", seq.get(0));
        assertEquals("P3", seq.get(1));
        assertEquals("P4", seq.get(2));
        assertEquals("P0", seq.get(3));
        assertEquals("P2", seq.get(4));

        // Verify step count
        assertEquals(5, result.getSteps().size());
    }

    @Test
    void testUnsafeState() {
        // Deliberately make an unsafe state
        int[][] allocation = {
                {0, 1, 0},
                {2, 0, 0},
                {3, 0, 2},
                {2, 1, 1},
                {0, 0, 2}
        };
        int[][] maximum = {
                {7, 5, 3},
                {3, 2, 2},
                {9, 0, 2},
                {2, 2, 2},
                {4, 3, 3}
        };
        // Available too low — no process can finish
        int[] available = {0, 0, 0};

        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);

        assertFalse(result.isSafe(), "System should be UNSAFE");
        assertFalse(result.getUnfinishedProcesses().isEmpty(), "Should have unfinished processes");
    }

    @Test
    void testTrivial_1P_1R() {
        int[][] allocation = {{1}};
        int[][] maximum = {{3}};
        int[] available = {2};

        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);

        assertTrue(result.isSafe());
        assertEquals(1, result.getSafeSequence().size());
        assertEquals("P0", result.getSafeSequence().get(0));
    }

    @Test
    void testTrivial_1P_1R_Unsafe() {
        int[][] allocation = {{1}};
        int[][] maximum = {{3}};
        int[] available = {1}; // Need = 2, but only 1 available

        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);

        assertFalse(result.isSafe());
        assertEquals(List.of("P0"), result.getUnfinishedProcesses());
    }

    @Test
    void testValidation_AllocationExceedsMax() {
        int[][] allocation = {{5, 0}, {0, 0}};
        int[][] maximum = {{3, 0}, {0, 0}};
        int[] available = {1, 1};

        List<String> errors = engine.validate(allocation, maximum, available);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("exceeds Maximum"));
    }

    @Test
    void testValidation_NegativeValues() {
        int[][] allocation = {{-1, 0}};
        int[][] maximum = {{3, 0}};
        int[] available = {1, 1};

        List<String> errors = engine.validate(allocation, maximum, available);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("negative"));
    }

    @Test
    void testValidation_MismatchedDimensions() {
        int[][] allocation = {{1, 0}, {0, 0}};
        int[][] maximum = {{3, 0}}; // only 1 row
        int[] available = {1, 1};

        List<String> errors = engine.validate(allocation, maximum, available);
        assertFalse(errors.isEmpty());
    }

    @Test
    void testValidation_EmptyAllocation() {
        int[][] allocation = {};
        int[][] maximum = {};
        int[] available = {};

        List<String> errors = engine.validate(allocation, maximum, available);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("At least one process"));
    }

    @Test
    void testNeedCalculation() {
        int[][] allocation = {{1, 2}, {3, 4}};
        int[][] maximum = {{5, 6}, {7, 8}};
        int[] available = {1, 1};

        BankerState state = new BankerState(allocation, maximum, available);
        int[][] need = state.getNeed();
        assertEquals(4, need[0][0]); // 5-1
        assertEquals(4, need[0][1]); // 6-2
        assertEquals(4, need[1][0]); // 7-3
        assertEquals(4, need[1][1]); // 8-4
    }

    @Test
    void testLargeInput_100P_10R() {
        int n = 100, m = 10;
        int[][] allocation = new int[n][m];
        int[][] maximum = new int[n][m];
        int[] available = new int[m];

        // Set up so each process has allocation=1 per resource, max=2
        // Available = n*1 (plenty of resources → safe)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                allocation[i][j] = 1;
                maximum[i][j] = 2;
            }
        }
        for (int j = 0; j < m; j++) {
            available[j] = n; // generous availability
        }

        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);

        assertTrue(result.isSafe());
        assertEquals(n, result.getSafeSequence().size());
        assertEquals(n, result.getSteps().size());
    }

    @Test
    void testStepVerificationLog() {
        // Simple 2P×1R case
        int[][] allocation = {{1}, {2}};
        int[][] maximum = {{3}, {4}};
        int[] available = {2};

        BankerState state = new BankerState(allocation, maximum, available);
        SafetyResult result = engine.checkSafety(state);

        assertTrue(result.isSafe());
        // Verify steps have meaningful descriptions
        for (SafetyResult.SafetyStep step : result.getSteps()) {
            assertNotNull(step.getDescription());
            assertTrue(step.getDescription().length() > 0);
            assertTrue(step.isNeedLessOrEqualWork());
            assertNotNull(step.getWorkAfter());
        }
    }
}
