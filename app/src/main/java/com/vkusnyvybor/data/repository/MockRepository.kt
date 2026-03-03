package com.vkusnyvybor.data.repository

import androidx.compose.ui.graphics.Color
import com.vkusnyvybor.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRepository @Inject constructor() {

    // ── Цветовые палитры брендов ──────────────────────────────────

    private val vkusnoColors = RestaurantColors(
        primary = Color(0xFF1B5E20),      // тёмно-зелёный
        secondary = Color(0xFFFF6D00),    // оранжевый
        accent = Color(0xFFFFA726),
        onPrimary = Color.White,
        gradientStart = Color(0xFF2E7D32),
        gradientEnd = Color(0xFF1B5E20)
    )

    private val burgerKingColors = RestaurantColors(
        primary = Color(0xFFEC1C24),      // красный
        secondary = Color(0xFFFDBD10),    // жёлтый
        accent = Color(0xFF0066B2),       // синий
        onPrimary = Color.White,
        gradientStart = Color(0xFFED7902),
        gradientEnd = Color(0xFFEC1C24)
    )

    private val rosticsColors = RestaurantColors(
        primary = Color(0xFFD32F2F),      // красный
        secondary = Color(0xFFFFFFFF),    // белый
        accent = Color(0xFFB71C1C),
        onPrimary = Color.White,
        gradientStart = Color(0xFFE53935),
        gradientEnd = Color(0xFFC62828)
    )

    // ── Моковые товары ────────────────────────────────────────────

    private val vkusnoItems = listOf(
        MenuItem("v1", "vkusno", "Биг Спешал", "Два рубленых бифштекса, сыр, салат, соус", 345, imageUrl = "burger_big_special", category = "Бургеры", rating = 4.5f, weight = "280г"),
        MenuItem("v2", "vkusno", "Картошка Фри", "Хрустящая золотистая картошка", 159, imageUrl = "fries", category = "Гарниры", rating = 4.3f, weight = "150г"),
        MenuItem("v3", "vkusno", "Наггетсы 9шт", "Куриные наггетсы в хрустящей панировке", 219, imageUrl = "nuggets", category = "Снэки", rating = 4.4f, weight = "180г"),
        MenuItem("v4", "vkusno", "Двойной Чизбургер", "Двойной бифштекс, двойной сыр", 189, imageUrl = "cheeseburger", category = "Бургеры", rating = 4.6f, weight = "200г"),
        MenuItem("v5", "vkusno", "Молочный коктейль", "Ванильный молочный коктейль", 159, imageUrl = "milkshake", category = "Напитки", rating = 4.2f, weight = "400мл"),
        MenuItem("v6", "vkusno", "Цезарь Ролл", "Куриная грудка, салат Цезарь в тортилье", 249, oldPrice = 299, imageUrl = "caesar_roll", category = "Роллы", rating = 4.5f, weight = "230г"),
        MenuItem("v7", "vkusno", "Кофе Латте", "Нежный латте на свежем молоке", 139, imageUrl = "latte", category = "Напитки", rating = 4.1f, weight = "300мл"),
        MenuItem("v8", "vkusno", "Пирожок Вишня", "Горячий пирожок с вишнёвой начинкой", 79, imageUrl = "pie_cherry", category = "Десерты", rating = 4.0f, weight = "90г"),
    )

    private val bkItems = listOf(
        MenuItem("b1", "bk", "Воппер", "Котлета на огне, томаты, лук, соленья, кетчуп", 299, imageUrl = "whopper", category = "Бургеры", rating = 4.7f, weight = "270г"),
        MenuItem("b2", "bk", "Кинг Фри Большой", "Большая порция картошки фри", 169, imageUrl = "king_fries", category = "Гарниры", rating = 4.2f, weight = "190г"),
        MenuItem("b3", "bk", "Наггетсы Кинг 9шт", "Куриные наггетсы", 199, imageUrl = "bk_nuggets", category = "Снэки", rating = 4.3f, weight = "175г"),
        MenuItem("b4", "bk", "Двойной Воппер", "Двойная котлета на огне", 399, imageUrl = "double_whopper", category = "Бургеры", rating = 4.8f, weight = "350г"),
        MenuItem("b5", "bk", "Онион Рингз", "Луковые кольца в хрустящей панировке", 149, imageUrl = "onion_rings", category = "Снэки", rating = 4.1f, weight = "130г"),
        MenuItem("b6", "bk", "Кинг Коктейль", "Шоколадный молочный коктейль", 179, imageUrl = "bk_shake", category = "Напитки", rating = 4.4f, weight = "400мл"),
    )

    private val rosticsItems = listOf(
        MenuItem("r1", "rostics", "Баскет Оригинальный", "Острые крылья и ножки, фирменный рецепт", 459, imageUrl = "basket_original", category = "Корзинки", rating = 4.6f, weight = "350г"),
        MenuItem("r2", "rostics", "Твистер Оригинальный", "Стрипсы, салат, томаты, соус в тортилье", 249, imageUrl = "twister", category = "Твистеры", rating = 4.4f, weight = "220г"),
        MenuItem("r3", "rostics", "Острые крылышки 6шт", "Крылышки в остром маринаде", 289, imageUrl = "hot_wings", category = "Курица", rating = 4.7f, weight = "280г"),
        MenuItem("r4", "rostics", "Картошка по-деревенски", "Дольки картофеля со специями", 139, imageUrl = "country_fries", category = "Гарниры", rating = 4.0f, weight = "160г"),
        MenuItem("r5", "rostics", "Сандерс Бургер", "Куриное филе, булочка бриошь, соус", 319, imageUrl = "sanders_burger", category = "Бургеры", rating = 4.5f, weight = "250г"),
    )

    // ── Рестораны ─────────────────────────────────────────────────

    fun getRestaurants(): List<Restaurant> = listOf(
        Restaurant(
            id = "vkusno",
            name = "Вкусно и точка",
            subtitle = "Самые выгодные предложения",
            slogan = "Всё как мы любим",
            logoUrl = "logo_vkusno",
            heroImageUrl = "hero_vkusno",
            rating = 4.5f,
            deliveryTime = "25-35 мин",
            deliveryPrice = "Бесплатно",
            colors = vkusnoColors,
            categories = listOf(
                MenuCategory("vc1", "Бургеры", vkusnoItems.filter { it.category == "Бургеры" }),
                MenuCategory("vc2", "Снэки", vkusnoItems.filter { it.category == "Снэки" }),
                MenuCategory("vc3", "Гарниры", vkusnoItems.filter { it.category == "Гарниры" }),
                MenuCategory("vc4", "Напитки", vkusnoItems.filter { it.category == "Напитки" }),
                MenuCategory("vc5", "Роллы", vkusnoItems.filter { it.category == "Роллы" }),
                MenuCategory("vc6", "Десерты", vkusnoItems.filter { it.category == "Десерты" }),
            )
        ),
        Restaurant(
            id = "bk",
            name = "Бургер Кинг",
            subtitle = "Приготовлено на огне",
            slogan = "Вкус, который зажигает",
            logoUrl = "logo_bk",
            heroImageUrl = "hero_bk",
            rating = 4.3f,
            deliveryTime = "30-40 мин",
            deliveryPrice = "199₽",
            colors = burgerKingColors,
            categories = listOf(
                MenuCategory("bc1", "Бургеры", bkItems.filter { it.category == "Бургеры" }),
                MenuCategory("bc2", "Снэки", bkItems.filter { it.category == "Снэки" }),
                MenuCategory("bc3", "Гарниры", bkItems.filter { it.category == "Гарниры" }),
                MenuCategory("bc4", "Напитки", bkItems.filter { it.category == "Напитки" }),
            )
        ),
        Restaurant(
            id = "rostics",
            name = "Rostics",
            subtitle = "Курица — наша страсть",
            slogan = "Вкус, проверенный временем",
            logoUrl = "logo_rostics",
            heroImageUrl = "hero_rostics",
            rating = 4.4f,
            deliveryTime = "20-30 мин",
            deliveryPrice = "149₽",
            colors = rosticsColors,
            categories = listOf(
                MenuCategory("rc1", "Корзинки", rosticsItems.filter { it.category == "Корзинки" }),
                MenuCategory("rc2", "Твистеры", rosticsItems.filter { it.category == "Твистеры" }),
                MenuCategory("rc3", "Курица", rosticsItems.filter { it.category == "Курица" }),
                MenuCategory("rc4", "Бургеры", rosticsItems.filter { it.category == "Бургеры" }),
                MenuCategory("rc5", "Гарниры", rosticsItems.filter { it.category == "Гарниры" }),
            )
        )
    )

    fun getRestaurantById(id: String): Restaurant? =
        getRestaurants().find { it.id == id }

    fun getAllMenuItems(): List<MenuItem> =
        vkusnoItems + bkItems + rosticsItems

    fun getRecentOrders(): List<Order> = listOf(
        Order("o1", "vkusno", "Вкусно и точка",
            listOf(CartItem(vkusnoItems[0], 2), CartItem(vkusnoItems[1], 1)),
            849, "Вчера", OrderStatus.COMPLETED),
        Order("o2", "bk", "Бургер Кинг",
            listOf(CartItem(bkItems[0], 1), CartItem(bkItems[2], 1)),
            498, "3 дня назад", OrderStatus.COMPLETED),
    )
}
