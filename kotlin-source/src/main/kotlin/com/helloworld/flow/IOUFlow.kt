package com.helloworld.flow

import co.paralleluniverse.fibers.Suspendable
import com.helloworld.contract.IOUContract
import com.helloworld.contract.IOUContract.Companion.IOU_CONTRACT_ID
import com.helloworld.state.IOUState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class IOUFlow(val iouValue: Int,
              val otherParty: Party) : FlowLogic<SignedTransaction>() {

    /** The progress tracker provides checkpoints indicating the progress of the flow to observers. */
    override val progressTracker = ProgressTracker()

    /** The flow logic is encapsulated within the call() method. */
    @Suspendable
    override fun call(): SignedTransaction {
        // We retrieve the notary identity from the network map.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
//
//        // We create the transaction components.
//        val outputState = IOUState(iouValue, ourIdentity, otherParty)
//        val cmd = Command(IOUContract.Commands.Create(), ourIdentity.owningKey)
//
//        // We create a transaction builder and add the components.
//        val txBuilder = TransactionBuilder(notary = notary)
//                .addOutputState(outputState, IOU_CONTRACT_ID)
//                .addCommand(cmd)
//
//        // We sign the transaction.
//        val signedTx = serviceHub.signInitialTransaction(txBuilder)
//
//        // We finalise the transaction.
//        return subFlow(FinalityFlow(signedTx))

        // We create a transaction builder.
        val txBuilder = TransactionBuilder(notary = notary)

        // We create the transaction components.
        val outputState = IOUState(iouValue, ourIdentity, otherParty)
        val outputContractAndState = StateAndContract(outputState, IOU_CONTRACT_ID)
        val cmd = Command(IOUContract.Commands.Create(), listOf(ourIdentity.owningKey, otherParty.owningKey))

        // We add the items to the builder.
        txBuilder.withItems(outputContractAndState, cmd)

        // Verifying the transaction.
        txBuilder.verify(serviceHub)

        // Signing the transaction.
        val signedTx = serviceHub.signInitialTransaction(txBuilder)

        // Creating a session with the other party.
        val otherpartySession = initiateFlow(otherParty)

        // Obtaining the counterparty's signature.
        val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, listOf(otherpartySession), CollectSignaturesFlow.tracker()))

        // Finalising the transaction.
        return subFlow(FinalityFlow(fullySignedTx))
    }
}

@InitiatedBy(IOUFlow::class)
class IOUFlowResponder(val otherPartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession, SignTransactionFlow.tracker()) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction." using (output is IOUState)
                val iou = output as IOUState
                "The IOU's value can't be too high." using (iou.value < 100)
            }
        }

        subFlow(signTransactionFlow)
    }
}
