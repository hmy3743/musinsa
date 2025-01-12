package myhan.musinsa

import myhan.jooq.generated.tables.references.BRAND_CATEGORY
import myhan.jooq.generated.tables.references.PRODUCT
import myhan.musinsa.domain.Product
import myhan.musinsa.dto.*
import myhan.musinsa.recordmapper.ProductRecordMapper
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
class ProductService(private val dsl: DSLContext) {
    fun listMinPriceProductPerCategory(): ListMinPriceProductPerCategoryResult {
        val minPriceProductsPerBrandCategory =
            dsl.select(
                PRODUCT.CATEGORY_ID,
                min(PRODUCT.PRICE)
                    .`as`(field("price", BigDecimal::class.java))
            )
                .from(PRODUCT)
                .groupBy(PRODUCT.CATEGORY_ID)

        val minPriceProductPerCategory = dsl.select(
            min(PRODUCT.BRAND_ID).`as`("brand_id"),
            PRODUCT.CATEGORY_ID,
            PRODUCT.PRICE
        )
            .from(PRODUCT)
            .join(minPriceProductsPerBrandCategory)
            .on(
                PRODUCT.CATEGORY_ID.eq(minPriceProductsPerBrandCategory.field(PRODUCT.CATEGORY_ID))
                    .and(PRODUCT.PRICE.eq(minPriceProductsPerBrandCategory.field("price", BigDecimal::class.java)))
            )
            .groupBy(PRODUCT.CATEGORY_ID, PRODUCT.PRICE)
            .fetch()

        val totalPrice = minPriceProductPerCategory.fold(BigDecimal(0)) { acc, record ->
            acc.add(record.get(PRODUCT.PRICE, BigDecimal::class.java))
        }

        return ListMinPriceProductPerCategoryResult(
            totalPrice,
            minPriceProductPerCategory.map { record ->
                ListMinPriceProductPerCategoryResultItem(
                    record.get("brand_id", String::class.java),
                    record.get(PRODUCT.CATEGORY_ID)!!,
                    record.get(PRODUCT.PRICE, BigDecimal::class.java)
                )
            }
        )
    }

    fun findMinPriceProductsWithOneBrand(): FindMinPriceProductsWithOneBrandResult {
        val cheapestProductsPerBrandCategory = dsl
            .select(
                PRODUCT.BRAND_ID,
                PRODUCT.CATEGORY_ID,
                min(PRODUCT.PRICE).`as`("min_price")
            )
            .from(PRODUCT)
            .groupBy(PRODUCT.BRAND_ID, PRODUCT.CATEGORY_ID)
            .fetch()

        val totalPricePerBrand = cheapestProductsPerBrandCategory.map { record ->
            Pair(record.get(PRODUCT.BRAND_ID)!!, record.get("min_price", BigDecimal::class.java))
        }.fold(mutableMapOf<String, BigDecimal>()) { acc, (brandId, price) ->
            acc[brandId] = acc.getOrDefault(brandId, BigDecimal.ZERO).add(price)
            acc
        }

        val brand = totalPricePerBrand.minByOrNull { it.value }!!.key
        val totalPrice = totalPricePerBrand[brand]!!

        return FindMinPriceProductsWithOneBrandResult(
            brand,
            cheapestProductsPerBrandCategory.filter { it.get(PRODUCT.BRAND_ID) == brand }.map { record ->
                FindMinPriceProductsWithOneBrandResultCategoryItem(
                    record.get(PRODUCT.BRAND_ID)!!,
                    record.get(PRODUCT.CATEGORY_ID)!!,
                    record.get("min_price", BigDecimal::class.java)
                )
            },
            totalPrice
        )
    }

    fun findMinMaxPriceProductByCategory(
        categoryId: String
    ): FindMinMaxPriceProductsByCategoryResult {
        val cheapestProductsByCategory = dsl.select().from(PRODUCT)
            .join(
                dsl.select(PRODUCT.PRICE.`as`("min_price"))
                    .from(PRODUCT)
                    .where(PRODUCT.CATEGORY_ID.eq(categoryId))
                    .orderBy(PRODUCT.PRICE.asc())
                    .limit(1)
                    .asTable("min_price_table")
            )
            .on(PRODUCT.PRICE.eq(field(name("min_price_table", "min_price"), BigDecimal::class.java)))
            .where(PRODUCT.CATEGORY_ID.eq(categoryId))
            .fetch()
            .map(ProductRecordMapper())

        val mostExpensiveProductsByCategory = dsl.select().from(PRODUCT)
            .join(
                dsl.select(PRODUCT.PRICE.`as`("max_price"))
                    .from(PRODUCT)
                    .where(PRODUCT.CATEGORY_ID.eq(categoryId))
                    .orderBy(PRODUCT.PRICE.desc())
                    .limit(1)
                    .asTable("max_price_table")
            )
            .on(PRODUCT.PRICE.eq(field(name("max_price_table", "max_price"), BigDecimal::class.java)))
            .where(PRODUCT.CATEGORY_ID.eq(categoryId))
            .fetch()
            .map(ProductRecordMapper())

        return FindMinMaxPriceProductsByCategoryResult(
            categoryId,
            cheapestProductsByCategory,
            mostExpensiveProductsByCategory
        )
    }

    fun listProducts(after: Int?, first: Int): List<Product> {
        val query = dsl.select().from(PRODUCT).orderBy(PRODUCT.ID.asc()).limit(first)
        if (after != null) {
            query.`$where`(PRODUCT.ID.gt(after))
        }
        return query.fetch().map(ProductRecordMapper())
    }

    @Transactional
    fun createProduct(brandId: String, categoryId: String, price: BigDecimal): Product {
        val updateCount = updateBrandCategoryCount(brandId, categoryId, 1)

        if (updateCount == 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Category \"$categoryId\" or brand \"$brandId\" not found"
            )
        }

        val product = dsl.insertInto(PRODUCT)
            .set(PRODUCT.BRAND_ID, brandId)
            .set(PRODUCT.CATEGORY_ID, categoryId)
            .set(PRODUCT.PRICE, price)
            .returning()
            .fetchOne()
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create product")

        return ProductRecordMapper().map(product)
    }

    @Transactional
    fun updateProduct(id: Int, brandId: String, categoryId: String, price: BigDecimal): Product {
        val original = dsl.select().from(PRODUCT).where(PRODUCT.ID.eq(id)).fetchOneInto(Product::class.java)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with id $id not found")

        if (original.brandId != brandId || original.categoryId != categoryId) {
            try {
                updateBrandCategoryCount(
                    original.brandId,
                    original.categoryId,
                    -1
                )
            } catch (e: DataIntegrityViolationException) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Every (brand, category) pair should have at least one product"
                )
            }

            val updateCount = updateBrandCategoryCount(
                brandId,
                categoryId,
                1
            )

            if (updateCount == 0) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category \"$categoryId\" or brand \"$brandId\" not found"
                )
            }
        }

        dsl.update(PRODUCT)
            .set(PRODUCT.BRAND_ID, brandId)
            .set(PRODUCT.CATEGORY_ID, categoryId)
            .set(PRODUCT.PRICE, price)
            .where(PRODUCT.ID.eq(id))
            .execute()

        return dsl.select().from(PRODUCT).where(PRODUCT.ID.eq(id)).fetchOneInto(Product::class.java)!!
    }

    @Transactional
    fun deleteProduct(id: Int) {
        val product = dsl.select().from(PRODUCT).where(PRODUCT.ID.eq(id)).fetchOneInto(Product::class.java)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with id $id not found")

        try {
            updateBrandCategoryCount(
                product.brandId,
                product.categoryId,
                -1
            )
        } catch (e: DataIntegrityViolationException) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Every (brand, category) pair should have at least one product"
            )
        }

        dsl.delete(PRODUCT).where(PRODUCT.ID.eq(id)).execute()
    }

    private fun updateBrandCategoryCount(brandId: String, categoryId: String, increment: Int): Int {
        return dsl.update(BRAND_CATEGORY)
            .set(BRAND_CATEGORY.COUNT, BRAND_CATEGORY.COUNT.add(increment))
            .where(BRAND_CATEGORY.BRAND_ID.eq(brandId))
            .and(BRAND_CATEGORY.CATEGORY_ID.eq(categoryId))
            .execute()
    }
}
