import {
    requestJson,
    jsonOptions
} from "../../common/js/api-client.js";

export {
    getProfile,
    updateProfile,
    changeEmail,
    changePassword
} from "../../common/js/profile-api.js";

export {
    logout
} from "../../common/js/session-api.js";

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
