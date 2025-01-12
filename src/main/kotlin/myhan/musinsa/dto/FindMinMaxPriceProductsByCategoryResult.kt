package myhan.musinsa.dto

import myhan.musinsa.domain.Product

data class FindMinMaxPriceProductsByCategoryResult(
    val categoryId: String,
    val minPriceProductList: List<Product>,
    val maxPriceProductList: List<Product>
)