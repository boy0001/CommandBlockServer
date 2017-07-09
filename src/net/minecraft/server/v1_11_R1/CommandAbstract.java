package net.minecraft.server.v1_11_R1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.craftbukkit.v1_11_R1.command.CraftBlockCommandSender;

import com.boydti.cbs.CommandProcessor;
import com.boydti.cbs.Main;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.gson.JsonParseException;

public abstract class CommandAbstract implements ICommand {
    
    /*
     * Custom code
     */

    private static boolean enabled = false;
    
    private static World lastWorld;
    private static BlockPosition lastPos;
    
    public CommandAbstract() {
        lastWorld = null;
        lastPos = null;
    }
    
    /**
     * Forwards to setOther, setCommandBlock or setPlayer
     * @param listener
     * @param command
     * @param clazz
     * @return
     */
    public static boolean setListener(final ICommandListener listener, final Class<?> clazz, String command) {
        lastWorld = listener.getWorld();
        lastPos = listener.getChunkCoordinates();
        if (!(listener instanceof CommandBlockListenerAbstract)) {
            if (listener instanceof EntityPlayer) {
                return enabled = CommandProcessor.manager.setPlayer(((EntityPlayer) listener).getBukkitEntity(), clazz, command);
            }
            if (listener instanceof DedicatedServer) {
                return enabled = CommandProcessor.manager.setConsole(clazz, command);
            }
            final Entity entity = listener.f();
            if (entity != null) {
                return enabled = CommandProcessor.manager.setLocation(entity.getBukkitEntity().getLocation(), clazz, command);
            }
            Main.debug("Unknown command sender: " + listener.getName() + " | " + listener.getClass());
            return enabled = CommandProcessor.manager.setOther(listener);
        }
        final CommandBlockListenerAbstract cmd = (CommandBlockListenerAbstract) listener;
        command = cmd.getCommand();
        final CraftBlockCommandSender sender = new CraftBlockCommandSender(listener);
        final org.bukkit.block.Block block = sender.getBlock();
        return enabled = CommandProcessor.manager.setCommandBlock(block, clazz, command);
    }
    
    /**
     * Forwards to isLocationAllowed
     * @param entity
     * @return
     */
    public static Entity isEntityAllowed(final Entity entity) {
        if (!enabled) {
            return null;
        }
        final BlockPosition loc = entity.getChunkCoordinates();
        if (CommandProcessor.manager.isLocationAllowed(entity.getWorld().worldData.getName(), loc.getX(), loc.getY(), loc.getZ())) {
            return entity;
        }
        return null;
    }
    
    /**
     * Forwards to isLocationAllowed
     * @param entity
     * @return
     */
    public static EntityPlayer isPlayerAllowed(final EntityPlayer entity) {
        return (EntityPlayer) isEntityAllowed(entity);
    }
    
    /**
     * Forwards to isLocationAllowed
     * @param world
     * @param loc
     * @return
     */
    public static BlockPosition isPositionAllowed(final World world, final BlockPosition loc) {
        if (!enabled) {
            return null;
        }
        if (CommandProcessor.manager.isLocationAllowed(world.worldData.getName(), loc.getX(), loc.getY(), loc.getZ())) {
            return loc;
        }
        return null;
    }
    
    /**
     * Forwards to isLocationAllowed
     * @param world
     * @param loc
     * @return
     */
    public static BlockPosition isPositionAllowed(final BlockPosition loc) {
        if (!enabled) {
            return null;
        }
        if (CommandProcessor.manager.isLocationAllowed(lastWorld.worldData.getName(), loc.getX(), loc.getY(), loc.getZ())) {
            return loc;
        }
        return null;
    }
    
    /*
     * Modified internal code
     */

    private static ICommandDispatcher a;
    
    protected static ExceptionInvalidSyntax a(JsonParseException paramJsonParseException) {
        Throwable localThrowable = ExceptionUtils.getRootCause(paramJsonParseException);
        String str = "";
        if (localThrowable != null) {
            str = localThrowable.getMessage();
            if (str.contains("setLenient")) {
                str = str.substring(str.indexOf("to accept ") + 10);
            }
        }
        return new ExceptionInvalidSyntax("commands.tellraw.jsonException", new Object[] { str });
    }
    
    protected static NBTTagCompound a(Entity paramEntity) {
        NBTTagCompound localNBTTagCompound = new NBTTagCompound();
        paramEntity.e(localNBTTagCompound);
        if ((paramEntity instanceof EntityHuman)) {
            ItemStack localItemStack = ((EntityHuman) paramEntity).inventory.getItemInHand();
            if ((localItemStack != null) && (localItemStack.getItem() != null)) {
                localNBTTagCompound.set("SelectedItem", localItemStack.save(new NBTTagCompound()));
            }
        }
        return localNBTTagCompound;
    }
    
    public int a() {
        return 4;
    }
    
    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean canUse(MinecraftServer paramMinecraftServer, ICommandListener listener) {
        enabled = setListener(listener, getClass(), getCommand()) && listener.a(a(), getCommand());
        return enabled;
    }
    
    @Override
    public List<String> tabComplete(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String[] paramArrayOfString, BlockPosition paramBlockPosition) {
        return Collections.emptyList();
    }
    
    public static int a(String paramString) throws ExceptionInvalidNumber {
        try {
            return Integer.parseInt(paramString);
        } catch (NumberFormatException localNumberFormatException) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
        }
    }
    
    public static int a(String paramString, int paramInt) throws ExceptionInvalidNumber {
        return a(paramString, paramInt, 2147483647);
    }
    
    public static int a(String paramString, int paramInt1, int paramInt2) throws ExceptionInvalidNumber {
        int i = a(paramString);
        if (i < paramInt1) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooSmall", new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt1) });
        }
        if (i > paramInt2) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooBig", new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt2) });
        }
        return i;
    }
    
    public static long b(String paramString) throws ExceptionInvalidNumber {
        try {
            return Long.parseLong(paramString);
        } catch (NumberFormatException localNumberFormatException) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
        }
    }
    
    public static long a(String paramString, long paramLong1, long paramLong2) throws ExceptionInvalidNumber {
        long l = b(paramString);
        if (l < paramLong1) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooSmall", new Object[] { Long.valueOf(l), Long.valueOf(paramLong1) });
        }
        if (l > paramLong2) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooBig", new Object[] { Long.valueOf(l), Long.valueOf(paramLong2) });
        }
        return l;
    }
    
    public static BlockPosition a(ICommandListener paramICommandListener, String[] paramArrayOfString, int paramInt, boolean paramBoolean) throws ExceptionInvalidNumber {
        BlockPosition localBlockPosition = paramICommandListener.getChunkCoordinates();
        BlockPosition toReturn = new BlockPosition(b(localBlockPosition.getX(), paramArrayOfString[paramInt], -30000000, 30000000, paramBoolean), b(localBlockPosition.getY(),
        paramArrayOfString[(paramInt + 1)], 0,
        256, false), b(localBlockPosition.getZ(), paramArrayOfString[(paramInt + 2)], -30000000, 30000000, paramBoolean));
        return isPositionAllowed(toReturn);
    }
    
    public static double c(String paramString) throws ExceptionInvalidNumber {
        try {
            double d = Double.parseDouble(paramString);
            if (!Doubles.isFinite(d)) {
                throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
            }
            return d;
        } catch (NumberFormatException localNumberFormatException) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
        }
    }
    
    public static double a(String paramString, double paramDouble) throws ExceptionInvalidNumber {
        return a(paramString, paramDouble, 1.7976931348623157E+308D);
    }
    
    public static double a(String paramString, double paramDouble1, double paramDouble2) throws ExceptionInvalidNumber {
        double d = c(paramString);
        if (d < paramDouble1) {
            throw new ExceptionInvalidNumber("commands.generic.double.tooSmall", new Object[] { Double.valueOf(d), Double.valueOf(paramDouble1) });
        }
        if (d > paramDouble2) {
            throw new ExceptionInvalidNumber("commands.generic.double.tooBig", new Object[] { Double.valueOf(d), Double.valueOf(paramDouble2) });
        }
        return d;
    }
    
    public static boolean d(String paramString) throws CommandException {
        if ((paramString.equals("true")) || (paramString.equals("1"))) {
            return true;
        }
        if ((paramString.equals("false")) || (paramString.equals("0"))) {
            return false;
        }
        throw new CommandException("commands.generic.boolean.invalid", new Object[] { paramString });
    }
    
    public static EntityPlayer a(ICommandListener paramICommandListener) throws ExceptionPlayerNotFound {
        if ((paramICommandListener instanceof EntityPlayer)) {
            return isPlayerAllowed((EntityPlayer) paramICommandListener);
        }
        throw new ExceptionPlayerNotFound("You must specify which player you wish to perform this action on.", new Object[0]);
    }
    
    public static EntityPlayer a(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String paramString) throws CommandException {
        EntityPlayer localEntityPlayer = PlayerSelector.getPlayer(paramICommandListener, paramString);
        if (localEntityPlayer == null) {
            try {
                localEntityPlayer = paramMinecraftServer.getPlayerList().a(UUID.fromString(paramString));
            } catch (IllegalArgumentException localIllegalArgumentException) {}
        }
        if (localEntityPlayer == null) {
            localEntityPlayer = paramMinecraftServer.getPlayerList().getPlayer(paramString);
        }
        if (localEntityPlayer == null) {
            throw new ExceptionPlayerNotFound("commands.generic.player.notFound", new Object[] { paramString });
        }
        return isPlayerAllowed(localEntityPlayer);
    }
    
    public static Entity b(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String paramString) throws CommandException {
        return isEntityAllowed(a(paramMinecraftServer, paramICommandListener, paramString, Entity.class));
    }
    
    public static <T extends Entity> T a(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String paramString, Class<? extends T> paramClass)
    throws CommandException {
        Object localObject = PlayerSelector.getEntity(paramICommandListener, paramString, paramClass);
        if (localObject == null) {
            localObject = paramMinecraftServer.getPlayerList().getPlayer(paramString);
        }
        if (localObject == null) {
            try {
                UUID localUUID = UUID.fromString(paramString);
                localObject = paramMinecraftServer.a(localUUID);
                if (localObject == null) {
                    localObject = paramMinecraftServer.getPlayerList().a(localUUID);
                }
            } catch (IllegalArgumentException localIllegalArgumentException) {
                throw new ExceptionEntityNotFound("commands.generic.entity.invalidUuid", new Object[0]);
            }
        }
        if ((localObject == null) || (!paramClass.isAssignableFrom(localObject.getClass()))) {
            throw new ExceptionEntityNotFound(paramString);
        }
        return (T)localObject;
    }
    
    public static List<Entity> c(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String paramString) throws CommandException {
        List<Entity> values;
        if (PlayerSelector.isPattern(paramString)) {
            values = PlayerSelector.getPlayers(paramICommandListener, paramString, Entity.class);
        } else {
            values = Lists.newArrayList(new Entity[] { b(paramMinecraftServer, paramICommandListener, paramString) });
        }
        final Iterator<Entity> iter = values.iterator();
        while (iter.hasNext()) {
            if (isEntityAllowed(iter.next()) == null) {
                iter.remove();
            }
        }
        return values;
    }
    
    public static String d(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String paramString) throws CommandException {
        try {
            return a(paramMinecraftServer, paramICommandListener, paramString).getName();
        } catch (ExceptionPlayerNotFound localExceptionPlayerNotFound) {
            if ((paramString == null) || (paramString.startsWith("@"))) {
                throw localExceptionPlayerNotFound;
            }
        }
        return paramString;
    }
    
    public static String e(MinecraftServer paramMinecraftServer, ICommandListener paramICommandListener, String paramString) throws CommandException {
        try {
            return a(paramMinecraftServer, paramICommandListener, paramString).getName();
        } catch (ExceptionPlayerNotFound localExceptionPlayerNotFound) {
            try {
                return b(paramMinecraftServer, paramICommandListener, paramString).getUniqueID().toString();
            } catch (ExceptionEntityNotFound localExceptionEntityNotFound) {
                if ((paramString == null) || (paramString.startsWith("@"))) {
                    throw localExceptionEntityNotFound;
                }
            }
        }
        return paramString;
    }
    
    public static IChatBaseComponent a(ICommandListener paramICommandListener, String[] paramArrayOfString, int paramInt) throws CommandException {
        return b(paramICommandListener, paramArrayOfString, paramInt, false);
    }
    
    public static IChatBaseComponent b(ICommandListener paramICommandListener, String[] paramArrayOfString, int paramInt, boolean paramBoolean) throws CommandException {
        ChatComponentText localChatComponentText = new ChatComponentText("");
        for (int i = paramInt; i < paramArrayOfString.length; i++) {
            if (i > paramInt) {
                localChatComponentText.a(" ");
            }
            Object localObject = new ChatComponentText(paramArrayOfString[i]);
            if (paramBoolean) {
                IChatBaseComponent localIChatBaseComponent = PlayerSelector.getPlayerNames(paramICommandListener, paramArrayOfString[i]);
                if (localIChatBaseComponent == null) {
                    if (PlayerSelector.isPattern(paramArrayOfString[i])) {
                        throw new ExceptionPlayerNotFound("commands.generic.selector.notFound", new Object[] { paramArrayOfString[i] });
                    }
                } else {
                    localObject = localIChatBaseComponent;
                }
            }
            localChatComponentText.addSibling((IChatBaseComponent) localObject);
        }
        return localChatComponentText;
    }
    
    public static String a(String[] paramArrayOfString, int paramInt) {
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = paramInt; i < paramArrayOfString.length; i++) {
            if (i > paramInt) {
                localStringBuilder.append(" ");
            }
            String str = paramArrayOfString[i];
            
            localStringBuilder.append(str);
        }
        return localStringBuilder.toString();
    }
    
    public static CommandNumber a(double paramDouble, String paramString, boolean paramBoolean) throws ExceptionInvalidNumber {
        return a(paramDouble, paramString, -30000000, 30000000, paramBoolean);
    }
    
    public static CommandNumber a(double paramDouble, String paramString, int paramInt1, int paramInt2, boolean paramBoolean) throws ExceptionInvalidNumber {
        boolean bool1 = paramString.startsWith("~");
        if ((bool1) && (Double.isNaN(paramDouble))) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { Double.valueOf(paramDouble) });
        }
        double d = 0.0D;
        if ((!bool1) || (paramString.length() > 1)) {
            boolean bool2 = paramString.contains(".");
            if (bool1) {
                paramString = paramString.substring(1);
            }
            d += c(paramString);
            if ((!bool2) && (!bool1) && (paramBoolean)) {
                d += 0.5D;
            }
        }
        if ((paramInt1 != 0) || (paramInt2 != 0)) {
            if (d < paramInt1) {
                throw new ExceptionInvalidNumber("commands.generic.double.tooSmall", new Object[] { Double.valueOf(d), Integer.valueOf(paramInt1) });
            }
            if (d > paramInt2) {
                throw new ExceptionInvalidNumber("commands.generic.double.tooBig", new Object[] { Double.valueOf(d), Integer.valueOf(paramInt2) });
            }
        }
        return new CommandNumber(d + (bool1 ? paramDouble : 0.0D), d, bool1);
    }
    
    public static double b(double paramDouble, String paramString, boolean paramBoolean) throws ExceptionInvalidNumber {
        return b(paramDouble, paramString, -30000000, 30000000, paramBoolean);
    }
    
    public static double b(double paramDouble, String paramString, int paramInt1, int paramInt2, boolean paramBoolean) throws ExceptionInvalidNumber {
        boolean bool1 = paramString.startsWith("~");
        if ((bool1) && (Double.isNaN(paramDouble))) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { Double.valueOf(paramDouble) });
        }
        double d = bool1 ? paramDouble : 0.0D;
        if ((!bool1) || (paramString.length() > 1)) {
            boolean bool2 = paramString.contains(".");
            if (bool1) {
                paramString = paramString.substring(1);
            }
            d += c(paramString);
            if ((!bool2) && (!bool1) && (paramBoolean)) {
                d += 0.5D;
            }
        }
        if ((paramInt1 != 0) || (paramInt2 != 0)) {
            if (d < paramInt1) {
                throw new ExceptionInvalidNumber("commands.generic.double.tooSmall", new Object[] { Double.valueOf(d), Integer.valueOf(paramInt1) });
            }
            if (d > paramInt2) {
                throw new ExceptionInvalidNumber("commands.generic.double.tooBig", new Object[] { Double.valueOf(d), Integer.valueOf(paramInt2) });
            }
        }
        return d;
    }
    
    public static class CommandNumber {
        private final double a;
        private final double b;
        private final boolean c;
        
        protected CommandNumber(double paramDouble1, double paramDouble2, boolean paramBoolean) {
            this.a = paramDouble1;
            this.b = paramDouble2;
            this.c = paramBoolean;
        }
        
        public double a() {
            return this.a;
        }
        
        public double b() {
            return this.b;
        }
    
        public boolean c() {
            return this.c;
        }
    }
    
    public static Item a(ICommandListener paramICommandListener, String paramString) throws ExceptionInvalidNumber {
        MinecraftKey localMinecraftKey = new MinecraftKey(paramString);
        Item localItem = Item.REGISTRY.get(localMinecraftKey);
        if (localItem == null) {
            throw new ExceptionInvalidNumber("commands.give.item.notFound", new Object[] { localMinecraftKey });
        }
        return localItem;
    }
    
    public static Block b(ICommandListener paramICommandListener, String paramString) throws ExceptionInvalidNumber {
        MinecraftKey localMinecraftKey = new MinecraftKey(paramString);
        if (!Block.REGISTRY.d(localMinecraftKey)) {
            throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { localMinecraftKey });
        }
        Block localBlock = Block.REGISTRY.get(localMinecraftKey);
        if (localBlock == null) {
            throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { localMinecraftKey });
        }
        return localBlock;
    }
    
    public static String a(Object[] paramArrayOfObject) {
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramArrayOfObject.length; i++) {
            String str = paramArrayOfObject[i].toString();
            if (i > 0) {
                if (i == paramArrayOfObject.length - 1) {
                    localStringBuilder.append(" and ");
                } else {
                    localStringBuilder.append(", ");
                }
            }
            localStringBuilder.append(str);
        }
        return localStringBuilder.toString();
    }
    
    public static IChatBaseComponent a(List<IChatBaseComponent> paramList) {
        ChatComponentText localChatComponentText = new ChatComponentText("");
        for (int i = 0; i < paramList.size(); i++) {
            if (i > 0) {
                if (i == paramList.size() - 1) {
                    localChatComponentText.a(" and ");
                } else if (i > 0) {
                    localChatComponentText.a(", ");
                }
            }
            localChatComponentText.addSibling(paramList.get(i));
        }
        return localChatComponentText;
    }
    
    public static String a(Collection<String> paramCollection) {
        return a(paramCollection.toArray(new String[paramCollection.size()]));
    }
    
    public static List<String> a(String[] paramArrayOfString, int paramInt, BlockPosition paramBlockPosition) {
        if (paramBlockPosition == null) {
            return Lists.newArrayList(new String[] { "~" });
        }
        int i = paramArrayOfString.length - 1;
        String str;
        if (i == paramInt) {
            str = Integer.toString(paramBlockPosition.getX());
        } else if (i == paramInt + 1) {
            str = Integer.toString(paramBlockPosition.getY());
        } else if (i == paramInt + 2) {
            str = Integer.toString(paramBlockPosition.getZ());
        } else {
            return Collections.emptyList();
        }
        return Lists.newArrayList(new String[] { str });
    }
    
    public static List<String> b(String[] paramArrayOfString, int paramInt, BlockPosition paramBlockPosition) {
        if (paramBlockPosition == null) {
            return Lists.newArrayList(new String[] { "~" });
        }
        int i = paramArrayOfString.length - 1;
        String str;
        if (i == paramInt) {
            str = Integer.toString(paramBlockPosition.getX());
        } else if (i == paramInt + 1) {
            str = Integer.toString(paramBlockPosition.getZ());
        } else {
            return null;
        }
        return Lists.newArrayList(new String[] { str });
    }
    
    public static boolean a(String paramString1, String paramString2) {
        return paramString2.regionMatches(true, 0, paramString1, 0, paramString1.length());
    }
    
    public static List<String> a(String[] paramArrayOfString1, String... paramVarArgs) {
        return a(paramArrayOfString1, Arrays.asList(paramVarArgs));
    }
    
    public static List<String> a(String[] paramArrayOfString, Collection<?> paramCollection) {
        String str = paramArrayOfString[(paramArrayOfString.length - 1)];
        ArrayList localArrayList = Lists.newArrayList();
        Iterator localIterator;
        Object localObject;
        if (!paramCollection.isEmpty()) {
            for (localIterator = Iterables.transform(paramCollection, Functions.toStringFunction()).iterator(); localIterator.hasNext();) {
                localObject = localIterator.next();
                if (a(str, (String) localObject)) {
                    localArrayList.add(localObject);
                }
            }
            if (localArrayList.isEmpty()) {
                for (localIterator = paramCollection.iterator(); localIterator.hasNext();) {
                    localObject = localIterator.next();
                    if (((localObject instanceof MinecraftKey)) && (a(str, ((MinecraftKey) localObject).a()))) {
                        localArrayList.add(String.valueOf(localObject));
                    }
                }
            }
        }
        return localArrayList;
    }
    
    @Override
    public boolean isListStart(String[] paramArrayOfString, int paramInt) {
        return false;
    }
    
    public static void a(ICommandListener paramICommandListener, ICommand paramICommand, String paramString, Object... paramVarArgs) {
        a(paramICommandListener, paramICommand, 0, paramString, paramVarArgs);
    }
    
    public static void a(ICommandListener paramICommandListener, ICommand paramICommand, int paramInt, String cmd, Object... args) {
        CommandProcessor.manager.handleError(cmd, new ChatMessage(cmd, args).getText());
    }
    
    public static void a(ICommandDispatcher paramICommandDispatcher) {
        a = paramICommandDispatcher;
    }
    
    public int a(ICommand paramICommand) {
        return getCommand().compareTo(paramICommand.getCommand());
    }
    
    public static Class<?> inject() {
        return CommandAbstract.class;
    }
}
