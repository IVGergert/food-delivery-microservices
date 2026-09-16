import {
    postJson
} from "../common/js/api-client.js";

import {
    clearUserData
} from "../common/js/storage.js";

export async function logout() {
    try {
        await postJson("/api/auth/logout", {}, false);
    } catch {
        // Local session cleanup must not depend on server response.
    } finally {
        clearUserData();
        localStorage.removeItem("cartItems");
        window.location.href = "/";
    }
}
