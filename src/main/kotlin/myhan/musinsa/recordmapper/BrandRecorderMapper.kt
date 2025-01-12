package myhan.musinsa.recordmapper

import myhan.jooq.generated.tables.references.BRAND
import myhan.musinsa.domain.Brand
import org.jooq.Record
import org.jooq.RecordMapper

class BrandRecorderMapper : RecordMapper<Record, Brand> {
    override fun map(record: Record): Brand {
        return Brand(record.get(BRAND.ID)!!)
    }
}