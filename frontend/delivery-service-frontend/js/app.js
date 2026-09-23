import * as api from "./api.js";

import {
    showProfile
} from "../../common/js/profile.js";

import {
    showError
} from "../../common/js/notifications.js";

import {
    updateNavigation,
    updatePageHeader
} from "../../common/js/layout.js";

import {
    state,
    toggleCourierStatus,
    fetchCourierStatus,
    fetchTodayStats,
    fetchCurrentDelivery,
    fetchWaitingDeliveries,
    fetchHistoryDeliveries,
    acceptDelivery,
    pickUpOrder,
    completeDelivery
} from "./deliveries.js";

function showSection(section) {
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
        showProfile(api);
        updateNavigation(section);
        return;
    }

    const [sectionId, title, subtitle] = sections[section] || sections.current;

    document.getElementById(sectionId)?.classList.remove("hidden");
    updatePageHeader(title, subtitle);
    updateNavigation(section);

    if (section === "current") {
        fetchCurrentDelivery();
    }

    if (section === "waiting") {
        fetchWaitingDeliveries();
    }

    if (section === "history") {
        fetchHistoryDeliveries();
    }
}


async function logout() {
    try {
        await api.logout();
    } catch {} finally {
        window.location.href = "/";
    }
}


async function handleLogout() {
    try {
        await api.validateLogout();
        await logout();
    } catch (error) {
        showError(error.message);
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    try {
        const profile = await api.getProfile();
        const emailElement = document.getElementById("userEmail");

        if (emailElement) {
            emailElement.textContent = profile?.email || "Курьер";
        }
    } catch {}

    await Promise.all([
        fetchCourierStatus(),
        fetchTodayStats(),
        fetchCurrentDelivery()
    ]);
});

document.addEventListener("click", async event => {
    const navItem = event.target.closest(".nav-item");

    if (navItem) {
        const section = navItem.dataset.section;

        if (section) {
            showSection(section);
        }

        return;
    }

    if (event.target.closest("#statusToggleButton")) {
        await toggleCourierStatus();
        return;
    }

    const acceptButton = event.target.closest(".accept-btn");

    if (acceptButton) {
        const orderId = acceptButton.dataset.orderId;

        if (orderId) {
            if (await acceptDelivery(orderId)) {
                showSection("current");
            }
        }

        return;
    }

    if (event.target.closest("#pickupButton")) {
        if (state.currentDelivery) {
            await pickUpOrder(state.currentDelivery.orderId);
        }

        return;
    }

    if (event.target.closest("#completeButton")) {
        if (state.currentDelivery) {
            await completeDelivery(state.currentDelivery.orderId);
        }

        return;
    }

    if (event.target.closest("#goToWaitingButton")) {
        showSection("waiting");
        return;
    }

    if (event.target.closest("#refreshWaitingButton")) {
        await fetchWaitingDeliveries();
        return;
    }

    if (event.target.closest("#refreshHistoryButton")) {
        await fetchHistoryDeliveries();
        return;
    }

    if (event.target.closest("#logoutButton")) {
        await handleLogout();
    }
});

document.addEventListener("profile-logout", handleLogout);
