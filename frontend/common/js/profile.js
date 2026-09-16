import {
    get,
    put
} from "./api-client.js";

import {
    getErrorMessage
} from "../error-handler.js";

const PROFILE_PATH = "/api/users/me/profile";
const EMAIL_PATH = "/api/users/me/email";
const PASSWORD_PATH = "/api/users/me/password";

export async function showProfile({
    containerSelector = ".main-content",
    title = "Профиль",
    subtitle = "Ваши личные данные"
} = {}) {
    let profileSection = document.getElementById("profileSection");

    if (!profileSection) {
        profileSection = createProfileSection();

        document
            .querySelector(containerSelector)
            ?.appendChild(profileSection);
    }

    profileSection?.classList.remove("hidden");

    const titleElement = document.getElementById("pageTitle");
    const subtitleElement = document.getElementById("pageSubtitle");

    if (titleElement) {
        titleElement.textContent = title;
    }

    if (subtitleElement) {
        subtitleElement.textContent = subtitle;
    }

    await loadProfile();
}

export function createProfileSection() {
    const section = document.createElement("section");

    section.id = "profileSection";
    section.className = "content-section hidden";

    section.innerHTML = `
        <div class="profile-card">
            <div class="profile-card-header">
                <h2>Профиль</h2>
            </div>

            <div class="profile-form">
                <div class="profile-section">
                    <h3>Личные данные</h3>

                    <div class="profile-field">
                        <div class="form-group">
                            <label for="profileEmail">Email</label>
                            <input type="email" id="profileEmail" readonly>
                        </div>

                        <button class="secondary-button" id="changeEmailButton" type="button">
                            Изменить email
                        </button>
                    </div>

                    <div class="form-group">
                        <label for="profileFirstName">Имя</label>
                        <input type="text" id="profileFirstName" placeholder="Введите имя">
                    </div>

                    <div class="form-group">
                        <label for="profileLastName">Фамилия</label>
                        <input type="text" id="profileLastName" placeholder="Введите фамилию">
                    </div>

                    <div class="form-group">
                        <label for="profileRole">Роль</label>
                        <input type="text" id="profileRole" readonly>
                    </div>

                    <div class="profile-field">
                        <div class="form-group">
                            <label for="profilePassword">Пароль</label>
                            <input type="password" id="profilePassword" value="********" readonly>
                        </div>

                        <button class="secondary-button" id="changePasswordButton" type="button">
                            Изменить пароль
                        </button>
                    </div>

                    <button class="primary-button" id="saveProfileButton" type="button">
                        Сохранить изменения
                    </button>
                </div>

                <div class="alert hidden" id="profileError"></div>

                <div class="profile-danger">
                    <button class="logout-profile-button" id="profileLogoutButton" type="button">
                        Выйти
                    </button>
                </div>
            </div>
        </div>
    `;

    section
        .querySelector("#saveProfileButton")
        .addEventListener("click", updateProfile);

    section
        .querySelector("#changeEmailButton")
        .addEventListener("click", openChangeEmailModal);

    section
        .querySelector("#changePasswordButton")
        .addEventListener("click", openChangePasswordModal);

    section
        .querySelector("#profileLogoutButton")
        .addEventListener("click", event => {
            event.stopPropagation();
            document.dispatchEvent(new CustomEvent("profile-logout"));
        });

    return section;
}

async function loadProfile() {
    try {
        const response = await get(PROFILE_PATH);

        if (!response.ok) {
            showProfileError(await getErrorMessage(response));
            return;
        }

        const profile = await response.json();

        document.getElementById("profileEmail").value = profile.email ?? "";
        document.getElementById("profileFirstName").value = profile.firstName ?? "";
        document.getElementById("profileLastName").value = profile.lastName ?? "";
        document.getElementById("profileRole").value = profile.role ?? "";

        hideProfileError();
    } catch (error) {
        showProfileError(error.message || "Не удалось загрузить профиль");
    }
}

async function updateProfile() {
    const firstName = document.getElementById("profileFirstName").value;
    const lastName = document.getElementById("profileLastName").value;

    try {
        const response = await put(
            PROFILE_PATH,
            {
                firstName,
                lastName
            }
        );

        if (!response.ok) {
            showProfileError(await getErrorMessage(response));
            return;
        }

        const profile = await response.json();

        document.getElementById("profileFirstName").value = profile.firstName ?? "";
        document.getElementById("profileLastName").value = profile.lastName ?? "";

        showProfileError("Профиль успешно обновлён", true);
    } catch (error) {
        showProfileError(error.message || "Не удалось обновить профиль");
    }
}

function openChangeEmailModal() {
    closeChangeEmailModal();

    const modal = document.createElement("div");

    modal.id = "changeEmailModal";
    modal.className = "modal-overlay";

    modal.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <h2>Изменить email</h2>
                <button class="modal-close" id="closeChangeEmailModal" type="button">×</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label for="newEmail">Новый email</label>
                    <input type="email" id="newEmail" placeholder="Введите новый email">
                </div>

                <div class="form-group">
                    <label for="emailCurrentPassword">Текущий пароль</label>
                    <input type="password" id="emailCurrentPassword" placeholder="Введите текущий пароль">
                </div>

                <div class="alert hidden" id="changeEmailError"></div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-secondary" id="cancelChangeEmail" type="button">Отмена</button>
                <button class="btn btn-primary" id="saveNewEmail" type="button">Сохранить</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    modal
        .querySelector("#closeChangeEmailModal")
        .addEventListener("click", closeChangeEmailModal);

    modal
        .querySelector("#cancelChangeEmail")
        .addEventListener("click", closeChangeEmailModal);

    modal
        .querySelector("#saveNewEmail")
        .addEventListener("click", changeEmail);
}

async function changeEmail() {
    const email = document.getElementById("newEmail").value;
    const currentPassword = document.getElementById("emailCurrentPassword").value;
    const errorElement = document.getElementById("changeEmailError");

    try {
        const response = await put(
            EMAIL_PATH,
            {
                email,
                currentPassword
            }
        );

        if (!response.ok) {
            errorElement.textContent = await getErrorMessage(response);
            errorElement.classList.remove("hidden");
            return;
        }

        closeChangeEmailModal();
        await loadProfile();
        showProfileError("Email успешно изменён", true);
    } catch (error) {
        errorElement.textContent = error.message || "Не удалось изменить email";
        errorElement.classList.remove("hidden");
    }
}

function closeChangeEmailModal() {
    document.getElementById("changeEmailModal")?.remove();
}

function openChangePasswordModal() {
    closeChangePasswordModal();

    const modal = document.createElement("div");

    modal.id = "changePasswordModal";
    modal.className = "modal-overlay";

    modal.innerHTML = `
        <div class="modal">
            <div class="modal-header">
                <h2>Изменить пароль</h2>
                <button class="modal-close" id="closeChangePasswordModal" type="button">×</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label for="currentPassword">Текущий пароль</label>
                    <input type="password" id="currentPassword" placeholder="Введите текущий пароль">
                </div>

                <div class="form-group">
                    <label for="newPassword">Новый пароль</label>
                    <input type="password" id="newPassword" placeholder="Введите новый пароль">
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Подтверждение пароля</label>
                    <input type="password" id="confirmPassword" placeholder="Повторите новый пароль">
                </div>

                <div class="alert hidden" id="changePasswordError"></div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-secondary" id="cancelChangePassword" type="button">Отмена</button>
                <button class="btn btn-primary" id="saveNewPassword" type="button">Сохранить</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    modal
        .querySelector("#closeChangePasswordModal")
        .addEventListener("click", closeChangePasswordModal);

    modal
        .querySelector("#cancelChangePassword")
        .addEventListener("click", closeChangePasswordModal);

    modal
        .querySelector("#saveNewPassword")
        .addEventListener("click", changePassword);
}

async function changePassword() {
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const errorElement = document.getElementById("changePasswordError");

    try {
        const response = await put(
            PASSWORD_PATH,
            {
                currentPassword,
                newPassword,
                confirmPassword
            }
        );

        if (!response.ok) {
            errorElement.textContent = await getErrorMessage(response);
            errorElement.classList.remove("hidden");
            return;
        }

        closeChangePasswordModal();
        showProfileError("Пароль успешно изменён", true);
    } catch (error) {
        errorElement.textContent = error.message || "Не удалось изменить пароль";
        errorElement.classList.remove("hidden");
    }
}

function closeChangePasswordModal() {
    document.getElementById("changePasswordModal")?.remove();
}

function showProfileError(message, success = false) {
    const errorElement = document.getElementById("profileError");

    if (!errorElement) {
        return;
    }

    errorElement.textContent = message;
    errorElement.classList.remove("hidden");
    errorElement.classList.toggle("success", success);
}

function hideProfileError() {
    const errorElement = document.getElementById("profileError");

    if (!errorElement) {
        return;
    }

    errorElement.textContent = "";
    errorElement.classList.add("hidden");
    errorElement.classList.remove("success");
}

if (typeof document !== "undefined") {
    document.addEventListener("close-modals", () => {
        closeChangeEmailModal();
        closeChangePasswordModal();
    });
}
