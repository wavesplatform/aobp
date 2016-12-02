package ru.tolsi.aobp.blockchain.waves.network.transport.messages

import ru.tolsi.aobp.blockchain.waves.network.transport.NetworkMessage

case class Score(score: BigInt) extends NetworkMessage {
  override val contentId: Byte = 0x18
}
