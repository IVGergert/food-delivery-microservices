import { postJson } from "../common/js/api-client.js";

export function loginRequest(email, password) {
    return postJson("/api/auth/login", { email, password }, false);
}

export function registerRequest(email, password, confirmPassword) {
    return postJson(
        "/api/auth/register",
        { email, password, confirmPassword },
        false
    );
}
