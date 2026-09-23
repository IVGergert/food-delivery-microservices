import {
    requestJson,
    jsonOptions
} from "../../common/js/api-client.js";

export function login(payload) {
    return requestJson(
        "/api/auth/login",
        jsonOptions("POST", payload),
        false
    );
}

export function register(payload) {
    return requestJson(
        "/api/auth/register",
        jsonOptions("POST", payload),
        false
    );
}
