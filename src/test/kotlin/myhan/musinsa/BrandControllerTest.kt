package myhan.musinsa

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import myhan.musinsa.domain.Brand
import myhan.musinsa.domain.Product
import myhan.musinsa.dto.CreateBrandRequestBody
import myhan.musinsa.dto.CreateBrandResult
import myhan.musinsa.dto.CreateProductWithBrandRequestBody
import myhan.musinsa.dto.UpdateBrandRequestBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class BrandControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var brandService: BrandService

    @InjectMocks
    private lateinit var brandController: BrandController

    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(brandController).build()
    }

    @Test
    fun `listBrands should return list of brands`() {
        val brandList = listOf(Brand("TEST_BRAND_A"), Brand("TEST_BRAND_B"))
        `when`(brandService.list(null, 10)).thenReturn(brandList)

        mockMvc.perform(
            get("/api/brand")
                .param("first", "10")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(brandList)))

        verify(brandService, times(1)).list(null, 10)
    }

    @Test
    fun `createBrand should return created brand result`() {
        val createBrandRequestBody = CreateBrandRequestBody(
            "TEST_BRAND_A", listOf(
                CreateProductWithBrandRequestBody("TEST_CATEGORY_상의", BigDecimal("10000")),
                CreateProductWithBrandRequestBody("TEST_CATEGORY_하의", BigDecimal("20000"))
            )
        )
        val createBrandResult = CreateBrandResult(
            Brand("TEST_BRAND_A"), listOf(
                Product(1, "TEST_BRAND_A", "TEST_CATEGORY_상의", BigDecimal("10000")),
                Product(2, "TEST_BRAND_A", "TEST_CATEGORY_하의", BigDecimal("20000"))
            )
        )
        `when`(
            brandService.createBrand(
                createBrandRequestBody.id,
                createBrandRequestBody.createProductWithBrandRequestBodyList
            )
        ).thenReturn(createBrandResult)

        mockMvc.perform(
            post("/api/brand")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBrandRequestBody))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(createBrandResult)))

        verify(brandService, times(1)).createBrand(
            createBrandRequestBody.id,
            createBrandRequestBody.createProductWithBrandRequestBodyList
        )
    }

    @Test
    fun `updateBrand should return updated brand`() {
        val updateBrandRequestBody = UpdateBrandRequestBody("TEST_BRAND_A-UPDATED")
        val updatedBrand = Brand("TEST_BRAND_A-UPDATED")
        `when`(brandService.updateBrand("TEST_BRAND_A", updateBrandRequestBody)).thenReturn(updatedBrand)

        mockMvc.perform(
            put("/api/brand/TEST_BRAND_A")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBrandRequestBody))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(updatedBrand)))

        verify(brandService, times(1)).updateBrand("TEST_BRAND_A", updateBrandRequestBody)
    }

    @Test
    fun `deleteBrand should return no content`() {
        doNothing().`when`(brandService).deleteBrand("1")

        mockMvc.perform(delete("/api/brand/1"))
            .andExpect(status().isNoContent)

        verify(brandService, times(1)).deleteBrand("1")
    }
}