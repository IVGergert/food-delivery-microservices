const MINIO_BASE_URL = "/menu-images/";

export function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

export function formatDate(date) {
    if (!date) {
        return "";
    }

    const parsed = new Date(date);

    if (Number.isNaN(parsed.getTime())) {
        return "";
    }

    return parsed.toLocaleString("ru-RU", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

export function formatPrice(price) {
    const value = Number(price);

    if (!Number.isFinite(value)) {
        return "0,00 ₽";
    }

    return `${value.toLocaleString("ru-RU", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })} ₽`;
}

export function getRussianItemWord(count) {
    const lastTwo = count % 100;
    const last = count % 10;

    if (lastTwo >= 11 && lastTwo <= 14) {
        return "позиций";
    }

    if (last === 1) {
        return "позиция";
    }

    if (last >= 2 && last <= 4) {
        return "позиции";
    }

    return "позиций";
}

export function buildImageUrl(imageUrl) {
    if (!imageUrl) {
        return "";
    }

    if (
        imageUrl.startsWith("http://") ||
        imageUrl.startsWith("https://") ||
        imageUrl.startsWith("/")
    ) {
        return imageUrl;
    }

    return `${MINIO_BASE_URL}${imageUrl}`;
}
