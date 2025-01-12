package myhan.musinsa

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import myhan.musinsa.domain.Product
import myhan.musinsa.dto.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal

class ProductControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var productService: ProductService

    @InjectMocks
    private lateinit var productController: ProductController

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this) // Mockito 초기화
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build() // 컨트롤러 설정
    }

    @Test
    fun `getMinPriceProductByCategory should return min price products per category`() {
        val result = ListMinPriceProductPerCategoryResult(
            BigDecimal("2000.00"),
            listOf(
                ListMinPriceProductPerCategoryResultItem("TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal("1000.00")),
                ListMinPriceProductPerCategoryResultItem("TEST_BRAND_A", "TEST_CATEGORY_하의", BigDecimal("1000.00"))
            )
        )

        `when`(productService.listMinPriceProductPerCategory()).thenReturn(result)

        mockMvc.perform(get("/api/product/min_price_products"))
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(result)))

        verify(productService, times(1)).listMinPriceProductPerCategory()
    }

    @Test
    fun `getMinPriceProductWithOneBrand should return min price products with one brand`() {
        val result = FindMinPriceProductsWithOneBrandResult(
            "TEST_BRAND_A", listOf(
                FindMinPriceProductsWithOneBrandResultCategoryItem(
                    "TEST_BRAND_A",
                    "TEST_CATEGORY_상의",
                    BigDecimal("1000.00")
                )
            ), BigDecimal("1000.00")
        )
        `when`(productService.findMinPriceProductsWithOneBrand()).thenReturn(result)

        mockMvc.perform(get("/api/product/min_price_products_with_one_brand"))
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(result)))

        verify(productService, times(1)).findMinPriceProductsWithOneBrand()
    }

    @Test
    fun `getMinMaxPriceProductByCategory should return min and max price products for a category`() {
        val categoryId = "TEST_CATEGORY_상의"
        val result = FindMinMaxPriceProductsByCategoryResult(
            "TEST_CATEGORY_상의",
            listOf(
                Product(1, "TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal("1000.00")),
                Product(2, "TEST_BRAND_B", "TEST_CATEGORY_상의", BigDecimal("1000.00"))
            ),
            listOf(
                Product(3, "TEST_BRAND_C", "TEST_CATEGORY_상의", BigDecimal("10000.00")),
                Product(4, "TEST_BRAND_D", "TEST_CATEGORY_상의", BigDecimal("10000.00"))
            )
        )
        `when`(productService.findMinMaxPriceProductByCategory(categoryId)).thenReturn(result)

        mockMvc.perform(get("/api/product/category/$categoryId/min_max_price_products"))
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(result)))

        verify(productService, times(1)).findMinMaxPriceProductByCategory(categoryId)
    }

    @Test
    fun `listProducts should return a list of products`() {
        val after = 1
        val first = 10
        val productList = listOf(Product(2, "TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal("1000.00")))
        `when`(productService.listProducts(after, first)).thenReturn(productList)

        mockMvc.perform(get("/api/product?after=$after&first=$first"))
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(productList)))

        verify(productService, times(1)).listProducts(after, first)
    }

    @Test
    fun `updateProduct should update and return the updated product`() {
        val productId = 1
        val productParams = UpdateProductRequestBody("TEST_BRAND_Z", "TEST_CATEGORY_하의", BigDecimal("10000.00"))
        val updatedProduct = Product(productId, productParams.brandId, productParams.categoryId, productParams.price)
        `when`(
            productService.updateProduct(
                productId,
                productParams.brandId,
                productParams.categoryId,
                productParams.price
            )
        )
            .thenReturn(updatedProduct)

        mockMvc.perform(
            put("/api/product/$productId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productParams))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(updatedProduct)))

        verify(productService, times(1)).updateProduct(
            productId,
            productParams.brandId,
            productParams.categoryId,
            productParams.price
        )
    }

    @Test
    fun `createProduct should create and return the created product`() {
        val productParams = CreateProductRequestBody("TEST_BRAND_Z", "TEST_CATEGORY_하의", BigDecimal("10000.00"))
        val createdProduct = Product(100, productParams.brandId, productParams.categoryId, productParams.price)
        `when`(productService.createProduct(productParams.brandId, productParams.categoryId, productParams.price))
            .thenReturn(createdProduct)

        mockMvc.perform(
            post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productParams))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(createdProduct)))

        verify(productService, times(1)).createProduct(
            productParams.brandId,
            productParams.categoryId,
            productParams.price
        )
    }

    @Test
    fun `deleteProduct should delete the product`() {
        val productId = 1

        mockMvc.perform(delete("/api/product/$productId"))
            .andExpect(status().isOk)

        verify(productService, times(1)).deleteProduct(productId)
    }
}