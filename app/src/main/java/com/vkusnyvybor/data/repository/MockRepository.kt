package com.vkusnyvybor.data.repository

import androidx.compose.ui.graphics.Color
import com.vkusnyvybor.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRepository @Inject constructor() {

    private val vkusnoColors = RestaurantColors(
        primary = Color(0xFF1B5E20), secondary = Color(0xFFFF6D00), accent = Color(0xFFFFA726),
        onPrimary = Color.White, gradientStart = Color(0xFF2E7D32), gradientEnd = Color(0xFF1B5E20)
    )
    private val burgerKingColors = RestaurantColors(
        primary = Color(0xFFEC1C24), secondary = Color(0xFFFDBD10), accent = Color(0xFF0066B2),
        onPrimary = Color.White, gradientStart = Color(0xFFED7902), gradientEnd = Color(0xFFEC1C24)
    )
    private val rosticsColors = RestaurantColors(
        primary = Color(0xFFD32F2F), secondary = Color(0xFFFFFFFF), accent = Color(0xFFB71C1C),
        onPrimary = Color.White, gradientStart = Color(0xFFE53935), gradientEnd = Color(0xFFC62828)
    )

    // ── Общие модификаторы ────────────────────────────────────────
    private val burgerMods = listOf(
        ItemModifier("mod_lettuce", "Салат", true),
        ItemModifier("mod_sauce", "Соус", true),
        ItemModifier("mod_onion", "Лук", true),
        ItemModifier("mod_pickles", "Соленья", true),
        ItemModifier("mod_cheese", "Сыр", true),
        ItemModifier("mod_extra_cheese", "Двойной сыр", false, 49),
        ItemModifier("mod_bacon", "Бекон", false, 79),
    )
    private val drinkSizes = listOf(
        SizeOption("sz_s", "Маленький", 0),
        SizeOption("sz_m", "Средний", 30),
        SizeOption("sz_l", "Большой", 60),
    )
    private val frySizes = listOf(
        SizeOption("sz_s", "Маленькая", 0),
        SizeOption("sz_m", "Средняя", 40),
        SizeOption("sz_l", "Большая", 70),
    )
    private val chickenMods = listOf(
        ItemModifier("mod_spicy", "Острый маринад", true),
        ItemModifier("mod_sauce_garlic", "Чесночный соус", true),
        ItemModifier("mod_sauce_bbq", "Соус BBQ", false, 39),
    )
    private val rollMods = listOf(
        ItemModifier("mod_lettuce", "Салат", true),
        ItemModifier("mod_tomato", "Томаты", true),
        ItemModifier("mod_sauce", "Соус", true),
        ItemModifier("mod_cheese", "Сыр", false, 39),
    )

    // ── Моковые товары ────────────────────────────────────────────

    private val vkusnoItems = listOf(
        MenuItem("v1", "vkusno", "Биг Спешал", "Два рубленых бифштекса, сыр, салат, соус", 345, category = "Бургеры", rating = 4.5f, weight = "280г", modifiers = burgerMods),
        MenuItem("v2", "vkusno", "Картошка Фри", "Хрустящая золотистая картошка", 159, category = "Гарниры", rating = 4.3f, weight = "150г", sizes = frySizes),
        MenuItem("v3", "vkusno", "Наггетсы 9шт", "Куриные наггетсы в хрустящей панировке", 219, category = "Снэки", rating = 4.4f, weight = "180г", modifiers = chickenMods.take(2)),
        MenuItem("v4", "vkusno", "Двойной Чизбургер", "Двойной бифштекс, двойной сыр", 189, category = "Бургеры", rating = 4.6f, weight = "200г", modifiers = burgerMods),
        MenuItem("v5", "vkusno", "Молочный коктейль", "Ванильный молочный коктейль", 159, category = "Напитки", rating = 4.2f, weight = "400мл", sizes = drinkSizes),
        MenuItem("v6", "vkusno", "Цезарь Ролл", "Куриная грудка, салат Цезарь в тортилье", 249, oldPrice = 299, category = "Роллы", rating = 4.5f, weight = "230г", modifiers = rollMods),
        MenuItem("v7", "vkusno", "Кофе Латте", "Нежный латте на свежем молоке", 139, category = "Напитки", rating = 4.1f, weight = "300мл", sizes = drinkSizes),
        MenuItem("v8", "vkusno", "Пирожок Вишня", "Горячий пирожок с вишнёвой начинкой", 79, category = "Десерты", rating = 4.0f, weight = "90г"),
    )

    private val bkItems = listOf(
        MenuItem("b1", "bk", "Воппер", "Котлета на огне, томаты, лук, соленья, кетчуп", 299, category = "Бургеры", rating = 4.7f, weight = "270г", modifiers = burgerMods),
        MenuItem("b2", "bk", "Кинг Фри", "Картошка фри", 129, category = "Гарниры", rating = 4.2f, weight = "120г", sizes = frySizes),
        MenuItem("b3", "bk", "Наггетсы Кинг 9шт", "Куриные наггетсы", 199, category = "Снэки", rating = 4.3f, weight = "175г"),
        MenuItem("b4", "bk", "Двойной Воппер", "Двойная котлета на огне", 399, category = "Бургеры", rating = 4.8f, weight = "350г", modifiers = burgerMods),
        MenuItem("b5", "bk", "Онион Рингз", "Луковые кольца в хрустящей панировке", 149, category = "Снэки", rating = 4.1f, weight = "130г"),
        MenuItem("b6", "bk", "Кинг Коктейль", "Шоколадный молочный коктейль", 179, category = "Напитки", rating = 4.4f, weight = "400мл", sizes = drinkSizes),
    )

    private val rosticsItems = listOf(
        MenuItem("r1", "rostics", "Баскет Оригинальный", "Острые крылья и ножки, фирменный рецепт", 459, category = "Корзинки", rating = 4.6f, weight = "350г", modifiers = chickenMods),
        MenuItem("r2", "rostics", "Твистер Оригинальный", "Стрипсы, салат, томаты, соус в тортилье", 249, category = "Твистеры", rating = 4.4f, weight = "220г", modifiers = rollMods),
        MenuItem("r3", "rostics", "Острые крылышки 6шт", "Крылышки в остром маринаде", 289, category = "Курица", rating = 4.7f, weight = "280г", modifiers = chickenMods),
        MenuItem("r4", "rostics", "Картошка по-деревенски", "Дольки картофеля со специями", 139, category = "Гарниры", rating = 4.0f, weight = "160г", sizes = frySizes),
        MenuItem("r5", "rostics", "Сандерс Бургер", "Куриное филе, булочка бриошь, соус", 319, category = "Бургеры", rating = 4.5f, weight = "250г", modifiers = burgerMods.take(5)),
    )

    fun getRestaurants(): List<Restaurant> = listOf(
        Restaurant(id = "vkusno", name = "Вкусно и точка", subtitle = "Самые выгодные предложения", slogan = "Всё как мы любим",
            rating = 4.5f, deliveryTime = "25-35 мин", colors = vkusnoColors,
            categories = listOf(
                MenuCategory("vc1", "Бургеры", vkusnoItems.filter { it.category == "Бургеры" }),
                MenuCategory("vc2", "Снэки", vkusnoItems.filter { it.category == "Снэки" }),
                MenuCategory("vc3", "Гарниры", vkusnoItems.filter { it.category == "Гарниры" }),
                MenuCategory("vc4", "Напитки", vkusnoItems.filter { it.category == "Напитки" }),
                MenuCategory("vc5", "Роллы", vkusnoItems.filter { it.category == "Роллы" }),
                MenuCategory("vc6", "Десерты", vkusnoItems.filter { it.category == "Десерты" }),
            )),
        Restaurant(id = "bk", name = "Бургер Кинг", subtitle = "Приготовлено на огне", slogan = "Вкус, который зажигает",
            rating = 4.3f, deliveryTime = "30-40 мин", colors = burgerKingColors,
            categories = listOf(
                MenuCategory("bc1", "Бургеры", bkItems.filter { it.category == "Бургеры" }),
                MenuCategory("bc2", "Снэки", bkItems.filter { it.category == "Снэки" }),
                MenuCategory("bc3", "Гарниры", bkItems.filter { it.category == "Гарниры" }),
                MenuCategory("bc4", "Напитки", bkItems.filter { it.category == "Напитки" }),
            )),
        Restaurant(id = "rostics", name = "Rostics", subtitle = "Курица — наша страсть", slogan = "Вкус, проверенный временем",
            rating = 4.4f, deliveryTime = "20-30 мин", colors = rosticsColors,
            categories = listOf(
                MenuCategory("rc1", "Корзинки", rosticsItems.filter { it.category == "Корзинки" }),
                MenuCategory("rc2", "Твистеры", rosticsItems.filter { it.category == "Твистеры" }),
                MenuCategory("rc3", "Курица", rosticsItems.filter { it.category == "Курица" }),
                MenuCategory("rc4", "Бургеры", rosticsItems.filter { it.category == "Бургеры" }),
                MenuCategory("rc5", "Гарниры", rosticsItems.filter { it.category == "Гарниры" }),
            )),
    )

    fun getRestaurantById(id: String): Restaurant? = getRestaurants().find { it.id == id }
    fun getAllMenuItems(): List<MenuItem> = vkusnoItems + bkItems + rosticsItems
    fun getRecentOrders(): List<Order> = listOf(
        Order("o1", "vkusno", "Вкусно и точка", listOf(CartItem(vkusnoItems[0], 2), CartItem(vkusnoItems[1], 1)), 849, "Вчера", OrderStatus.COMPLETED),
        Order("o2", "bk", "Бургер Кинг", listOf(CartItem(bkItems[0], 1), CartItem(bkItems[2], 1)), 498, "3 дня назад", OrderStatus.COMPLETED),
    )
}
