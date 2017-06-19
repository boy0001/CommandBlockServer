package net.minecraft.server.v1_11_R1;

import java.io.IOException;

public class PacketPlayOutEntityStatus implements Packet<PacketListenerPlayOut> {
    private int a;
    private byte b;
    
    public PacketPlayOutEntityStatus() {}
    
    public PacketPlayOutEntityStatus(Entity paramEntity, byte paramByte) {
        this.a = paramEntity.getId();
        if (paramByte >= 24 && paramByte <= 28 && paramEntity instanceof EntityPlayer) {
            paramByte = 28;
        }
        this.b = paramByte;
    }
    
    @Override
    public void a(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        this.a = paramPacketDataSerializer.readInt();
        this.b = paramPacketDataSerializer.readByte();
    }
    
    @Override
    public void b(PacketDataSerializer paramPacketDataSerializer) throws IOException {
        paramPacketDataSerializer.writeInt(this.a);
        paramPacketDataSerializer.writeByte(this.b);
    }
    
    @Override
    public void a(PacketListenerPlayOut paramPacketListenerPlayOut) {
        paramPacketListenerPlayOut.a(this);
    }
    
    public static Class<?> inject() {
        return PacketPlayOutEntityStatus.class;
    }
}
