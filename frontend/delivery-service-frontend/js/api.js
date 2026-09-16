import {
    postJson,
    requestJson
} from "../common/js/api-client.js";

export const getCourierStatus = () =>
    requestJson("/api/deliveries/courier/status");

export const getTodayStatistics = () =>
    requestJson("/api/deliveries/statistics/today");

export const getCurrentDeliveryRequest = () =>
    requestJson("/api/deliveries/current");

export const getWaitingDeliveriesRequest = () =>
    requestJson("/api/deliveries/waiting");

export const getHistoryDeliveriesRequest = () =>
    requestJson("/api/deliveries/history");

export const goOnlineRequest = () =>
    postJson("/api/deliveries/courier/go-online", {});

export const goOfflineRequest = () =>
    postJson("/api/deliveries/courier/go-offline", {});

export const validateLogoutRequest = () =>
    postJson("/api/deliveries/courier/validate-logout", {});

export const acceptDeliveryRequest = orderId =>
    postJson(`/api/deliveries/${orderId}/accept`, {});

export const pickUpOrderRequest = orderId =>
    postJson(`/api/deliveries/${orderId}/pickup`, {});

export const completeDeliveryRequest = orderId =>
    postJson(`/api/deliveries/${orderId}/complete`, {});

export function logoutRequest() {
    return postJson("/api/auth/logout", {}, false);
}
