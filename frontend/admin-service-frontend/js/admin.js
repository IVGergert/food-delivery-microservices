import * as api from "./api.js";

import {
    showError,
    showSuccess,
    hideAlert
} from "../../common/js/notifications.js";

import {
    escapeHtml
} from "../../common/js/utils.js";


const ROLE_LABELS = {
    ROLE_CUSTOMER: "Клиент",
    ROLE_COURIER: "Курьер",
    ROLE_ADMIN: "Администратор"
};


const TRANSPORT_LABELS = {
    ON_FOOT: "Пешком",
    BICYCLE: "Велосипед",
    CAR: "Автомобиль"
};


let users = [];
let searchValue = "";
let roleFilter = "ALL";


export async function loadUsers() {
    const loading = document.getElementById("usersLoading");
    const empty = document.getElementById("usersEmpty");

    loading?.classList.remove("hidden");
    empty?.classList.add("hidden");

    try {
        const data = await api.getUsers();

        users = Array.isArray(data)
            ? data
            : [];

        renderUsers();

    } catch (error) {
        users = [];
        renderUsers();

        showError(
            error.message
            || "Не удалось загрузить пользователей"
        );

    } finally {
        loading?.classList.add("hidden");
    }
}


function renderUsers() {
    const tableBody = document.getElementById("usersTableBody");
    const empty = document.getElementById("usersEmpty");

    if (!tableBody) {
        return;
    }

    const filteredUsers = getFilteredUsers();

    tableBody.innerHTML = "";

    if (!filteredUsers.length) {
        empty?.classList.remove("hidden");
        return;
    }

    empty?.classList.add("hidden");

    filteredUsers.forEach(user => {
        tableBody.appendChild(
            createUserRow(user)
        );
    });
}


function getFilteredUsers() {
    const normalizedSearch = searchValue.trim().toLowerCase();

    return users.filter(user => {

        if (roleFilter !== "ALL" && user?.role !== roleFilter) {
            return false;
        }

        if (!normalizedSearch) {
            return true;
        }

        const fields = [
            user?.userId,
            user?.email,
            user?.firstName,
            user?.lastName,
            user?.role,
            ROLE_LABELS[user?.role],
            user?.transportType,
            TRANSPORT_LABELS[user?.transportType]
        ];

        return fields.some(value =>
            String(value ?? "")
                .toLowerCase()
                .includes(normalizedSearch)
        );
    });
}


function createUserRow(user) {
    const row = document.createElement("tr");

    const role = user?.role || "";

    const roleLabel = ROLE_LABELS[role]
        || role
        || "-";

    const transportLabel = TRANSPORT_LABELS[user?.transportType]
        || user?.transportType
        || "-";

    row.innerHTML = `
        <td class="user-id">
            ${escapeHtml(user?.userId)}
        </td>

        <td class="user-email">
            ${escapeHtml(user?.email)}
        </td>

        <td class="user-name">
            ${escapeHtml(user?.firstName)}
        </td>

        <td class="user-name">
            ${escapeHtml(user?.lastName)}
        </td>

        <td>
            <span class="role-badge ${getRoleClass(role)}">
                ${escapeHtml(roleLabel)}
            </span>
        </td>

        <td class="user-transport">
            ${escapeHtml(transportLabel)}
        </td>

        <td>
            <div class="table-actions">

                <button
                    type="button"
                    class="table-action-button primary"
                    data-action="edit-user"
                    data-user-id="${escapeHtml(user?.userId)}"
                >
                    Изменить
                </button>

                ${role !== "ROLE_COURIER" ? `
                            <button
                                type="button"
                                class="table-action-button"
                                data-action="change-role"
                                data-user-id="${escapeHtml(user?.userId)}"> Роль</button>
                        `
            : ""
    }

                <button
                    type="button"
                    class="table-action-button"
                    data-action="reset-password"
                    data-user-id="${escapeHtml(user?.userId)}"> Пароль</button>

            </div>
        </td>
    `;

    return row;
}


function getRoleClass(role) {
    switch (role) {
        case "ROLE_ADMIN":
            return "role-admin";

        case "ROLE_COURIER":
            return "role-courier";

        case "ROLE_CUSTOMER":
            return "role-customer";

        default:
            return "";
    }
}


function getUserById(userId) {
    return users.find(
        user => String(user?.userId) === String(userId)
    );
}


function openEditUserModal(user) {
    closeModal();

    const modal = createModal(
        "editUserModal",
        "Изменить пользователя"
    );

    modal.querySelector(".modal-body").innerHTML = `
        <div class="form-group">
            <label for="editUserEmail">
                Email
            </label>

            <input
                type="email"
                id="editUserEmail"
                value="${escapeHtml(user?.email)}"
            >
        </div>

        <div class="form-group">
            <label for="editUserFirstName">
                Имя
            </label>

            <input type="text" id="editUserFirstName" value="${escapeHtml(user?.firstName)}">
        </div>

        <div class="form-group">
            <label for="editUserLastName">
                Фамилия
            </label>

            <input type="text" id="editUserLastName" value="${escapeHtml(user?.lastName)}">
        </div>
    `;

    addModalButtons(modal, "Сохранить", "saveEditUserButton");

    modal
        .querySelector("#saveEditUserButton")
        ?.addEventListener(
            "click",
            () => saveUserChanges(user.userId)
        );

    showModal(modal);
}


async function saveUserChanges(userId) {
    const email = document.getElementById("editUserEmail")
        ?.value
        .trim();

    const firstName = document.getElementById("editUserFirstName")
        ?.value
        .trim();

    const lastName = document.getElementById("editUserLastName")
        ?.value
        .trim();

    try {
        await api.updateUser(
            userId,
            {
                email,
                firstName,
                lastName
            }
        );

        closeModal();

        showSuccess(
            "Данные пользователя успешно обновлены"
        );

        await loadUsers();

    } catch (error) {
        showError(
            error.message
            || "Не удалось обновить пользователя"
        );
    }
}


function openRoleModal(user) {
    closeModal();

    const modal = createModal(
        "changeRoleModal",
        "Изменить роль"
    );

    modal.querySelector(".modal-body").innerHTML = `
        <p class="admin-modal-message">
            Пользователь:
            <strong>${escapeHtml(user?.email)}</strong>
        </p>

        <div class="form-group">
            <label for="newUserRole">
                Новая роль
            </label>

            <select id="newUserRole">

                <option
                    value="ROLE_CUSTOMER"
                    ${user?.role === "ROLE_CUSTOMER" ? "selected" : ""}
                >
                    Клиент
                </option>

                <option
                    value="ROLE_ADMIN"
                    ${user?.role === "ROLE_ADMIN" ? "selected" : ""}
                >
                    Администратор
                </option>

            </select>
        </div>
    `;

    addModalButtons(
        modal,
        "Сохранить",
        "saveRoleButton"
    );

    modal
        .querySelector("#saveRoleButton")
        ?.addEventListener(
            "click",
            () => saveUserRole(user.userId)
        );

    showModal(modal);
}


async function saveUserRole(userId) {
    const role = document
        .getElementById("newUserRole")
        ?.value;

    if (!role) {
        return;
    }

    try {
        await api.changeRole(
            userId,
            role
        );

        closeModal();

        showSuccess(
            "Роль пользователя успешно изменена"
        );

        await loadUsers();

    } catch (error) {
        showError(
            error.message
            || "Не удалось изменить роль"
        );
    }
}


function openResetPasswordModal(user) {
    closeModal();

    const modal = createModal(
        "resetPasswordModal",
        "Сбросить пароль"
    );

    modal.querySelector(".modal-body").innerHTML = `
        <p class="admin-modal-message">
            Новый пароль будет установлен для:
            <strong>${escapeHtml(user?.email)}</strong>
        </p>

        <div class="form-group">
            <label for="resetUserPassword">
                Новый пароль
            </label>

            <input
                type="password"
                id="resetUserPassword"
                autocomplete="new-password"
            >
        </div>

        <div class="form-group">
            <label for="resetUserConfirmPassword">
                Повторите пароль
            </label>

            <input
                type="password"
                id="resetUserConfirmPassword"
                autocomplete="new-password"
            >
        </div>
    `;

    addModalButtons(
        modal,
        "Изменить пароль",
        "saveResetPasswordButton"
    );

    modal
        .querySelector("#saveResetPasswordButton")
        ?.addEventListener(
            "click",
            () => saveResetPassword(user.userId)
        );

    showModal(modal);
}


async function saveResetPassword(userId) {
    const newPassword = document
        .getElementById("resetUserPassword")
        ?.value;

    const confirmPassword = document
        .getElementById("resetUserConfirmPassword")
        ?.value;

    if (newPassword !== confirmPassword) {
        showError("Пароли не совпадают");
        return;
    }

    try {
        await api.resetPassword(userId,
            {
                newPassword,
                confirmPassword
            }
        );

        closeModal();
        showSuccess("Пароль пользователя успешно изменён");

    } catch (error) {
        showError(error.message || "Не удалось изменить пароль");
    }
}


function openCreateAdminModal() {
    openCreateUserModal("admin");
}


function openCreateCourierModal() {
    openCreateUserModal("courier");
}


function openCreateUserModal(type) {
    closeModal();

    const isCourier = type === "courier";

    const title = isCourier
            ? "Добавить курьера"
            : "Добавить администратора";

    const modal = createModal("createUserModal", title
    );

    modal.querySelector(".modal-body").innerHTML = `
        <div class="form-group">
            <label for="createUserEmail">
                Email
            </label>

            <input type="email" id="createUserEmail" autocomplete="email">
        </div>

        <div class="form-group">
            <label for="createUserPassword">
                Пароль
            </label>

            <input type="password" id="createUserPassword" autocomplete="new-password">
        </div>

        <div class="form-group">
            <label for="createUserConfirmPassword">
                Повторите пароль
            </label>

            <input type="password" id="createUserConfirmPassword" autocomplete="new-password">
        </div>

        <div class="form-group">
            <label for="createUserFirstName">
                Имя
            </label>

            <input type="text" id="createUserFirstName" autocomplete="given-name">
        </div>

        <div class="form-group">
            <label for="createUserLastName">
                Фамилия
            </label>

            <input type="text" id="createUserLastName" autocomplete="family-name">
        </div>

        ${isCourier ? `
                    <div class="form-group">
                        <label for="createCourierTransportType">
                            Вид транспорта
                        </label>

                        <select id="createCourierTransportType">

                            <option value="ON_FOOT">
                                Пешком
                            </option>

                            <option value="BICYCLE">
                                Велосипед
                            </option>

                            <option value="CAR">
                                Автомобиль
                            </option>

                        </select>
                    </div>
                `
            : ""
    }
    `;

    addModalButtons(
        modal,
        isCourier
            ? "Создать курьера"
            : "Создать администратора",
        "createUserSubmitButton"
    );

    modal
        .querySelector("#createUserSubmitButton")
        ?.addEventListener(
            "click",
            () => createUser(type)
        );

    showModal(modal);
}


async function createUser(type) {
    const email = document
        .getElementById("createUserEmail")
        ?.value
        .trim();

    const password = document
        .getElementById("createUserPassword")
        ?.value;

    const confirmPassword = document
        .getElementById("createUserConfirmPassword")
        ?.value;

    const firstName = document
        .getElementById("createUserFirstName")
        ?.value
        .trim();

    const lastName = document
        .getElementById("createUserLastName")
        ?.value
        .trim();

    if (password !== confirmPassword) {
        showError("Пароли не совпадают");
        return;
    }

    try {
        if (type === "courier") {

            const transportType = document
                .getElementById("createCourierTransportType")
                ?.value;

            await api.createCourier({
                email,
                password,
                confirmPassword,
                firstName,
                lastName,
                transportType
            });

            closeModal();

            showSuccess(
                "Курьер успешно создан"
            );

        } else {

            await api.createAdmin({
                email,
                password,
                confirmPassword,
                firstName,
                lastName
            });

            closeModal();

            showSuccess(
                "Администратор успешно создан"
            );
        }

        await loadUsers();

    } catch (error) {
        showError(
            error.message
            || "Не удалось создать пользователя"
        );
    }
}


function createModal(id, title) {
    const overlay =
        document.createElement("div");

    overlay.id = id;
    overlay.className = "modal-overlay";

    overlay.innerHTML = `
        <div class="modal admin-modal">

            <div class="modal-header">

                <h2>
                    ${escapeHtml(title)}
                </h2>

                <button
                    type="button"
                    class="modal-close"
                    data-modal-close
                    aria-label="Закрыть"
                >
                    ×
                </button>

            </div>

            <div class="modal-body"></div>

            <div class="modal-footer"></div>

        </div>
    `;

    document.body.appendChild(overlay);

    overlay.addEventListener(
        "click",
        event => {

            if (
                event.target === overlay
                || event.target.closest("[data-modal-close]")
            ) {
                closeModal();
            }
        }
    );

    return overlay;
}


function addModalButtons(
    modal,
    submitText,
    submitId
) {
    const footer =
        modal.querySelector(".modal-footer");

    if (!footer) {
        return;
    }

    footer.innerHTML = `
        <button
            type="button"
            class="secondary-button"
            data-modal-close
        >
            Отмена
        </button>

        <button
            type="button"
            class="primary-button"
            id="${submitId}"
        >
            ${escapeHtml(submitText)}
        </button>
    `;
}


function showModal(modal) {
    requestAnimationFrame(() => {
        modal.classList.add("visible");
    });
}


function closeModal() {
    document
        .querySelectorAll(".modal-overlay")
        .forEach(modal => modal.remove());
}


document.addEventListener(
    "click",
    event => {

        const actionButton = event.target.closest("[data-action]");

        if (!actionButton) {
            return;
        }

        const action = actionButton.dataset.action;
        const userId = actionButton.dataset.userId;
        const user = getUserById(userId);

        if (!user) {
            showError("Пользователь не найден");
            return;
        }

        if (action === "edit-user") {
            openEditUserModal(user);
            return;
        }

        if (action === "change-role") {
            openRoleModal(user);
            return;
        }

        if (action === "reset-password") {
            openResetPasswordModal(user);
        }
    }
);


document.addEventListener(
    "input",
    event => {

        if (event.target.matches("#userSearchInput")) {
            searchValue = event.target.value;
            renderUsers();
        }
    }
);


document.addEventListener(
    "change",
    event => {

        if (event.target.matches("#userRoleFilter")) {
            roleFilter = event.target.value;
            renderUsers();
        }
    }
);


document.addEventListener(
    "click",
    event => {

        if (event.target.closest("#refreshUsersButton")) {
            hideAlert();
            loadUsers();
            return;
        }

        if (event.target.closest("#addCourierButton")) {
            openCreateCourierModal();
            return;
        }

        if (event.target.closest("#addAdminButton")) {
            openCreateAdminModal();
        }
    }
);


document.addEventListener(
    "keydown",
    event => {

        if (event.key === "Escape") {
            closeModal();
        }
    }
);

async function logout() {
    try {
        await api.logout();
    } catch {} finally {
        window.location.href = "/";
    }
}


document.addEventListener(
    "click",
    async event => {

        if (event.target.closest("#logoutButton")) {
            await logout();
        }
    }
);

export function closeAdminModals() {
    closeModal();
}