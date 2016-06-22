package com.boydti.cbs;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Main {
    public static void main(final String[] args) {
        File file = new File("CommandBlockServer_lib" + File.separator + "README.txt");
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                writer.println("Put a file called spigot.jar in here which is your server jar");
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        debug("Injecting custom classes into NMS/CB");
        inject_1_8_R3();
        inject_1_9_R1();
        inject_1_9_R2();
        inject_1_10_R1();
        try {
            loadJar(new File("CommandBlockServer_lib/spigot.jar"));
        } catch (IOException e) {
            System.out.println("====== spigot.jar NOT FOUND ======");
            e.printStackTrace();
            System.out.println("==================================");
            System.out.println("See CommandBlockServer_lib/README.txt");
            System.out.println("==================================");
        }
        try {
            org.bukkit.craftbukkit.Main.main(Arrays.copyOfRange(args, Math.min(args.length, 1), args.length));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        debug("Done!");
    }
    
    public static void inject_1_8_R3() {
        try {
            net.minecraft.server.v1_8_R3.CommandAbstract.inject();
            org.bukkit.craftbukkit.v1_8_R3.command.VanillaCommandWrapper.inject();
            net.minecraft.server.v1_8_R3.CommandBlockListenerAbstract.inject();
        } catch (Throwable e) {
            
        }
    }
    
    public static void inject_1_9_R1() {
        try {
            net.minecraft.server.v1_9_R1.CommandAbstract.inject();
            org.bukkit.craftbukkit.v1_9_R1.command.VanillaCommandWrapper.inject();
            net.minecraft.server.v1_9_R1.CommandBlockListenerAbstract.inject();
        } catch (Throwable e) {
            
        }
    }
    
    public static void inject_1_9_R2() {
        try {
            net.minecraft.server.v1_9_R2.CommandAbstract.inject();
            org.bukkit.craftbukkit.v1_9_R2.command.VanillaCommandWrapper.inject();
            net.minecraft.server.v1_9_R2.CommandBlockListenerAbstract.inject();
            net.minecraft.server.v1_9_R2.PlayerInteractManager.inject();
        } catch (Throwable e) {
            
        }
    }
    
    public static void inject_1_10_R1() {
        try {
            net.minecraft.server.v1_10_R1.CommandAbstract.inject();
            org.bukkit.craftbukkit.v1_10_R1.command.VanillaCommandWrapper.inject();
            net.minecraft.server.v1_10_R1.CommandBlockListenerAbstract.inject();
        } catch (Throwable e) {
            
        }
    }

    public static void debug(final String m) {
        System.out.println(m);
    }

    private static final Class[] parameters = new Class[] { URL.class };

    public static void loadJar(File file) throws IOException {
        try {
            URL url = file.toURI().toURL();
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
