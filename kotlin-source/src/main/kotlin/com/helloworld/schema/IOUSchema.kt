package com.helloworld.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for IOUState.
 */
object IOUSchema

/**
 * An IOUState schema.
 */
object IOUSchemaV : MappedSchema(
        schemaFamily = IOUSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentIOUSchema::class.java)) {
    @Entity
    @Table(name = "iou_states")
    class PersistentIOUSchema(
            @Column(name = "value")
            var value: Int,

            @Column(name = "lender")
            var lenderName: String,

            @Column(name = "borrower")
            var borrowerName: String,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // This is a default constructor required by hibernate.
        constructor(): this(0, "", "", UUID.randomUUID())
    }
}