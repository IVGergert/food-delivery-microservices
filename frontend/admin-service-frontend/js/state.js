import {
    logoutRequest
} from "./api.js";

export const state = {
    courierStatus: "OFFLINE",
    currentDelivery: null,
    waitingDeliveries: [],
    historyDeliveries: []
};

export function getUserEmail() {
    return localStorage.getItem("userEmail");
}

export function clearUserData() {
    localStorage.removeItem("userId");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("userRole");
}

export async function logout() {
    try {
        await logoutRequest();
    } catch (error) {
        console.error("Logout error:", error);
    } finally {
        clearUserData();
        window.location.href = "/";
    }
}
