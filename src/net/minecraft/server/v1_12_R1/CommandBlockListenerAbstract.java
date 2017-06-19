package net.minecraft.server.v1_12_R1;

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
import org.bukkit.craftbukkit.v1_12_R1.command.VanillaCommandWrapper;

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
    public CommandSender sender;
    
    public int k() {
        return this.b;
    }
    
    public void a(int i) {
        this.b = i;
    }
    
    public IChatBaseComponent l() {
        return this.d == null ? new ChatComponentText("") : this.d;
    }
    
    public NBTTagCompound a(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("Command", this.e);
        nbttagcompound.setInt("SuccessCount", this.b);
        nbttagcompound.setString("CustomName", this.f);
        nbttagcompound.setBoolean("TrackOutput", this.c);
        if ((this.d != null) && (this.c)) {
            nbttagcompound.setString("LastOutput", IChatBaseComponent.ChatSerializer.a(this.d));
        }
        this.g.b(nbttagcompound);
        return nbttagcompound;
    }
    
    public void b(NBTTagCompound nbttagcompound) {
        this.e = nbttagcompound.getString("Command");
        this.b = nbttagcompound.getInt("SuccessCount");
        if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
            this.f = nbttagcompound.getString("CustomName");
        }
        if (nbttagcompound.hasKeyOfType("TrackOutput", 1)) {
            this.c = nbttagcompound.getBoolean("TrackOutput");
        }
        if ((nbttagcompound.hasKeyOfType("LastOutput", 8)) && (this.c)) {
            try {
                this.d = IChatBaseComponent.ChatSerializer.a(nbttagcompound.getString("LastOutput"));
            } catch (Throwable throwable) {
                this.d = new ChatComponentText(throwable.getMessage());
            }
        } else {
            this.d = null;
        }
        this.g.a(nbttagcompound);
    }
    
    @Override
    public boolean a(int i, String s) {
        return i <= 2;
    }
    
    public void setCommand(String s) {
        this.e = s;
        this.b = 0;
    }
    
    public String getCommand() {
        return this.e;
    }
    
    public void a(World world) {
        if (world.isClientSide) {
            this.b = 0;
        } else if ("Searge".equalsIgnoreCase(this.e)) {
            this.d = new ChatComponentText("#itzlipofutzli"); // what?
            this.b = 1;
        } else {
            MinecraftServer minecraftserver = C_();
            if ((minecraftserver != null) && (minecraftserver.M()) && (minecraftserver.getEnableCommandBlock())) {
                minecraftserver.getCommandHandler();
                try {
                    this.d = null;
                    
                    this.b = executeCommand(this, this.sender, this.e);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Executing command block");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Command to be executed");
                    
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
                this.b = 0;
            }
        }
    }
    
    public static int executeCommand(ICommandListener sender, CommandSender bSender, String command) throws CommandException {
        SimpleCommandMap commandMap = sender.getWorld().getServer().getCommandMap();
        Joiner joiner = Joiner.on(" ");
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        String[] args = command.split(" ");
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
        
        WorldServer[] prev = MinecraftServer.getServer().worldServer;
        MinecraftServer server = MinecraftServer.getServer();
        server.worldServer = new WorldServer[server.worlds.size()];
        server.worldServer[0] = ((WorldServer) sender.getWorld());
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
            ArrayList<String[]> newCommands = new ArrayList();
            for (int i = 0; i < args.length; i++) {
                if (PlayerSelector.isPattern(args[i])) {
                    for (int j = 0; j < commands.size(); j++) {
                        newCommands.addAll(buildCommands(sender, commands.get(j), i));
                    }
                    ArrayList<String[]> temp = commands;
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
            } catch (Throwable exception) {
                if ((sender.f() instanceof EntityMinecartCommandBlock)) {
                    MinecraftServer.getServer().server.getLogger().log(
                    Level.WARNING,
                    String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command",
                    new Object[] { Integer.valueOf(sender.getChunkCoordinates().getX()), Integer.valueOf(sender.getChunkCoordinates().getY()), Integer.valueOf(sender.getChunkCoordinates().getZ()) }),
                    exception);
                } else if ((sender instanceof CommandBlockListenerAbstract)) {
                    CommandBlockListenerAbstract listener = (CommandBlockListenerAbstract) sender;
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
    
    private static ArrayList<String[]> buildCommands(ICommandListener sender, String[] args, int pos) throws CommandException {
        ArrayList<String[]> commands = new ArrayList();
        List<EntityPlayer> players = PlayerSelector.getPlayers(sender, args[pos], EntityPlayer.class);
        if (players != null) {
            for (EntityPlayer player : players) {
                if (player.world == sender.getWorld()) {
                    String[] command = args.clone();
                    command[pos] = player.getName();
                    commands.add(command);
                }
            }
        }
        return commands;
    }
    
    @Override
    public String getName() {
        return this.f;
    }
    
    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(getName());
    }
    
    public void setName(String s) {
        this.f = s;
    }
    
    @Override
    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        if ((this.c) && (getWorld() != null) && (!getWorld().isClientSide)) {
            this.d = new ChatComponentText("[" + a.format(new Date()) + "] ").addSibling(ichatbasecomponent);
            i();
        }
    }
    
    @Override
    public boolean getSendCommandFeedback() {
        MinecraftServer minecraftserver = C_();
    
        return (minecraftserver == null) || (!minecraftserver.M()) || (minecraftserver.worldServer[0].getGameRules().getBoolean("commandBlockOutput"));
    }
    
    @Override
    public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {
        this.g.a(C_(), this, commandobjectiveexecutor_enumcommandresult, i);
    }
    
    public abstract void i();
    
    public void b(IChatBaseComponent ichatbasecomponent) {
        this.d = ichatbasecomponent;
    }
    
    public void a(boolean flag) {
        this.c = flag;
    }
    
    public boolean n() {
        return this.c;
    }
    
    public boolean a(EntityHuman entityhuman) {
        if (!entityhuman.abilities.canInstantlyBuild) {
            return false;
        }
        if (entityhuman.getWorld().isClientSide) {
            entityhuman.a(this);
        }
        return true;
    }
    
    public CommandObjectiveExecutor o() {
        return this.g;
    }
    
    public static Class<?> inject() {
        return CommandAbstract.class;
    }
}
