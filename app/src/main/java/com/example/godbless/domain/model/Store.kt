package com.example.godbless.domain.model

/**
 * Модель магазина с информацией для открытия приложения или сайта
 */
data class Store(
    val id: String,
    val name: String,
    val packageName: String?, // Package name приложения Android
    val webUrlTemplate: String, // URL сайта с placeholder %s для поиска товара
    val iconEmoji: String // Эмодзи иконка магазина
)

/**
 * Список поддерживаемых магазинов
 */
object StoreProvider {
    val stores = listOf(
        Store(
            id = "pyaterochka",
            name = "Пятёрочка",
            packageName = "com.xfive.android",
            webUrlTemplate = "https://5ka.ru/search/?text=%s",
            iconEmoji = "🛒"
        ),
        Store(
            id = "magnit",
            name = "Магнит",
            packageName = "ru.magnit.mm",
            webUrlTemplate = "https://magnit.ru/promo/?q=%s",
            iconEmoji = "🧲"
        ),
        Store(
            id = "perekrestok",
            name = "Перекрёсток",
            packageName = "ru.perekrestok.app",
            webUrlTemplate = "https://www.perekrestok.ru/cat/search?search=%s",
            iconEmoji = "🛍️"
        ),
        Store(
            id = "samokat",
            name = "Самокат",
            packageName = "com.samokat",
            webUrlTemplate = "https://samokat.ru/search?query=%s",
            iconEmoji = "🛴"
        ),
        Store(
            id = "vkusvill",
            name = "ВкусВилл",
            packageName = "ru.vkusvill.app",
            webUrlTemplate = "https://vkusvill.ru/search/?text=%s",
            iconEmoji = "🌱"
        ),
        Store(
            id = "auchan",
            name = "Ашан",
            packageName = "ru.auchan.mobile",
            webUrlTemplate = "https://www.auchan.ru/search/?text=%s",
            iconEmoji = "🏪"
        ),
        Store(
            id = "lenta",
            name = "Лента",
            packageName = "com.lenta.loyalty",
            webUrlTemplate = "https://lenta.com/search/?query=%s",
            iconEmoji = "🎀"
        ),
        Store(
            id = "yandex_lavka",
            name = "Яндекс Лавка",
            packageName = "ru.yandex.lavka",
            webUrlTemplate = "https://lavka.yandex.ru/search?text=%s",
            iconEmoji = "🟡"
        ),
        Store(
            id = "ozon",
            name = "Ozon",
            packageName = "ru.ozon.app.android",
            webUrlTemplate = "https://www.ozon.ru/search/?text=%s",
            iconEmoji = "💙"
        ),
        Store(
            id = "wildberries",
            name = "Wildberries",
            packageName = "com.wildberries.ru",
            webUrlTemplate = "https://www.wildberries.ru/catalog/0/search.aspx?search=%s",
            iconEmoji = "💜"
        )
    )
}
