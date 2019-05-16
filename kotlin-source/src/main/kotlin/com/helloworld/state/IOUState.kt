package com.helloworld.state

import com.helloworld.schema.IOUSchemaV
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

data class IOUState(val value: Int,
                    val lender: Party,
                    val borrower: Party,
                    override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    override val participants: List<AbstractParty> get() = listOf(lender, borrower)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is IOUSchemaV -> IOUSchemaV.PersistentIOUSchema(
                    this.value,
                    this.lender.name.toString(),
                    this.borrower.name.toString(),
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(IOUSchemaV)
}
