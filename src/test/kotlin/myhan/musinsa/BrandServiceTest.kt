package myhan.musinsa

import myhan.jooq.generated.tables.Brand
import myhan.jooq.generated.tables.BrandCategory
import myhan.jooq.generated.tables.Category
import myhan.jooq.generated.tables.Product
import myhan.musinsa.dto.CreateProductWithBrandRequestBody
import myhan.musinsa.dto.UpdateBrandRequestBody
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BrandServiceTest {

    @Autowired
    lateinit var brandService: BrandService

    @Autowired
    lateinit var dsl: DSLContext

    @BeforeEach
    fun setUp() {
        // 테스트 전에 필요한 초기 데이터를 설정합니다.
        dsl.deleteFrom(Product.PRODUCT).execute()
        dsl.deleteFrom(BrandCategory.BRAND_CATEGORY).execute()
        dsl.deleteFrom(Category.CATEGORY).execute()
        dsl.deleteFrom(Brand.BRAND).execute()

        insertCategory("TEST_CATEGORY_아우터")
        insertCategory("TEST_CATEGORY_신발")
    }

    @Test
    fun testCreateBrand() {
        val productList = listOf(
            CreateProductWithBrandRequestBody(categoryId = "TEST_CATEGORY_아우터", price = BigDecimal("10000")),
            CreateProductWithBrandRequestBody(categoryId = "TEST_CATEGORY_신발", price = BigDecimal("20000"))
        )

        val result = brandService.createBrand("TEST_BRAND_1", productList)

        assertNotNull(result.brand)
        assertEquals("TEST_BRAND_1", result.brand.id)
        assertEquals(2, result.productList.size)
    }

    @Test
    fun testCreateBrandWithDuplicateId() {
        insertBrand("TEST_BRAND_1")

        val productList = listOf(
            CreateProductWithBrandRequestBody(categoryId = "TEST_CATEGORY_아우터", price = BigDecimal("10000"))
        )

        val exception = assertThrows(ResponseStatusException::class.java) {
            brandService.createBrand("TEST_BRAND_1", productList)
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
    }

    @Test
    fun testCreateBrandWithNonExistentCategory() {
        val productList = listOf(
            CreateProductWithBrandRequestBody(categoryId = "nonExistentCategory", price = BigDecimal("10000"))
        )

        val exception = assertThrows(ResponseStatusException::class.java) {
            brandService.createBrand("TEST_BRAND_1", productList)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
    }

    @Test
    fun testUpdateBrand() {
        insertBrand("TEST_BRAND_1")
        val fixtureProduct = insertProduct("TEST_BRAND_1", "TEST_CATEGORY_아우터", BigDecimal("10000"))
        insertBrandCategory("TEST_BRAND_1", "TEST_CATEGORY_아우터", 1)

        val updateBrandRequestBody = UpdateBrandRequestBody(newId = "TEST_BRAND_2")
        val updatedBrand = brandService.updateBrand("TEST_BRAND_1", updateBrandRequestBody)

        assertEquals("TEST_BRAND_2", updatedBrand.id)

        val product = dsl.selectFrom(Product.PRODUCT).where(Product.PRODUCT.ID.eq(fixtureProduct.id)).fetchOne()
        assertEquals("TEST_BRAND_2", product?.get(Product.PRODUCT.BRAND_ID))

        val brandCategory = dsl.selectFrom(BrandCategory.BRAND_CATEGORY)
            .where(BrandCategory.BRAND_CATEGORY.CATEGORY_ID.eq("TEST_CATEGORY_아우터"))
            .and(BrandCategory.BRAND_CATEGORY.BRAND_ID.eq("TEST_BRAND_2"))
            .and(BrandCategory.BRAND_CATEGORY.COUNT.eq(1))
            .fetchOne()
        assertNotNull(brandCategory)
    }

    @Test
    fun testUpdateBrandWithDuplicateId() {
        insertBrand("TEST_BRAND_1")
        insertBrand("TEST_BRAND_2")

        val updateBrandRequestBody = UpdateBrandRequestBody(newId = "TEST_BRAND_2")
        val exception = assertThrows(ResponseStatusException::class.java) {
            brandService.updateBrand("TEST_BRAND_1", updateBrandRequestBody)
        }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
    }

    @Test
    fun testDeleteBrand() {
        insertBrand("TEST_BRAND_1")
        insertProduct("TEST_BRAND_1", "TEST_CATEGORY_아우터", BigDecimal("10000"))
        insertBrandCategory("TEST_BRAND_1", "TEST_CATEGORY_아우터", 1)

        brandService.deleteBrand("TEST_BRAND_1")

        val brand = dsl.selectFrom(Brand.BRAND).where(Brand.BRAND.ID.eq("TEST_BRAND_1")).fetchOne()
        assertNull(brand)

        val productList = dsl.selectFrom(Product.PRODUCT).where(Product.PRODUCT.BRAND_ID.eq("TEST_BRAND_1")).fetch()
        assertEquals(0, productList.size)

        val brandCategory = dsl.selectFrom(BrandCategory.BRAND_CATEGORY)
            .where(BrandCategory.BRAND_CATEGORY.BRAND_ID.eq("TEST_BRAND_1"))
            .fetchOne()
        assertNull(brandCategory)
    }

    @Test
    fun testDeleteNonExistentBrand() {
        val exception = assertThrows(ResponseStatusException::class.java) {
            brandService.deleteBrand("nonExistentBrand")
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    // Helper functions to insert data
    private fun insertCategory(categoryId: String) {
        dsl.insertInto(Category.CATEGORY)
            .set(Category.CATEGORY.ID, categoryId)
            .execute()
    }

    private fun insertBrand(brandId: String) {
        dsl.insertInto(Brand.BRAND)
            .set(Brand.BRAND.ID, brandId)
            .execute()
    }

    private fun insertProduct(brandId: String, categoryId: String, price: BigDecimal): myhan.musinsa.domain.Product {
        return dsl.insertInto(Product.PRODUCT)
            .set(Product.PRODUCT.BRAND_ID, brandId)
            .set(Product.PRODUCT.CATEGORY_ID, categoryId)
            .set(Product.PRODUCT.PRICE, price)
            .returning()
            .fetchOneInto(myhan.musinsa.domain.Product::class.java)!!
    }

    private fun insertBrandCategory(
        brandId: String,
        categoryId: String,
        count: Int
    ): myhan.jooq.generated.tables.pojos.BrandCategory {
        return dsl.insertInto(BrandCategory.BRAND_CATEGORY)
            .set(BrandCategory.BRAND_CATEGORY.BRAND_ID, brandId)
            .set(BrandCategory.BRAND_CATEGORY.CATEGORY_ID, categoryId)
            .set(BrandCategory.BRAND_CATEGORY.COUNT, count)
            .returning()
            .fetchOneInto(myhan.jooq.generated.tables.pojos.BrandCategory::class.java)!!
    }
}