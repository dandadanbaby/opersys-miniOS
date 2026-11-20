package com.beastos.paging.model;

import java.util.List;

/**
 * Complete output of a two-level (hierarchical) paging simulation.
 */
public class TwoLevelPagingOutput {
    private final int pageSize;
    private final int outerCount;
    private final int innerCount;
    private final int[][] pageDirectory;
    private final List<TwoLevelTranslationResult> translations;
    private final int validCount;
    private final int invalidCount;

    public TwoLevelPagingOutput(int pageSize, int outerCount, int innerCount,
                                int[][] pageDirectory,
                                List<TwoLevelTranslationResult> translations) {
        this.pageSize = pageSize;
        this.outerCount = outerCount;
        this.innerCount = innerCount;
        this.pageDirectory = pageDirectory;
        this.translations = List.copyOf(translations);

        int valid = 0, invalid = 0;
        for (TwoLevelTranslationResult t : translations) {
            if (t.isValid()) valid++;
            else invalid++;
        }
        this.validCount = valid;
        this.invalidCount = invalid;
    }

    public int getPageSize() { return pageSize; }
    public int getOuterCount() { return outerCount; }
    public int getInnerCount() { return innerCount; }
    public int[][] getPageDirectory() { return pageDirectory; }
    public List<TwoLevelTranslationResult> getTranslations() { return translations; }
    public int getValidCount() { return validCount; }
    public int getInvalidCount() { return invalidCount; }
    public int getTranslationCount() { return translations.size(); }
}
