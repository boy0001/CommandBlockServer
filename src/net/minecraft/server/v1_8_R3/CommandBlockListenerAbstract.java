package net.minecraft.server.v1_8_R3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.command.VanillaCommandWrapper;

import com.boydti.cbs.Main;
import com.google.common.base.Joiner;

public abstract class CommandBlockListenerAbstract implements ICommandListener {
    private static final SimpleDateFormat a = new SimpleDateFormat("HH:mm:ss");
    private int b;
    private boolean c = true;
    private IChatBaseComponent d = null;
    private String e = "";
    private String f = "@";
    private final CommandObjectiveExecutor g = new CommandObjectiveExecutor();
    protected CommandSender sender;

    public int j() {
        return b;
    }

    public IChatBaseComponent k() {
        return d;
    }

    public void a(final NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("Command", e);
        nbttagcompound.setInt("SuccessCount", b);
        nbttagcompound.setString("CustomName", f);
        nbttagcompound.setBoolean("TrackOutput", c);
        if ((d != null) && (c)) {
            nbttagcompound.setString("LastOutput", IChatBaseComponent.ChatSerializer.a(d));
        }
        g.b(nbttagcompound);
    }

    public void b(final NBTTagCompound nbttagcompound) {
        e = nbttagcompound.getString("Command");
        b = nbttagcompound.getInt("SuccessCount");
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            f = nbttagcompound.getString("CustomName");
        }
        if (nbttagcompound.hasKeyOfType("TrackOutput", 1)) {
            c = nbttagcompound.getBoolean("TrackOutput");
        }
        if ((nbttagcompound.hasKeyOfType("LastOutput", 8)) && (c)) {
            d = IChatBaseComponent.ChatSerializer.a(nbttagcompound.getString("LastOutput"));
        }
        g.a(nbttagcompound);
    }

    @Override
    public boolean a(final int i, final String s) {
        return i <= 2;
    }

    public void setCommand(final String s) {
        e = s;
        b = 0;
    }

    public String getCommand() {
        return e;
    }

    public void a(final World world) {
        if (world.isClientSide) {
            b = 0;
        }
        final MinecraftServer minecraftserver = MinecraftServer.getServer();
        if ((minecraftserver != null) && (minecraftserver.O()) && (minecraftserver.getEnableCommandBlock())) {
            minecraftserver.getCommandHandler();
            try {
                d = null;

                b = executeCommand(this, sender, e);
            } catch (final Throwable throwable) {
                final CrashReport crashreport = CrashReport.a(throwable, "Executing command block");
                final CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Command to be executed");

                crashreportsystemdetails.a("Command", new Callable() {
                    public String a() throws Exception {
                        return CommandBlockListenerAbstract.this.getCommand();
                    }

                    @Override
                    public Object call() throws Exception {
                        return a();
                    }
                });
                crashreportsystemdetails.a("Name", new Callable() {
                    public String a() throws Exception {
                        return CommandBlockListenerAbstract.this.getName();
                    }

                    @Override
                    public Object call() throws Exception {
                        return a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        } else {
            b = 0;
        }
    }

    public static int executeCommand(final ICommandListener sender, final CommandSender bSender, String command) {
        final SimpleCommandMap commandMap = sender.getWorld().getServer().getCommandMap();
        final Joiner joiner = Joiner.on(" ");
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        final String[] args = command.split(" ");
        ArrayList<String[]> commands = new ArrayList();

        String cmd = args[0];
        if (cmd.startsWith("minecraft:")) {
            cmd = cmd.substring("minecraft:".length());
        }
        if (cmd.startsWith("bukkit:")) {
            cmd = cmd.substring("bukkit:".length());
        }
        if ((cmd.equalsIgnoreCase("stop"))
        || (cmd.equalsIgnoreCase("kick"))
        || (cmd.equalsIgnoreCase("op"))
        || (cmd.equalsIgnoreCase("deop"))
        || (cmd.equalsIgnoreCase("ban"))
        || (cmd.equalsIgnoreCase("ban-ip"))
        || (cmd.equalsIgnoreCase("pardon"))
        || (cmd.equalsIgnoreCase("pardon-ip"))
        || (cmd.equalsIgnoreCase("reload"))) {
            return 0;
        }
        if (sender.getWorld().players.isEmpty()) {
            return 0;
        }
        Command commandBlockCommand = commandMap.getCommand(args[0]);
        if (sender.getWorld().getServer().getCommandBlockOverride(args[0])) {
            commandBlockCommand = commandMap.getCommand("minecraft:" + args[0]);
        }
        if ((commandBlockCommand instanceof VanillaCommandWrapper)) {
            command = command.trim();
            if (command.startsWith("/")) {
                command = command.substring(1);
            }
            String[] as = command.split(" ");
            as = VanillaCommandWrapper.dropFirstArgument(as);
            if (!((VanillaCommandWrapper) commandBlockCommand).testPermission(bSender)) {
                return 0;
            }
            return ((VanillaCommandWrapper) commandBlockCommand).dispatchVanillaCommand(bSender, sender, as);
        }
        if (commandMap.getCommand(args[0]) == null) {
            return 0;
        }
        commands.add(args);

        final WorldServer[] prev = MinecraftServer.getServer().worldServer;
        final MinecraftServer server = MinecraftServer.getServer();
        server.worldServer = new WorldServer[server.worlds.size()];
        server.worldServer[0] = ((WorldServer) sender.getWorld());
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
            ArrayList<String[]> newCommands = new ArrayList();
            for (int i = 0; i < args.length; i++) {
                if (PlayerSelector.isPattern(args[i])) {
                    for (int j = 0; j < commands.size(); j++) {
                        newCommands.addAll(buildCommands(sender, commands.get(j), i));
                    }
                    final ArrayList<String[]> temp = commands;
                    commands = newCommands;
                    newCommands = temp;
                    newCommands.clear();
                }
            }
        } finally {
            MinecraftServer.getServer().worldServer = prev;
        }
        int completed = 0;
        for (int i = 0; i < commands.size(); i++) {
            try {
                // FIXME //
                if (CommandAbstract.setListener(sender, commandBlockCommand.getClass(), command)) {
                    command = joiner.join(Arrays.asList(commands.get(i)));
                    if (commandMap.dispatch(bSender, command)) {
                        completed++;
                    }
                } else {
                    Main.debug("[CommandBlockServer] Denied: " + command + " from " + sender);
                }
                /////////
            } catch (final Throwable exception) {
                if ((sender.f() instanceof EntityMinecartCommandBlock)) {
                    MinecraftServer.getServer().server.getLogger().log(
                    Level.WARNING,
                    String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command",
                    new Object[] { Integer.valueOf(sender.getChunkCoordinates().getX()), Integer.valueOf(sender.getChunkCoordinates().getY()), Integer.valueOf(sender.getChunkCoordinates().getZ()) }),
                    exception);
                } else if ((sender instanceof CommandBlockListenerAbstract)) {
                    final CommandBlockListenerAbstract listener = (CommandBlockListenerAbstract) sender;
                    MinecraftServer.getServer().server.getLogger().log(
                    Level.WARNING,
                    String.format(
                    "CommandBlock at (%d,%d,%d) failed to handle command",
                    new Object[] {
                    Integer.valueOf(listener.getChunkCoordinates().getX()),
                    Integer.valueOf(listener.getChunkCoordinates().getY()),
                    Integer.valueOf(listener.getChunkCoordinates().getZ()) }), exception);
                } else {
                    MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("Unknown CommandBlock failed to handle command", new Object[0]), exception);
                }
            }
        }
        return completed;
    }

    private static ArrayList<String[]> buildCommands(final ICommandListener sender, final String[] args, final int pos) {
        final ArrayList<String[]> commands = new ArrayList();
        final List<EntityPlayer> players = PlayerSelector.getPlayers(sender, args[pos], EntityPlayer.class);
        if (players != null) {
            for (final EntityPlayer player : players) {
                if (player.world == sender.getWorld()) {
                    final String[] command = args.clone();
                    command[pos] = player.getName();
                    commands.add(command);
                }
            }
        }
        return commands;
    }

    @Override
    public String getName() {
        return f;
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(getName());
    }

    public void setName(final String s) {
        f = s;
    }

    @Override
    public void sendMessage(final IChatBaseComponent ichatbasecomponent) {
        if ((c) && (getWorld() != null) && (!getWorld().isClientSide)) {
            d = new ChatComponentText("[" + a.format(new Date()) + "] ").addSibling(ichatbasecomponent);
            h();
        }
    }

    @Override
    public boolean getSendCommandFeedback() {
        final MinecraftServer minecraftserver = MinecraftServer.getServer();

        return (minecraftserver == null) || (!minecraftserver.O()) || (minecraftserver.worldServer[0].getGameRules().getBoolean("commandBlockOutput"));
    }

    @Override
    public void a(final CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, final int i) {
        g.a(this, commandobjectiveexecutor_enumcommandresult, i);
    }

    public abstract void h();

    public void b(final IChatBaseComponent ichatbasecomponent) {
        d = ichatbasecomponent;
    }

    public void a(final boolean flag) {
        c = flag;
    }

    public boolean m() {
        return c;
    }

    public boolean a(final EntityHuman entityhuman) {
        if (!entityhuman.abilities.canInstantlyBuild) {
            return false;
        }
        if (entityhuman.getWorld().isClientSide) {
            entityhuman.a(this);
        }
        return true;
    }

    public CommandObjectiveExecutor n() {
        return g;
    }

    public static Class<?> inject() {
        return CommandBlockListenerAbstract.class;
    }
}
