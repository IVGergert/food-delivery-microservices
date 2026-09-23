import {
    getMenu
} from "./api.js";

import {
    formatPrice,
    getRussianItemWord,
    buildImageUrl,
    escapeHtml
} from "../../common/js/utils.js";

import {
    addToCart
} from "./cart.js";

const categories = [
    { value: "ALL", title: "Все", icon: "🍽️" },
    { value: "PIZZA", title: "Пицца", icon: "🍕" },
    { value: "BURGERS", title: "Бургеры", icon: "🍔" },
    { value: "SUSHI", title: "Суши", icon: "🍣" },
    { value: "SNACKS", title: "Снеки", icon: "🍟" },
    { value: "DRINKS", title: "Напитки", icon: "🥤" }
];

let menuItems = [];
let activeCategory = "ALL";

export async function loadMenu() {
    showMenuLoading();

    try {
        const data = await getMenu();
        menuItems = Array.isArray(data) ? data : [];

        hideMenuLoading();
        renderCategories();
        renderMenu();

    } catch (error) {
        hideMenuLoading();
        showMenuError(error.message);
    }
}

function renderCategories() {
    const container = document.getElementById("categories");

    if (!container) return;

    container.innerHTML = "";

    categories.forEach(category => {
        const count = category.value === "ALL"
            ? menuItems.length
            : menuItems.filter(item => item.category === category.value).length;

        const button = document.createElement("button");

        button.type = "button";
        button.className = `category-button ${category.value === activeCategory ? "active" : ""}`;
        button.dataset.category = category.value;

        button.innerHTML = `
            <span class="category-icon">${category.icon}</span>
            <span class="category-title">${escapeHtml(category.title)}</span>
            <span class="category-badge">${count}</span>
        `;

        button.addEventListener("click", () => {
            activeCategory = category.value;
            renderCategories();
            renderMenu();
        });

        container.appendChild(button);
    });
}

function renderMenu() {
    const container = document.getElementById("menuGrid");
    const emptyState = document.getElementById("menuEmpty");

    if (!container) return;

    container.innerHTML = "";

    if (!menuItems || menuItems.length === 0) {
        if (emptyState) {
            emptyState.classList.remove("hidden");
        }

        return;
    }

    if (emptyState) {
        emptyState.classList.add("hidden");
    }

    if (activeCategory === "ALL") {
        renderAllCategories(container);
        return;
    }

    const filteredItems = menuItems.filter(
        item => item.category === activeCategory
    );

    if (filteredItems.length === 0) {
        renderEmptyCategory(container);
        return;
    }

    const categoryObj = categories.find(
        category => category.value === activeCategory
    );

    const categoryTitle = categoryObj
        ? categoryObj.title
        : activeCategory;

    const categoryIcon = categoryObj
        ? categoryObj.icon
        : "🍽️";

    const section = document.createElement("section");
    section.className = "menu-category-section";

    section.innerHTML = `
        <div class="menu-category-header">
            <div>
                <h2>${categoryIcon} ${escapeHtml(categoryTitle)}</h2>
                <span>
                    ${filteredItems.length}
                    ${getRussianItemWord(filteredItems.length)}
                </span>
            </div>
        </div>

        <div class="menu-category-grid"></div>
    `;

    const grid = section.querySelector(".menu-category-grid");

    filteredItems.forEach(item => {
        grid.appendChild(createMenuCard(item));
    });

    container.appendChild(section);
}


function renderAllCategories(container) {
    categories
        .filter(category => category.value !== "ALL")
        .forEach(category => {
            const items = menuItems.filter(
                item => item.category === category.value
            );

            if (items.length === 0) return;

            const section = document.createElement("section");
            section.className = "menu-category-section";

            section.innerHTML = `
                <div class="menu-category-header">
                    <div>
                        <h2>${category.icon} ${escapeHtml(category.title)}</h2>
                    </div>
                </div>

                <div class="menu-category-grid"></div>
            `;

            const grid = section.querySelector(".menu-category-grid");

            items.forEach(item => {
                grid.appendChild(createMenuCard(item));
            });

            container.appendChild(section);
        });

    if (!container.children.length) {
        renderEmptyCategory(container);
    }
}

function renderEmptyCategory(container) {
    container.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">🍽️</div>
            <h3>Ничего не найдено</h3>
            <p>В этой категории пока нет доступных блюд.</p>
        </div>
    `;
}


function createMenuCard(item) {
    const card = document.createElement("article");
    card.className = "menu-card";

    const imageUrl = buildImageUrl(item.imageUrl);

    card.innerHTML = `
        <div class="menu-card-image">
            ${imageUrl ? `
                <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(item.name)}" loading="lazy">` : `
                <div class="image-placeholder">🍽️</div>`}
        </div>

        <div class="menu-card-content">
            <span class="menu-card-category">
                ${escapeHtml(item.categoryTitle || getCategoryTitle(item.category))}
            </span>

            <h3 class="menu-card-title">
                ${escapeHtml(item.name)}
            </h3>

            <p class="menu-card-description">
                ${escapeHtml(item.description || "Описание отсутствует")}
            </p>

            <div class="menu-card-bottom">
                <span class="menu-card-price">
                    ${formatPrice(item.price)}
                </span>

                <button type="button" class="add-to-cart-button">
                    В корзину
                </button>
            </div>
        </div>
    `;

    const image = card.querySelector("img");

    if (image) {
        image.addEventListener("error", () => {
            image.style.display = "none";

            const imageContainer = image.parentElement;

            if (imageContainer) {
                imageContainer.classList.add("image-error");
                imageContainer.innerHTML = '<span class="image-error-text">Изображение недоступно</span>';
            }
        });
    }

    const addButton = card.querySelector(".add-to-cart-button");

    addButton.addEventListener("click", () => {
        addToCart(item);
    });

    return card;
}

function showMenuLoading() {
    const loading = document.getElementById("menuLoading");
    const empty = document.getElementById("menuEmpty");
    const grid = document.getElementById("menuGrid");

    if (loading) {
        loading.classList.remove("hidden");
    }

    if (empty) {
        empty.classList.add("hidden");
    }

    if (grid) {
        grid.innerHTML = "";
    }
}

function hideMenuLoading() {
    const loading = document.getElementById("menuLoading");

    if (loading) {
        loading.classList.add("hidden");
    }
}

function showMenuError(message) {
    const grid = document.getElementById("menuGrid");

    if (!grid) return;

    grid.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <h3>Не удалось загрузить меню</h3>
            <p>${escapeHtml(message)}</p>

            <button type="button" class="primary-button" id="retryMenuButton">
                Повторить попытку
            </button>
        </div>
    `;
}

function getCategoryTitle(category) {
    const found = categories.find(
        item => item.value === category
    );

    return found ? found.title : category || "";
}
