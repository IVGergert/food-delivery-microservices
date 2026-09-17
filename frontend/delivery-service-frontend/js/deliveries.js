import * as api from "./api.js";

import {
    showError,
    showSuccess
} from "../../common/js/notifications.js";

import {
    formatDate,
    escapeHtml
} from "../../common/js/utils.js";

export const state = {
    courierStatus: "OFFLINE",
    currentDelivery: null,
    waitingDeliveries: [],
    historyDeliveries: []
};

const DELIVERY_STATUS_TITLES = {
    WAITING_FOR_COURIER: "Ожидает курьера",
    COURIER_ASSIGNED: "Назначен курьер",
    PICKED_UP: "Заказ забран",
    DELIVERED: "Доставлен 🎉"
};

function getDeliveryStatusTitle(status) {
    return DELIVERY_STATUS_TITLES[status] || status || "В работе";
}

function updateStatusUI() {
    const toggleBtn = document.getElementById("statusToggleButton");
    const toggleText = document.getElementById("statusToggleText");
    const statusBadge = document.getElementById("courierStatusBadge");
    const isOnline = state.courierStatus !== "OFFLINE";

    toggleBtn?.classList.toggle("online", isOnline);
    toggleBtn?.classList.toggle("offline", !isOnline);

    if (toggleText) {
        toggleText.textContent = isOnline ? "На линии (Завершить)" : "Выйти на линию";
    }

    statusBadge?.classList.toggle("online", isOnline);
    statusBadge?.classList.toggle("offline", !isOnline);

    if (statusBadge) {
        statusBadge.textContent = isOnline ? "Онлайн" : "Оффлайн";
    }
}

export async function toggleCourierStatus() {
    if (state.courierStatus === "OFFLINE") {
        await goOnline();
        return;
    }

    await goOffline();
}

export async function fetchCourierStatus() {
    try {
        const data = await api.getCourierStatus();
        state.courierStatus = data?.status || "OFFLINE";
        updateStatusUI();
    } catch (error) {
        showError(error.message);
    }
}

async function goOnline() {
    try {
        const data = await api.goOnline();
        state.courierStatus = data?.status || state.courierStatus;
        updateStatusUI();
        showSuccess("Вы успешно вышли на линию!");
        await fetchWaitingDeliveries();
    } catch (error) {
        showError(error.message);
    }
}

async function goOffline() {
    try {
        const data = await api.goOffline();
        state.courierStatus = data?.status || state.courierStatus;
        updateStatusUI();
        showSuccess("Вы ушли с линии.");
    } catch (error) {
        showError(error.message);
    }
}

export async function fetchCurrentDelivery() {
    document.getElementById("currentLoading")?.classList.remove("hidden");

    try {
        state.currentDelivery = await api.getCurrentDelivery();
        renderCurrentDelivery();
    } catch (error) {
        state.currentDelivery = null;
        renderCurrentDelivery();
        showError(error.message);
    } finally {
        document.getElementById("currentLoading")?.classList.add("hidden");
    }
}

function renderCurrentDelivery() {
    const card = document.getElementById("activeDeliveryCard");
    const empty = document.getElementById("noActiveDelivery");
    const pickupBtn = document.getElementById("pickupButton");
    const completeBtn = document.getElementById("completeButton");
    const delivery = state.currentDelivery;

    if (!delivery) {
        card?.classList.add("hidden");
        empty?.classList.remove("hidden");
        return;
    }

    empty?.classList.add("hidden");
    card?.classList.remove("hidden");

    document.getElementById("activeOrderId").textContent = `Заказ №${delivery.orderId}`;
    document.getElementById("activeAddress").textContent = delivery.address || "Не указан";
    document.getElementById("activeEta").textContent = delivery.etaMinutes ? `~${delivery.etaMinutes} мин.` : "-";
    document.getElementById("activeDeliveryStatus").textContent = getDeliveryStatusTitle(delivery.deliveryStatus);

    if (delivery.deliveryStatus === "COURIER_ASSIGNED") {
        document.getElementById("activeCourierState").textContent = "Едет в ресторан 🏪";
        pickupBtn?.classList.remove("hidden");
        completeBtn?.classList.add("hidden");
    } else if (delivery.deliveryStatus === "PICKED_UP") {
        document.getElementById("activeCourierState").textContent = "В пути к клиенту 🚚";
        pickupBtn?.classList.add("hidden");
        completeBtn?.classList.remove("hidden");
    } else {
        pickupBtn?.classList.add("hidden");
        completeBtn?.classList.add("hidden");
    }
}

export async function acceptDelivery(orderId) {
    try {
        await api.acceptDelivery(orderId);
        showSuccess(`Заказ №${orderId} успешно принят!`);
        await fetchCourierStatus();
        await fetchCurrentDelivery();
        return true;
    } catch (error) {
        showError(error.message);
        return false;
    }
}

export async function pickUpOrder(orderId) {
    try {
        await api.pickUpOrder(orderId);
        showSuccess("Заказ забран из ресторана! Направляйтесь к клиенту.");
        await fetchCourierStatus();
        await fetchCurrentDelivery();
    } catch (error) {
        showError(error.message);
    }
}

export async function completeDelivery(orderId) {
    try {
        await api.completeDelivery(orderId);
        showSuccess(`Заказ №${orderId} успешно доставлен!`);
        await fetchCourierStatus();
        await fetchTodayStats();
        await fetchCurrentDelivery();
    } catch (error) {
        showError(error.message);
    }
}

export async function fetchWaitingDeliveries() {
    const container = document.getElementById("waitingList");
    const empty = document.getElementById("waitingEmpty");
    const loading = document.getElementById("waitingLoading");

    loading?.classList.remove("hidden");
    empty?.classList.add("hidden");

    if (container) {
        container.innerHTML = "";
    }

    try {
        state.waitingDeliveries = await api.getWaitingDeliveries() || [];
        renderWaitingDeliveries();
    } catch (error) {
        if (container) {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">⚠️</div>
                    <h3>Не удалось загрузить заказы</h3>
                    <p>${escapeHtml(error.message)}</p>
                </div>
            `;
        }
    } finally {
        loading?.classList.add("hidden");
    }
}

function renderWaitingDeliveries() {
    const container = document.getElementById("waitingList");
    const empty = document.getElementById("waitingEmpty");

    if (!container) {
        return;
    }

    container.innerHTML = "";
    const deliveries = state.waitingDeliveries;

    if (!deliveries.length) {
        empty?.classList.remove("hidden");
        return;
    }

    empty?.classList.add("hidden");

    deliveries.forEach(delivery => {
        const card = document.createElement("article");
        card.className = "delivery-card";
        card.innerHTML = `
            <div class="delivery-card-header">
                <h3>Заказ №${delivery.orderId}</h3>
                <span class="order-status status-warning">Ожидает курьера</span>
            </div>
            <div class="delivery-card-body">
                <div>
                    <span>Адрес доставки</span>
                    <strong>${escapeHtml(delivery.address || "Не указан")}</strong>
                </div>
                <div>
                    <span>Время на доставку</span>
                    <strong>~${delivery.etaMinutes ?? "-"} мин.</strong>
                </div>
                <div>
                    <button type="button" class="primary-button accept-btn" data-order-id="${delivery.orderId}">
                        Принять заказ
                    </button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

export async function fetchTodayStats() {
    try {
        const data = await api.getTodayStatistics();
        const countElement = document.getElementById("todayCount");

        if (countElement) {
            countElement.textContent = data?.completedToday ?? 0;
        }
    } catch (error) {
        showError(error.message);
    }
}

export async function fetchHistoryDeliveries() {
    const container = document.getElementById("historyList");
    const empty = document.getElementById("historyEmpty");
    const loading = document.getElementById("historyLoading");

    loading?.classList.remove("hidden");
    empty?.classList.add("hidden");

    if (container) {
        container.innerHTML = "";
    }

    try {
        state.historyDeliveries = await api.getHistoryDeliveries() || [];
        renderHistory();
    } catch (error) {
        if (container) {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">⚠️</div>
                    <h3>Не удалось загрузить историю</h3>
                    <p>${escapeHtml(error.message)}</p>
                </div>
            `;
        }
    } finally {
        loading?.classList.add("hidden");
    }
}

function renderHistory() {
    const container = document.getElementById("historyList");
    const empty = document.getElementById("historyEmpty");

    if (!container) {
        return;
    }

    container.innerHTML = "";
    const deliveries = state.historyDeliveries;

    if (!deliveries.length) {
        empty?.classList.remove("hidden");
        return;
    }

    empty?.classList.add("hidden");

    deliveries.forEach(delivery => {
        const card = document.createElement("article");
        card.className = "delivery-card";
        card.innerHTML = `
            <div class="delivery-card-header">
                <div>
                    <h3>Заказ №${delivery.orderId}</h3>
                    <span class="order-date">
                        Доставка завершена: ${formatDate(delivery.completedAt)}
                    </span>
                </div>
                <span class="order-status status-success">Доставлен 🎉</span>
            </div>
            <div class="delivery-card-body">
                <div>
                    <span>Адрес доставки</span>
                    <strong>${escapeHtml(delivery.address || "Не указан")}</strong>
                </div>
                <div>
                    <span>Время доставки</span>
                    <strong>${delivery.etaMinutes != null ? `${delivery.etaMinutes} мин.` : "-"}</strong>
                </div>
                <div>
                    <span>Статус</span>
                    <strong>Завершено</strong>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}
