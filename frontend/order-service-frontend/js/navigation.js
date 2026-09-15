import {
    showProfile
} from "./profile.js";

import {
    loadMyOrders
} from "./orders.js";


export function initNavigation() {
    document.addEventListener("click", event => {
        const navItem = event.target.closest(".nav-item");

        if (!navItem) {
            return;
        }

        const section = navItem.dataset.section;

        if (section) {
            showSection(section);
        }
    });
}


function showSection(section) {
    hideSections();

    if (section === "menu") {
        document
            .getElementById("menuSection")
            ?.classList.remove("hidden");

        updatePageHeader("Меню", "Выберите блюда для заказа");
    }

    if (section === "orders") {
        document
            .getElementById("ordersSection")
            ?.classList.remove("hidden");

        updatePageHeader("Мои заказы", "История ваших заказов");

        loadMyOrders();
    }

    if (section === "profile") {
        showProfile();

        updatePageHeader("Профиль", "Ваши личные данные");
    }

    updateNavigation(section);
}


function hideSections() {
    document
        .getElementById("menuSection")
        ?.classList.add("hidden");

    document
        .getElementById("ordersSection")
        ?.classList.add("hidden");

    document
        .getElementById("profileSection")
        ?.classList.add("hidden");
}


function updateNavigation(section) {
    document
        .querySelectorAll(".nav-item")
        .forEach(item => {
            item.classList.remove("active");
        });

    document
        .querySelector(`[data-section="${section}"]`)
        ?.classList.add("active");
}


function updatePageHeader(title, subtitle) {
    document
        .getElementById("pageTitle")
        .textContent = title;

    document
        .getElementById("pageSubtitle")
        .textContent = subtitle;
}