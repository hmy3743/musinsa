package myhan.musinsa.dto

import java.math.BigDecimal

data class UpdateProductRequestBody(
    val brandId: String,
    val categoryId: String,
    val price: BigDecimal
)