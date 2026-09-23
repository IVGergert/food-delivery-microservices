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


export function getUsers() {
    return requestJson("/api/admin/users");
}


export function updateUser(userId, payload) {
    return requestJson(
        `/api/admin/users/${userId}`,
        jsonOptions("PUT", payload)
    );
}


export function changeRole(userId, role) {
    return requestJson(
        `/api/admin/users/${userId}/role`,
        jsonOptions("PUT", {
            role
        })
    );
}


export function resetPassword(userId, payload) {
    return requestJson(
        `/api/admin/users/${userId}/password`,
        jsonOptions("PUT", payload)
    );
}


export function createAdmin(payload) {
    return requestJson(
        "/api/admin/users/admins",
        jsonOptions("POST", payload)
    );
}


export function createCourier(payload) {
    return requestJson(
        "/api/admin/users/couriers",
        jsonOptions("POST", payload)
    );
}