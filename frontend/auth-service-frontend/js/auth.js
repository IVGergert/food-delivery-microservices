import {
    login,
    register
} from "./api.js";

import {
    showError,
    hideAlert
} from "../../common/js/notifications.js";

let selectedRole = "CUSTOMER";

function selectRole(type) {
    const btnCustomer = document.getElementById("btnCustomer");
    const btnCourier = document.getElementById("btnCourier");
    const btnAdmin = document.getElementById("btnAdmin");
    const registerTab = document.getElementById("registerTab");
    const roleNotice = document.getElementById("roleNotice");

    selectedRole = type;
    hideAlert();

    btnCustomer.classList.remove("active");
    btnCourier.classList.remove("active");
    btnAdmin.classList.remove("active");

    if (type === "CUSTOMER") {
        btnCustomer.classList.add("active");
        registerTab.classList.remove("hidden");
        roleNotice.classList.add("hidden");
        return;
    }

    if (type === "COURIER") {
        btnCourier.classList.add("active");
        registerTab.classList.add("hidden");
        roleNotice.textContent = "Аккаунты курьеров выдаются администратором.";
        roleNotice.classList.remove("hidden");
        switchTab("login");
        return;
    }

    btnAdmin.classList.add("active");
    registerTab.classList.add("hidden");
    roleNotice.textContent = "Вход доступен только для администратора.";
    roleNotice.classList.remove("hidden");
    switchTab("login");
}

function switchTab(tab) {
    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");
    const loginTab = document.getElementById("loginTab");
    const registerTab = document.getElementById("registerTab");

    hideAlert();

    const loginActive = tab === "login";

    loginForm.classList.toggle("hidden", !loginActive);
    registerForm.classList.toggle("hidden", loginActive);
    loginTab.classList.toggle("active", loginActive);
    registerTab.classList.toggle("active", !loginActive);
}

async function onLogin(event) {
    event.preventDefault();
    hideAlert();

    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    try {
        const data = await login({
            email,
            password
        });

        const expectedRole = {
            CUSTOMER: "ROLE_CUSTOMER",
            COURIER: "ROLE_COURIER",
            ADMIN: "ROLE_ADMIN"
        }[selectedRole];

        if (data.role !== expectedRole) {
            const messages = {
                CUSTOMER: "Этот аккаунт не является аккаунтом клиента.",
                COURIER: "Этот аккаунт не является аккаунтом курьера.",
                ADMIN: "Этот аккаунт не является аккаунтом администратора."
            };

            showError(messages[selectedRole]);
            return;
        }

        redirectByRole(data.role);
    } catch (error) {
        showError(error.message);
    }
}

async function onRegister(event) {
    event.preventDefault();
    hideAlert();

    const email = document.getElementById("regEmail").value.trim();
    const password = document.getElementById("regPassword").value;
    const confirmPassword = document.getElementById("regConfirmPassword").value;

    if (password !== confirmPassword) {
        showError("Пароли не совпадают");
        return;
    }

    try {
        const data = await register({
            email,
            password,
            confirmPassword
        });

        redirectByRole(data.role);
    } catch (error) {
        showError(error.message);
    }
}

function redirectByRole(role) {
    const paths = {
        ROLE_CUSTOMER: "/customer/",
        ROLE_COURIER: "/courier/",
        ROLE_ADMIN: "/admin/"
    };

    const path = paths[role];

    if (path) {
        window.location.href = path;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    document
        .getElementById("btnCustomer")
        .addEventListener("click", () => selectRole("CUSTOMER"));

    document
        .getElementById("btnCourier")
        .addEventListener("click", () => selectRole("COURIER"));

    document
        .getElementById("btnAdmin")
        .addEventListener("click", () => selectRole("ADMIN"));

    document
        .getElementById("loginTab")
        .addEventListener("click", () => switchTab("login"));

    document
        .getElementById("registerTab")
        .addEventListener("click", () => switchTab("register"));

    document
        .getElementById("loginForm")
        .addEventListener("submit", onLogin);

    document
        .getElementById("registerForm")
        .addEventListener("submit", onRegister);
});
