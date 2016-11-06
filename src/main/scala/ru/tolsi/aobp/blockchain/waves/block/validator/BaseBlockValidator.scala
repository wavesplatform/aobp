package ru.tolsi.aobp.blockchain.waves.block.validator

import ru.tolsi.aobp.blockchain.base.{AbstractBlockValidator, BlockValidationError}
import ru.tolsi.aobp.blockchain.waves.WavesBlockChain
import ru.tolsi.aobp.blockchain.waves.block.BaseBlock

object BaseBlockValidator extends AbstractBlockValidator[WavesBlockChain, BaseBlock] {
  override def validate(tx: BaseBlock)(implicit wbc: WavesBlockChain): Either[Seq[BlockValidationError[WavesBlockChain, BaseBlock]], WavesBlockChain#B] = ???
}