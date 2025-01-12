package myhan.musinsa.dto

import myhan.musinsa.domain.Brand
import myhan.musinsa.domain.Product

data class CreateBrandResult(
    val brand: Brand,
    val productList: List<Product>
)