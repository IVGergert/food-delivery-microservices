import {
    requestJson
} from "../../common/js/api-client.js";

export function login(payload) {
    return requestJson(
        "/api/auth/login",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        },
        false
    );
}

export function register(payload) {
    return requestJson(
        "/api/auth/register",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        },
        false
    );
}
