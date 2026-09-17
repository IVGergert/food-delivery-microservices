import {
    getMyOrders
} from "./api.js";

import {
    escapeHtml,
    formatDate
} from "../../common/js/utils.js";

import {
    formatPrice
} from "./utils.js";


function getOrderStatusTitle(status) {
    const statuses = {
        PENDING_PAYMENT: "Ожидает оплаты",
        PAYMENT_FAILED: "Ошибка оплаты",
        CASH_ON_DELIVERY: "Оплата при получении",
        PAID: "Оплачен",
        DELIVERY_ASSIGNED: "Курьер назначен 🛵",
        IN_DELIVERY: "В пути 🚚",
        DELIVERED: "Доставлен 🎉",
        CANCELLED: "Отменён"
    };

    return statuses[status] || status || "Неизвестно";
}

function getOrderStatusClass(status) {
    switch (status) {
        case "PAID":
        case "DELIVERED":
            return "status-success";

        case "DELIVERY_ASSIGNED":
        case "IN_DELIVERY":
            return "status-info";

        case "PENDING_PAYMENT":
        case "CASH_ON_DELIVERY":
            return "status-warning";

        case "PAYMENT_FAILED":
        case "CANCELLED":
            return "status-danger";

        default:
            return "status-default";
    }
}

export async function loadMyOrders() {
    const container = document.getElementById("ordersList");

    const empty = document.getElementById("ordersEmpty");

    const loading = document.getElementById("ordersLoading");

    if (!container) return;

    if (loading) {
        loading.classList.remove("hidden");
    }

    if (empty) {
        empty.classList.add("hidden");
    }

    container.innerHTML = "";

    try {
        const orders = await getMyOrders();

        renderOrders(orders);

    } catch (error) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>

                <h3>Не удалось загрузить заказы</h3>

                <p>
                    ${escapeHtml(error.message)}
                </p>
            </div>
        `;
    } finally {
        if (loading) {
            loading.classList.add("hidden");
        }
    }
}

function renderOrders(orders) {
    const container = document.getElementById("ordersList");
    const empty = document.getElementById("ordersEmpty");

    if (!container) return;

    container.innerHTML = "";

    if (!orders || orders.length === 0) {
        if (empty) {
            empty.classList.remove("hidden");
        }

        return;
    }

    if (empty) {
        empty.classList.add("hidden");
    }

    orders.forEach(order => {
        container.appendChild(createOrderCard(order));
    });
}

function createOrderCard(order) {
    const card = document.createElement("article");
    card.className = "order-card";

    const statusClass = getOrderStatusClass(order.orderStatus);

    card.innerHTML = `
        <div class="order-card-header">
            <div>
                <h3>Заказ №${order.id}</h3>

                <span class="order-date">
                    ${formatDate(order.createdAt)}
                </span>
            </div>

            <span class="order-status ${statusClass}">
               ${escapeHtml(getOrderStatusTitle(order.orderStatus))}
            </span>
        </div>

        <div class="order-card-info">
            <div class="info-col-address">
                <span>Адрес</span>

                <strong title="${escapeHtml(order.address || "")}">
                    ${escapeHtml(order.address || "-")}
                </strong>
            </div>

            <div>
                <span>Позиций</span>

                <strong>
                    ${order.items?.length || 0}
                </strong>
            </div>

            <div>
                <span>Сумма</span>

                <strong>${formatPrice(order.totalAmount)}</strong>
            </div>

            <div class="order-card-action">
                <button type="button" class="secondary-button order-details-button"> 
                    Подробнее
                </button>
            </div>
        </div>
    `;

    card
        .querySelector(".order-details-button")
        .addEventListener("click", () => {
            openOrderDetails(order);
        });

    return card;
}

function openOrderDetails(order) {
    const modal = document.getElementById("orderModal");
    const title = document.getElementById("orderModalTitle");
    const status = document.getElementById("orderModalStatus");
    const address = document.getElementById("orderAddress");
    const courier = document.getElementById("orderCourier");
    const eta = document.getElementById("orderEta");
    const total = document.getElementById("orderTotal");

    if (!modal) return;

    if (title) {
        title.textContent = `Заказ №${order.id}`;
    }

    if (status) {
        status.textContent = getOrderStatusTitle(order.orderStatus);
        status.className = `order-status ${getOrderStatusClass(order.orderStatus)}`;
    }

    if (address) {
        address.textContent =
            order.address
                ? order.address
                : "-";
    }

    if (courier) {
        courier.textContent =
            order.courierName
                ? order.courierName
                : "-";
    }

    if (eta) {
        eta.textContent =
            order.etaMinutes != null
                ? `${order.etaMinutes} мин.`
                : "-";
    }

    if (total) {
        total.textContent = formatPrice(order.totalAmount);
    }

    renderOrderDetailsItems(order.items || []);
    modal.classList.remove("hidden");
}

function renderOrderDetailsItems(items) {
    const container =
        document.getElementById("orderItems");

    if (!container) return;

    container.innerHTML = "";

    if (!items || items.length === 0) {
        container.innerHTML = `
            <div class="order-items-empty">
                Состав заказа недоступен.
            </div>
        `;

        return;
    }

    items.forEach(item => {
        const element = document.createElement("div");

        element.className = "order-item";

        const price =
            item.priceAtPurchase ||
            item.price ||
            0;

        const qty = item.quantity || 1;

        const itemTotal = Number(price) * Number(qty);

        element.innerHTML = `
            <div class="order-item-info">
                <strong>
                    ${escapeHtml(item.itemName || item.name || `Товар #${item.itemId}`)}
                </strong>

                <span>
                    ${qty} × ${formatPrice(price)}
                </span>
            </div>

            <strong class="order-item-total">
                ${formatPrice(itemTotal)}
            </strong>
        `;

        container.appendChild(element);
    });
}

export function closeOrderDetails() {
    document
        .getElementById("orderModal")
        ?.classList.add("hidden");
}
