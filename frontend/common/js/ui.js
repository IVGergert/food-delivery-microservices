import {
    getUserEmail
} from "./storage.js";

function getAlertElement(elementId = "alertBox") {
    return document.getElementById(elementId);
}

export function showAlert(message,
                          isError = true,
                          elementId = "alertBox") {

    const alertBox = getAlertElement(elementId);

    if (!alertBox) {
        return;
    }

    alertBox.textContent = message;
    alertBox.className = `alert ${
        isError ? "alert-danger" : "alert-success"
    }`;
}

export function showError(message, elementId = "alertBox") {
    showAlert(message, true, elementId);
}

export function showSuccess(message, elementId = "alertBox") {
    const alertBox = getAlertElement(elementId);

    if (!alertBox) {
        return;
    }

    showAlert(message, false, elementId);

    clearTimeout(alertBox._hideTimeout);
    alertBox._hideTimeout = setTimeout(() => {
        alertBox.className = "alert hidden";
    }, 3500);
}

export function hideAlert(elementId = "alertBox") {
    const alertBox = getAlertElement(elementId);

    if (!alertBox) {
        return;
    }

    clearTimeout(alertBox._hideTimeout);
    alertBox.className = "alert hidden";
}

function renderUserInfo({
                            emailElementId = "userEmail",
                            fallback = "Пользователь"} = {}) {

    const email = getUserEmail(fallback);
    const emailElement = document.getElementById(emailElementId);

    if (emailElement) {
        emailElement.textContent = email;
    }
}

export function setButtonLoading(button, loading, loadingText = "Обработка...") {
    if (!button) {
        return;
    }

    if (loading) {
        if (!button.dataset.originalText) {
            button.dataset.originalText = button.textContent;
        }

        button.textContent = loadingText;
        button.disabled = true;
        return;
    }

    button.textContent = button.dataset.originalText || button.textContent;
    button.disabled = false;
}
