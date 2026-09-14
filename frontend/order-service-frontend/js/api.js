import {
    getErrorMessage
} from "../../common/error-handler.js";

function getCsrfToken() {
    const cookie = document.cookie
        .split("; ")
        .find(row => row.startsWith("XSRF-TOKEN="));

    return cookie
        ? decodeURIComponent(cookie.split("=")[1])
        : null;
}

async function ensureCsrfToken() {
    const csrfToken = getCsrfToken();

    if (csrfToken) {
        return csrfToken;
    }

    const response = await fetch("/api/auth/csrf", {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error(await getErrorMessage(response));
    }

    return response.text();
}

export async function request(url, options = {}) {
    const method = (options.method || "GET").toUpperCase();

    const headers = {
        ...(options.headers || {})
    };

    if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
        const csrfToken = await ensureCsrfToken();

        headers["X-XSRF-TOKEN"] = csrfToken;
    }

    const response = await fetch(url, {
        ...options,
        method,
        credentials: "include",
        headers
    });

    return response;
}

export async function get(url, headers = {}) {
    return request(url, {
        method: "GET",
        headers
    });
}

export async function post(url, body, headers = {}) {
    return request(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...headers
        },
        body: JSON.stringify(body)
    });
}

export function logoutRequest() {
    return request("/api/auth/logout", {
        method: "POST"
    });
}

export {
    getErrorMessage
};