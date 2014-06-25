package crazypants.enderio.machine.hypercube;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketChannelList implements IMessage, IMessageHandler<PacketChannelList, IMessage>  {

  private boolean isPrivate;
  private List<Channel> channels;
  private UUID userId;

  public PacketChannelList() {
  }

  public PacketChannelList(EntityPlayer player, boolean isPrivate) {
    this(player.getGameProfile().getId(), isPrivate);
  }

  public PacketChannelList(UUID uuid, boolean isPrivate) {
    this.userId = uuid;
    this.isPrivate = isPrivate;
    if(isPrivate && uuid == null) {
      throw new RuntimeException("Null user ID.");
    }

    List<Channel> res;
    if(isPrivate) {
      res = HyperCubeRegister.instance.getChannelsForUser(uuid);
    } else {
      res = HyperCubeRegister.instance.getPublicChannels();
    }

    if(res != null && !res.isEmpty()) {
      channels = new ArrayList<Channel>(res);
    } else {
      channels = new ArrayList<Channel>();
    }
  }

  @Override
  public void toBytes(ByteBuf buffer) {
    buffer.writeBoolean(isPrivate);
    if(isPrivate) {
      ByteBufUtils.writeUTF8String(buffer, userId.toString());
    }
    buffer.writeInt(channels.size());
    for (Channel channel : channels) {
      ByteBufUtils.writeUTF8String(buffer, channel.name);
    }

  }

  @Override
  public void fromBytes(ByteBuf buffer) {
    isPrivate = buffer.readBoolean();
    if(isPrivate) {
      userId = UUID.fromString(ByteBufUtils.readUTF8String(buffer));
    } else {
      userId = null;
    }
    int numChannels = buffer.readInt();
    channels = new ArrayList<Channel>();
    for (int i = 0; i < numChannels; i++) {
      channels.add(new Channel(ByteBufUtils.readUTF8String(buffer), userId));
    }
  }

  @Override
  public IMessage onMessage(PacketChannelList message, MessageContext ctx) {
    if(message.isPrivate) {
      ClientChannelRegister.instance.setPrivateChannels(message.channels);
    } else {
      ClientChannelRegister.instance.setPublicChannels(message.channels);
    }
    return null;
  }

  

}
