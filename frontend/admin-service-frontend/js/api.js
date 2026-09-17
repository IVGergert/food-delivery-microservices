import {
    requestJson
} from "../../common/js/api-client.js";

export function getProfile() {
    return requestJson("/api/users/me/profile");
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
