<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AudioFilesSorter Report</title>
    <!-- External libraries -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/simple-datatables@9.0.0/dist/style.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <style>
<#include "partials/styles.ftl">
    </style>
    <script>
    function toggleSection(header) {
        header.classList.toggle('collapsed');
        const content = header.nextElementSibling;
        if (content && (content.classList.contains('card-content') || content.classList.contains('alert-content'))) {
            content.classList.toggle('collapsed');
        }
    }
    </script>
</head>
<body>
    <div class="container">
        <header>
            <h1>AudioFilesSorter Report</h1>
            <p class="subtitle">Generated on ${generatedAt} - Duration: ${durationFormatted}</p>
            <p style="margin-top: 1rem;"><a href="catalog.html" class="catalog-link">View Music Catalog</a></p>
        </header>

        <!-- Configuration -->
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Configuration</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <div class="config-grid">
                    <div class="config-item">
                        <span class="config-label">Input Directory</span>
                        <span class="config-value">${inputDirectory}</span>
                    </div>
                    <div class="config-item">
                        <span class="config-label">Output Directory</span>
                        <span class="config-value">${outputDirectory}</span>
                    </div>
                    <div class="config-item">
                        <span class="config-label">Debug Mode</span>
                        <span class="config-value">${debugMode?string('Yes (no files copied)', 'No (files copied)')}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Summary Statistics -->
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Summary</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <div class="stats-grid">
                    <div class="stat-item">
                        <div class="stat-value">${directoriesTotal}</div>
                        <div class="stat-label">Directories Scanned</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value ok">${okDirs}</div>
                        <div class="stat-label">Successful</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value warning">${noTagDirs}</div>
                        <div class="stat-label">Missing Tags</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value error">${copyErrorDirs}</div>
                        <div class="stat-label">Copy Errors</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">${emptyDirs}</div>
                        <div class="stat-label">Empty</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value warning">${duplicateAlbumsCount}</div>
                        <div class="stat-label">Duplicates</div>
                    </div>
                </div>

                <div class="stats-grid" style="margin-top: 1rem;">
                    <div class="stat-item">
                        <div class="stat-value">${totalFiles}</div>
                        <div class="stat-label">Total Files</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">${audioFiles}</div>
                        <div class="stat-label">Audio Files</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">${nonAudioFiles}</div>
                        <div class="stat-label">Other Files</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value ok">${filesCopied}</div>
                        <div class="stat-label">Files Copied</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value error">${filesFailed}</div>
                        <div class="stat-label">Files Failed</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">${totalMBCopied?string["0.##"]} MB</div>
                        <div class="stat-label">Data Copied</div>
                    </div>
                </div>

                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${successRate}%"></div>
                </div>
                <p style="text-align: center; margin-top: 0.5rem; color: var(--color-text-secondary);">
                    Success Rate: ${successRate}%
                </p>
            </div>
        </div>

        <!-- Statistics Charts -->
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Collection Statistics</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <div class="charts-grid">
                    <div class="chart-container">
                        <div class="chart-title">Directory Status</div>
                        <canvas id="statusChart"></canvas>
                    </div>
                    <div class="chart-container">
                        <div class="chart-title">Audio Formats</div>
                        <canvas id="formatChart"></canvas>
                    </div>
                    <div class="chart-container">
                        <div class="chart-title">Bitrate Quality</div>
                        <canvas id="bitrateChart"></canvas>
                    </div>
                    <div class="chart-container tall">
                        <div class="chart-title">Top 30 Artists (by files)</div>
                        <canvas id="artistChart"></canvas>
                    </div>
                    <div class="chart-container tall">
                        <div class="chart-title">Top 30 Artists (by albums)</div>
                        <canvas id="artistAlbumChart"></canvas>
                    </div>
                </div>

                <#if (chartData.yearDirLabels?size > 0)>
                <div style="margin-top: 1.5rem;">
                    <div class="chart-container tall">
                        <div class="chart-title">Albums by Year</div>
                        <canvas id="yearDirChart"></canvas>
                    </div>
                </div>
                </#if>

                <#if (chartData.yearLabels?size > 0)>
                <div style="margin-top: 1.5rem;">
                    <div class="chart-container tall">
                        <div class="chart-title">Files by Year</div>
                        <canvas id="yearChart"></canvas>
                    </div>
                </div>
                </#if>
            </div>
        </div>

        <!-- Analysis Section -->
        <#if (smallAlbums?size > 0) || (suspiciousYears?size > 0) || (missingCovers?size > 0)>
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Analysis</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <#if smallAlbums?size gt 0>
                <div class="alert" style="background: #e0f2fe; border-left: 4px solid #0ea5e9;">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">Small Albums - ≤2 files (${smallAlbums?size})</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <p style="margin-bottom: 0.5rem; font-size: 0.85rem; color: #666;">May be singles, EPs, or incomplete albums.</p>
                        <ul style="margin-left: 1rem;">
                            <#list smallAlbums as dir>
                            <li style="margin-bottom: 0.25rem;">
                                <a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a>
                                <span class="metadata-tag">${dir.filesCount} file(s)</span>
                                <#if dir.artist?? && dir.album??>
                                <span style="color: #666;"> - ${dir.artist} / ${dir.album}</span>
                                </#if>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>

                <#if suspiciousYears?size gt 0>
                <div class="alert" style="background: #fef3c7; border-left: 4px solid #f59e0b;">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">Suspicious Years (${suspiciousYears?size})</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <p style="margin-bottom: 0.5rem; font-size: 0.85rem; color: #666;">Years before 1900 or in the future - likely typos.</p>
                        <ul style="margin-left: 1rem;">
                            <#list suspiciousYears as dir>
                            <li style="margin-bottom: 0.25rem;">
                                <a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a>
                                <span class="metadata-tag" style="background: #fef3c7;">${dir.year!'-'}</span>
                                <#if dir.artist?? && dir.album??>
                                <span style="color: #666;"> - ${dir.artist} / ${dir.album}</span>
                                </#if>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>

                <#if missingCovers?size gt 0>
                <div class="alert" style="background: #f3f4f6; border-left: 4px solid #6b7280;">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">Missing Cover Art (${missingCovers?size})</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <p style="margin-bottom: 0.5rem; font-size: 0.85rem; color: #666;">No image file (jpg, png) found in directory.</p>
                        <ul style="margin-left: 1rem;">
                            <#list missingCovers as dir>
                            <li style="margin-bottom: 0.25rem;"><a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a></li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>
            </div>
        </div>
        </#if>

        <!-- Duplicate Albums Section -->
        <#if (duplicateAlbums?size > 0)>
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Duplicate Albums (${duplicateAlbumsCount})</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <p style="margin-bottom: 1rem; color: var(--color-text-secondary);">
                    Albums found in multiple formats/locations. Consider keeping only the best quality version.
                </p>
                <#list duplicateAlbums as group>
                <div class="alert" style="background: #fef3c7; border-left: 4px solid #f59e0b;">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">${group.artist} - ${group.album} (${group.count} copies)</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <table style="width: 100%; font-size: 0.85rem;">
                            <tr style="background: rgba(0,0,0,0.05);">
                                <th style="padding: 0.5rem; text-align: left;">Path</th>
                                <th style="padding: 0.5rem; text-align: left;">Format</th>
                                <th style="padding: 0.5rem; text-align: right;">Bitrate</th>
                                <th style="padding: 0.5rem; text-align: right;">Files</th>
                            </tr>
                            <#list group.directories as dir>
                            <tr>
                                <td style="padding: 0.5rem;"><a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a></td>
                                <td style="padding: 0.5rem;">${dir.format!'-'}</td>
                                <td style="padding: 0.5rem; text-align: right;"><#if dir.bitrate??>${dir.bitrate} kbps<#else>-</#if></td>
                                <td style="padding: 0.5rem; text-align: right;">${dir.filesCount}</td>
                            </tr>
                            </#list>
                        </table>
                    </div>
                </div>
                </#list>
            </div>
        </div>
        </#if>

        <!-- Problems Section -->
        <#if (directoriesWithoutTags?size > 0) || (failedFiles?size > 0) || (emptyDirectories?size > 0)>
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Problems</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <#if directoriesWithoutTags?size gt 0>
                <div class="alert alert-warning">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">Directories Without Tags (${directoriesWithoutTags?size})</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <ul style="margin-left: 1rem;">
                            <#list directoriesWithoutTags as dir>
                            <li style="margin-bottom: 0.25rem;"><a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a></li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>

                <#if failedFiles?size gt 0>
                <div class="alert alert-error">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">Failed File Copies (${failedFiles?size})</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <ul style="margin-left: 1rem;">
                            <#list failedFiles as file>
                            <li style="margin-bottom: 0.5rem;">
                                <a href="file:///${file.source?replace('\\', '/')}" target="_blank" class="path-link">${file.source}</a>
                                <br><small style="color: #991b1b;">${file.errorMessage!'-'}</small>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>

                <#if emptyDirectories?size gt 0>
                <div class="alert" style="background: #f3f4f6; border-left: 4px solid var(--color-empty);">
                    <div class="alert-header" onclick="toggleSection(this)">
                        <div class="alert-title">Empty Directories (${emptyDirectories?size})</div>
                        <span class="collapse-icon">▼</span>
                    </div>
                    <div class="alert-content collapsed">
                        <ul style="margin-left: 1rem;">
                            <#list emptyDirectories as dir>
                            <li style="margin-bottom: 0.25rem;"><a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a></li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>
            </div>
        </div>
        </#if>

        <!-- Directory Details -->
        <div class="card">
            <div class="card-header" onclick="toggleSection(this)">
                <h2>Directory Details (${directories?size})</h2>
                <span class="collapse-icon">▼</span>
            </div>
            <div class="card-content">
                <#if directories?size gt 0>
                <table id="directories-table">
                    <thead>
                        <tr>
                            <th>Status</th>
                            <th>Path</th>
                            <th>Artist</th>
                            <th>Album</th>
                            <th>Year</th>
                            <th>Files</th>
                            <th>Copied</th>
                            <th>Errors</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list directories as dir>
                        <tr>
                            <td data-sort="${dir.status}"><span class="status-badge ${dir.statusClass}">${dir.status}</span></td>
                            <td title="${dir.path}"><a href="file:///${dir.path?replace('\\', '/')}" target="_blank" class="path-link">${dir.path}</a></td>
                            <td>${dir.artist!'-'}</td>
                            <td>
                                ${dir.album!'-'}
                                <#if dir.format??>
                                <br><span class="metadata-tag">${dir.format}<#if dir.bitrate??> ${dir.bitrate}kbps</#if></span>
                                </#if>
                            </td>
                            <td>${dir.year!'-'}</td>
                            <td class="number-cell">${dir.filesCount}</td>
                            <td class="number-cell">${dir.copiedCount}</td>
                            <td class="number-cell <#if (dir.errorCount > 0)>error</#if>">${dir.errorCount}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
                <#else>
                <p class="empty-message">No directories processed.</p>
                </#if>
            </div>
        </div>

        <footer>
            <p>AudioFilesSorter - Audio File Organization Tool</p>
        </footer>
    </div>

    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/simple-datatables@9.0.0/dist/umd/simple-datatables.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // DataTable initialization
            const table = document.getElementById('directories-table');
            if (table) {
                new simpleDatatables.DataTable(table, {
                    searchable: true,
                    sortable: true,
                    perPage: 25,
                    perPageSelect: [10, 25, 50, 100],
                    labels: {
                        placeholder: "Search...",
                        noRows: "No entries found",
                        info: "Showing {start} to {end} of {rows} entries"
                    },
                    columns: [
                        { select: 5, type: "number" },
                        { select: 6, type: "number" },
                        { select: 7, type: "number" }
                    ]
                });
            }
        });
    </script>
    <script>
        // Charts (separate script to isolate errors)
        document.addEventListener('DOMContentLoaded', function() {
<#include "partials/charts.ftl">
        });
    </script>
</body>
</html>
