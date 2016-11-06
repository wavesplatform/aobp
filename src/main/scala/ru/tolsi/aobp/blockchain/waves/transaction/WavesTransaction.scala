package ru.tolsi.aobp.blockchain.waves.transaction

import ru.tolsi.aobp.blockchain.base.BlockChainTransaction
import ru.tolsi.aobp.blockchain.waves._

abstract class WavesTransaction extends BlockChainTransaction[WavesBlockChain] {
  def typeId: TransactionType.Value

  val recipient: Address

  def timestamp: Long

  def amount: BigDecimal

  def quantity: Long

  def currency: WavesСurrency

  def fee: Long

  def feeCurrency: WavesСurrency

  // todo is it good idea? external implicit balance changes calculator
  //  def balanceChanges(): Seq[(WavesAccount, Long)]
}