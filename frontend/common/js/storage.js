const USER_STORAGE_KEYS = {
    USER_ID: "userId",
    USER_EMAIL: "userEmail",
    USER_ROLE: "userRole",
    USER_NAME: "userName"
};

export const CART_STORAGE_KEY = "cartItems";

export function saveUserData(data) {
    localStorage.setItem(USER_STORAGE_KEYS.USER_ID, data.userId);
    localStorage.setItem(USER_STORAGE_KEYS.USER_EMAIL, data.email);
    localStorage.setItem(USER_STORAGE_KEYS.USER_ROLE, data.role);
}

export function getUserEmail(fallback = "") {
    return localStorage.getItem(USER_STORAGE_KEYS.USER_EMAIL) || fallback;
}

export function clearUserData() {
    Object.values(USER_STORAGE_KEYS).forEach(key => {
        localStorage.removeItem(key);
    });
}
