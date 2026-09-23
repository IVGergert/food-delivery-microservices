import {
    updatePageHeader
} from "./layout.js";

const ROLE_LABELS = {
    ROLE_CUSTOMER: "Клиент",
    ROLE_COURIER: "Курьер",
    ROLE_ADMIN: "Администратор"
};

export async function showProfile(api) {
    let profileSection = document.getElementById("profileSection");

    if (!profileSection) {
        profileSection = createProfileSection(api);

        document
            .querySelector(".main-content")
            ?.appendChild(profileSection);
    }

    profileSection?.classList.remove("hidden");

    updatePageHeader(
        "Профиль",
        "Ваши личные данные"
    );

    await loadProfile(api);
}

function createProfileSection(api) {
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
        .addEventListener("click", () => updateProfile(api));

    section
        .querySelector("#changeEmailButton")
        .addEventListener("click", () => openChangeEmailModal(api));

    section
        .querySelector("#changePasswordButton")
        .addEventListener("click", () => openChangePasswordModal(api));

    section
        .querySelector("#profileLogoutButton")
        .addEventListener("click", event => {
            event.stopPropagation();
            document.dispatchEvent(
                new CustomEvent("profile-logout")
            );
        });

    return section;
}

async function loadProfile(api) {
    try {
        const profile = await api.getProfile();

        const email = document.getElementById("profileEmail");
        const firstName = document.getElementById("profileFirstName");
        const lastName = document.getElementById("profileLastName");
        const role = document.getElementById("profileRole");

        if (email) {
            email.value = profile?.email ?? "";
        }

        if (firstName) {
            firstName.value = profile?.firstName ?? "";
        }

        if (lastName) {
            lastName.value = profile?.lastName ?? "";
        }

        if (role) {
            role.value =
                ROLE_LABELS[profile?.role]
                ?? profile?.role
                ?? "";
        }

        hideProfileMessage();
    } catch (error) {
        showProfileMessage(
            error.message || "Не удалось загрузить профиль",
            false
        );
    }
}

async function updateProfile(api) {
    const firstName = document
        .getElementById("profileFirstName")
        ?.value.trim() || "";

    const lastName = document
        .getElementById("profileLastName")
        ?.value.trim() || "";

    try {
        const profile = await api.updateProfile({
            firstName,
            lastName
        });

        document.getElementById("profileFirstName").value =
            profile?.firstName ?? "";

        document.getElementById("profileLastName").value =
            profile?.lastName ?? "";

        showProfileMessage(
            "Профиль успешно обновлён",
            true
        );
    } catch (error) {
        showProfileMessage(
            error.message || "Не удалось обновить профиль",
            false
        );
    }
}

function openChangeEmailModal(api) {
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
        .addEventListener("click", () => changeEmail(api));
}

async function changeEmail(api) {
    const email = document
        .getElementById("newEmail")
        ?.value.trim() || "";

    const currentPassword = document
        .getElementById("emailCurrentPassword")
        ?.value || "";

    try {
        await api.changeEmail({
            email,
            currentPassword
        });

        closeChangeEmailModal();
        await loadProfile(api);

        showProfileMessage(
            "Email успешно изменён",
            true
        );

    } catch (error) {
        showModalError(
            "changeEmailError",
            error.message || "Не удалось изменить email"
        );
    }
}

function closeChangeEmailModal() {
    closeModal("changeEmailModal");
}

function openChangePasswordModal(api) {
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

    modal.querySelector("#closeChangePasswordModal")
        .addEventListener("click", closeChangePasswordModal);

    modal.querySelector("#cancelChangePassword")
        .addEventListener("click", closeChangePasswordModal);

    modal.querySelector("#saveNewPassword")
        .addEventListener("click", () => changePassword(api));
}

async function changePassword(api) {
    const currentPassword = document
        .getElementById("currentPassword")
        ?.value || "";

    const newPassword = document
        .getElementById("newPassword")
        ?.value || "";

    const confirmPassword = document
        .getElementById("confirmPassword")
        ?.value || "";

    if (newPassword !== confirmPassword) {
        showModalError(
            "changePasswordError",
            "Пароли не совпадают"
        );

        return;
    }

    try {
        await api.changePassword({
            currentPassword,
            newPassword,
            confirmPassword
        });

        closeChangePasswordModal();
        showProfileMessage(
            "Пароль успешно изменён",
            true
        );

    } catch (error) {
        showModalError(
            "changePasswordError",
            error.message || "Не удалось изменить пароль"
        );
    }
}

function closeChangePasswordModal() {
    closeModal("changePasswordModal");
}

function closeModal(modalId) {
    document
        .getElementById(modalId)
        ?.remove();
}

function showModalError(elementId, message) {
    setMessage(elementId, message, false);
}

function showProfileMessage(message, success) {
    setMessage("profileError", message, success);
}

function setMessage(elementId, message, success) {
    const element = document.getElementById(elementId);

    if (!element) {
        return;
    }

    element.textContent = message;
    element.className = success
        ? "alert alert-success"
        : "alert alert-danger";
}

function hideProfileMessage() {
    const element = document.getElementById("profileError");

    if (!element) {
        return;
    }

    element.textContent = "";
    element.className = "alert hidden";
}


document.addEventListener("close-modals", () => {
    closeChangeEmailModal();
    closeChangePasswordModal();
});
