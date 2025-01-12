package myhan.musinsa

import myhan.jooq.generated.tables.references.BRAND
import myhan.jooq.generated.tables.references.BRAND_CATEGORY
import myhan.jooq.generated.tables.references.CATEGORY
import myhan.jooq.generated.tables.references.PRODUCT
import myhan.musinsa.domain.Brand
import myhan.musinsa.domain.Product
import myhan.musinsa.dto.CreateBrandResult
import myhan.musinsa.dto.CreateProductWithBrandRequestBody
import myhan.musinsa.dto.UpdateBrandRequestBody
import myhan.musinsa.recordmapper.BrandRecorderMapper
import org.jooq.DSLContext
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class BrandService(val dsl: DSLContext) {
    fun list(after: String?, first: Int): List<Brand> {
        val query = dsl.selectFrom(BRAND)
            .orderBy(BRAND.ID)
            .limit(first)

        if (after != null) {
            query.`$where`(BRAND.ID.gt(after))
        }

        return query.fetch().map(BrandRecorderMapper())
    }

    @Transactional
    fun createBrand(id: String, productList: List<CreateProductWithBrandRequestBody>): CreateBrandResult {
        val brand = dsl.selectCount().from(BRAND).where(BRAND.ID.eq(id)).fetchOne()!!.get(0, Int::class.java)

        if (brand != 0) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Brand $id already exists")
        }

        val categorySet = productList.map { it.categoryId }.toSet()
        val categoryList = dsl.select(CATEGORY.ID).from(CATEGORY).fetch().map { it.get(CATEGORY.ID) }

        for (categoryId in categoryList) {
            if (categoryId !in categorySet) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category $categoryId for brand $id does not exist"
                )
            }
        }

        val createdBrand: Brand
        try {
            createdBrand = dsl.insertInto(BRAND)
                .set(BRAND.ID, id)
                .returning()
                .fetchOne()!!
                .into(Brand::class.java)
        } catch (e: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand $id must not contain small case english letters")
        }

        val createdProductList = productList.map { productParams ->
            val createdProduct = dsl.insertInto(PRODUCT)
                .set(PRODUCT.BRAND_ID, id)
                .set(PRODUCT.CATEGORY_ID, productParams.categoryId)
                .set(PRODUCT.PRICE, productParams.price)
                .returning()
                .fetchOne()!!
                .into(Product::class.java)

            dsl.insertInto(BRAND_CATEGORY)
                .set(BRAND_CATEGORY.BRAND_ID, id)
                .set(BRAND_CATEGORY.CATEGORY_ID, productParams.categoryId)
                .set(BRAND_CATEGORY.COUNT, 1)
                .onDuplicateKeyUpdate()
                .set(BRAND_CATEGORY.COUNT, BRAND_CATEGORY.COUNT.add(1))
                .execute()

            createdProduct
        }

        return CreateBrandResult(
            brand = createdBrand,
            productList = createdProductList
        )
    }

    @Transactional
    fun updateBrand(id: String, updateBrandRequestBody: UpdateBrandRequestBody): Brand {
        val newBrand: Brand
        try {
            newBrand = dsl.insertInto(BRAND)
                .set(BRAND.ID, updateBrandRequestBody.newId)
                .returning()
                .fetchOne()!!
                .into(Brand::class.java)
        } catch (e: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Brand ${updateBrandRequestBody.newId} already exists")
        }

        dsl.update(PRODUCT)
            .set(PRODUCT.BRAND_ID, updateBrandRequestBody.newId)
            .where(PRODUCT.BRAND_ID.eq(id))
            .execute()

        dsl.update(BRAND_CATEGORY)
            .set(BRAND_CATEGORY.BRAND_ID, updateBrandRequestBody.newId)
            .where(BRAND_CATEGORY.BRAND_ID.eq(id))
            .execute()

        dsl.deleteFrom(BRAND).where(BRAND.ID.eq(id)).execute()

        return newBrand
    }

    @Transactional
    fun deleteBrand(id: String) {
        dsl.selectFrom(BRAND).where(BRAND.ID.eq(id)).fetchOne()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Brand $id does not exist")
        dsl.deleteFrom(PRODUCT).where(PRODUCT.BRAND_ID.eq(id)).execute()
        dsl.deleteFrom(BRAND_CATEGORY).where(BRAND_CATEGORY.BRAND_ID.eq(id)).execute()
        dsl.deleteFrom(BRAND).where(BRAND.ID.eq(id)).execute()
    }
}