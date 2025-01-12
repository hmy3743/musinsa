package myhan.musinsa.dto

import java.math.BigDecimal

data class FindMinPriceProductsWithOneBrandResult(
    val brand: String,
    val category: List<FindMinPriceProductsWithOneBrandResultCategoryItem>,
    val totalCost: BigDecimal
)