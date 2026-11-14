package com.beastos.paging.model;

import java.util.List;

/**
 * Complete output of a paging simulation.
 * Contains the page table and all translation results.
 */
public class PagingOutput {
    private final int pageSize;
    private final int totalPages;
    private final int[] pageTable;  // pageTable[pageNumber] = frameNumber
    private final List<TranslationResult> translations;
    private final int validCount;
    private final int invalidCount;

    public PagingOutput(int pageSize, int totalPages, int[] pageTable,
                        List<TranslationResult> translations) {
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.pageTable = pageTable.clone();
        this.translations = List.copyOf(translations);

        int valid = 0, invalid = 0;
        for (TranslationResult t : translations) {
            if (t.isValid()) valid++;
            else invalid++;
        }
        this.validCount = valid;
        this.invalidCount = invalid;
    }

    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public int[] getPageTable() { return pageTable.clone(); }
    public int getFrame(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= pageTable.length) return -1;
        return pageTable[pageNumber];
    }
    public List<TranslationResult> getTranslations() { return translations; }
    public int getValidCount() { return validCount; }
    public int getInvalidCount() { return invalidCount; }
    public int getTranslationCount() { return translations.size(); }
}
