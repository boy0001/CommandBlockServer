package net.minecraft.server.v1_8_R3;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.block.BlockBreakEvent;

public class PacketPlayInCustomPayload implements Packet<PacketListenerPlayIn> {
    private String a;
    private PacketDataSerializer b;
    
    @Override
    public void a(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        this.a = paramPacketDataSerializer.c(20);
        int var2 = paramPacketDataSerializer.readableBytes();
        if (var2 >= 0 && var2 <= 32767) {
            this.b = new PacketDataSerializer(paramPacketDataSerializer.readBytes(var2));
        } else {
            throw new IOException("Payload may not be larger than 32767 bytes");
        }
    }
    
    @Override
    public void b(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        paramPacketDataSerializer.a(this.a);
        paramPacketDataSerializer.writeBytes(this.b);
    }
    
    @Override
    public void a(PacketListenerPlayIn paramPacketListenerPlayIn) {
        if (paramPacketListenerPlayIn instanceof PlayerConnection) {
            PlayerConnection connection = (PlayerConnection) paramPacketListenerPlayIn;
            String id = this.a();
            switch (id) {
                case "MC|AdvCmd":
                    if (this.b().readByte() != 0) {
                        this.b().resetReaderIndex();
                        break;
                    }
                case "MC|AutoCmd":
                    PacketDataSerializer serializer = this.b();
                    if (id.equals("MC|AutoCmd") || serializer.readByte() == 0) {
                        EntityPlayer player = connection.player;
                        CraftPlayer bukkitPlayer = player.getBukkitEntity();
                        int x = serializer.readInt();
                        int y = serializer.readInt();
                        int z = serializer.readInt();
                        BlockBreakEvent event = new BlockBreakEvent(bukkitPlayer.getWorld().getBlockAt(x, y, z), bukkitPlayer);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            if (!player.a(2, "") && bukkitPlayer.hasPermission("commandblock.use")) {
                                try {
                                    bukkitPlayer.setOp(true);
                                    this.b().resetReaderIndex();
                                    paramPacketListenerPlayIn.a(this);
                                    if (this.b != null) {
                                        this.b.release();
                                    }
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                } finally {
                                    bukkitPlayer.setOp(false);
                                }
                                return;
                            }
                        }
                    }
                    this.b().resetReaderIndex();
            }
        }
        paramPacketListenerPlayIn.a(this);
        if (this.b != null) {
            this.b.release();
        }
    }
    
    public String a() {
        return this.a;
    }
    
    public PacketDataSerializer b() {
        return this.b;
    }
    
    public static Class<?> inject() {
        return PacketPlayInCustomPayload.class;
    }
}
