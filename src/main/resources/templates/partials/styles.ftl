<#-- Styles partial for AudioFilesSorter Report -->
:root {
    --color-ok: #10b981;
    --color-warning: #f59e0b;
    --color-error: #ef4444;
    --color-empty: #6b7280;
    --color-bg: #f8fafc;
    --color-card: #ffffff;
    --color-text: #1e293b;
    --color-text-secondary: #64748b;
    --color-border: #e2e8f0;
    --color-primary: #3b82f6;
}

* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    background-color: var(--color-bg);
    color: var(--color-text);
    line-height: 1.6;
    padding: 2rem;
}

.container {
    max-width: 1400px;
    margin: 0 auto;
}

header {
    text-align: center;
    margin-bottom: 2rem;
}

header h1 {
    font-size: 2rem;
    font-weight: 700;
    margin-bottom: 0.5rem;
}

header .subtitle {
    color: var(--color-text-secondary);
    font-size: 0.9rem;
}

.card {
    background: var(--color-card);
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    margin-bottom: 1.5rem;
    overflow: hidden;
}

.card h2 {
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: var(--color-text);
    border-bottom: 2px solid var(--color-border);
    padding-bottom: 0.5rem;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
}

.stat-item {
    background: var(--color-bg);
    padding: 1rem;
    border-radius: 8px;
    text-align: center;
}

.stat-value {
    font-size: 2rem;
    font-weight: 700;
    color: var(--color-text);
}

.stat-label {
    font-size: 0.85rem;
    color: var(--color-text-secondary);
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.stat-value.ok { color: var(--color-ok); }
.stat-value.warning { color: var(--color-warning); }
.stat-value.error { color: var(--color-error); }

.config-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1rem;
}

.config-item {
    display: flex;
    flex-direction: column;
}

.config-label {
    font-size: 0.75rem;
    text-transform: uppercase;
    color: var(--color-text-secondary);
    margin-bottom: 0.25rem;
}

.config-value {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 0.9rem;
    background: var(--color-bg);
    padding: 0.5rem;
    border-radius: 4px;
    word-break: break-all;
}

.progress-bar {
    height: 8px;
    background: var(--color-border);
    border-radius: 4px;
    overflow: hidden;
    margin-top: 1rem;
}

.progress-fill {
    height: 100%;
    background: var(--color-ok);
    transition: width 0.3s ease;
}

.status-badge {
    display: inline-block;
    padding: 0.25rem 0.75rem;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-ok { background: #d1fae5; color: #065f46; }
.status-warning { background: #fef3c7; color: #92400e; }
.status-error { background: #fee2e2; color: #991b1b; }
.status-empty { background: #f3f4f6; color: #4b5563; }
.status-skipped { background: #e0e7ff; color: #3730a3; }

.path-cell {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 0.8rem;
    max-width: 400px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.number-cell {
    text-align: right;
    font-family: 'Monaco', 'Menlo', monospace;
}

.alert {
    padding: 0;
    border-radius: 8px;
    margin-bottom: 1rem;
    overflow: hidden;
}

.alert-warning {
    background: #fef3c7;
    border-left: 4px solid var(--color-warning);
}

.alert-error {
    background: #fee2e2;
    border-left: 4px solid var(--color-error);
}

.alert-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    cursor: pointer;
    user-select: none;
}

.alert-header:hover {
    filter: brightness(0.97);
}

.alert-title {
    font-weight: 600;
    margin: 0;
}

.alert-content {
    padding: 0 1rem 1rem 1rem;
    max-height: 400px;
    overflow-y: auto;
}

.alert-content.collapsed {
    display: none;
}

.alert-header.collapsed .collapse-icon {
    transform: rotate(-90deg);
}

.metadata-tag {
    display: inline-block;
    background: var(--color-bg);
    padding: 0.125rem 0.5rem;
    border-radius: 4px;
    font-size: 0.75rem;
    margin-right: 0.25rem;
    font-family: 'Monaco', 'Menlo', monospace;
}

.empty-message {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-secondary);
}

/* Collapsible sections */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    user-select: none;
    padding: 1rem 1.5rem;
    transition: background-color 0.2s;
}

.card-header:hover {
    background-color: var(--color-bg);
}

.card-header h2 {
    margin-bottom: 0;
    border-bottom: none;
    padding-bottom: 0;
}

.collapse-icon {
    font-size: 1.2rem;
    color: var(--color-text-secondary);
    transition: transform 0.3s ease;
}

.card-header.collapsed .collapse-icon {
    transform: rotate(-90deg);
}

.card-content {
    overflow: hidden;
    padding: 0 1.5rem 1.5rem 1.5rem;
}

.card-content.collapsed {
    display: none;
}

/* Charts section */
.charts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
    gap: 1.5rem;
}

.chart-container {
    background: var(--color-bg);
    border-radius: 8px;
    padding: 1rem;
    position: relative;
    height: 300px;
}

.chart-container.tall {
    height: 400px;
}

.chart-title {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--color-text);
    margin-bottom: 0.75rem;
    text-align: center;
}

footer {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-secondary);
    font-size: 0.85rem;
}

/* Simple-DataTables custom styling */
.datatable-wrapper { font-family: inherit; }
.datatable-wrapper .datatable-top,
.datatable-wrapper .datatable-bottom { padding: 0.75rem 0; }

.datatable-wrapper .datatable-search input {
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: 6px;
    font-size: 0.9rem;
    outline: none;
    transition: border-color 0.2s;
}

.datatable-wrapper .datatable-search input:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.datatable-wrapper .datatable-selector {
    padding: 0.5rem;
    border: 1px solid var(--color-border);
    border-radius: 6px;
    font-size: 0.9rem;
}

.datatable-wrapper .datatable-table {
    border-collapse: collapse;
    width: 100%;
}

.datatable-wrapper .datatable-table th {
    background: var(--color-bg);
    font-weight: 600;
    text-transform: uppercase;
    font-size: 0.75rem;
    letter-spacing: 0.05em;
    color: var(--color-text-secondary);
    padding: 0.75rem;
    border-bottom: 2px solid var(--color-border);
    cursor: pointer;
    user-select: none;
}

.datatable-wrapper .datatable-table th:hover { background: var(--color-border); }

.datatable-wrapper .datatable-table td {
    padding: 0.75rem;
    border-bottom: 1px solid var(--color-border);
    vertical-align: middle;
}

.datatable-wrapper .datatable-table tbody tr:hover { background: var(--color-bg); }

.datatable-wrapper .datatable-pagination .datatable-pagination-list {
    display: flex;
    gap: 0.25rem;
    list-style: none;
    padding: 0;
    margin: 0;
}

.datatable-wrapper .datatable-pagination .datatable-pagination-list-item-link {
    display: inline-block;
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: 6px;
    text-decoration: none;
    color: var(--color-text);
    font-size: 0.9rem;
    transition: all 0.2s;
}

.datatable-wrapper .datatable-pagination .datatable-pagination-list-item-link:hover {
    background: var(--color-bg);
    border-color: var(--color-primary);
}

.datatable-wrapper .datatable-pagination .datatable-pagination-list-item.datatable-active .datatable-pagination-list-item-link {
    background: var(--color-primary);
    border-color: var(--color-primary);
    color: white;
}

.datatable-wrapper .datatable-info {
    color: var(--color-text-secondary);
    font-size: 0.85rem;
}

/* Sort indicators */
.datatable-sorter {
    position: relative;
    padding-right: 20px !important;
}

.datatable-sorter::before,
.datatable-sorter::after { display: none !important; }

.datatable-sorter::after {
    display: inline-block !important;
    content: "⇅" !important;
    position: absolute;
    right: 4px;
    top: 50%;
    transform: translateY(-50%);
    border: none !important;
    font-size: 0.7rem;
    color: var(--color-text-secondary);
    opacity: 0.5;
}

.datatable-ascending .datatable-sorter::after {
    content: "↑" !important;
    opacity: 1;
    color: var(--color-primary);
}

.datatable-descending .datatable-sorter::after {
    content: "↓" !important;
    opacity: 1;
    color: var(--color-primary);
}

/* Layout fixes */
.datatable-wrapper .datatable-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

.datatable-wrapper .datatable-dropdown {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.datatable-wrapper .datatable-dropdown label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.85rem;
    color: var(--color-text-secondary);
}

.datatable-wrapper .datatable-selector {
    min-width: 70px;
    padding: 0.4rem 0.5rem;
}

.datatable-wrapper .datatable-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

@media (max-width: 768px) {
    body { padding: 1rem; }
    .stats-grid { grid-template-columns: repeat(2, 1fr); }
    .charts-grid { grid-template-columns: 1fr; }
    .datatable-wrapper .datatable-table { font-size: 0.8rem; }
    .datatable-wrapper .datatable-table th,
    .datatable-wrapper .datatable-table td { padding: 0.5rem; }
}
