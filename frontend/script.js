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

/** Tier palette for Chart.js — mirrors CSS custom properties. */
var CreditFlowColors = {
    tierA: '#059669',
    tierB: '#2563eb',
    tierC: '#d97706',
    rejected: '#dc2626',
    neutral: '#94a3b8',
    chartPalette: ['#2563eb', '#059669', '#d97706', '#dc2626', '#7c3aed', '#0891b2', '#db2777', '#ea580c']
};
