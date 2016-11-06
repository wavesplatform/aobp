package ru.tolsi.aobp.blockchain.waves.transaction

import ru.tolsi.aobp.blockchain.waves.{Waves, WavesBlockChain, WavesСurrency}

case class GenesisTransaction(recipient: WavesBlockChain#AD, timestamp: Long, quantity: Long) extends WavesTransaction {
  override val typeId = TransactionType.GenesisTransaction

  override val fee: Long = 0

  override val currency: WavesСurrency = Waves

  override val feeCurrency: WavesСurrency = Waves

  override def amount: BigDecimal = BigDecimal(quantity)
}