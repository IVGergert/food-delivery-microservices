import {
    requestJson,
    jsonOptions
} from "./api-client.js";

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
