import {
    state,
    logout
} from "./state.js";

import {
    showSection
} from "./ui.js";

import {
    renderUserInfo
} from "../common/js/ui.js";

import {
    fetchCourierStatus,
    fetchTodayStats,
    fetchCurrentDelivery,
    fetchWaitingDeliveries,
    fetchHistoryDeliveries,
    goOnline,
    goOffline,
    acceptDelivery,
    pickUpOrder,
    completeDelivery,
    validateLogout
} from "./deliveries.js";

// Application start

document.addEventListener(
    "DOMContentLoaded",
    async () => {

        renderUserInfo();

        await fetchCourierStatus();
        await fetchTodayStats();
        await fetchCurrentDelivery();
    }
);


// User actions

document.addEventListener(
    "click",
    async event => {


        const navItem = event.target.closest(".nav-item");

        if (navItem) {
            const section = navItem.dataset.section;

            if (!section) return;

            showSection(section);

            if (section === "current") {
                await fetchCurrentDelivery();
            }

            if (section === "waiting") {
                await fetchWaitingDeliveries();
            }

            if (section === "history") {
                await fetchHistoryDeliveries();
            }

            return;
        }


        // Online / Offline

        if (event.target.closest("#statusToggleButton")) {
            if (state.courierStatus === "OFFLINE") {
                await goOnline();
            } else {
                await goOffline();
            }

            return;
        }


        // Accept delivery

        const acceptButton = event.target.closest(".accept-btn");

        if (acceptButton) {
            const orderId = acceptButton.dataset.orderId;

            if (orderId) {
                await acceptDelivery(orderId);
            }

            return;
        }


        // Pickup

        if (event.target.closest("#pickupButton")) {
            if (state.currentDelivery) {
                await pickUpOrder(state.currentDelivery.orderId);
            }

            return;
        }


        // Complete

        if (event.target.closest("#completeButton")) {
            if (state.currentDelivery) {
                await completeDelivery(state.currentDelivery.orderId);
            }

            return;
        }


        // Go to waiting

        if (event.target.closest("#goToWaitingButton")) {
            showSection("waiting");
            await fetchWaitingDeliveries();
            return;
        }


        // Refresh waiting

        if (event.target.closest("#refreshWaitingButton")) {
            await fetchWaitingDeliveries();
            return;
        }


        // Refresh history

        if (event.target.closest("#refreshHistoryButton")) {
            await fetchHistoryDeliveries();
            return;
        }


        // Logout

        if (event.target.closest("#logoutButton")) {
            const canLogout = await validateLogout();

            if (canLogout) {
                await logout();
            }
        }
    }
);

document.addEventListener("profile-logout", async () => {
    const canLogout = await validateLogout();

    if (canLogout) {
        await logout();
    }
});
