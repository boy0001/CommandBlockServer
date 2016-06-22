package net.minecraft.server.v1_8_R3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_8_R3.command.CraftBlockCommandSender;

import com.boydti.cbs.CommandProcessor;
import com.boydti.cbs.Main;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public abstract class CommandAbstract implements ICommand {

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
        final CraftBlockCommandSender sender = new CraftBlockCommandSender(cmd);
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

    private static ICommandDispatcher a;

    public int a() {
        return 4;
    }

    @Override
    public List<String> b() {
        return Collections.emptyList();
    }

    @Override
    public boolean canUse(final ICommandListener listener) {
        enabled = setListener(listener, getClass(), getCommand()) && listener.a(a(), getCommand());
        return enabled;
    }

    @Override
    public List<String> tabComplete(final ICommandListener listener, final String[] paramArrayOfString, final BlockPosition paramBlockPosition) {
        return null;
    }

    public static int a(final String paramString) throws ExceptionInvalidNumber {
        try {
            return Integer.parseInt(paramString);
        } catch (final NumberFormatException localNumberFormatException) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
        }
    }

    public static int a(final String paramString, final int paramInt) throws ExceptionInvalidNumber {
        return a(paramString, paramInt, 2147483647);
    }

    public static int a(final String paramString, final int paramInt1, final int paramInt2) throws ExceptionInvalidNumber {
        final int i = a(paramString);
        if (i < paramInt1) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooSmall", new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt1) });
        }
        if (i > paramInt2) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooBig", new Object[] { Integer.valueOf(i), Integer.valueOf(paramInt2) });
        }
        return i;
    }

    public static long b(final String paramString) throws ExceptionInvalidNumber {
        try {
            return Long.parseLong(paramString);
        } catch (final NumberFormatException localNumberFormatException) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
        }
    }

    public static long a(final String paramString, final long paramLong1, final long paramLong2) throws ExceptionInvalidNumber {
        final long l = b(paramString);
        if (l < paramLong1) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooSmall", new Object[] { Long.valueOf(l), Long.valueOf(paramLong1) });
        }
        if (l > paramLong2) {
            throw new ExceptionInvalidNumber("commands.generic.num.tooBig", new Object[] { Long.valueOf(l), Long.valueOf(paramLong2) });
        }
        return l;
    }

    public static BlockPosition a(final ICommandListener listener, final String[] paramArrayOfString, final int paramInt, final boolean paramBoolean) throws ExceptionInvalidNumber {
        final BlockPosition localBlockPosition = listener.getChunkCoordinates();
        final BlockPosition toReturn = new BlockPosition(b(localBlockPosition.getX(), paramArrayOfString[paramInt], -30000000, 30000000, paramBoolean), b(localBlockPosition.getY(),
        paramArrayOfString[(paramInt + 1)], 0, 256, false), b(localBlockPosition.getZ(), paramArrayOfString[(paramInt + 2)], -30000000, 30000000, paramBoolean));
        //setListener2(listener);
        return isPositionAllowed(toReturn);
    }

    public static double c(final String paramString) throws ExceptionInvalidNumber {
        try {
            final double d = Double.parseDouble(paramString);
            if (!Doubles.isFinite(d)) {
                throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
            }
            return d;
        } catch (final NumberFormatException localNumberFormatException) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { paramString });
        }
    }

    public static double a(final String paramString, final double paramDouble) throws ExceptionInvalidNumber {
        return a(paramString, paramDouble, 1.7976931348623157E+308D);
    }

    public static double a(final String paramString, final double paramDouble1, final double paramDouble2) throws ExceptionInvalidNumber {
        final double d = c(paramString);
        if (d < paramDouble1) {
            throw new ExceptionInvalidNumber("commands.generic.double.tooSmall", new Object[] { Double.valueOf(d), Double.valueOf(paramDouble1) });
        }
        if (d > paramDouble2) {
            throw new ExceptionInvalidNumber("commands.generic.double.tooBig", new Object[] { Double.valueOf(d), Double.valueOf(paramDouble2) });
        }
        return d;
    }

    public static boolean d(final String paramString) throws CommandException {
        if ((paramString.equals("true")) || (paramString.equals("1"))) {
            return true;
        }
        if ((paramString.equals("false")) || (paramString.equals("0"))) {
            return false;
        }
        throw new CommandException("commands.generic.boolean.invalid", new Object[] { paramString });
    }

    public static EntityPlayer b(final ICommandListener listener) throws ExceptionPlayerNotFound {
        if ((listener instanceof EntityPlayer)) {
            //setListener2(listener);
            return isPlayerAllowed((EntityPlayer) listener);
        }
        throw new ExceptionPlayerNotFound("You must specify which player you wish to perform this action on.", new Object[0]);
    }

    public static EntityPlayer a(final ICommandListener listener, final String paramString) throws ExceptionPlayerNotFound {
        //setListener2(listener);
        EntityPlayer localEntityPlayer = PlayerSelector.getPlayer(listener, paramString);
        if (localEntityPlayer == null) {
            try {
                localEntityPlayer = MinecraftServer.getServer().getPlayerList().a(UUID.fromString(paramString));
            } catch (final IllegalArgumentException localIllegalArgumentException) {}
        }
        if (localEntityPlayer == null) {
            localEntityPlayer = MinecraftServer.getServer().getPlayerList().getPlayer(paramString);
        }
        if (localEntityPlayer == null) {
            throw new ExceptionPlayerNotFound();
        }
        return isPlayerAllowed(localEntityPlayer);
    }

    public static Entity b(final ICommandListener listener, final String paramString) throws ExceptionEntityNotFound {
        return isEntityAllowed(a(listener, paramString, Entity.class));
    }

    public static <T extends Entity> Entity a(final ICommandListener listener, final String paramString, final Class<? extends T> paramClass) throws ExceptionEntityNotFound {
        Object localObject = PlayerSelector.getEntity(listener, paramString, paramClass);

        final MinecraftServer localMinecraftServer = MinecraftServer.getServer();
        if (localObject == null) {
            localObject = localMinecraftServer.getPlayerList().getPlayer(paramString);
        }
        if (localObject == null) {
            try {
                final UUID localUUID = UUID.fromString(paramString);
                localObject = localMinecraftServer.a(localUUID);
                if (localObject == null) {
                    localObject = localMinecraftServer.getPlayerList().a(localUUID);
                }
            } catch (final IllegalArgumentException localIllegalArgumentException) {
                throw new ExceptionEntityNotFound("commands.generic.entity.invalidUuid", new Object[0]);
            }
        }
        if ((localObject == null) || (!paramClass.isAssignableFrom(localObject.getClass()))) {
            throw new ExceptionEntityNotFound();
        }
        return isEntityAllowed((Entity) localObject);
    }

    public static List<Entity> c(final ICommandListener listener, final String paramString) throws ExceptionEntityNotFound {
        //setListener2(listener);
        List<Entity> values;
        if (PlayerSelector.isPattern(paramString)) {
            values = PlayerSelector.getPlayers(listener, paramString, Entity.class);
        } else {
            values = Lists.newArrayList(new Entity[] { b(listener, paramString) });
        }
        final Iterator<Entity> iter = values.iterator();
        while (iter.hasNext()) {
            if (isEntityAllowed(iter.next()) == null) {
                iter.remove();
            }
        }
        return values;
    }

    public static String d(final ICommandListener listener, final String paramString) throws ExceptionPlayerNotFound {
        //setListener2(listener);
        try {
            return a(listener, paramString).getName();
        } catch (final ExceptionPlayerNotFound localExceptionPlayerNotFound) {
            if (PlayerSelector.isPattern(paramString)) {
                throw localExceptionPlayerNotFound;
            }
        }
        return paramString;
    }

    public static String e(final ICommandListener listener, final String paramString) throws ExceptionEntityNotFound {
        //setListener2(listener);
        try {
            return a(listener, paramString).getName();
        } catch (final ExceptionPlayerNotFound localExceptionPlayerNotFound) {
            try {
                return b(listener, paramString).getUniqueID().toString();
            } catch (final ExceptionEntityNotFound localExceptionEntityNotFound) {
                if (PlayerSelector.isPattern(paramString)) {
                    throw localExceptionEntityNotFound;
                }
            }
        }
        return paramString;
    }

    public static IChatBaseComponent a(final ICommandListener listener, final String[] paramArrayOfString, final int paramInt) throws ExceptionPlayerNotFound {
        //setListener2(listener);
        return b(listener, paramArrayOfString, paramInt, false);
    }

    public static IChatBaseComponent b(final ICommandListener listener, final String[] paramArrayOfString, final int paramInt, final boolean paramBoolean) throws ExceptionPlayerNotFound {
        //setListener2(listener);
        final ChatComponentText localChatComponentText = new ChatComponentText("");
        for (int i = paramInt; i < paramArrayOfString.length; i++) {
            if (i > paramInt) {
                localChatComponentText.a(" ");
            }
            Object localObject = new ChatComponentText(paramArrayOfString[i]);
            if (paramBoolean) {
                final IChatBaseComponent localIChatBaseComponent = PlayerSelector.getPlayerNames(listener, paramArrayOfString[i]);
                if (localIChatBaseComponent == null) {
                    if (PlayerSelector.isPattern(paramArrayOfString[i])) {
                        throw new ExceptionPlayerNotFound();
                    }
                } else {
                    localObject = localIChatBaseComponent;
                }
            }
            localChatComponentText.addSibling((IChatBaseComponent) localObject);
        }
        return localChatComponentText;
    }

    public static String a(final String[] paramArrayOfString, final int paramInt) {
        final StringBuilder localStringBuilder = new StringBuilder();
        for (int i = paramInt; i < paramArrayOfString.length; i++) {
            if (i > paramInt) {
                localStringBuilder.append(" ");
            }
            final String str = paramArrayOfString[i];

            localStringBuilder.append(str);
        }
        return localStringBuilder.toString();
    }

    public static CommandNumber a(final double paramDouble, final String paramString, final boolean paramBoolean) throws ExceptionInvalidNumber {
        return a(paramDouble, paramString, -30000000, 30000000, paramBoolean);
    }

    public static CommandNumber a(final double paramDouble, String paramString, final int paramInt1, final int paramInt2, final boolean paramBoolean) throws ExceptionInvalidNumber {
        final boolean bool1 = paramString.startsWith("~");
        if ((bool1) && (Double.isNaN(paramDouble))) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { Double.valueOf(paramDouble) });
        }
        double d = 0.0D;
        if ((!bool1) || (paramString.length() > 1)) {
            final boolean bool2 = paramString.contains(".");
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

    public static double b(final double paramDouble, final String paramString, final boolean paramBoolean) throws ExceptionInvalidNumber {
        return b(paramDouble, paramString, -30000000, 30000000, paramBoolean);
    }

    public static double b(final double paramDouble, String paramString, final int paramInt1, final int paramInt2, final boolean paramBoolean) throws ExceptionInvalidNumber {
        final boolean bool1 = paramString.startsWith("~");
        if ((bool1) && (Double.isNaN(paramDouble))) {
            throw new ExceptionInvalidNumber("commands.generic.num.invalid", new Object[] { Double.valueOf(paramDouble) });
        }
        double d = bool1 ? paramDouble : 0.0D;
        if ((!bool1) || (paramString.length() > 1)) {
            final boolean bool2 = paramString.contains(".");
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

        protected CommandNumber(final double paramDouble1, final double paramDouble2, final boolean paramBoolean) {
            a = paramDouble1;
            b = paramDouble2;
            c = paramBoolean;
        }

        public double a() {
            return a;
        }

        public double b() {
            return b;
        }

        public boolean c() {
            return c;
        }
    }

    public static Item f(final ICommandListener listener, final String paramString) throws ExceptionInvalidNumber {
        //setListener2(listener);
        final MinecraftKey localMinecraftKey = new MinecraftKey(paramString);
        final Item localItem = Item.REGISTRY.get(localMinecraftKey);
        if (localItem == null) {
            throw new ExceptionInvalidNumber("commands.give.item.notFound", new Object[] { localMinecraftKey });
        }
        return localItem;
    }

    public static Block g(final ICommandListener listener, final String paramString) throws ExceptionInvalidNumber {
        final MinecraftKey localMinecraftKey = new MinecraftKey(paramString);
        if (!Block.REGISTRY.d(localMinecraftKey)) {
            throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { localMinecraftKey });
        }
        final Block localBlock = Block.REGISTRY.get(localMinecraftKey);
        if (localBlock == null) {
            throw new ExceptionInvalidNumber("commands.give.block.notFound", new Object[] { localMinecraftKey });
        }
        return localBlock;
    }

    public static String a(final Object[] paramArrayOfObject) {
        final StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < paramArrayOfObject.length; i++) {
            final String str = paramArrayOfObject[i].toString();
            if (i > 0) {
                if (i == (paramArrayOfObject.length - 1)) {
                    localStringBuilder.append(" and ");
                } else {
                    localStringBuilder.append(", ");
                }
            }
            localStringBuilder.append(str);
        }
        return localStringBuilder.toString();
    }

    public static IChatBaseComponent a(final List<IChatBaseComponent> paramList) {
        final ChatComponentText localChatComponentText = new ChatComponentText("");
        for (int i = 0; i < paramList.size(); i++) {
            if (i > 0) {
                if (i == (paramList.size() - 1)) {
                    localChatComponentText.a(" and ");
                } else if (i > 0) {
                    localChatComponentText.a(", ");
                }
            }
            localChatComponentText.addSibling(paramList.get(i));
        }
        return localChatComponentText;
    }

    public static String a(final Collection<String> paramCollection) {
        return a(paramCollection.toArray(new String[paramCollection.size()]));
    }

    public static List<String> a(final String[] paramArrayOfString, final int paramInt, final BlockPosition paramBlockPosition) {
        if (paramBlockPosition == null) {
            return null;
        }
        final int i = paramArrayOfString.length - 1;
        String str;
        if (i == paramInt) {
            str = Integer.toString(paramBlockPosition.getX());
        } else if (i == (paramInt + 1)) {
            str = Integer.toString(paramBlockPosition.getY());
        } else if (i == (paramInt + 2)) {
            str = Integer.toString(paramBlockPosition.getZ());
        } else {
            return null;
        }
        return Lists.newArrayList(new String[] { str });
    }

    public static List<String> b(final String[] paramArrayOfString, final int paramInt, final BlockPosition paramBlockPosition) {
        if (paramBlockPosition == null) {
            return null;
        }
        final int i = paramArrayOfString.length - 1;
        String str;
        if (i == paramInt) {
            str = Integer.toString(paramBlockPosition.getX());
        } else if (i == (paramInt + 1)) {
            str = Integer.toString(paramBlockPosition.getZ());
        } else {
            return null;
        }
        return Lists.newArrayList(new String[] { str });
    }

    public static boolean a(final String paramString1, final String paramString2) {
        return paramString2.regionMatches(true, 0, paramString1, 0, paramString1.length());
    }

    public static List<String> a(final String[] paramArrayOfString1, final String... paramVarArgs) {
        return a(paramArrayOfString1, Arrays.asList(paramVarArgs));
    }

    public static List<String> a(final String[] paramArrayOfString, final Collection<?> paramCollection) {
        final String str = paramArrayOfString[(paramArrayOfString.length - 1)];
        final ArrayList localArrayList = Lists.newArrayList();
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
    public boolean isListStart(final String[] paramArrayOfString, final int paramInt) {
        return false;
    }

    public static void a(final ICommandListener listener, final ICommand paramICommand, final String cmd, final Object... args) {
        a(listener, paramICommand, 0, cmd, args);
    }

    public static void a(final ICommandListener listener, final ICommand paramICommand, final int paramInt, final String cmd, final Object... args) {
        CommandProcessor.manager.handleError(cmd, new ChatMessage(cmd, args).getText());
//        if (a != null) {
//            a.a(listener, paramICommand, paramInt, cmd, args);
//        }
    }

    public static void a(final ICommandDispatcher dispatcher) {
        a = dispatcher;
    }

    public int a(final ICommand cmd) {
        return getCommand().compareTo(cmd.getCommand());
    }

    public static Class<?> inject() {
        return CommandAbstract.class;
    }
}
