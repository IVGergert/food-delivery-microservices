function getAlertElement() {
    return document.getElementById("alertBox");
}

export function showError(message) {
    const alertBox = getAlertElement();

    if (!alertBox) {
        return;
    }

    alertBox.textContent = message;
    alertBox.className = "alert alert-danger";
}

export function showSuccess(message) {
    const alertBox = getAlertElement();

    if (!alertBox) {
        return;
    }

    alertBox.textContent = message;
    alertBox.className = "alert alert-success";

    clearTimeout(alertBox._hideTimeout);
    alertBox._hideTimeout = setTimeout(() => {
        alertBox.className = "alert hidden";
    }, 3500);
}

export function hideAlert() {
    const alertBox = getAlertElement();

    if (!alertBox) {
        return;
    }

    clearTimeout(alertBox._hideTimeout);
    alertBox.className = "alert hidden";
}
