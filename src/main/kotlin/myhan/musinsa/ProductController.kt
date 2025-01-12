package myhan.musinsa

import io.swagger.v3.oas.annotations.Operation
import myhan.musinsa.domain.Product
import myhan.musinsa.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/product")
class ProductController(private val productService: ProductService) {
    @GetMapping("/min_price_products")
    @Operation(summary = "구현 1) - 카테고리 별 최저가격 브랜드와 상품 가격, 총액을 조회하는 API")
    fun getMinPriceProductByCategory(): ResponseEntity<ListMinPriceProductPerCategoryResult> {
        val productList = productService.listMinPriceProductPerCategory()
        return ResponseEntity.ok(productList)
    }

    @GetMapping("/min_price_products_with_one_brand")
    @Operation(
        summary = "구현 2) - 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격에 판매하는 브랜드와 카테고리의 상품가격, 총액을\n" +
                "조회하는 API"
    )
    fun getMinPriceProductWithOneBrand(): ResponseEntity<FindMinPriceProductsWithOneBrandResult> {
        val result = productService.findMinPriceProductsWithOneBrand()
        return ResponseEntity.ok(result)
    }

    @GetMapping("/category/{category_id}/min_max_price_products")
    @Operation(summary = "구현 3) - 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격을 조회하는 API")
    fun getMinMaxPriceProductByCategory(@PathVariable("category_id") categoryId: String): ResponseEntity<FindMinMaxPriceProductsByCategoryResult> {
        val productList = productService.findMinMaxPriceProductByCategory(categoryId)
        return ResponseEntity.ok(productList)
    }

    @GetMapping("")
    fun listProducts(
        @RequestParam after: Int?,
        @RequestParam(required = true, defaultValue = "10") first: Int
    ): ResponseEntity<List<Product>> {
        val productList = productService.listProducts(after, first)
        return ResponseEntity.ok(productList)
    }

    @PutMapping("/{product_id}")
    @Operation(summary = "과제 4) 상품 정보 수정")
    fun updateProduct(
        @PathVariable("product_id") productId: Int,
        @RequestBody productParams: UpdateProductRequestBody
    ): ResponseEntity<Product> {
        val product = productService.updateProduct(
            productId,
            productParams.brandId,
            productParams.categoryId,
            productParams.price
        )
        return ResponseEntity.ok(product)
    }

    @PostMapping("")
    @Operation(summary = "과제 4) 상품 추가")
    fun createProduct(@RequestBody productParams: CreateProductRequestBody): ResponseEntity<Product> {
        val product = productService.createProduct(productParams.brandId, productParams.categoryId, productParams.price)
        return ResponseEntity.ok(product)
    }

    @DeleteMapping("/{product_id}")
    @Operation(summary = "과제 4) 상품 삭제")
    fun deleteProduct(@PathVariable("product_id") productId: Int): ResponseEntity<Unit> {
        productService.deleteProduct(productId)
        return ResponseEntity.ok().build()
    }
}