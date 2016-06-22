package org.bukkit.craftbukkit.v1_8_R3.command;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.CommandAbstract;
import net.minecraft.server.v1_8_R3.CommandBlockListenerAbstract;
import net.minecraft.server.v1_8_R3.CommandException;
import net.minecraft.server.v1_8_R3.CommandObjectiveExecutor;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityMinecartCommandBlock;
import net.minecraft.server.v1_8_R3.EnumChatFormat;
import net.minecraft.server.v1_8_R3.ExceptionUsage;
import net.minecraft.server.v1_8_R3.ICommandListener;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerSelector;
import net.minecraft.server.v1_8_R3.RemoteControlCommandListener;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import com.boydti.cbs.CommandProcessor;

public final class VanillaCommandWrapper extends VanillaCommand {
    protected final CommandAbstract vanillaCommand;

    public VanillaCommandWrapper(final CommandAbstract vanillaCommand) {
        super(vanillaCommand.getCommand());
        this.vanillaCommand = vanillaCommand;
    }

    public VanillaCommandWrapper(final CommandAbstract vanillaCommand, final String usage) {
        super(vanillaCommand.getCommand());
        this.vanillaCommand = vanillaCommand;
        description = "A Mojang provided command.";
        usageMessage = usage;
        setPermission("minecraft.command." + vanillaCommand.getCommand());
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        final ICommandListener icommandlistener = getListener(sender);
        dispatchVanillaCommand(sender, icommandlistener, args);
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");
        return vanillaCommand.tabComplete(getListener(sender), args, new BlockPosition(0, 0, 0));
    }

    public static CommandSender lastSender = null;

    public final int dispatchVanillaCommand(final CommandSender bSender, final ICommandListener icommandlistener, final String[] as) {
        final int i = getPlayerListSize(as);
        int j = 0;

        final WorldServer[] prev = MinecraftServer.getServer().worldServer;
        final MinecraftServer server = MinecraftServer.getServer();
        server.worldServer = new WorldServer[server.worlds.size()];
        server.worldServer[0] = ((WorldServer) icommandlistener.getWorld());
        int bpos = 0;
        for (int pos = 1; pos < server.worldServer.length; pos++) {
            final WorldServer world = server.worlds.get(bpos++);
            if (server.worldServer[0] == world) {
                pos--;
            } else {
                server.worldServer[pos] = world;
            }
        }
        try {
            if (vanillaCommand.canUse(icommandlistener)) {
                if (i > -1) {
                    // TODO investigate
                    final List<Entity> list = PlayerSelector.getPlayers(icommandlistener, as[i], Entity.class);
                    final String s2 = as[i];

                    icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ENTITIES, list.size());
                    final Iterator<Entity> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        final Entity entity = iterator.next();

                        final CommandSender oldSender = lastSender;
                        lastSender = bSender;
                        try {
                            as[i] = entity.getUniqueID().toString();
                            // FIXME CATCH ERRORS
                            try {
                                vanillaCommand.execute(icommandlistener, as);
                            } catch (final NullPointerException e) {
                                CommandProcessor.manager.handleError("NotWithin", "Not within your allowed region");
                            }
                            /////////
                            j++;
                        } catch (final ExceptionUsage exceptionusage) {
                            final ChatMessage chatmessage = new ChatMessage("commands.generic.usage", new Object[] { new ChatMessage(exceptionusage.getMessage(), exceptionusage.getArgs()) });
                            chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                            icommandlistener.sendMessage(chatmessage);
                        } catch (final CommandException commandexception) {
                            CommandAbstract.a(icommandlistener, vanillaCommand, 1, commandexception.getMessage(), commandexception.getArgs());
                        } finally {
                            lastSender = oldSender;
                        }
                    }
                    as[i] = s2;
                } else {
                    icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ENTITIES, 1);
                    // FIXME CATCH ERRORS
                    try {
                        vanillaCommand.execute(icommandlistener, as);
                    } catch (final NullPointerException e) {
                        CommandProcessor.manager.handleError("NotWithin", "Not within your allowed region");
                    }
                    /////////
                    j++;
                }
            } else {
                final ChatMessage chatmessage = new ChatMessage("commands.generic.permission", new Object[0]);
                chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                icommandlistener.sendMessage(chatmessage);
            }
        } catch (final ExceptionUsage exceptionusage) {
            final ChatMessage chatmessage1 = new ChatMessage("commands.generic.usage", new Object[] { new ChatMessage(exceptionusage.getMessage(), exceptionusage.getArgs()) });
            chatmessage1.getChatModifier().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage1);
        } catch (final CommandException commandexception) {
            CommandAbstract.a(icommandlistener, vanillaCommand, 1, commandexception.getMessage(), commandexception.getArgs());
        } catch (final Throwable throwable) {
            final ChatMessage chatmessage3 = new ChatMessage("commands.generic.exception", new Object[0]);
            chatmessage3.getChatModifier().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage3);
            if ((icommandlistener.f() instanceof EntityMinecartCommandBlock)) {
                MinecraftServer.LOGGER.log(
                Level.WARN,
                String.format(
                "MinecartCommandBlock at (%d,%d,%d) failed to handle command",
                new Object[] {
                Integer.valueOf(icommandlistener.getChunkCoordinates().getX()),
                Integer.valueOf(icommandlistener.getChunkCoordinates().getY()),
                Integer.valueOf(icommandlistener.getChunkCoordinates().getZ()) }), throwable);
            } else if ((icommandlistener instanceof CommandBlockListenerAbstract)) {
                final CommandBlockListenerAbstract listener = (CommandBlockListenerAbstract) icommandlistener;
                MinecraftServer.LOGGER.log(Level.WARN, String.format("CommandBlock at (%d,%d,%d) failed to handle command", new Object[] {
                Integer.valueOf(listener.getChunkCoordinates().getX()),
                Integer.valueOf(listener.getChunkCoordinates().getY()),
                Integer.valueOf(listener.getChunkCoordinates().getZ()) }), throwable);
            } else {
                MinecraftServer.LOGGER.log(Level.WARN, String.format("Unknown CommandBlock failed to handle command", new Object[0]), throwable);
            }
        } finally {
            MinecraftServer.getServer().worldServer = prev;
        }
        icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.SUCCESS_COUNT, j);
        return j;
    }

    private ICommandListener getListener(final CommandSender sender) {
        if ((sender instanceof Player)) {
            return ((CraftPlayer) sender).getHandle();
        }
        if ((sender instanceof BlockCommandSender)) {
            return ((CraftBlockCommandSender) sender).getTileEntity();
        }
        if ((sender instanceof CommandMinecart)) {
            return ((EntityMinecartCommandBlock) ((CraftMinecartCommand) sender).getHandle()).getCommandBlock();
        }
        if ((sender instanceof RemoteConsoleCommandSender)) {
            return RemoteControlCommandListener.getInstance();
        }
        if ((sender instanceof ConsoleCommandSender)) {
            return ((CraftServer) sender.getServer()).getServer();
        }
        if ((sender instanceof ProxiedCommandSender)) {
            return ((ProxiedNativeCommandSender) sender).getHandle();
        }
        throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
    }

    private int getPlayerListSize(final String[] as) {
        for (int i = 0; i < as.length; i++) {
            if ((vanillaCommand.isListStart(as, i)) && (PlayerSelector.isList(as[i]))) {
                return i;
            }
        }
        return -1;
    }

    public static String[] dropFirstArgument(final String[] as) {
        final String[] as1 = new String[as.length - 1];
        for (int i = 1; i < as.length; i++) {
            as1[(i - 1)] = as[i];
        }
        return as1;
    }

    public static Class<?> inject() {
        return VanillaCommandWrapper.class;
    }
}
