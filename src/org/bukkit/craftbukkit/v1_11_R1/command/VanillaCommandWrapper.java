package org.bukkit.craftbukkit.v1_11_R1.command;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.ChatMessage;
import net.minecraft.server.v1_11_R1.CommandAbstract;
import net.minecraft.server.v1_11_R1.CommandBlockListenerAbstract;
import net.minecraft.server.v1_11_R1.CommandException;
import net.minecraft.server.v1_11_R1.CommandObjectiveExecutor;
import net.minecraft.server.v1_11_R1.DedicatedServer;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityMinecartCommandBlock;
import net.minecraft.server.v1_11_R1.EnumChatFormat;
import net.minecraft.server.v1_11_R1.ExceptionUsage;
import net.minecraft.server.v1_11_R1.ICommandListener;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerSelector;
import net.minecraft.server.v1_11_R1.WorldServer;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import com.boydti.cbs.CommandProcessor;

public final class VanillaCommandWrapper extends VanillaCommand {
    protected final CommandAbstract vanillaCommand;
    
    public VanillaCommandWrapper(CommandAbstract vanillaCommand) {
        super(vanillaCommand.getCommand());
        this.vanillaCommand = vanillaCommand;
    }
    
    public VanillaCommandWrapper(CommandAbstract vanillaCommand, String usage) {
        super(vanillaCommand.getCommand());
        this.vanillaCommand = vanillaCommand;
        this.description = "A Mojang provided command.";
        this.usageMessage = usage;
        setPermission("minecraft.command." + vanillaCommand.getCommand());
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return true;
    }
        ICommandListener icommandlistener = getListener(sender);
        try {
			dispatchVanillaCommand(sender, icommandlistener, args);
		} catch (CommandException e) {
			e.printStackTrace();
		}
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");
        return this.vanillaCommand.tabComplete(MinecraftServer.getServer(), getListener(sender), args, new BlockPosition(0, 0, 0));
    }
    
    public static CommandSender lastSender = null;
    
    public final int dispatchVanillaCommand(CommandSender bSender, ICommandListener icommandlistener, String[] as) throws CommandException {
        int i = getPlayerListSize(as);
        int j = 0;

        WorldServer[] prev = MinecraftServer.getServer().worldServer;
        MinecraftServer server = MinecraftServer.getServer();
        server.worldServer = new WorldServer[server.worlds.size()];
        server.worldServer[0] = ((WorldServer) icommandlistener.getWorld());
        int bpos = 0;
        for (int pos = 1; pos < server.worldServer.length; pos++) {
            WorldServer world = server.worlds.get(bpos++);
            if (server.worldServer[0] == world) {
                pos--;
            } else {
                server.worldServer[pos] = world;
            }
    }
        try {
            if (this.vanillaCommand.canUse(server, icommandlistener)) {
                if (i > -1) {
                    // TODO investigate
                    List<Entity> list = PlayerSelector.getPlayers(icommandlistener, as[i], Entity.class);
                    String s2 = as[i];
                    
                    icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ENTITIES, list.size());
                    Iterator<Entity> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        Entity entity = iterator.next();
                        
                        CommandSender oldSender = lastSender;
                        lastSender = bSender;
                        try {
                            as[i] = entity.getUniqueID().toString();
                            // FIXME CATCH ERRORS
                            try {
                                this.vanillaCommand.execute(server, icommandlistener, as);
                            } catch (final NullPointerException e) {
                                CommandProcessor.manager.handleError("NotWithin", "Not within your allowed region");
                            }
                            /////////
                            j++;
                        } catch (ExceptionUsage exceptionusage) {
                            ChatMessage chatmessage = new ChatMessage("commands.generic.usage", new Object[] { new ChatMessage(exceptionusage.getMessage(), exceptionusage.getArgs()) });
                            chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                            icommandlistener.sendMessage(chatmessage);
                        } catch (CommandException commandexception) {
                            CommandAbstract.a(icommandlistener, this.vanillaCommand, 0, commandexception.getMessage(), commandexception.getArgs());
                        } finally {
                            lastSender = oldSender;
                        }
                    }
                    as[i] = s2;
                } else {
                    icommandlistener.a(CommandObjectiveExecutor.EnumCommandResult.AFFECTED_ENTITIES, 1);
                    // FIXME CATCH ERRORS
                    try {
                        this.vanillaCommand.execute(server, icommandlistener, as);
                    } catch (final NullPointerException e) {
                        CommandProcessor.manager.handleError("NotWithin", "Not within your allowed region");
                    }
                    /////////
                    j++;
        }
            } else {
                ChatMessage chatmessage = new ChatMessage("commands.generic.permission", new Object[0]);
                chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                icommandlistener.sendMessage(chatmessage);
            }
        } catch (ExceptionUsage exceptionusage) {
            ChatMessage chatmessage1 = new ChatMessage("commands.generic.usage", new Object[] { new ChatMessage(exceptionusage.getMessage(), exceptionusage.getArgs()) });
            chatmessage1.getChatModifier().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage1);
        } catch (CommandException commandexception) {
            CommandAbstract.a(icommandlistener, this.vanillaCommand, 0, commandexception.getMessage(), commandexception.getArgs());
        } catch (Throwable throwable) {
            ChatMessage chatmessage3 = new ChatMessage("commands.generic.exception", new Object[0]);
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
                CommandBlockListenerAbstract listener = (CommandBlockListenerAbstract) icommandlistener;
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
    
    private ICommandListener getListener(CommandSender sender) {
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
            return ((DedicatedServer) MinecraftServer.getServer()).remoteControlCommandListener;
    }
        if ((sender instanceof ConsoleCommandSender)) {
            return ((CraftServer) sender.getServer()).getServer();
    }
        if ((sender instanceof ProxiedCommandSender)) {
            return ((ProxiedNativeCommandSender) sender).getHandle();
    }
        throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
    }
    
    private int getPlayerListSize(String[] as) throws CommandException {
        for (int i = 0; i < as.length; i++) {
            if ((this.vanillaCommand.isListStart(as, i)) && (PlayerSelector.isList(as[i]))) {
                return i;
            }
    }
        return -1;
    }
    
    public static String[] dropFirstArgument(String[] as) {
        String[] as1 = new String[as.length - 1];
        for (int i = 1; i < as.length; i++) {
            as1[(i - 1)] = as[i];
    }
        return as1;
    }
    
    public static Class<?> inject() {
        return CommandAbstract.class;
    }
}
