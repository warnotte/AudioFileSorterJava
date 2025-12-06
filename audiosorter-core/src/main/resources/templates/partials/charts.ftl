<#-- Charts JavaScript partial for AudioFilesSorter Report -->
// Chart.js configuration
const chartColors = {
    ok: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    empty: '#6b7280',
    primary: '#3b82f6',
    secondary: '#8b5cf6',
    palette: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16', '#f97316', '#6366f1']
};

const defaultChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            position: 'bottom',
            labels: { padding: 15, usePointStyle: true }
        }
    }
};

const defaultBarOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
        y: { beginAtZero: true, grid: { color: '#e2e8f0' } },
        x: { grid: { display: false } }
    }
};

// Status Distribution Pie Chart
new Chart(document.getElementById('statusChart'), {
    type: 'doughnut',
    data: {
        labels: ['OK', 'Missing Tags', 'Copy Errors', 'Empty'],
        datasets: [{
            data: [${okDirs?c}, ${noTagDirs?c}, ${copyErrorDirs?c}, ${emptyDirs?c}],
            backgroundColor: [chartColors.ok, chartColors.warning, chartColors.error, chartColors.empty],
            borderWidth: 0
        }]
    },
    options: defaultChartOptions
});

// Format Distribution Chart
<#if (chartData.formatLabels?size > 0)>
new Chart(document.getElementById('formatChart'), {
    type: 'doughnut',
    data: {
        labels: [<#list chartData.formatLabels as label>'${label?js_string}'<#sep>, </#list>],
        datasets: [{
            data: [<#list chartData.formatValues as value>${value?c}<#sep>, </#list>],
            backgroundColor: chartColors.palette,
            borderWidth: 0
        }]
    },
    options: defaultChartOptions
});
<#else>
// No format data available
document.getElementById('formatChart').parentElement.innerHTML = '<p class="empty-message">No format data</p>';
</#if>

// Bitrate Distribution Chart
<#if (chartData.bitrateLabels?size > 0)>
new Chart(document.getElementById('bitrateChart'), {
    type: 'bar',
    data: {
        labels: [<#list chartData.bitrateLabels as label>'${label?js_string} kbps'<#sep>, </#list>],
        datasets: [{
            label: 'Files',
            data: [<#list chartData.bitrateValues as value>${value?c}<#sep>, </#list>],
            backgroundColor: chartColors.primary,
            borderRadius: 4
        }]
    },
    options: defaultBarOptions
});
<#else>
document.getElementById('bitrateChart').parentElement.innerHTML = '<p class="empty-message">No bitrate data</p>';
</#if>

// Top Artists by Files Chart
<#if (chartData.artistLabels?size > 0)>
new Chart(document.getElementById('artistChart'), {
    type: 'bar',
    data: {
        labels: [<#list chartData.artistLabels as label>'${label?js_string}'<#sep>, </#list>],
        datasets: [{
            label: 'Files',
            data: [<#list chartData.artistValues as value>${value?c}<#sep>, </#list>],
            backgroundColor: chartColors.secondary,
            borderRadius: 4
        }]
    },
    options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { beginAtZero: true, grid: { color: '#e2e8f0' } },
            y: { grid: { display: false }, ticks: { autoSkip: false } }
        }
    }
});
<#else>
document.getElementById('artistChart').parentElement.innerHTML = '<p class="empty-message">No artist data</p>';
</#if>

// Top Artists by Albums Chart
<#if (chartData.artistAlbumLabels?size > 0)>
new Chart(document.getElementById('artistAlbumChart'), {
    type: 'bar',
    data: {
        labels: [<#list chartData.artistAlbumLabels as label>'${label?js_string}'<#sep>, </#list>],
        datasets: [{
            label: 'Albums',
            data: [<#list chartData.artistAlbumValues as value>${value?c}<#sep>, </#list>],
            backgroundColor: chartColors.primary,
            borderRadius: 4
        }]
    },
    options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { beginAtZero: true, grid: { color: '#e2e8f0' } },
            y: { grid: { display: false }, ticks: { autoSkip: false } }
        }
    }
});
<#else>
document.getElementById('artistAlbumChart').parentElement.innerHTML = '<p class="empty-message">No artist data</p>';
</#if>

// Albums by Year Chart
<#if (chartData.yearDirLabels?size > 0)>
const yearDirAlbums = [
<#list chartData.yearDirAlbums as albumList>
    [<#list albumList as album>'${album?js_string}'<#sep>, </#list>]<#sep>,
</#list>
];
new Chart(document.getElementById('yearDirChart'), {
    type: 'bar',
    data: {
        labels: [<#list chartData.yearDirLabels as label>'${label?js_string}'<#sep>, </#list>],
        datasets: [{
            label: 'Albums',
            data: [<#list chartData.yearDirValues as value>${value?c}<#sep>, </#list>],
            backgroundColor: chartColors.secondary,
            borderRadius: 2
        }]
    },
    options: {
        ...defaultBarOptions,
        plugins: {
            legend: { display: false },
            tooltip: {
                callbacks: {
                    afterBody: function(context) {
                        const idx = context[0].dataIndex;
                        const albums = yearDirAlbums[idx] || [];
                        const total = context[0].raw;
                        if (total <= 15) return albums;
                        return albums.slice(0, 15).concat(['... +' + (total - 15) + ' more']);
                    }
                }
            }
        }
    }
});
</#if>

// Files by Year Timeline Chart
<#if (chartData.yearLabels?size > 0)>
new Chart(document.getElementById('yearChart'), {
    type: 'bar',
    data: {
        labels: [<#list chartData.yearLabels as label>'${label?js_string}'<#sep>, </#list>],
        datasets: [{
            label: 'Files',
            data: [<#list chartData.yearValues as value>${value?c}<#sep>, </#list>],
            backgroundColor: chartColors.primary,
            borderRadius: 2
        }]
    },
    options: defaultBarOptions
});
</#if>
