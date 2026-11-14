package com.beastos.paging.engine;

import com.beastos.paging.model.PagingOutput;
import com.beastos.paging.model.TranslationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Paging engine: manages a page table and translates logical addresses
 * to physical addresses.
 *
 * Formula: Physical Address = (Frame Number × Page Size) + Offset
 * Where:   Page Number = Logical Address / Page Size
 *          Offset      = Logical Address % Page Size
 *
 * Pure Java — no JavaFX dependency.
 */
public class PagingEngine {

    /**
     * Create a paging output with a user-defined page table, then translate
     * a list of logical addresses.
     *
     * @param pageSize         size of each page in bytes (must be > 0)
     * @param pageTable        mapping: pageTable[pageNumber] = frameNumber
     * @param logicalAddresses list of logical addresses to translate
     * @return complete paging output with page table and translation results
     */
    public PagingOutput translate(int pageSize, int[] pageTable, int[] logicalAddresses) {
        int totalPages = pageTable.length;
        List<TranslationResult> results = new ArrayList<>();

        for (int i = 0; i < logicalAddresses.length; i++) {
            int logical = logicalAddresses[i];
            String reqId = "P" + (i + 1);

            int pageNumber = logical / pageSize;
            int offset = logical % pageSize;

            if (pageNumber >= totalPages || pageNumber < 0) {
                // Out of bounds — trap
                results.add(new TranslationResult(
                        reqId, logical, pageNumber, offset,
                        -1, -1, false,
                        "TRAP: Out of bounds (>" + (totalPages - 1) + ")"
                ));
            } else {
                int frame = pageTable[pageNumber];
                int physicalAddress = frame * pageSize + offset;

                // Validate offset < pageSize (always true by modulo, but check boundary)
                results.add(new TranslationResult(
                        reqId, logical, pageNumber, offset,
                        frame, physicalAddress, true,
                        "SUCCESS / VALID"
                ));
            }
        }

        return new PagingOutput(pageSize, totalPages, pageTable, results);
    }

    /**
     * Validate paging input parameters.
     * Returns a list of error messages (empty if valid).
     */
    public List<String> validate(int pageSize, int[] pageTable) {
        List<String> errors = new ArrayList<>();

        if (pageSize <= 0) {
            errors.add("Page size must be a positive integer. Got: " + pageSize);
        }
        if (pageTable == null || pageTable.length == 0) {
            errors.add("Page table must have at least one entry.");
        } else {
            for (int i = 0; i < pageTable.length; i++) {
                if (pageTable[i] < 0) {
                    errors.add("Frame number for page " + i + " cannot be negative.");
                }
            }
        }

        return errors;
    }
}
