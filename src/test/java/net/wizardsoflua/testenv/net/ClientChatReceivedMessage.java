package net.wizardsoflua.testenv.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.wizardsoflua.testenv.event.TestPlayerReceivedChatEvent;

public class ClientChatReceivedMessage extends AbstractMessage implements ServerHandledMessage {
  private String text;

  public ClientChatReceivedMessage() {}

  public ClientChatReceivedMessage(String text) {
    this.text = text;
  }

  @Override
  protected void read(PacketBuffer buffer) {
    text = readString(buffer);
  }

  @Override
  protected void write(PacketBuffer buffer) {
    writeString(buffer, text);
  }

  @Override
  public void handleServerSide(EntityPlayer player) {
    EntityPlayerMP mpPlayer = (EntityPlayerMP) player;
    MinecraftForge.EVENT_BUS.post(new TestPlayerReceivedChatEvent(mpPlayer, text));
  }

}
