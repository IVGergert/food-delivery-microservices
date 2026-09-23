import * as api from "./api.js";

import {
    showProfile
} from "../../common/js/profile.js";

import {
    updateNavigation,
    updatePageHeader
} from "../../common/js/layout.js";

import {
    loadMenu
} from "./menu.js";

import {
    loadCart,
    openCart,
    closeCart,
    openCheckout,
    closeCheckout,
    createOrder
} from "./cart.js";

import {
    loadMyOrders,
    closeOrderDetails,
    closeCancelOrderConfirmation,
    confirmOrderCancellation
} from "./orders.js";


const SECTIONS = {
    menu: ["menuSection", "Меню", "Выберите блюда для заказа"],
    orders: ["ordersSection", "Мои заказы", "История ваших заказов"]
};

function showSection(section) {
    hideSections();

    if (section === "profile") {
        showProfile(api);
        updateNavigation(section);
        return;
    }

    const [sectionId, title, subtitle] = SECTIONS[section] || SECTIONS.menu;

    document.getElementById(sectionId)?.classList.remove("hidden");
    updatePageHeader(title, subtitle);
    updateNavigation(section);

    if (section === "orders") {
        loadMyOrders();
    }
}

function hideSections() {
    [
        "menuSection",
        "ordersSection",
        "profileSection"
    ].forEach(id => {
        document.getElementById(id)?.classList.add("hidden");
    });
}


async function logout() {
    try {
        await api.logout();
    } catch {
        // The browser is redirected even when the server logout fails.
    } finally {
        localStorage.removeItem("cartItems");
        window.location.href = "/";
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    loadCart();

    try {
        const profile = await api.getProfile();
        const emailElement = document.getElementById("userEmail");

        if (emailElement) {
            emailElement.textContent = profile?.email || "Пользователь";
        }
    } catch {
        // API client handles expired sessions; keep the page bootstrap quiet here.
    }

    await loadMenu();
});

document.addEventListener("keydown", event => {
    if (event.key !== "Escape") {
        return;
    }

    closeCart();
    closeCheckout();
    closeOrderDetails();
    closeCancelOrderConfirmation();
    document.dispatchEvent(new CustomEvent("close-modals"));
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

    if (event.target.closest("#logoutButton")) {
        await logout();
        return;
    }

    if (event.target.closest("#cartButton")) {
        openCart();
        return;
    }

    if (event.target.closest("#closeCartButton")) {
        closeCart();
        return;
    }

    if (event.target.closest("#cartOverlay")) {
        closeCart();
        return;
    }

    if (event.target.closest("#checkoutButton")) {
        openCheckout();
        return;
    }

    if (event.target.closest("#closeCheckoutButton")) {
        closeCheckout();
        return;
    }

    if (event.target.closest("#refreshOrdersButton")) {
        const ordersButton = event.target.closest("#refreshOrdersButton");
        ordersButton.disabled = true;

        try {
            await loadMyOrders();
        } finally {
            ordersButton.disabled = false;
        }

        return;
    }

    if (event.target.closest("#retryMenuButton")) {
        await loadMenu();
        return;
    }

    if (event.target.closest("#goToMenuButton")) {
        document.querySelector('[data-section="menu"]')?.click();
        return;
    }

    if (event.target.closest("#closeOrderButton")) {
        closeOrderDetails();
        return;
    }

    if (event.target.closest("#closeCancelOrderButton")
        || event.target.closest("#closeCancelOrderButtonSecondary")) {
        closeCancelOrderConfirmation();
        return;
    }

    if (event.target.closest("#confirmCancelOrderButton")) {
        await confirmOrderCancellation();
    }
});

document.addEventListener("submit", event => {
    if (event.target.id === "checkoutForm") {
        event.preventDefault();
        createOrder();
    }
});

document.addEventListener("profile-logout", logout);
