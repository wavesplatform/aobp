package ru.tolsi.aobp.blockchain.base

import ru.tolsi.aobp.blockchain.base.bytes.BytesSerializable
import rx.Observable
import scorex.crypto.encode.Base58

case class Sign[+WS <: WithSign](value: Array[Byte])
trait WithSign
trait SignCreator[WS <: WithSign] {
  def createSign(ws: WS): Sign[WS]
}

trait Signable

abstract class Signature[V] {
  def value: V
}

abstract class Signer[BC <: BlockChain, S <: Signable with WithSign, SV <: Signed[S, SI], SI <: Signature[Array[Byte]]] {
  def sign(obj: S)(implicit bc: BC): SV
}

abstract class ArrayByteSignature extends Signature[Array[Byte]] {
  override def hashCode(): Int = {
    LyHash.compute(value)
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case as: ArrayByteSignature => as.value sameElements value
    case _ => false
  }

  override def toString: String = Base58.encode(value)
}

class Signature32(val value: Array[Byte]) extends Signature[Array[Byte]] {
  require(value.length == 32, "Signature32 should have 32 bytes")
}

class Signature64(val value: Array[Byte]) extends Signature[Array[Byte]] {
  require(value.length == 64, "Signature64 should have 64 bytes")
}

sealed trait Validable

abstract class AbstractValidationError[V <: Validable](m: => String) {
  def message: String = m
}

trait Signed[+S <: Signable with WithSign, SI <: Signature[Array[Byte]]] {
  def signature: SI
  def signed: S
}

trait StateChangeReason

// todo VE, E <: AbstractValidationError[VE] ?
abstract class ValidatorOnBlockChain[BC <: BlockChain, V <: Validable, VO >: V <: Validable, E <: AbstractValidationError[VO]] {
  type ResultT = Either[ErrorsSeqT, ValidatedT]
  type ErrorsSeqT = Seq[E]
  type ValidatedT = VO
  def validate(tx: V)(implicit bc: BC): ResultT
}

abstract class BlockChainTransaction[+BC <: BlockChain] extends WithSign with Signable with Validable with StateChangeReason with BytesSerializable

trait BlockChainSignedTransaction[BC <: BlockChain, TX <: BC#T, SI <: Signature[Array[Byte]]] extends BlockChainTransaction[BC] with Signed[TX, SI] {

  protected trait ValidationError extends AbstractValidationError[BC#T]

}

abstract class BlockChainBlock[+BC <: BlockChain] extends WithSign with Signable with Validable with StateChangeReason with BytesSerializable {
  type Id
}

trait BlockChainSignedBlock[BC <: BlockChain, BL <: BC#B, SI <: Signature[Array[Byte]]] extends BlockChainBlock[BC] with Signed[BL, SI] {

  protected trait ValidationError extends AbstractValidationError[BC#B]

}

case class StateChange[BC <: BlockChain](account: BC#BA, amount: Long)

abstract class BlockChainAccount[BC <: BlockChain](val publicKey: Array[Byte], val privateKey: Option[Array[Byte]])

abstract class BlockChainAddress[BC <: BlockChain](val address: Array[Byte]) extends Validable

abstract class TransactionValidationError[BC <: BlockChain, +TX <: BC#T](message: => String) extends AbstractValidationError[BC#T](message)

abstract class BlockValidationError[BC <: BlockChain, +BL <: BC#B](message: => String) extends AbstractValidationError[BC#B](message)

abstract class TransactionValidator[BC <: BlockChain, TX <: BC#T] extends ValidatorOnBlockChain[BC, TX, BC#T, TransactionValidationError[BC, BC#T]]

abstract class AbstractSignedTransactionValidator[BC <: BlockChain, TX <: BC#T, STX <: BC#ST[TX]] extends ValidatorOnBlockChain[BC, STX, BC#T, TransactionValidationError[BC, BC#T]]

abstract class AbstractBlockValidator[BC <: BlockChain, BL <: BC#B] extends ValidatorOnBlockChain[BC, BL, BC#B, BlockValidationError[BC, BC#B]]
abstract class AbstractSignedBlockValidator[BC <: BlockChain, BL <: BC#B, SBL <: BC#SB[BL]] extends ValidatorOnBlockChain[BC, SBL, BC#B, BlockValidationError[BC, BC#B]]

trait BlockTransactionParameters[BC <: BlockChain]

trait BlockChain {
  type T <: BlockChainTransaction[_ <: BlockChain]
  type ST[TX <: T] <: Signed[TX, Signature64] with T
  type B <: BlockChainBlock[_ <: BlockChain]
  type SB[BL <: B] <: Signed[BL, Signature64] with B
  type AC <: BlockChainAccount[_ <: BlockChain]
  type AD <: BlockChainAddress[_ <: BlockChain]
  type TXV <: AbstractSignedTransactionValidator[_ <: BlockChain, T, ST[T]]
  type TVP <: BlockTransactionParameters[_ <: BlockChain]
  type SBV <: AbstractSignedBlockValidator[_ <: BlockChain, B, SB[B]]
  type BA

  def genesis: B
}


abstract class Wallet[BC <: BlockChain] {
  def createNewAccount: BC#AC
}

abstract class BlockGenerator[BC <: BlockChain] {
  def blocks: Observable[BC#B]
}

trait BlockChainApp[BC <: BlockChain] {
  def blockChain: BlockChain

  def wallet: Wallet[BC]

  def utx: BC#T

  def miner: BlockGenerator[BC]
}

// todo хранить блокчейн как дерево и удалять неосновные ветки после N
abstract class BlockStorage[BC <: BlockChain, BSB <: BC#SB[BC#B], BId <: BC#B#Id] {
  type BlockId = BId
  type SignedBlock = BSB
  def put(block: SignedBlock): Unit

  def get(id: BlockId): Option[SignedBlock]

  def contains(id: BlockId): Boolean

  def remove(id: BlockId): Option[SignedBlock]
}

abstract class UnconfirmedTransactionStorage[BC <: BlockChain] {
  def put(tx: BC#T): Unit

  def all: Seq[BC#T]

  def remove(tx: BC#T): Option[BC#T]
}

abstract class StateStorage[BC <: BlockChain, SignedBlock <: BC#SB[BC#B], BBA <: BC#BA] {
  type BalanceValue = Long

  def currentState: Map[BBA, BalanceValue]

  def currentBalance(balanceAccount: BBA): Option[BalanceValue]

  def add(b: SignedBlock): Unit

  def switchTo(b: SignedBlock): Unit

  def lastBlock: SignedBlock
}

trait StateValidator[BC <: BlockChain, Block <: BC#B, Transaction <: BC#T] {
  def isLeadToValidState(b: Block): Boolean
  def isLeadToValidState(t: Transaction): Boolean
  def isLeadToValidState(t: Seq[Transaction]): Boolean
}

sealed trait ProtocolRequest

trait IncomingNetworkLayer[BC <: BlockChain] {
  def incomingRequests: Observable[ProtocolRequest]

  def incomingTx: Observable[BC#T]

  def incomingBlocks: Observable[BC#B]
}

trait OutgoingNetworkLayer[BC <: BlockChain] {
  def outgoingRequests: Observable[ProtocolRequest]

  def outgoingTx: Observable[BC#T]

  def outgoingBlocks: Observable[BC#B]
}

trait NetworkLayer[BC <: BlockChain] extends IncomingNetworkLayer[BC] with OutgoingNetworkLayer[BC]
