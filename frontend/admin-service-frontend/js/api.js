import {
    logout
} from "./state.js";

import {
    getErrorMessage
} from "../../common/error-handler.js";

function getCsrfToken() {
    const cookie = document.cookie
        .split("; ")
        .find(row => row.startsWith("XSRF-TOKEN="));

    if (!cookie) {
        return null;
    }

    return decodeURIComponent(
        cookie.substring("XSRF-TOKEN=".length)
    );
}

async function ensureCsrfToken() {
    const existingToken = getCsrfToken();

    if (existingToken) {
        return existingToken;
    }

    const response = await fetch("/api/auth/csrf", {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error(await getErrorMessage(response));
    }

    const responseToken = (await response.text()).trim();

    const cookieToken = getCsrfToken();

    return cookieToken || responseToken;
}

async function refreshAccessToken() {
    const csrfToken = await ensureCsrfToken();

    const response = await fetch("/api/auth/refresh", {
        method: "POST",
        credentials: "include",
        headers: {
            "X-XSRF-TOKEN": csrfToken
        }
    });

    if (!response.ok) {
        return false;
    }

    return true;
}

async function request(url, options = {}, retry = true) {
    const method =
        (options.method || "GET").toUpperCase();

    const headers = {
        ...(options.headers || {})
    };

    if (
        ["POST", "PUT", "PATCH", "DELETE"]
            .includes(method)
    ) {
        const csrfToken = await ensureCsrfToken();

        headers["X-XSRF-TOKEN"] = csrfToken;
    }

    const response = await fetch(url, {
        ...options,
        method,
        credentials: "include",
        headers
    });

    if (response.status === 401 && retry) {
        const refreshed = await refreshAccessToken();

        if (refreshed) {
            return request(url, options, false);
        }

        logout();
        return null;
    }

    if (response.status === 204) {
        return null;
    }

    if (!response.ok) {
        const message = await getErrorMessage(response);
        throw new Error(message);
    }

    return response.json();
}

export const getCourierStatus = () =>
    request("/api/deliveries/courier/status");

export const getTodayStatistics = () =>
    request("/api/deliveries/statistics/today");

export const getCurrentDeliveryRequest = () =>
    request("/api/deliveries/current");

export const getWaitingDeliveriesRequest = () =>
    request("/api/deliveries/waiting");

export const getHistoryDeliveriesRequest = () =>
    request("/api/deliveries/history");

export const goOnlineRequest = () =>
    request("/api/deliveries/courier/go-online", {
        method: "POST"
    });

export const goOfflineRequest = () =>
    request("/api/deliveries/courier/go-offline", {
        method: "POST"
    });

export const validateLogoutRequest = () =>
    request("/api/deliveries/courier/validate-logout", {
        method: "POST"
    });

export const acceptDeliveryRequest = orderId =>
    request(`/api/deliveries/${orderId}/accept`, {
        method: "POST"
    });

export const pickUpOrderRequest = orderId =>
    request(`/api/deliveries/${orderId}/pickup`, {
        method: "POST"
    });

export const completeDeliveryRequest = orderId =>
    request(`/api/deliveries/${orderId}/complete`, {
        method: "POST"
    });

export function logoutRequest() {
    return request("/api/auth/logout", {
        method: "POST"
    });
}