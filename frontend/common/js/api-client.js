import {
    getErrorMessage
} from "../error-handler.js";

import {
    clearUserData
} from "./storage.js";

const MUTATING_METHODS = new Set([
    "POST",
    "PUT",
    "PATCH",
    "DELETE"
]);

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
    return getCsrfToken() || responseToken;
}

async function refreshAccessToken() {
    try {
        const csrfToken = await ensureCsrfToken();

        const response = await fetch("/api/auth/refresh", {
            method: "POST",
            credentials: "include",
            headers: {
                "X-XSRF-TOKEN": csrfToken
            }
        });

        return response.ok;
    } catch {
        return false;
    }
}

function redirectToLogin() {
    clearUserData();
    window.location.href = "/";
}

export async function request(
    url,
    options = {},
    retryUnauthorized = true
) {
    const method = (options.method || "GET").toUpperCase();
    const headers = {
        ...(options.headers || {})
    };

    if (MUTATING_METHODS.has(method)) {
        headers["X-XSRF-TOKEN"] = await ensureCsrfToken();
    }

    const response = await fetch(url, {
        ...options,
        method,
        credentials: "include",
        headers
    });

    if (response.status === 401 && retryUnauthorized) {
        const refreshed = await refreshAccessToken();

        if (refreshed) {
            return request(url, options, false);
        }

        redirectToLogin();
    }

    return response;
}

export function get(url, headers = {}) {
    return request(url, {
        method: "GET",
        headers
    });
}

export function post(url, body, headers = {}) {
    return request(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...headers
        },
        body: JSON.stringify(body)
    });
}

export function put(url, body, headers = {}) {
    return request(url, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            ...headers
        },
        body: JSON.stringify(body)
    });
}

export async function requestJson(
    url,
    options = {},
    retryUnauthorized = true
) {
    const response = await request(
        url,
        options,
        retryUnauthorized
    );

    if (!response.ok) {
        throw new Error(await getErrorMessage(response));
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

export function postJson(url, body, retryUnauthorized = true) {
    return requestJson(
        url,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        },
        retryUnauthorized
    );
}
