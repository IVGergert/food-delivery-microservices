import {
    requestJson
} from "./api-client.js";

export function logout() {
    return requestJson(
        "/api/auth/logout",
        {
            method: "POST"
        },
        false
    );
}
