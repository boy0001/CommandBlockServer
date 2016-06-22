package com.boydti.cbs;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class CommandProcessor {

    /**
     * Replace this with your own command processor to enable commands
     */
    public static CommandProcessor manager = new CommandProcessor() {

        
        @Override
        public boolean setLocation(final Location loc, final Class<?> clazz, final String command) {
            Main.debug("0| Please place CommandBlock.jar in the plugins directory to finish installation.");
            return false;
        }

        @Override
        public boolean setPlayer(final Player player, final Class<?> clazz, final String command) {
            Main.debug("1| Please place CommandBlock.jar in the plugins directory to finish installation.");
            return false;
        }

        @Override
        public boolean setConsole(final Class<?> clazz, final String command) {
            Main.debug("2| Please place CommandBlock.jar in the plugins directory to finish installation.");
            return false;
        }

        @Override
        public boolean setCommandBlock(final Block block, final Class<?> clazz, final String command) {
            Main.debug("3| Please place CommandBlock.jar in the plugins directory to finish installation.");
            return false;
        }

        @Override
        public boolean setOther(final Object other) {
            Main.debug("4| Please place CommandBlock.jar in the plugins directory to finish installation.");
            Main.debug("" + (1 / 0));
            return false;
        }

        @Override
        public boolean isLocationAllowed(final String world, final int x, final int y, final int z) {
            Main.debug("5| Please place CommandBlock.jar in the plugins directory to finish installation.");
            Main.debug("" + (1 / 0));
            return false;
        }

        @Override
        public void handleError(String id, String message) {}
    };

    /**
     * Set the command processor
     * @param p
     */
    public static void setProcessor(final CommandProcessor p) {
        manager = p;
    }

    /**
     * If the command doesn't work, notify something
     * @param player
     * @param clazz
     * @param command
     * @return
     */
    public abstract void handleError(String id, String message);
    
    /**
     * Set the scope to a given player (they are executing the command)
     *  - Return false if you want to completely restrict this command
     * @param player
     * @return
     */
    public abstract boolean setPlayer(final Player player, final Class<?> clazz, final String command);

    /**
     * Set the scope to the given command block
     * @param block
     * @param command
     * @return
     */
    public abstract boolean setCommandBlock(final Block block, final Class<?> clazz, final String command);

    /**
     * Set the scope to the given command block
     * @param block
     * @param command
     * @return
     */
    public abstract boolean setLocation(final Location loc, final Class<?> clazz, final String command);

    /**
     * Set the scope to the given command block
     * @param block
     * @param command
     * @return
     */
    public abstract boolean setConsole(final Class<?> clazz, final String command);

    /**
     * Set the scope to some other command sender (not fully implemented)
     * @param other
     * @return
     */
    public abstract boolean setOther(final Object other);

    /**
     * Check if a location is allowed in the set scope
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    public abstract boolean isLocationAllowed(final String world, final int x, final int y, final int z);
}
