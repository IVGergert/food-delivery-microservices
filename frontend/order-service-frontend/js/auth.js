import {
    logoutRequest
} from "./api.js";

const CART_STORAGE_KEY = "cartItems";

export function getUserEmail() {
    return localStorage.getItem("userEmail") || "Пользователь";
}

export async function logout() {
    try {
        await logoutRequest();
    } catch (error) {
        console.error("Logout error:", error);
    } finally {
        localStorage.removeItem("userId");
        localStorage.removeItem("userEmail");
        localStorage.removeItem("userRole");
        localStorage.removeItem("userName");
        localStorage.removeItem(CART_STORAGE_KEY);

        window.location.href = "/";
    }
}

export function renderUserInfo() {
    const email = getUserEmail();
    const emailElement = document.getElementById("userEmail");
    const avatarElement = document.getElementById("userAvatar");

    if (emailElement) {
        emailElement.textContent = email;
    }

    if (avatarElement) {
        avatarElement.textContent = email.charAt(0).toUpperCase();
    }
}