package io.github.bilektugrul.bduels.utils;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.language.LanguageManager;
import io.github.bilektugrul.bduels.placeholders.CustomPlaceholderManager;
import io.github.bilektugrul.bduels.stuff.MessageType;
import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.compat.Titles;
import me.despical.commons.util.Strings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class Utils {

    private Utils() {}

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);
    private static final CustomPlaceholderManager placeholderManager = plugin.getPlaceholderManager();
    private static final LanguageManager languageManager = plugin.getLanguageManager();

    private static final boolean isPAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    public static void noPermission(CommandSender sender) {
        sender.sendMessage(getMessage("no-permission", sender));
    }

    public static String getMessage(String string) {
        return getString(languageManager.getLanguage(), "messages." + string, null, false, false);
    }

    public static String getMessage(String string, CommandSender from) {
        return getString(languageManager.getLanguage(), "messages." + string, from);
    }

    public static String getMessage(String string, CommandSender from, boolean replacePersonalPlaceholders) {
        return getString(languageManager.getLanguage(), "messages." + string, from, replacePersonalPlaceholders);
    }

    public static String getString(String string, CommandSender from) {
        return replacePlaceholders(plugin.getConfig().getString(string), from, true);
    }

    public static String getString(String string, CommandSender from, boolean replacePersonalPlaceholders) {
        return replacePlaceholders(plugin.getConfig().getString(string), from, replacePersonalPlaceholders);
    }

    public static String getString(FileConfiguration file, String string, CommandSender from) {
        return replacePlaceholders(file.getString(string), from, true);
    }

    public static String getString(FileConfiguration file, String string, CommandSender from, boolean replacePersonalPlaceholders) {
        return replacePlaceholders(file.getString(string), from, replacePersonalPlaceholders);
    }

    public static String getString(FileConfiguration file, String string, CommandSender from, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        return replacePlaceholders(file.getString(string), from, replacePersonalPlaceholders, replacePAPI);
    }

    public static String replacePlaceholders(String msg, CommandSender from, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        boolean isPlayer = from instanceof Player;
        if (msg == null) {
            plugin.getLogger().warning(org.bukkit.ChatColor.RED + "Your language file[s] is/are corrupted or old. Please reset or update them.");
            return "";
        }
        msg = placeholderManager.replacePlaceholders(Strings.format(msg))
                .replace("%nl%", "\n");
        if (replacePersonalPlaceholders) {
            msg = msg.replace("%player%", matchName(from));
        }
        if (isPAPIEnabled && replacePAPI) {
            return PlaceholderAPI.setPlaceholders(isPlayer ? (Player) from : null, msg);
        }
        return msg;
    }

    public static String replacePlaceholders(String msg, CommandSender from, boolean replacePersonalPlaceholders) {
        return replacePlaceholders(msg, from, replacePersonalPlaceholders, true);
    }

    public static String getString(String string) {
        return plugin.getConfig().getString(string);
    }

    public static String matchName(CommandSender entity) {
        return entity instanceof Player ? entity.getName() : "CONSOLE";
    }

    public static boolean getBoolean(String string, boolean def) {
        return plugin.getConfig().getBoolean(string, def);
    }

    public static boolean getBoolean(String string) {
        return plugin.getConfig().getBoolean(string);
    }

    public static boolean getLanguageBoolean(String string) {
        return languageManager.getBoolean(string);
    }

    public static int getInt(String path) {
        return plugin.getConfig().getInt(path);
    }

    public static boolean getBoolean(FileConfiguration file, String string, boolean def) {
        return file.getBoolean(string, def);
    }

    public static boolean getBoolean(FileConfiguration file, String string) {
        return file.getBoolean(string);
    }

    public static int getInt(FileConfiguration file, String path) {
        return file.getInt(path);
    }

    public static float getFloat(YamlConfiguration yaml, String path) {
        return Float.parseFloat(yaml.getString(path));
    }

    public static int getLanguageInt(String path) {
        return languageManager.getLanguage().getInt(path);
    }

    public static Location getLocation(YamlConfiguration yaml, String key) {
        return (Location) yaml.get(key);
    }

    public static boolean isSameLoc(Location loc1, Location loc2) {
        return (loc1.getBlockX() == loc2.getBlockX()) && (loc1.getBlockY() == loc2.getBlockY()) && (loc1.getBlockZ() == loc2.getBlockZ());
    }

    public static String replaceWinnerAndLoser(String msg, Player winner, Player loser) {
        return msg
                .replace("%winner%", winner.getName())
                .replace("%loser%", loser.getName());
    }

    public static void sendWinMessage(MessageType messageType, Player winner, Player loser) {
        Player[] players = new Player[]{winner, loser};
        for (Player p : players) {
            switch (messageType) {
                case CHAT:
                    p.sendMessage(replaceWinnerAndLoser(getMessage("duel.win.chat", p), winner, loser));
                    break;
                case TITLE:
                    String title = replaceWinnerAndLoser(getMessage("duel.win.title.title", p), winner, loser);
                    String subtitle = replaceWinnerAndLoser(getMessage("duel.win.title.subtitle", p), winner, loser);
                    Titles.sendTitle(p, title, subtitle, getInt("titles.fade-in"), getInt("titles.stay"), getInt("titles-fade-out"));
                    break;
                case ACTIONBAR:
                    sendActionBar(p, replaceWinnerAndLoser(getMessage("duel.win.actionbar", p), winner, loser));
                    break;
            }
        }
    }

    public static boolean matchMode(String mode) {
        mode = mode.toLowerCase(Locale.ROOT);
        if (mode.contains("on") || mode.contains("true") || mode.contains("aç") || mode.contains("aktif")) {
            return true;
        } else if (mode.contains("off") || mode.contains("false") || mode.contains("kapat") || mode.contains("de-aktif") || mode.contains("deaktif")) {
            return false;
        }
        return false;
    }

    public static String arrayToString(String[] array, CommandSender sender, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        String str = String.join(" ", array);
        return replacePlaceholders(str, sender, replacePersonalPlaceholders, replacePAPI);
    }

    public static String listToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            builder.append(s).append("\n");
        }
        return builder.toString();
    }

    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) return;
        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = nmsVersion.substring(nmsVersion.lastIndexOf(".") + 1);

        //1.10 and up
        if (!nmsVersion.startsWith("v1_9_R") && !nmsVersion.startsWith("v1_8_R")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            return;
        }

        //1.8.x and 1.9.x
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);

            Class<?> ppoc = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> packet = Class.forName("net.minecraft.server." + nmsVersion + ".Packet");
            Object packetPlayOutChat;
            Class<?> chat = Class.forName("net.minecraft.server." + nmsVersion + (nmsVersion.equalsIgnoreCase("v1_8_R1") ? ".ChatSerializer" : ".ChatComponentText"));
            Class<?> chatBaseComponent = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");

            Method method = null;
            if (nmsVersion.equalsIgnoreCase("v1_8_R1")) method = chat.getDeclaredMethod("a", String.class);

            Object object = nmsVersion.equalsIgnoreCase("v1_8_R1") ? chatBaseComponent.cast(method.invoke(chat, "{'text': '" + message + "'}")) : chat.getConstructor(new Class[]{String.class}).newInstance(message);
            packetPlayOutChat = ppoc.getConstructor(new Class[]{chatBaseComponent, Byte.TYPE}).newInstance(object, (byte) 2);

            Method handle = craftPlayerClass.getDeclaredMethod("getHandle");
            Object iCraftPlayer = handle.invoke(craftPlayer);
            Field playerConnectionField = iCraftPlayer.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(iCraftPlayer);
            Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", packet);
            sendPacket.invoke(playerConnection, packetPlayOutChat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
