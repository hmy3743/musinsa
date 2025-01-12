package myhan.musinsa.recordmapper

import myhan.jooq.generated.tables.references.PRODUCT
import myhan.musinsa.domain.Product
import org.jooq.Record
import org.jooq.RecordMapper
import java.math.BigDecimal

class ProductRecordMapper : RecordMapper<Record, Product> {
    override fun map(record: Record): Product {
        return Product(
            record.get(PRODUCT.ID)!!,
            record.get(PRODUCT.BRAND_ID)!!,
            record.get(PRODUCT.CATEGORY_ID)!!,
            record.get(PRODUCT.PRICE, BigDecimal::class.java)
        )
    }
}