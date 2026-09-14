const USER_KEYS = {
    USER_ID: "userId",
    USER_EMAIL: "userEmail",
    USER_ROLE: "userRole"
};

export function saveUserData(data) {
    localStorage.setItem(USER_KEYS.USER_ID, data.userId);
    localStorage.setItem(USER_KEYS.USER_EMAIL, data.email);
    localStorage.setItem(USER_KEYS.USER_ROLE, data.role);
}

export function getUserId() {
    return localStorage.getItem(USER_KEYS.USER_ID);
}

export function getUserEmail() {
    return localStorage.getItem(USER_KEYS.USER_EMAIL);
}

export function getUserRole() {
    return localStorage.getItem(USER_KEYS.USER_ROLE);
}

export function clearUserData() {
    localStorage.removeItem(USER_KEYS.USER_ID);
    localStorage.removeItem(USER_KEYS.USER_EMAIL);
    localStorage.removeItem(USER_KEYS.USER_ROLE);
}