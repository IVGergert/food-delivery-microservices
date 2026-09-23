export function updateNavigation(section) {
    document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.toggle("active", item.dataset.section === section);
    });
}

export function updatePageHeader(title, subtitle) {
    const titleElement = document.getElementById("pageTitle");
    const subtitleElement = document.getElementById("pageSubtitle");

    if (titleElement) {
        titleElement.textContent = title;
    }

    if (subtitleElement) {
        subtitleElement.textContent = subtitle;
    }
}
