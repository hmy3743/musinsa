package myhan.musinsa.dto

import java.math.BigDecimal

data class CreateProductWithBrandRequestBody(
    val categoryId: String,
    val price: BigDecimal
)