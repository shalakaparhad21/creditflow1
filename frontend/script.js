/* CreditFlow — shared utilities */

function navigateTo(url) {
    window.location.href = url;
}

/** Wrap a Chart.js canvas in a loading skeleton until the chart is ready. */
function withChartLoading(canvasId, renderFn) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    const container = canvas.closest('.chart-container') || canvas.parentElement;
    container.classList.add('chart-loading');

    requestAnimationFrame(function () {
        setTimeout(function () {
            try {
                renderFn(canvas);
            } finally {
                container.classList.remove('chart-loading');
            }
        }, 180);
    });
}

/** Tier palette for Chart.js — mirrors CSS custom properties in style.css */
var CreditFlowColors = {
    tierA: '#059669',
    tierB: '#2563eb',
    tierC: '#d97706',
    rejected: '#dc2626',
    neutral: '#94a3b8',
    navy: '#0f172a',
    surface: '#ffffff',
    chartPalette: ['#2563eb', '#059669', '#d97706', '#dc2626', '#334155', '#64748b', '#0891b2', '#7c3aed'],
    tierPalette: ['#059669', '#2563eb', '#d97706', '#dc2626'],
    rejectionPalette: ['#dc2626', '#d97706', '#334155', '#94a3b8']
};

/** Shared Chart.js options — respects fixed-height containers */
var CreditFlowChartDefaults = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            labels: {
                font: { family: "'Inter', system-ui, sans-serif", size: 11 },
                padding: 10,
                boxWidth: 12
            }
        }
    }
};

function mergeChartOptions(overrides) {
    var base = JSON.parse(JSON.stringify(CreditFlowChartDefaults));
    if (!overrides) return base;
    if (overrides.plugins) {
        base.plugins = Object.assign({}, base.plugins, overrides.plugins);
        delete overrides.plugins;
    }
    return Object.assign(base, overrides);
}
