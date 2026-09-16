import {
    logout
} from "./auth.js";

import {
    renderUserInfo
} from "../common/js/ui.js";

import {
    loadMenu,
    renderCategories
} from "./menu.js";

import {
    loadCart,
    updateCartCounter,
    updateCartItemsCount,
    renderCart,
    openCart,
    closeCart,
    openCheckout,
    closeCheckout,
    createOrder
} from "./cart.js";

import {
    initNavigation
} from "./navigation.js";

import {
    closeOrderDetails
} from "./orders.js";

document.addEventListener("DOMContentLoaded", async () => {
    loadCart();
    renderUserInfo();

    renderCategories();
    updateCartCounter();
    updateCartItemsCount();
    renderCart();

    initNavigation();

    await loadMenu();
});

document.addEventListener("keydown", event => {
    if (event.key === "Escape") {
        document.dispatchEvent(
            new CustomEvent("close-modals")
        );
    }
});

document.addEventListener("click", event => {

    if (event.target.closest("#logoutButton")) {
        logout();
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
        document.dispatchEvent(new CustomEvent("refresh-orders"));
        return;
    }

    if (event.target.closest("#goToMenuButton")) {
        document.querySelector('[data-section="menu"]')?.click();
        return;
    }

    if (event.target.closest("#closeOrderButton")) {
        closeOrderDetails();
    }
});

document.addEventListener("submit", event => {
    if (event.target.id === "checkoutForm") {
        event.preventDefault();
        createOrder();
    }
});

document.addEventListener("profile-logout", () => {
    logout();
});
