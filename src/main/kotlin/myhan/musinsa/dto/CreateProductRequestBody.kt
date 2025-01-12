package myhan.musinsa.dto

import java.math.BigDecimal

data class CreateProductRequestBody(
    val brandId: String,
    val categoryId: String,
    val price: BigDecimal
)