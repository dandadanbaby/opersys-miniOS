package com.beastos.paging.engine;

import com.beastos.paging.model.PagingOutput;
import com.beastos.paging.model.TranslationResult;
import com.beastos.paging.model.TwoLevelPagingOutput;
import com.beastos.paging.model.TwoLevelTranslationResult;

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

    /**
     * Request for hierarchical two-level translation: either a flat logical address
     * or explicit (outerPage, innerPage, offset) coordinates.
     */
    public static class TwoLevelPagingRequest {
        private final int rawLogical;
        private final int outerPage;
        private final int innerPage;
        private final int offset;
        private final boolean isTuple;

        public TwoLevelPagingRequest(int logicalAddress) {
            this.rawLogical = logicalAddress;
            this.outerPage = -1;
            this.innerPage = -1;
            this.offset = -1;
            this.isTuple = false;
        }

        public TwoLevelPagingRequest(int outerPage, int innerPage, int offset) {
            this.outerPage = outerPage;
            this.innerPage = innerPage;
            this.offset = offset;
            this.rawLogical = -1;
            this.isTuple = true;
        }

        public int getRawLogical() { return rawLogical; }
        public int getOuterPage() { return outerPage; }
        public int getInnerPage() { return innerPage; }
        public int getOffset() { return offset; }
        public boolean isTuple() { return isTuple; }
    }

    /**
     * Perform hierarchical two-level address translation.
     *
     * Logical Address is partitioned into:
     *   [ Outer Page p1 | Inner Page p2 | Offset d ]
     *
     * @param pageSize        size of each page in bytes
     * @param innerTableSize  number of pages per inner page table
     * @param pageDirectory   pageDirectory[p1][p2] = physical frame number
     * @param requests        list of translation requests
     * @return TwoLevelPagingOutput with step results and verification
     */
    public TwoLevelPagingOutput translateTwoLevel(int pageSize, int innerTableSize, int[][] pageDirectory,
                                                  List<TwoLevelPagingRequest> requests) {
        int outerCount = pageDirectory.length;
        List<TwoLevelTranslationResult> results = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            TwoLevelPagingRequest req = requests.get(i);
            String reqId = "P" + (i + 1);

            int p1;
            int p2;
            int offset;
            int logical;

            if (req.isTuple()) {
                p1 = req.getOuterPage();
                p2 = req.getInnerPage();
                offset = req.getOffset();
                logical = (p1 >= 0 && p1 < outerCount && p2 >= 0 && p2 < innerTableSize)
                        ? ((p1 * innerTableSize + p2) * pageSize + offset)
                        : -1;

                if (p1 < 0 || p1 >= outerCount) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, -1, -1, false,
                            "TRAP: Outer Page p1 out of bounds (>" + (outerCount - 1) + ")"
                    ));
                } else if (pageDirectory[p1] == null) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, -1, -1, false,
                            "TRAP: Page Directory entry " + p1 + " not present"
                    ));
                } else if (p2 < 0 || p2 >= pageDirectory[p1].length) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, -1, -1, false,
                            "TRAP: Inner Page p2 out of bounds (>" + (pageDirectory[p1].length - 1) + ")"
                    ));
                } else if (offset >= pageSize || offset < 0) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, pageDirectory[p1][p2], -1, false,
                            "TRAP: Offset " + offset + " exceeds Page Size (" + pageSize + ")"
                    ));
                } else {
                    int frame = pageDirectory[p1][p2];
                    if (frame < 0) {
                        results.add(new TwoLevelTranslationResult(
                                reqId, logical, p1, p2, offset, -1, -1, false,
                                "TRAP: Page (" + p1 + ", " + p2 + ") not mapped (Page Fault)"
                        ));
                    } else {
                        int physicalAddress = frame * pageSize + offset;
                        results.add(new TwoLevelTranslationResult(
                                reqId, logical, p1, p2, offset, frame, physicalAddress, true,
                                "SUCCESS / VALID"
                        ));
                    }
                }
            } else {
                logical = req.getRawLogical();
                offset = logical % pageSize;
                int fullPage = logical / pageSize;
                p2 = fullPage % innerTableSize;
                p1 = fullPage / innerTableSize;

                if (p1 < 0 || p1 >= outerCount) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, -1, -1, false,
                            "TRAP: Outer Page p1 out of bounds (>" + (outerCount - 1) + ")"
                    ));
                } else if (pageDirectory[p1] == null) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, -1, -1, false,
                            "TRAP: Page Directory entry " + p1 + " not present"
                    ));
                } else if (p2 < 0 || p2 >= pageDirectory[p1].length) {
                    results.add(new TwoLevelTranslationResult(
                            reqId, logical, p1, p2, offset, -1, -1, false,
                            "TRAP: Inner Page p2 out of bounds (>" + (pageDirectory[p1].length - 1) + ")"
                    ));
                } else {
                    int frame = pageDirectory[p1][p2];
                    if (frame < 0) {
                        results.add(new TwoLevelTranslationResult(
                                reqId, logical, p1, p2, offset, -1, -1, false,
                                "TRAP: Page (" + p1 + ", " + p2 + ") not mapped (Page Fault)"
                        ));
                    } else {
                        int physicalAddress = frame * pageSize + offset;
                        results.add(new TwoLevelTranslationResult(
                                reqId, logical, p1, p2, offset, frame, physicalAddress, true,
                                "SUCCESS / VALID"
                        ));
                    }
                }
            }
        }

        return new TwoLevelPagingOutput(pageSize, outerCount, innerTableSize, pageDirectory, results);
    }

    /**
     * Convenience method to translate an array of raw logical addresses in two-level paging.
     */
    public TwoLevelPagingOutput translateTwoLevelAddresses(int pageSize, int innerTableSize,
                                                           int[][] pageDirectory, int[] logicalAddresses) {
        List<TwoLevelPagingRequest> requests = new ArrayList<>();
        for (int addr : logicalAddresses) {
            requests.add(new TwoLevelPagingRequest(addr));
        }
        return translateTwoLevel(pageSize, innerTableSize, pageDirectory, requests);
    }
}
