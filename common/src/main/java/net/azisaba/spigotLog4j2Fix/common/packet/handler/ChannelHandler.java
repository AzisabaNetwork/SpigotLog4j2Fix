package net.azisaba.spigotLog4j2Fix.common.packet.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.azisaba.spigotLog4j2Fix.common.SpigotLog4j2Fix;
import net.azisaba.spigotLog4j2Fix.common.packet.PacketData;
import net.azisaba.spigotLog4j2Fix.common.util.LoggedPrintStream;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChannelHandler extends ChannelOutboundHandlerAdapter {
    private final Player player;

    public ChannelHandler(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg.getClass().getSimpleName().contains("Packet")
                || msg.getClass().getSimpleName().contains("Clientbound")
                || msg.getClass().getSimpleName().contains("Serverbound")) {
            try {
                for (Object p : SpigotLog4j2Fix.getVersionDependant().processOutgoingPacket(new PacketData(player, msg))) {
                    super.write(ctx, p, promise);
                }
            } catch (Throwable e) {
                if (e instanceof VirtualMachineError) {
                    throw e;
                }
                SpigotLog4j2Fix.getLogger().severe("Exception while processing packet from " + player.getName());
                e.printStackTrace(new LoggedPrintStream(SpigotLog4j2Fix.getLogger(), System.err));
                throw e;
            }
            return;
        }
        super.write(ctx, msg, promise);
    }
}
