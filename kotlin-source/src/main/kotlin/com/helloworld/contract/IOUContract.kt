package com.helloworld.contract

import com.helloworld.state.IOUState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class IOUContract : Contract {
    companion object {
        @JvmStatic
        val IOU_CONTRACT_ID = "com.helloworld.contract.IOUContract"
    }


    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands.Create>()
        requireThat {
            requireThat {
                // Constraints on the shape of the transaction.
                "No inputs should be consumed when issuing an IOU." using (tx.inputs.isEmpty())
                "There should be one output state of type IOUState." using (tx.outputs.size == 1)

                // IOU-specific constraints.
                val out = tx.outputsOfType<IOUState>().single()
                "The IOU's value must be non-negative." using (out.value > 0)
                "The lender and the borrower cannot be the same entity." using (out.lender != out.borrower)

                // Constraints on the signers.
                "There must be two signers." using (command.signers.toSet().size == 2)
                "The borrower and lender must be signers." using (command.signers.containsAll(listOf(
                        out.borrower.owningKey, out.lender.owningKey)))
            }
        }
    }


    interface Commands : CommandData {
        class Create : Commands
    }
}
