package myhan.musinsa.dto

import java.math.BigDecimal

data class ListMinPriceProductPerCategoryResult(
    val totalPrice: BigDecimal,
    val items: List<ListMinPriceProductPerCategoryResultItem>
)