document.addEventListener('DOMContentLoaded', () => {
    // 1. Dark Mode Toggle
    const themeToggle = document.getElementById('theme-toggle');
    const htmlElement = document.documentElement;

    const currentTheme = localStorage.getItem('theme') || 'light';
    htmlElement.setAttribute('data-theme', currentTheme);
    updateThemeIcon(currentTheme);

    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            let theme = htmlElement.getAttribute('data-theme');
            let newTheme = theme === 'dark' ? 'light' : 'dark';
            
            htmlElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            updateThemeIcon(newTheme);
        });
    }

    function updateThemeIcon(theme) {
        if (!themeToggle) return;
        const icon = themeToggle.querySelector('i');
        if (theme === 'dark') {
            icon.className = 'bi bi-sun-fill text-warning';
        } else {
            icon.className = 'bi bi-moon-stars-fill text-dark';
        }
    }

    // 2. Sidebar Toggle (Mobile)
    const sidebarCollapse = document.getElementById('sidebar-collapse');
    const sidebar = document.querySelector('.sidebar');
    if (sidebarCollapse && sidebar) {
        sidebarCollapse.addEventListener('click', () => {
            sidebar.classList.toggle('active');
        });
    }

    // 3. OTP Auto-Focus Flow
    const otpInputs = document.querySelectorAll('.otp-digit');
    if (otpInputs.length > 0) {
        otpInputs.forEach((input, index) => {
            input.addEventListener('keyup', (e) => {
                if (e.key >= 0 && e.key <= 9) {
                    if (index < otpInputs.length - 1) {
                        otpInputs[index + 1].focus();
                    }
                } else if (e.key === 'Backspace') {
                    if (index > 0) {
                        otpInputs[index - 1].focus();
                    }
                }
                
                // Combine into hidden input
                const hiddenInput = document.getElementById('otp-code-hidden');
                if (hiddenInput) {
                    let otpVal = '';
                    otpInputs.forEach(inp => otpVal += inp.value);
                    hiddenInput.value = otpVal;
                }
            });
        });
    }

    // 4. Toast Notifications Auto-Hide
    const toasts = document.querySelectorAll('.toast');
    toasts.forEach(toast => {
        setTimeout(() => {
            toast.classList.remove('show');
        }, 5000);
    });
});

// Helper to initialize charts with professional default themes
window.createFuelChart = function(canvasId, type, label, labels, data, chartColors) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const textColor = isDark ? '#94a3b8' : '#475569';
    const gridColor = isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)';

    const defaultColors = [
        'rgba(79, 70, 229, 0.85)',
        'rgba(6, 182, 212, 0.85)',
        'rgba(244, 63, 94, 0.85)',
        'rgba(245, 158, 11, 0.85)',
        'rgba(16, 185, 129, 0.85)'
    ];

    const borderColors = [
        '#4f46e5',
        '#06b6d4',
        '#f43f5e',
        '#f59e0b',
        '#10b981'
    ];

    const colors = chartColors || defaultColors;

    return new Chart(ctx, {
        type: type,
        data: {
            labels: labels,
            datasets: [{
                label: label,
                data: data,
                backgroundColor: type === 'line' ? 'rgba(124, 58, 237, 0.15)' : colors,
                borderColor: type === 'line' ? '#7c3aed' : borderColors,
                borderWidth: 2,
                fill: type === 'line',
                tension: type === 'line' ? 0.3 : 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: textColor,
                        font: { family: 'Poppins', size: 11 }
                    }
                }
            },
            scales: type !== 'doughnut' && type !== 'pie' ? {
                x: {
                    grid: { color: gridColor },
                    ticks: { color: textColor, font: { family: 'Poppins', size: 10 } }
                },
                y: {
                    grid: { color: gridColor },
                    ticks: { color: textColor, font: { family: 'Poppins', size: 10 } }
                }
            } : {}
        }
    });
};
