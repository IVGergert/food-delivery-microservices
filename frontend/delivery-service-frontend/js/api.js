import {
    requestJson,
    jsonOptions
} from "../../common/js/api-client.js";

export {
    getProfile,
    updateProfile,
    changeEmail,
    changePassword
} from "../../common/js/profile-api.js";

export {
    logout
} from "../../common/js/session-api.js";

export function getCourierStatus() {
    return requestJson("/api/deliveries/courier/status");
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
