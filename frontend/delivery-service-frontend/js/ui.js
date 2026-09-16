import { state } from "./state.js";

import {
    showProfile as showCommonProfile
} from "../common/js/profile.js";

export function updateStatusUI() {
    const toggleBtn = document.getElementById("statusToggleButton");
    const toggleText = document.getElementById("statusToggleText");
    const statusBadge = document.getElementById("courierStatusBadge");
    const isOnline = state.courierStatus !== "OFFLINE";

    if (toggleBtn) {
        toggleBtn.className = `status-toggle-button ${isOnline ? "online" : "offline"}`;
    }

    if (toggleText) {
        toggleText.textContent = isOnline
            ? "На линии (Завершить)"
            : "Выйти на линию";
    }

    if (statusBadge) {
        statusBadge.className = `courier-status-badge ${isOnline ? "online" : "offline"}`;
        statusBadge.textContent = isOnline ? "Онлайн" : "Оффлайн";
    }
}

export function showSection(section) {
    [
        "currentSection",
        "waitingSection",
        "historySection",
        "profileSection"
    ].forEach(id => {
        document.getElementById(id)?.classList.add("hidden");
    });

    const sections = {
        current: ["currentSection", "Текущий заказ", "Управление вашим активным заказом"],
        waiting: ["waitingSection", "Доступные заказы", "Выберите заказ для доставки"],
        history: ["historySection", "История доставок", "Выполненные заказы"]
    };

    if (section === "profile") {
        showCommonProfile();
    } else {
        const [sectionId, title, subtitle] = sections[section] || [];

        if (sectionId) {
            document.getElementById(sectionId)?.classList.remove("hidden");
            updatePageHeader(title, subtitle);
        }
    }

    updateNavigation(section);
}

function updateNavigation(section) {
    document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.remove("active");
    });

    document
        .querySelector(`[data-section="${section}"]`)
        ?.classList.add("active");
}

function updatePageHeader(title, subtitle) {
    const titleElement = document.getElementById("pageTitle");
    const subtitleElement = document.getElementById("pageSubtitle");

    if (titleElement) {
        titleElement.textContent = title;
    }

    if (subtitleElement) {
        subtitleElement.textContent = subtitle;
    }
}

export function showCurrentLoading() {
    document.getElementById("currentLoading")?.classList.remove("hidden");
}

export function hideCurrentLoading() {
    document.getElementById("currentLoading")?.classList.add("hidden");
}

export function getDeliveryStatusTitle(status) {
    const statuses = {
        WAITING_FOR_COURIER: "Ожидает курьера",
        COURIER_ASSIGNED: "Назначен курьер",
        PICKED_UP: "Заказ забран",
        DELIVERED: "Доставлен 🎉"
    };

    return statuses[status] || status || "В работе";
}
