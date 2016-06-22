package net.minecraft.server.v1_9_R2;

import javax.annotation.Nullable;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_9_R2.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;


public class PlayerInteractManager {
    public World world;
    public EntityPlayer player;
    public WorldSettings.EnumGamemode gamemode;
    public boolean d;
    public int lastDigTick;
    public BlockPosition f;
    public int currentTick;
    public boolean h;
    public BlockPosition i;
    public int j;
    public int k;
    
    public PlayerInteractManager(World world) {
        this.gamemode = WorldSettings.EnumGamemode.NOT_SET;
        this.f = BlockPosition.ZERO;
        this.i = BlockPosition.ZERO;
        this.k = -1;
        this.world = world;
    }
    
    public void setGameMode(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        this.gamemode = worldsettings_enumgamemode;
        worldsettings_enumgamemode.a(this.player.abilities);
        this.player.updateAbilities();
        this.player.server.getPlayerList().sendAll(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, new EntityPlayer[] { this.player }), this.player);
        this.world.everyoneSleeping();
    }
    
    public WorldSettings.EnumGamemode getGameMode() {
        return this.gamemode;
    }
    
    public boolean c() {
        return this.gamemode.e();
    }
    
    public boolean isCreative() {
        return this.gamemode.isCreative();
    }
    
    public void b(WorldSettings.EnumGamemode worldsettings_enumgamemode) {
        if (this.gamemode == WorldSettings.EnumGamemode.NOT_SET) {
            this.gamemode = worldsettings_enumgamemode;
        }
        setGameMode(this.gamemode);
    }
    
    public void a() {
        this.currentTick = MinecraftServer.currentTick;
        if (this.h) {
            int j = this.currentTick - this.j;
            IBlockData iblockdata = this.world.getType(this.i);
            iblockdata.getBlock();
            if (iblockdata.getMaterial() == Material.AIR) {
                this.h = false;
            } else {
                float f = iblockdata.a(this.player, this.player.world, this.i) * (j + 1);
                int i = (int) (f * 10.0F);
                if (i != this.k) {
                    this.world.c(this.player.getId(), this.i, i);
                    this.k = i;
                }
                if (f >= 1.0F) {
                    this.h = false;
                    breakBlock(this.i);
                }
            }
        } else if (this.d) {
            IBlockData iblockdata1 = this.world.getType(this.f);
            iblockdata1.getBlock();
            if (iblockdata1.getMaterial() == Material.AIR) {
                this.world.c(this.player.getId(), this.f, -1);
                this.k = -1;
                this.d = false;
            } else {
                int k = this.currentTick - this.lastDigTick;
                
                float f = iblockdata1.a(this.player, this.player.world, this.i) * (k + 1);
                int i = (int) (f * 10.0F);
                if (i != this.k) {
                    this.world.c(this.player.getId(), this.f, i);
                    this.k = i;
                }
            }
        }
    }
    
    public void a(BlockPosition blockposition, EnumDirection enumdirection) {
        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.inventory.getItemInHand(),
        EnumHand.MAIN_HAND);
        if (event.isCancelled()) {
            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
            
            TileEntity tileentity = this.world.getTileEntity(blockposition);
            if (tileentity != null) {
                this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
            }
            return;
        }
        if (isCreative()) {
            if (!this.world.douseFire(null, blockposition, enumdirection)) {
                breakBlock(blockposition);
            }
        } else {
            IBlockData iblockdata = this.world.getType(blockposition);
            Block block = iblockdata.getBlock();
            if (this.gamemode.c()) {
                if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                    return;
                }
                if (!this.player.cV()) {
                    ItemStack itemstack = this.player.getItemInMainHand();
                    if (itemstack == null) {
                        return;
                    }
                    if (!itemstack.a(block)) {
                        return;
                    }
                }
            }
            this.lastDigTick = this.currentTick;
            float f = 1.0F;
            if (event.useInteractedBlock() == Event.Result.DENY) {
                IBlockData data = this.world.getType(blockposition);
                if (block == Blocks.WOODEN_DOOR) {
                    boolean bottom = data.get(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, bottom ? blockposition.up() : blockposition.down()));
                } else if (block == Blocks.TRAPDOOR) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                }
            } else if (iblockdata.getMaterial() != Material.AIR) {
                block.attack(this.world, blockposition, this.player);
                f = iblockdata.a(this.player, this.player.world, blockposition);
                
                this.world.douseFire(null, blockposition, enumdirection);
            }
            if (event.useItemInHand() == Event.Result.DENY) {
                if (f > 1.0F) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                }
                return;
            }
            BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.player.inventory.getItemInHand(),
            f >= 1.0F);
            if (blockEvent.isCancelled()) {
                this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                return;
            }
            if (blockEvent.getInstaBreak()) {
                f = 2.0F;
            }
            if ((iblockdata.getMaterial() != Material.AIR) && (f >= 1.0F)) {
                breakBlock(blockposition);
            } else {
                this.d = true;
                this.f = blockposition;
                int i = (int) (f * 10.0F);
                
                this.world.c(this.player.getId(), blockposition, i);
                this.k = i;
            }
        }
    }
    
    public void a(BlockPosition blockposition) {
        if (blockposition.equals(this.f)) {
            this.currentTick = MinecraftServer.currentTick;
            int i = this.currentTick - this.lastDigTick;
            IBlockData iblockdata = this.world.getType(blockposition);
            if (iblockdata.getMaterial() != Material.AIR) {
                float f = iblockdata.a(this.player, this.player.world, blockposition) * (i + 1);
                if (f >= 0.7F) {
                    this.d = false;
                    this.world.c(this.player.getId(), blockposition, -1);
                    breakBlock(blockposition);
                } else if (!this.h) {
                    this.d = false;
                    this.h = true;
                    this.i = blockposition;
                    this.j = this.lastDigTick;
                }
            }
        } else {
            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
        }
    }
    
    public void e() {
        this.d = false;
        this.world.c(this.player.getId(), this.f, -1);
    }
    
    public boolean c(BlockPosition blockposition) {
        IBlockData iblockdata = this.world.getType(blockposition);
        
        iblockdata.getBlock().a(this.world, blockposition, iblockdata, this.player);
        boolean flag = this.world.setAir(blockposition);
        if (flag) {
            iblockdata.getBlock().postBreak(this.world, blockposition, iblockdata);
        }
        return flag;
    }
    
    public boolean breakBlock(BlockPosition blockposition) {
        BlockBreakEvent event = null;
        if ((this.player instanceof EntityPlayer)) {
            org.bukkit.block.Block block = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            
            boolean isSwordNoBreak = (this.gamemode.isCreative()) && (this.player.getItemInMainHand() != null) && ((this.player.getItemInMainHand().getItem() instanceof ItemSword));
            if ((this.world.getTileEntity(blockposition) == null) && (!isSwordNoBreak)) {
                PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(this.world, blockposition);
                packet.block = Blocks.AIR.getBlockData();
                this.player.playerConnection.sendPacket(packet);
            }
            event = new BlockBreakEvent(block, this.player.getBukkitEntity());
            
            event.setCancelled(isSwordNoBreak);
            
            IBlockData nmsData = this.world.getType(blockposition);
            Block nmsBlock = nmsData.getBlock();
            
            ItemStack itemstack = this.player.getEquipment(EnumItemSlot.MAINHAND);
            if ((nmsBlock != null) && (!event.isCancelled()) && (!isCreative()) && (this.player.hasBlock(nmsBlock.getBlockData()))) {
                if ((!nmsBlock.p()) || (EnchantmentManager.getEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) <= 0)) {
                    block.getData();
                    int bonusLevel = EnchantmentManager.getEnchantmentLevel(Enchantments.LOOT_BONUS_BLOCKS, itemstack);
                    
                    event.setExpToDrop(nmsBlock.getExpDrop(this.world, nmsData, bonusLevel));
                }
            }
            this.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                if (isSwordNoBreak) {
                    return false;
                }
                this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
                
                TileEntity tileentity = this.world.getTileEntity(blockposition);
                if (tileentity != null) {
                    this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
                }
                return false;
            }
        }
        IBlockData iblockdata = this.world.getType(blockposition);
        if (iblockdata.getBlock() == Blocks.AIR) {
            return false;
        }
        TileEntity tileentity = this.world.getTileEntity(blockposition);
        if ((iblockdata.getBlock() == Blocks.SKULL) && (!isCreative())) {
            iblockdata.getBlock().dropNaturally(this.world, blockposition, iblockdata, 1.0F, 0);
            return c(blockposition);
        }
        //        if (((iblockdata.getBlock() instanceof BlockCommand)) && (!this.player.a(2, ""))) {
        //            this.world.notify(blockposition, iblockdata, iblockdata, 3);
//            return false;
//        }
        if (this.gamemode.c()) {
            if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                return false;
            }
            if (!this.player.cV()) {
                ItemStack itemstack = this.player.getItemInMainHand();
                if (itemstack == null) {
                    return false;
                }
                if (!itemstack.a(iblockdata.getBlock())) {
                    return false;
                }
            }
        }
        this.world.a(this.player, 2001, blockposition, Block.getCombinedId(iblockdata));
        boolean flag = c(blockposition);
        if (isCreative()) {
            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(this.world, blockposition));
        } else {
            ItemStack itemstack1 = this.player.getItemInMainHand();
            ItemStack itemstack2 = itemstack1 == null ? null : itemstack1.cloneItemStack();
            boolean flag1 = this.player.hasBlock(iblockdata);
            if (itemstack1 != null) {
                itemstack1.a(this.world, iblockdata, blockposition, this.player);
                if (itemstack1.count == 0) {
                    this.player.a(EnumHand.MAIN_HAND, null);
                }
            }
            if ((flag) && (flag1)) {
                iblockdata.getBlock().a(this.world, this.player, blockposition, iblockdata, tileentity, itemstack2);
            }
        }
        if ((flag) && (event != null)) {
            iblockdata.getBlock().dropExperience(this.world, blockposition, event.getExpToDrop());
        }
        return flag;
    }
    
    public EnumInteractionResult a(EntityHuman entityhuman, World world, ItemStack itemstack, EnumHand enumhand) {
        if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
            return EnumInteractionResult.PASS;
        }
        if (entityhuman.db().a(itemstack.getItem())) {
            return EnumInteractionResult.PASS;
        }
        int i = itemstack.count;
        int j = itemstack.getData();
        InteractionResultWrapper interactionresultwrapper = itemstack.a(world, entityhuman, enumhand);
        ItemStack itemstack1 = (ItemStack) interactionresultwrapper.b();
        if ((itemstack1 == itemstack) && (itemstack1.count == i) && (itemstack1.l() <= 0) && (itemstack1.getData() == j)) {
            return interactionresultwrapper.a();
        }
        entityhuman.a(enumhand, itemstack1);
        if (isCreative()) {
            itemstack1.count = i;
            if (itemstack1.e()) {
                itemstack1.setData(j);
            }
        }
        if (itemstack1.count == 0) {
            entityhuman.a(enumhand, null);
        }
        if (!entityhuman.ct()) {
            ((EntityPlayer) entityhuman).updateInventory(entityhuman.defaultContainer);
        }
        return interactionresultwrapper.a();
    }
    
    public boolean interactResult = false;
    public boolean firedInteract = false;
    
    // Place block ?
    public EnumInteractionResult a(EntityHuman entityhuman, World world, @Nullable ItemStack itemstack, EnumHand enumhand, BlockPosition blockposition, EnumDirection enumdirection, float f, float f1,
    float f2) {
        IBlockData blockdata = world.getType(blockposition);
        EnumInteractionResult result = EnumInteractionResult.FAIL;
        if (blockdata.getBlock() != Blocks.AIR) {
            boolean cancelledBlock = false;
            if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                TileEntity te = world.getTileEntity(blockposition);
                cancelledBlock = (!(te instanceof ITileInventory)) && (!(te instanceof IInventory));
            }
            { // Added
                CraftHumanEntity human = entityhuman.getBukkitEntity();
                if (!human.isOp() && human instanceof Player) {
                    if (!((Player) human).isSneaking() && blockdata.getBlock() == Blocks.COMMAND_BLOCK) {
                        TileEntity te = world.getTileEntity(blockposition);
                        if (te instanceof TileEntityCommand) {
                            human.sendMessage("Use /setcommand <command>");
                            return EnumInteractionResult.SUCCESS;
                        }
                    }
                }
            }
            //            if ((!entityhuman.getBukkitEntity().isOp()) && (itemstack != null) && ((Block.asBlock(itemstack.getItem()) instanceof BlockCommand))) {
            //                cancelledBlock = true;
            //            }
            PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, blockposition, enumdirection, itemstack, cancelledBlock, enumhand);
            this.firedInteract = true;
            this.interactResult = (event.useItemInHand() == Event.Result.DENY);
            if (event.useInteractedBlock() == Event.Result.DENY) {
                if ((blockdata.getBlock() instanceof BlockDoor)) {
                    boolean bottom = blockdata.get(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
                    ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutBlockChange(world, bottom ? blockposition.up() : blockposition.down()));
                }
                result = event.useItemInHand() != Event.Result.ALLOW ? EnumInteractionResult.SUCCESS : EnumInteractionResult.PASS;
            } else {
                if (this.gamemode == WorldSettings.EnumGamemode.SPECTATOR) {
                    TileEntity tileentity = world.getTileEntity(blockposition);
                    if ((tileentity instanceof ITileInventory)) {
                        Block block = world.getType(blockposition).getBlock();
                        ITileInventory itileinventory = (ITileInventory) tileentity;
                        if (((itileinventory instanceof TileEntityChest)) && ((block instanceof BlockChest))) {
                            itileinventory = ((BlockChest) block).c(world, blockposition);
                        }
                        if (itileinventory != null) {
                            entityhuman.openContainer(itileinventory);
                            return EnumInteractionResult.SUCCESS;
                        }
                    } else if ((tileentity instanceof IInventory)) {
                        entityhuman.openContainer((IInventory) tileentity);
                        return EnumInteractionResult.SUCCESS;
                    }
                    return EnumInteractionResult.PASS;
                }
                if ((!entityhuman.isSneaking()) || ((entityhuman.getItemInMainHand() == null) && (entityhuman.getItemInOffHand() == null))) {
                    result = blockdata.getBlock().interact(world, blockposition, blockdata, entityhuman, enumhand, itemstack, enumdirection, f, f1, f2) ? EnumInteractionResult.SUCCESS
                    : EnumInteractionResult.PASS;
                }
            }
            if ((itemstack != null) && (result != EnumInteractionResult.SUCCESS) && (!this.interactResult)) {
                int j1 = itemstack.getData();
                int k1 = itemstack.count;
                
                result = itemstack.placeItem(entityhuman, world, blockposition, enumhand, enumdirection, f, f1, f2);
                if (isCreative()) {
                    itemstack.setData(j1);
                    itemstack.count = k1;
                }
            }
        }
        return result;
    }
    
    public void a(WorldServer worldserver) {
        this.world = worldserver;
    }
    
    public static Class<?> inject() {
        return PlayerInteractManager.class;
    }
}
