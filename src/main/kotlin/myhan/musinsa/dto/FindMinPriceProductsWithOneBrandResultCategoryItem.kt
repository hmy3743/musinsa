package myhan.musinsa.dto

import java.math.BigDecimal

data class FindMinPriceProductsWithOneBrandResultCategoryItem(
    val brandId: String, val categoryId: String, val price: BigDecimal
)