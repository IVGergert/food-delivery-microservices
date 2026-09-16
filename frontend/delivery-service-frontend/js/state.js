import {
    logoutRequest
} from "./api.js";

import {
    clearUserData,
    getUserEmail
} from "../common/js/storage.js";

export const state = {
    courierStatus: "OFFLINE",
    currentDelivery: null,
    waitingDeliveries: [],
    historyDeliveries: []
};


export async function logout() {
    try {
        await logoutRequest();
    } catch {
        // The local session is cleared even when the server logout fails.
    } finally {
        clearUserData();
        window.location.href = "/";
    }
}
