package net.minecraft.server.v1_10_R1;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.event.block.BlockBreakEvent;

public class PacketPlayInCustomPayload implements Packet<PacketListenerPlayIn> {
    private String a;
    private PacketDataSerializer b;
    
    @Override
    public void a(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        this.a = paramPacketDataSerializer.e(20);
        int i = paramPacketDataSerializer.readableBytes();
        if ((i < 0) || (i > 32767)) {
            throw new IOException("Payload may not be larger than 32767 bytes");
        }
        this.b = new PacketDataSerializer(paramPacketDataSerializer.readBytes(i));
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
            System.out.println("Id " + id);
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
                            if (!player.dh() && bukkitPlayer.hasPermission("commandblock.use")) {
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
