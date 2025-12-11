package com.example.godbless.utils
import com.example.godbless.domain.model.ProductCategory
object CategoryMapper {
    fun mapFromOpenFoodFacts(categories: String?): ProductCategory {
        if (categories.isNullOrBlank()) return ProductCategory.OTHER
        val lowerCategories = categories.lowercase()
        return when {
            lowerCategories.contains("dairy") ||
            lowerCategories.contains("milk") ||
            lowerCategories.contains("cheese") ||
            lowerCategories.contains("yogurt") ||
            lowerCategories.contains("молоч") ||
            lowerCategories.contains("молоко") ||
            lowerCategories.contains("сыр") ||
            lowerCategories.contains("йогурт") ||
            lowerCategories.contains("кефир") ||
            lowerCategories.contains("творог") ||
            lowerCategories.contains("сметана") -> ProductCategory.DAIRY
            lowerCategories.contains("meat") ||
            lowerCategories.contains("chicken") ||
            lowerCategories.contains("pork") ||
            lowerCategories.contains("beef") ||
            lowerCategories.contains("sausage") ||
            lowerCategories.contains("мясо") ||
            lowerCategories.contains("курица") ||
            lowerCategories.contains("свинина") ||
            lowerCategories.contains("говядина") ||
            lowerCategories.contains("колбас") ||
            lowerCategories.contains("сосиск") -> ProductCategory.MEAT
            lowerCategories.contains("fish") ||
            lowerCategories.contains("seafood") ||
            lowerCategories.contains("salmon") ||
            lowerCategories.contains("tuna") ||
            lowerCategories.contains("рыба") ||
            lowerCategories.contains("морепродукт") ||
            lowerCategories.contains("креветк") ||
            lowerCategories.contains("лосось") -> ProductCategory.FISH
            lowerCategories.contains("vegetable") ||
            lowerCategories.contains("томат") ||
            lowerCategories.contains("огурец") ||
            lowerCategories.contains("картофель") ||
            lowerCategories.contains("овощ") ||
            lowerCategories.contains("капуста") ||
            lowerCategories.contains("морковь") ||
            lowerCategories.contains("salad") ||
            lowerCategories.contains("томат") -> ProductCategory.VEGETABLES
            lowerCategories.contains("fruit") ||
            lowerCategories.contains("apple") ||
            lowerCategories.contains("banana") ||
            lowerCategories.contains("orange") ||
            lowerCategories.contains("фрукт") ||
            lowerCategories.contains("яблок") ||
            lowerCategories.contains("банан") ||
            lowerCategories.contains("апельсин") ||
            lowerCategories.contains("груша") ||
            lowerCategories.contains("ягод") -> ProductCategory.FRUITS
            lowerCategories.contains("bread") ||
            lowerCategories.contains("bakery") ||
            lowerCategories.contains("pastry") ||
            lowerCategories.contains("cake") ||
            lowerCategories.contains("хлеб") ||
            lowerCategories.contains("булк") ||
            lowerCategories.contains("батон") ||
            lowerCategories.contains("выпечк") ||
            lowerCategories.contains("торт") ||
            lowerCategories.contains("печенье") -> ProductCategory.BAKERY
            lowerCategories.contains("beverage") ||
            lowerCategories.contains("drink") ||
            lowerCategories.contains("juice") ||
            lowerCategories.contains("water") ||
            lowerCategories.contains("soda") ||
            lowerCategories.contains("напиток") ||
            lowerCategories.contains("сок") ||
            lowerCategories.contains("вода") ||
            lowerCategories.contains("газировк") ||
            lowerCategories.contains("чай") ||
            lowerCategories.contains("кофе") -> ProductCategory.BEVERAGES
            lowerCategories.contains("frozen") ||
            lowerCategories.contains("ice cream") ||
            lowerCategories.contains("замороженн") ||
            lowerCategories.contains("мороженое") ||
            lowerCategories.contains("пельмен") -> ProductCategory.FROZEN
            lowerCategories.contains("canned") ||
            lowerCategories.contains("preserved") ||
            lowerCategories.contains("консерв") ||
            lowerCategories.contains("тушенк") ||
            lowerCategories.contains("маринованн") -> ProductCategory.CANNED
            else -> ProductCategory.OTHER
        }
    }
}
