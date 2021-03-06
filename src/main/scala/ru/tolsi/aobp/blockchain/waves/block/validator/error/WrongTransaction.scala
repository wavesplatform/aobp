package ru.tolsi.aobp.blockchain.waves.block.validator.error

import ru.tolsi.aobp.blockchain.waves.BlockValidationError
import ru.tolsi.aobp.blockchain.waves.block.WavesBlock

class WrongTransaction(message: => String) extends BlockValidationError[WavesBlock](message)
