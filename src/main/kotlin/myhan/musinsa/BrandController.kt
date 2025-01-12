package myhan.musinsa

import io.swagger.v3.oas.annotations.Operation
import myhan.musinsa.domain.Brand
import myhan.musinsa.dto.CreateBrandRequestBody
import myhan.musinsa.dto.CreateBrandResult
import myhan.musinsa.dto.UpdateBrandRequestBody
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/brand")
class BrandController(val brandService: BrandService) {
    @GetMapping("")
    fun listBrands(
        @RequestParam after: String?,
        @RequestParam(required = true, defaultValue = "10") first: Int
    ): ResponseEntity<List<Brand>> {
        val brandList = brandService.list(after, first)
        return ResponseEntity.ok(brandList)
    }

    @PostMapping("")
    @Operation(summary = "과제 4) 브랜드 추가")
    fun createBrand(@RequestBody createBrandRequestBody: CreateBrandRequestBody): ResponseEntity<CreateBrandResult> {
        val result = brandService.createBrand(
            createBrandRequestBody.id,
            createBrandRequestBody.createProductWithBrandRequestBodyList
        )
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{id}")
    @Operation(summary = "과제 4) 브랜드 정보 수정")
    fun updateBrand(
        @PathVariable id: String,
        @RequestBody updateBrandRequestBody: UpdateBrandRequestBody
    ): ResponseEntity<Brand> {
        val brand = brandService.updateBrand(id, updateBrandRequestBody)
        return ResponseEntity.ok(brand)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "과제 4) 브랜드 삭제")
    fun deleteBrand(@PathVariable id: String): ResponseEntity<Unit> {
        brandService.deleteBrand(id)
        return ResponseEntity.noContent().build()
    }
}