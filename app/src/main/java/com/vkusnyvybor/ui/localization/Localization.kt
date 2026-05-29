package com.vkusnyvybor.ui.localization

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Поддерживаемые языки интерфейса.
 *
 * `code` — ISO-код языка, `nativeName` — самоназвание (как показываем в списке).
 */
enum class AppLanguage(val code: String, val nativeName: String) {
    RU("ru", "Русский"),
    EN("en", "English"),
    UK("uk", "Українська"),
    PL("pl", "Polski"),
    FR("fr", "Français"),
    LV("lv", "Latviešu");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: RU
    }
}

/**
 * Набор строк интерфейса. Подключён к ключевым экранам (Профиль/Настройки,
 * Авторизация, Главная). Остальные экраны переводятся по той же схеме:
 * добавить поле сюда, заполнить во всех языках и заменить литерал на
 * `LocalStrings.current.<поле>`.
 */
data class Strings(
    // Общее
    val back: String,
    val close: String,
    // Профиль
    val profileTitle: String,
    val authorizedViaTelegram: String,
    val guestMode: String,
    val sectionOrders: String,
    val myOrders: String,
    val myOrdersSub: String,
    val addresses: String,
    val addressesSub: String,
    val sectionSettings: String,
    val themeTitle: String,
    val notifications: String,
    val notificationsSub: String,
    val language: String,
    val sectionAbout: String,
    val version: String,
    val userAgreement: String,
    val logout: String,
    val chooseLanguage: String,
    // Авторизация
    val authSubtitle: String,
    val loginWithTelegram: String,
    val connectingGateway: String,
    val finishInBrowser: String,
    val confirmedLogin: String,
    val orLabel: String,
    val continueAsGuest: String,
    val securedBy: String,
    val telegramLoginTitle: String,
    // Главная
    val searchHint: String,
    val atRestaurant: String,
    val chooseRestaurant: String,
    val choose: String,
    val change: String,
    val mapPickerTitle: String
)

/** Возвращает набор строк для выбранного языка. */
fun stringsFor(lang: AppLanguage): Strings = when (lang) {
    AppLanguage.RU -> Strings(
        back = "Назад",
        close = "Закрыть",
        profileTitle = "Профиль",
        authorizedViaTelegram = "Авторизован через Telegram",
        guestMode = "Гостевой режим",
        sectionOrders = "Заказы",
        myOrders = "Мои заказы",
        myOrdersSub = "История и текущие заказы",
        addresses = "Адреса",
        addressesSub = "Сохранённые предприятия",
        sectionSettings = "Настройки",
        themeTitle = "Тема оформления",
        notifications = "Уведомления",
        notificationsSub = "Настроить оповещения",
        language = "Язык",
        sectionAbout = "О приложении",
        version = "Версия",
        userAgreement = "Пользовательское соглашение",
        logout = "Выйти из аккаунта",
        chooseLanguage = "Выберите язык",
        authSubtitle = "Войдите через Telegram, чтобы продолжить",
        loginWithTelegram = "Войти через Telegram",
        connectingGateway = "Связываемся со шлюзом…",
        finishInBrowser = "Завершите вход в браузере и вернитесь сюда.",
        confirmedLogin = "Я подтвердил вход",
        orLabel = "или",
        continueAsGuest = "Продолжить как гость",
        securedBy = "Защищено протоколом OpenID Connect • Telegram",
        telegramLoginTitle = "Вход через Telegram",
        searchHint = "Найти блюдо...",
        atRestaurant = "В предприятии",
        chooseRestaurant = "Выберите предприятие",
        choose = "Выбрать",
        change = "Изменить",
        mapPickerTitle = "Выбор предприятия"
    )
    AppLanguage.EN -> Strings(
        back = "Back",
        close = "Close",
        profileTitle = "Profile",
        authorizedViaTelegram = "Signed in via Telegram",
        guestMode = "Guest mode",
        sectionOrders = "Orders",
        myOrders = "My orders",
        myOrdersSub = "Order history and current orders",
        addresses = "Addresses",
        addressesSub = "Saved places",
        sectionSettings = "Settings",
        themeTitle = "Theme",
        notifications = "Notifications",
        notificationsSub = "Configure alerts",
        language = "Language",
        sectionAbout = "About",
        version = "Version",
        userAgreement = "Terms of Service",
        logout = "Log out",
        chooseLanguage = "Choose language",
        authSubtitle = "Sign in with Telegram to continue",
        loginWithTelegram = "Sign in with Telegram",
        connectingGateway = "Connecting to the gateway…",
        finishInBrowser = "Finish signing in in the browser and come back here.",
        confirmedLogin = "I've signed in",
        orLabel = "or",
        continueAsGuest = "Continue as guest",
        securedBy = "Secured by OpenID Connect • Telegram",
        telegramLoginTitle = "Telegram sign-in",
        searchHint = "Search for a dish...",
        atRestaurant = "At venue",
        chooseRestaurant = "Choose a venue",
        choose = "Choose",
        change = "Change",
        mapPickerTitle = "Choose a venue"
    )
    AppLanguage.UK -> Strings(
        back = "Назад",
        close = "Закрити",
        profileTitle = "Профіль",
        authorizedViaTelegram = "Авторизовано через Telegram",
        guestMode = "Гостьовий режим",
        sectionOrders = "Замовлення",
        myOrders = "Мої замовлення",
        myOrdersSub = "Історія та поточні замовлення",
        addresses = "Адреси",
        addressesSub = "Збережені заклади",
        sectionSettings = "Налаштування",
        themeTitle = "Тема оформлення",
        notifications = "Сповіщення",
        notificationsSub = "Налаштувати сповіщення",
        language = "Мова",
        sectionAbout = "Про застосунок",
        version = "Версія",
        userAgreement = "Угода користувача",
        logout = "Вийти з акаунту",
        chooseLanguage = "Виберіть мову",
        authSubtitle = "Увійдіть через Telegram, щоб продовжити",
        loginWithTelegram = "Увійти через Telegram",
        connectingGateway = "З'єднання зі шлюзом…",
        finishInBrowser = "Завершіть вхід у браузері та поверніться сюди.",
        confirmedLogin = "Я підтвердив вхід",
        orLabel = "або",
        continueAsGuest = "Продовжити як гість",
        securedBy = "Захищено протоколом OpenID Connect • Telegram",
        telegramLoginTitle = "Вхід через Telegram",
        searchHint = "Знайти страву...",
        atRestaurant = "У закладі",
        chooseRestaurant = "Виберіть заклад",
        choose = "Вибрати",
        change = "Змінити",
        mapPickerTitle = "Вибір закладу"
    )
    AppLanguage.PL -> Strings(
        back = "Wstecz",
        close = "Zamknij",
        profileTitle = "Profil",
        authorizedViaTelegram = "Zalogowano przez Telegram",
        guestMode = "Tryb gościa",
        sectionOrders = "Zamówienia",
        myOrders = "Moje zamówienia",
        myOrdersSub = "Historia i bieżące zamówienia",
        addresses = "Adresy",
        addressesSub = "Zapisane lokale",
        sectionSettings = "Ustawienia",
        themeTitle = "Motyw",
        notifications = "Powiadomienia",
        notificationsSub = "Skonfiguruj powiadomienia",
        language = "Język",
        sectionAbout = "O aplikacji",
        version = "Wersja",
        userAgreement = "Warunki korzystania",
        logout = "Wyloguj się",
        chooseLanguage = "Wybierz język",
        authSubtitle = "Zaloguj się przez Telegram, aby kontynuować",
        loginWithTelegram = "Zaloguj się przez Telegram",
        connectingGateway = "Łączenie z bramą…",
        finishInBrowser = "Dokończ logowanie w przeglądarce i wróć tutaj.",
        confirmedLogin = "Potwierdziłem logowanie",
        orLabel = "lub",
        continueAsGuest = "Kontynuuj jako gość",
        securedBy = "Zabezpieczone przez OpenID Connect • Telegram",
        telegramLoginTitle = "Logowanie przez Telegram",
        searchHint = "Znajdź danie...",
        atRestaurant = "W lokalu",
        chooseRestaurant = "Wybierz lokal",
        choose = "Wybierz",
        change = "Zmień",
        mapPickerTitle = "Wybór lokalu"
    )
    AppLanguage.FR -> Strings(
        back = "Retour",
        close = "Fermer",
        profileTitle = "Profil",
        authorizedViaTelegram = "Connecté via Telegram",
        guestMode = "Mode invité",
        sectionOrders = "Commandes",
        myOrders = "Mes commandes",
        myOrdersSub = "Historique et commandes en cours",
        addresses = "Adresses",
        addressesSub = "Établissements enregistrés",
        sectionSettings = "Paramètres",
        themeTitle = "Thème",
        notifications = "Notifications",
        notificationsSub = "Configurer les alertes",
        language = "Langue",
        sectionAbout = "À propos",
        version = "Version",
        userAgreement = "Conditions d'utilisation",
        logout = "Se déconnecter",
        chooseLanguage = "Choisir la langue",
        authSubtitle = "Connectez-vous avec Telegram pour continuer",
        loginWithTelegram = "Se connecter avec Telegram",
        connectingGateway = "Connexion à la passerelle…",
        finishInBrowser = "Terminez la connexion dans le navigateur et revenez ici.",
        confirmedLogin = "J'ai confirmé la connexion",
        orLabel = "ou",
        continueAsGuest = "Continuer en tant qu'invité",
        securedBy = "Sécurisé par OpenID Connect • Telegram",
        telegramLoginTitle = "Connexion Telegram",
        searchHint = "Rechercher un plat...",
        atRestaurant = "Au point de vente",
        chooseRestaurant = "Choisissez un établissement",
        choose = "Choisir",
        change = "Modifier",
        mapPickerTitle = "Choisir un établissement"
    )
    AppLanguage.LV -> Strings(
        back = "Atpakaļ",
        close = "Aizvērt",
        profileTitle = "Profils",
        authorizedViaTelegram = "Pieslēdzies ar Telegram",
        guestMode = "Viesa režīms",
        sectionOrders = "Pasūtījumi",
        myOrders = "Mani pasūtījumi",
        myOrdersSub = "Vēsture un pašreizējie pasūtījumi",
        addresses = "Adreses",
        addressesSub = "Saglabātās vietas",
        sectionSettings = "Iestatījumi",
        themeTitle = "Tēma",
        notifications = "Paziņojumi",
        notificationsSub = "Konfigurēt paziņojumus",
        language = "Valoda",
        sectionAbout = "Par lietotni",
        version = "Versija",
        userAgreement = "Lietošanas noteikumi",
        logout = "Iziet no konta",
        chooseLanguage = "Izvēlieties valodu",
        authSubtitle = "Pieslēdzieties ar Telegram, lai turpinātu",
        loginWithTelegram = "Pieslēgties ar Telegram",
        connectingGateway = "Savienojas ar vārteju…",
        finishInBrowser = "Pabeidziet pieslēgšanos pārlūkā un atgriezieties šeit.",
        confirmedLogin = "Esmu pieslēdzies",
        orLabel = "vai",
        continueAsGuest = "Turpināt kā viesim",
        securedBy = "Aizsargāts ar OpenID Connect • Telegram",
        telegramLoginTitle = "Pieslēgšanās ar Telegram",
        searchHint = "Meklēt ēdienu...",
        atRestaurant = "Vietā",
        chooseRestaurant = "Izvēlieties vietu",
        choose = "Izvēlēties",
        change = "Mainīt",
        mapPickerTitle = "Vietas izvēle"
    )
}

/**
 * Лёгкий движок локализации в стиле ThemeEngine: хранит выбранный язык,
 * персистит его и отдаёт текущий набор строк. Смена языка происходит мгновенно
 * (без перезапуска), потому что `current` — это Compose-состояние, которое
 * читает провайдер `LocalStrings` в теме приложения.
 */
object LocalizationEngine {
    private const val PREFS_NAME = "vkusny_lang_prefs"
    private const val KEY_LANGUAGE = "app_language"

    @SuppressLint("StaticFieldLeak")
    private var prefs: SharedPreferences? = null

    var current by mutableStateOf(AppLanguage.RU)
        private set

    val strings: Strings get() = stringsFor(current)

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        current = AppLanguage.fromCode(p.getString(KEY_LANGUAGE, AppLanguage.RU.code))
    }

    fun setLanguage(lang: AppLanguage) {
        current = lang
        prefs?.edit()?.putString(KEY_LANGUAGE, lang.code)?.apply()
    }
}

/** Текущие строки интерфейса. По умолчанию — русский (до инициализации движка). */
val LocalStrings = compositionLocalOf { stringsFor(AppLanguage.RU) }
