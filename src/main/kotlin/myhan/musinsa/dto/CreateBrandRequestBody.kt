package myhan.musinsa.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CreateBrandRequestBody(
    @Schema(description = "Brand ID", example = "AA")
    val id: String,
    @Schema(
        description = "List of products to create with the brand.  Must contains at least one product for every category.",
        example = "[\n" +
                "  {\"categoryId\":\"상의\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"아우터\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"바지\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"스니커즈\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"가방\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"모자\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"양말\", \"price\":\"10000\"},\n" +
                "  {\"categoryId\":\"액세서리\", \"price\":\"10000\"}\n" +
                "]"
    )
    val createProductWithBrandRequestBodyList: List<CreateProductWithBrandRequestBody>
)