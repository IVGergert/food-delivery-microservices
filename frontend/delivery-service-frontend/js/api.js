import {
    requestJson
} from "../../common/js/api-client.js";

function jsonOptions(method, body = {}) {
    return {
        method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    };
}

export function getProfile() {
    return requestJson("/api/users/me/profile");
}

export function updateProfile(payload) {
    return requestJson(
        "/api/users/me/profile",
        jsonOptions("PUT", payload)
    );
}

export function changeEmail(payload) {
    return requestJson(
        "/api/users/me/email",
        jsonOptions("PUT", payload)
    );
}

export function changePassword(payload) {
    return requestJson(
        "/api/users/me/password",
        jsonOptions("PUT", payload)
    );
}

export function logout() {
    return requestJson(
        "/api/auth/logout",
        {
            method: "POST"
        },
        false
    );
}

export function getCourierStatus() {
    return requestJson(
        "/api/deliveries/courier/status"
    );
}

export function goOnline() {
    return requestJson(
        "/api/deliveries/courier/go-online",
        jsonOptions("POST")
    );
}

export function goOffline() {
    return requestJson(
        "/api/deliveries/courier/go-offline",
        jsonOptions("POST")
    );
}

export function validateLogout() {
    return requestJson(
        "/api/deliveries/courier/validate-logout",
        jsonOptions("POST")
    );
}

export function getCurrentDelivery() {
    return requestJson("/api/deliveries/current");
}

export function getTodayStatistics() {
    return requestJson(
        "/api/deliveries/statistics/today"
    );
}

export function getHistoryDeliveries() {
    return requestJson("/api/deliveries/history");
}

export function getWaitingDeliveries() {
    return requestJson("/api/deliveries/waiting");
}

export function acceptDelivery(orderId) {
    return requestJson(
        `/api/deliveries/${orderId}/accept`,
        jsonOptions("POST")
    );
}

export function pickUpOrder(orderId) {
    return requestJson(
        `/api/deliveries/${orderId}/pickup`,
        jsonOptions("POST")
    );
}

export function completeDelivery(orderId) {
    return requestJson(
        `/api/deliveries/${orderId}/complete`,
        jsonOptions("POST")
    );
}
