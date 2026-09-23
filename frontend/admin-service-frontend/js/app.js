import * as api from "./api.js";

import {
    loadUsers,
    closeAdminModals
} from "./admin.js";

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


const SECTIONS = {
    users: {
        id: "usersSection",
        title: "Пользователи",
        subtitle: "Управление пользователями системы"
    }
};


function hideSections() {
    [
        "usersSection",
        "profileSection"
    ].forEach(id => {
        document.getElementById(id)
            ?.classList.add("hidden");
    });
}


async function showSection(section) {
    hideSections();

    if (section === "profile") {
        updateNavigation("profile");
        await showProfile(api);
        return;
    }

    const config =
        SECTIONS[section]
        || SECTIONS.users;

    document.getElementById(config.id)
        ?.classList.remove("hidden");

    updateNavigation(section);

    updatePageHeader(
        config.title,
        config.subtitle
    );

    if (section === "users") {
        await loadUsers();
    }
}


async function handleLogout() {
    try {
        await api.logout();
    } catch {} finally {
        window.location.href = "/";
    }
}


async function initializePage() {
    try {
        const profile =
            await api.getProfile();

        const emailElement =
            document.getElementById("userEmail");

        if (emailElement) {
            emailElement.textContent = profile?.email || "Пользователь";
        }

    } catch (error) {
        showError(error.message || "Не удалось загрузить профиль");
        return;
    }

    await showSection("users");
}


document.addEventListener(
    "DOMContentLoaded",
    initializePage
);


document.addEventListener(
    "click",
    async event => {

        const navItem =
            event.target.closest(".nav-item");

        if (navItem) {

            const section =
                navItem.dataset.section;

            if (section) {
                await showSection(section);
            }

            return;
        }

        if (
            event.target.closest("#logoutButton")
        ) {
            await handleLogout();
        }
    }
);


document.addEventListener(
    "profile-logout",
    handleLogout
);


document.addEventListener(
    "keydown",
    event => {

        if (event.key === "Escape") {
            closeAdminModals();
        }
    }
);