import {
    requestJson
} from "../../common/js/api-client.js";

function jsonOptions(method, body) {
    return {
        method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    };
}

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

export function logout() {
    return requestJson(
        "/api/auth/logout",
        {
            method: "POST"
        },
        false
    );
}

export function getMenu() {
    return requestJson("/api/menu");
}

export function getMyOrders() {
    return requestJson("/api/orders/my");
}

export function createOrder(payload) {
    return requestJson(
        "/api/orders",
        jsonOptions("POST", payload)
    );
}

export function payOrder(orderId, paymentMethod) {
    return requestJson(
        `/api/orders/${orderId}/pay`,
        jsonOptions("POST", { paymentMethod })
    );
}

export function cancelOrder(orderId) {
    return requestJson(
        `/api/orders/${orderId}/cancel`,
        {
            method: "POST"
        }
    );
}
