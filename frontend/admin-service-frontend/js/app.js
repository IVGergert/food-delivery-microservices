import * as api from "./api.js";

const SECTION_INFO = {
    menu: ["Меню", "Управление меню ресторана"],
    users: ["Пользователи", "Управление пользователями"],
    couriers: ["Курьеры", "Управление курьерами"],
    orders: ["Заказы", "Управление заказами"],
    statistics: ["Статистика", "Статистика доставки и заказов"]
};

async function loadProfile() {
    try {
        const profile = await api.getProfile();
        const emailElement = document.getElementById("userEmail");

        if (emailElement) {
            emailElement.textContent = profile?.email || "Администратор";
        }
    } catch {
        const emailElement = document.getElementById("userEmail");

        if (emailElement) {
            emailElement.textContent = "Администратор";
        }
    }
}

function showSection(section) {
    const [title, description] =
        SECTION_INFO[section] || SECTION_INFO.menu;

    document.getElementById("pageTitle").textContent = title;
    document.getElementById("pageDescription").textContent = description;

    document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.toggle(
            "active",
            item.dataset.section === section
        );
    });

    const content = document.getElementById("content");

    if (!content) {
        return;
    }

    content.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">${getSectionIcon(section)}</div>
            <h2>${title}</h2>
            <p>${description}.</p>
        </div>
    `;
}

function getSectionIcon(section) {
    return {
        menu: "🍕",
        users: "👥",
        couriers: "🚴",
        orders: "📦",
        statistics: "📊"
    }[section] || "📋";
}

async function logout() {
    try {
        await api.logout();
    } finally {
        window.location.href = "/";
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadProfile();
    showSection("menu");
});

document.addEventListener("click", async event => {
    const navItem = event.target.closest(".nav-item");

    if (navItem?.dataset.section) {
        showSection(navItem.dataset.section);
        return;
    }

    if (event.target.closest("#logoutButton")) {
        await logout();
    }
});
