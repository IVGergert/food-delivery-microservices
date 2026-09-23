import {
    getErrorMessage
} from "./error-handler.js";

const MUTATING_METHODS = new Set([
    "POST",
    "PUT",
    "PATCH",
    "DELETE"
]);

export function jsonOptions(method, body = {}) {
    return {
        method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    };
}

let refreshPromise = null;

function getCsrfToken() {
    if (typeof document === "undefined") {
        return null;
    }

    const cookie = document.cookie
        .split("; ")
        .find(row => row.startsWith("XSRF-TOKEN="));

    if (!cookie) {
        return null;
    }

    const value = cookie.substring("XSRF-TOKEN=".length);

    try {
        return decodeURIComponent(value);
    } catch {
        return value;
    }
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
    if (refreshPromise) {
        return refreshPromise;
    }

    refreshPromise = (async () => {
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
        } finally {
            refreshPromise = null;
        }
    })();

    return refreshPromise;
}

function redirectToLogin() {
    if (typeof window !== "undefined") {
        window.location.href = "/";
    }
}

async function request(url,
                       options = {},
                       retryUnauthorized = true) {

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

    if (response.status !== 401 || !retryUnauthorized) {
        return response;
    }

    const refreshed = await refreshAccessToken();

    if (!refreshed) {
        redirectToLogin();
        return response;
    }

    return request(url, options, false);
}

export async function requestJson(url,
                                  options = {},
                                  retryUnauthorized = true) {

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
