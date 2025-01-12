package myhan.musinsa.dto

import java.math.BigDecimal

data class ListMinPriceProductPerCategoryResultItem(
    val brandId: String, val categoryId: String, val price: BigDecimal
)