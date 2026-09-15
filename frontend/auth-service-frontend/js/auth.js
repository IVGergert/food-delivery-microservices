import {
    loginRequest,
    registerRequest
} from "./api.js";

import {
    saveUserData
} from "./state.js";

import {
    showAlert,
    hideAlert
} from "./ui.js";

let selectedRole = "CUSTOMER";

export function selectRole(type) {
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

    if (type === "ADMIN") {
        btnAdmin.classList.add("active");
        registerTab.classList.add("hidden");

        roleNotice.textContent = "Вход доступен только для администратора.";
        roleNotice.classList.remove("hidden");

        switchTab("login");
    }
}


export function switchTab(tab) {
    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

    const loginTab = document.getElementById("loginTab");
    const registerTab = document.getElementById("registerTab");

    hideAlert();

    if (tab === "login") {
        loginForm.classList.remove("hidden");
        registerForm.classList.add("hidden");

        loginTab.classList.add("active");
        registerTab.classList.remove("active");

        return;
    }

    loginForm.classList.add("hidden");
    registerForm.classList.remove("hidden");

    loginTab.classList.remove("active");
    registerTab.classList.add("active");
}


export async function onLogin(event) {
    event.preventDefault();

    hideAlert();

    const email = document
            .getElementById("loginEmail")
            .value
            .trim();

    const password = document
            .getElementById("loginPassword")
            .value;

    try {
        const data = await loginRequest(email, password);

        let expectedRole;

        switch (selectedRole) {
            case "CUSTOMER":
                expectedRole = "ROLE_CUSTOMER";
                break;
            case "COURIER":
                expectedRole = "ROLE_COURIER";
                break;
            case "ADMIN":
                expectedRole = "ROLE_ADMIN";
                break;
        }

        if (data.role !== expectedRole) {
            const messages = {
                CUSTOMER: "Этот аккаунт не является аккаунтом клиента.",
                COURIER: "Этот аккаунт не является аккаунтом курьера.",
                ADMIN: "Этот аккаунт не является аккаунтом администратора."
            };

            showAlert(messages[selectedRole]);

            return;
        }

        saveUserData(data);
        redirectByRole(data.role);

    } catch (error) {
        showAlert(error.message);
    }
}


export async function onRegister(event) {
    event.preventDefault();

    hideAlert();

    const email = document.getElementById("regEmail").value.trim();
    const password = document.getElementById("regPassword").value;

    const confirmPassword = document.getElementById("regConfirmPassword").value;

    if (password !== confirmPassword) {
        showAlert("Пароли не совпадают");
        return;
    }

    try {
        const data = await registerRequest(
            email,
            password,
            confirmPassword
        );

        saveUserData(data);
        redirectByRole(data.role);

    } catch (error) {
        showAlert(error.message);
    }
}

function redirectByRole(role) {
    if (role === "ROLE_CUSTOMER") {
        window.location.href = "/customer/";
        return;
    }

    if (role === "ROLE_COURIER") {
        window.location.href = "/courier/";
    }

    if (role === "ROLE_ADMIN") {
        window.location.href = "/admin/";
    }
}