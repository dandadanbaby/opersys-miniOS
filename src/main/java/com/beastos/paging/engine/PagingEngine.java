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
     * Represents a request for translation: either a flat logical address
     * or an explicit (pageNumber, offset) pair.
     */
    public static class PagingRequest {
        private final int pageNumber;
        private final int offset;
        private final int rawLogical;
        private final boolean isPageOffset;

        public PagingRequest(int logicalAddress) {
            this.rawLogical = logicalAddress;
            this.pageNumber = -1;
            this.offset = -1;
            this.isPageOffset = false;
        }

        public PagingRequest(int pageNumber, int offset) {
            this.pageNumber = pageNumber;
            this.offset = offset;
            this.rawLogical = -1;
            this.isPageOffset = true;
        }

        public int getPageNumber() { return pageNumber; }
        public int getOffset() { return offset; }
        public int getRawLogical() { return rawLogical; }
        public boolean isPageOffset() { return isPageOffset; }
    }

    /**
     * Create a paging output with a user-defined page table, translating
     * a list of PagingRequest objects (which can be either raw logical addresses or (page, offset) pairs).
     */
    public PagingOutput translateRequests(int pageSize, int[] pageTable, List<PagingRequest> requests) {
        int totalPages = pageTable.length;
        List<TranslationResult> results = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            PagingRequest req = requests.get(i);
            String reqId = "P" + (i + 1);

            int pageNumber;
            int offset;
            int logical;

            if (req.isPageOffset()) {
                pageNumber = req.getPageNumber();
                offset = req.getOffset();
                logical = (pageNumber >= 0 && pageNumber < totalPages) ? (pageNumber * pageSize + offset) : -1;

                if (pageNumber >= totalPages || pageNumber < 0) {
                    results.add(new TranslationResult(
                            reqId, logical, pageNumber, offset,
                            -1, -1, false,
                            "TRAP: Out of bounds (>" + (totalPages - 1) + ")"
                    ));
                } else if (offset >= pageSize || offset < 0) {
                    results.add(new TranslationResult(
                            reqId, logical, pageNumber, offset,
                            pageTable[pageNumber], -1, false,
                            "TRAP: Offset " + offset + " exceeds Page Size (" + pageSize + ")"
                    ));
                } else {
                    int frame = pageTable[pageNumber];
                    int physicalAddress = frame * pageSize + offset;
                    results.add(new TranslationResult(
                            reqId, logical, pageNumber, offset,
                            frame, physicalAddress, true,
                            "SUCCESS / VALID"
                    ));
                }
            } else {
                logical = req.getRawLogical();
                pageNumber = logical / pageSize;
                offset = logical % pageSize;

                if (pageNumber >= totalPages || pageNumber < 0) {
                    results.add(new TranslationResult(
                            reqId, logical, pageNumber, offset,
                            -1, -1, false,
                            "TRAP: Out of bounds (>" + (totalPages - 1) + ")"
                    ));
                } else {
                    int frame = pageTable[pageNumber];
                    int physicalAddress = frame * pageSize + offset;
                    results.add(new TranslationResult(
                            reqId, logical, pageNumber, offset,
                            frame, physicalAddress, true,
                            "SUCCESS / VALID"
                    ));
                }
            }
        }

        return new PagingOutput(pageSize, totalPages, pageTable, results);
    }

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
        List<PagingRequest> requests = new ArrayList<>();
        for (int addr : logicalAddresses) {
            requests.add(new PagingRequest(addr));
        }
        return translateRequests(pageSize, pageTable, requests);
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
