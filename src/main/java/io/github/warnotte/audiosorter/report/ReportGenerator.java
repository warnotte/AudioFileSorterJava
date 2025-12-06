package io.github.warnotte.audiosorter.report;

import io.github.warnotte.audiosorter.core.SortConfiguration;
import io.github.warnotte.audiosorter.model.RunTotals;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for generating reports from sorting results.
 */
public interface ReportGenerator {

    /**
     * Generate a report from the sorting results.
     * @param totals the run totals containing all results
     * @param config the configuration used for the sort
     * @param outputPath where to write the report
     * @throws IOException if writing fails
     */
    void generate(RunTotals totals, SortConfiguration config, Path outputPath) throws IOException;

    /**
     * Get the file extension for this report type.
     */
    String getFileExtension();

    /**
     * Get a description of this report type.
     */
    String getDescription();
}
