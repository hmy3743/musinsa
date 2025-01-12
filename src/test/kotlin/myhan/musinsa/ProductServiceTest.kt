package myhan.musinsa

import myhan.jooq.generated.tables.BrandCategory
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import myhan.jooq.generated.tables.Brand as BrandTable
import myhan.jooq.generated.tables.Category as CategoryTable
import myhan.jooq.generated.tables.Product as ProductTable

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceTest {

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var dsl: DSLContext

    private val productTable = ProductTable.PRODUCT
    private val brandCategoryTable = BrandCategory.BRAND_CATEGORY
    private val brandTable = BrandTable.BRAND
    private val categoryTable = CategoryTable.CATEGORY

    @BeforeEach
    fun setUp() {
        // 테스트 전에 데이터베이스 초기화
        dsl.deleteFrom(productTable).execute()
        dsl.deleteFrom(brandCategoryTable).execute()
        dsl.deleteFrom(brandTable).execute()
        dsl.deleteFrom(categoryTable).execute()

        // 초기 데이터 삽입
        dsl.insertInto(brandTable)
            .columns(brandTable.ID)
            .values("TEST_BRAND_A")
            .values("TEST_BRAND_B")
            .execute()

        dsl.insertInto(categoryTable)
            .columns(categoryTable.ID)
            .values("TEST_CATEGORY_상의")
            .values("TEST_CATEGORY_하의")
            .execute()

        dsl.insertInto(productTable)
            .columns(productTable.BRAND_ID, productTable.CATEGORY_ID, productTable.PRICE)
            .values("TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal.valueOf(1000))
            .values("TEST_BRAND_B", "TEST_CATEGORY_상의", BigDecimal.valueOf(1000))
            .values("TEST_BRAND_A", "TEST_CATEGORY_하의", BigDecimal.valueOf(1000))
            .values("TEST_BRAND_B", "TEST_CATEGORY_하의", BigDecimal.valueOf(1000))
            .execute()

        dsl.insertInto(brandCategoryTable)
            .columns(brandCategoryTable.BRAND_ID, brandCategoryTable.CATEGORY_ID, brandCategoryTable.COUNT)
            .values("TEST_BRAND_A", "TEST_CATEGORY_상의", 1)
            .values("TEST_BRAND_B", "TEST_CATEGORY_상의", 1)
            .values("TEST_BRAND_A", "TEST_CATEGORY_하의", 1)
            .values("TEST_BRAND_B", "TEST_CATEGORY_하의", 1)
            .execute()
    }

    @Test
    fun testListMinPriceProductPerCategory() {
        // Given
        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_A")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_상의")
            .set(productTable.PRICE, BigDecimal.valueOf(100))
            .execute()

        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_B")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_하의")
            .set(productTable.PRICE, BigDecimal.valueOf(200))
            .execute()

        // When
        val result = productService.listMinPriceProductPerCategory()

        // Then
        assertEquals(BigDecimal("300.00"), result.totalPrice)
        assertEquals(2, result.items.size)
    }

    @Test
    fun testFindMinPriceProductsWithOneBrand() {
        // Given
        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_A")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_상의")
            .set(productTable.PRICE, BigDecimal.valueOf(100))
            .execute()

        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_A")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_하의")
            .set(productTable.PRICE, BigDecimal.valueOf(200))
            .execute()

        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_B")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_상의")
            .set(productTable.PRICE, BigDecimal.valueOf(100))
            .execute()

        // When
        val result = productService.findMinPriceProductsWithOneBrand()

        // Then
        assertEquals("TEST_BRAND_A", result.brand)
        assertEquals(2, result.category.size)
        assertEquals(BigDecimal("300.00"), result.totalCost)
    }

    @Test
    fun testFindMinMaxPriceProductByCategory() {
        // Given
        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_A")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_상의")
            .set(productTable.PRICE, BigDecimal.valueOf(1))
            .execute()

        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_B")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_상의")
            .set(productTable.PRICE, BigDecimal.valueOf(999999))
            .execute()

        // When
        val result = productService.findMinMaxPriceProductByCategory("TEST_CATEGORY_상의")

        // Then
        assertEquals("TEST_CATEGORY_상의", result.categoryId)
        assertEquals(1, result.minPriceProductList.size)
        assertEquals(1, result.maxPriceProductList.size)
        assertEquals("TEST_BRAND_A", result.minPriceProductList[0].brandId)
        assertEquals(BigDecimal("1.00"), result.minPriceProductList[0].price)
        assertEquals("TEST_BRAND_B", result.maxPriceProductList[0].brandId)
        assertEquals(BigDecimal("999999.00"), result.maxPriceProductList[0].price)

    }

    @Test
    fun testListProducts() {
        // Given
        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_A")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_상의")
            .set(productTable.PRICE, BigDecimal.valueOf(1000))
            .execute()

        dsl.insertInto(productTable)
            .set(productTable.BRAND_ID, "TEST_BRAND_B")
            .set(productTable.CATEGORY_ID, "TEST_CATEGORY_하의")
            .set(productTable.PRICE, BigDecimal.valueOf(2000))
            .execute()

        // When
        val result = productService.listProducts(null, 2)

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun testCreateProduct() {
        // When
        val product = productService.createProduct("TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal.valueOf(1000))

        // Then
        assertEquals("TEST_BRAND_A", product.brandId)
        assertEquals("TEST_CATEGORY_상의", product.categoryId)
        assertEquals(BigDecimal("1000.00"), product.price)
    }

    @Test
    fun testCreateProductWithInvalidBrandOrCategory() {
        // When & Then
        val exception = assertThrows(ResponseStatusException::class.java) {
            productService.createProduct("invalidBrand", "invalidCategory", BigDecimal.valueOf(1000))
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun testUpdateProduct() {
        // Given
        val product = productService.createProduct("TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal.valueOf(1000))

        // When
        val updatedProduct =
            productService.updateProduct(product.id, "TEST_BRAND_B", "TEST_CATEGORY_하의", BigDecimal.valueOf(2000))

        // Then
        assertEquals("TEST_BRAND_B", updatedProduct.brandId)
        assertEquals("TEST_CATEGORY_하의", updatedProduct.categoryId)
        assertEquals(BigDecimal("2000.00"), updatedProduct.price)
    }

    @Test
    fun testUpdateProductWithInvalidBrandOrCategory() {
        // Given
        val product = productService.createProduct("TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal.valueOf(1000))

        // When & Then
        val exception = assertThrows(ResponseStatusException::class.java) {
            productService.updateProduct(product.id, "invalidBrand", "invalidCategory", BigDecimal.valueOf(2000))
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun testDeleteProduct() {
        // Given
        val product = productService.createProduct("TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal.valueOf(1000))

        // When
        productService.deleteProduct(product.id)

        // Then
        val exception = assertThrows(ResponseStatusException::class.java) {
            productService.updateProduct(product.id, "TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal.valueOf(1001))
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun testDeleteProductWithInvalidId() {
        // When & Then
        val exception = assertThrows(ResponseStatusException::class.java) {
            productService.deleteProduct(-1)
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }
}