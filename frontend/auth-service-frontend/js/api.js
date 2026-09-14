import {
    getErrorMessage
} from "/common/error-handler.js";

function getCsrfToken() {
    const cookie = document.cookie
        .split("; ")
        .find(row => row.startsWith("XSRF-TOKEN="));

    return cookie
        ? decodeURIComponent(cookie.split("=")[1])
        : null;
}


async function ensureCsrfToken() {
    let csrfToken = getCsrfToken();

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

    return await response.text();
}

async function request(url, options = {}) {
    const method = (options.method || "GET").toUpperCase();

    const headers = {
        "Content-Type": "application/json",
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

    if (!response.ok) {
        throw new Error(await getErrorMessage(response));
    }

    return response.json();
}

export function loginRequest(email, password) {
    return request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
            email,
            password
        })
    });
}

export function registerRequest(email, password, confirmPassword) {
    return request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
            email,
            password,
            confirmPassword
        })
    });
}

export function logoutRequest() {
    return request("/api/auth/logout", {
        method: "POST"
    });
}