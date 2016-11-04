package ru.tolsi.aobp.blockchain.waves

import ru.tolsi.aobp.blockchain.base._
import ru.tolsi.aobp.blockchain.waves.crypto.ScorexHashChain
import scorex.crypto.hash.Blake256

private[waves] abstract class WavesBlockChain extends BlockChain
  with WavesTransactions
  with WavesTransactionsSigners
  with WavesTransactionsValidators
  with WavesAccounts
  with WavesBlocks
  with WavesBlocksValidators
  with WavesBlocksSigns
  with WavesBlocksSigners
  with WavesConfiguration {
  final type T = WavesTransaction
  final type ST[TX <: T] = SignedTransaction[TX]
  final type B = WavesBlock
  final type SB[BL <: B] = SignedBlock[BL]
  //BlockChainSignedBlock[BL, Array[Byte], ArraySignature64]
  final type AС = Account
  final type AD = Address
  type TXV = AbstractSignedTransactionValidator[T, ST[T]]
  type TVP = WavesTransactionValidationParameters
  type SBV = AbstractSignedBlockValidator[B, SB[B]]

  final val secureHash = ScorexHashChain
  final val fastHash = Blake256

  def state: StateStorage[this.type]

  def blocksStorage: BlockStorage[this.type]
}
