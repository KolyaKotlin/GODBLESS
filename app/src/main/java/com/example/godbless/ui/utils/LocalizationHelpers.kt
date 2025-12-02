package com.example.godbless.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.godbless.R
import com.example.godbless.domain.model.ProductCategory
import com.example.godbless.domain.model.StorageLocation

@Composable
fun ProductCategory.getLocalizedName(): String {
    return when (this) {
        ProductCategory.DAIRY -> stringResource(R.string.category_dairy)
        ProductCategory.MEAT -> stringResource(R.string.category_meat)
        ProductCategory.FISH -> stringResource(R.string.category_fish)
        ProductCategory.VEGETABLES -> stringResource(R.string.category_vegetables)
        ProductCategory.FRUITS -> stringResource(R.string.category_fruits)
        ProductCategory.BAKERY -> stringResource(R.string.category_bakery)
        ProductCategory.FROZEN -> stringResource(R.string.category_frozen)
        ProductCategory.CANNED -> stringResource(R.string.category_canned)
        ProductCategory.BEVERAGES -> stringResource(R.string.category_beverages)
        ProductCategory.OTHER -> stringResource(R.string.category_other)
    }
}

@Composable
fun StorageLocation.getLocalizedName(): String {
    return when (this) {
        StorageLocation.FRIDGE -> stringResource(R.string.location_fridge)
        StorageLocation.FREEZER -> stringResource(R.string.location_freezer)
        StorageLocation.PANTRY -> stringResource(R.string.location_pantry)
        StorageLocation.COUNTER -> stringResource(R.string.location_counter)
    }
}
