package io.github.bilektugrul.bduels.utils;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.economy.EconomyAdapter;
import io.github.bilektugrul.bduels.features.language.LanguageManager;
import io.github.bilektugrul.bduels.features.placeholders.CustomPlaceholderManager;
import io.github.bilektugrul.bduels.stuff.InGameSettingMode;
import io.github.bilektugrul.bduels.stuff.MessageType;
import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.compat.Titles;
import me.despical.commons.util.Strings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private Utils() {}

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);
    private static final CustomPlaceholderManager placeholderManager = plugin.getPlaceholderManager();
    private static final LanguageManager languageManager = plugin.getLanguageManager();
    private static final EconomyAdapter economy = plugin.getEconomyAdapter();

    private static final boolean isPAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    public static void noPermission(CommandSender sender) {
        sender.sendMessage(getMessage("no-permission", sender));
    }

    public static String getMessage(String string) {
        return getString(languageManager.getLanguage(), "messages." + string, null, false, false);
    }

    public static List<String> getMessageList(String string, CommandSender from) {
        List<String> strings = new ArrayList<>();
        for (String value : languageManager.getLanguage().getStringList("messages." + string)) {
            strings.add(replacePlaceholders(value, from, false, false));
        }
        return strings;
    }

    public static List<String> getStringList(String string, CommandSender from) {
        List<String> strings = new ArrayList<>();
        for (String value : plugin.getConfig().getStringList(string)) {
            strings.add(replacePlaceholders(value, from, false, false));
        }
        return strings;
    }

    public static String getMessage(String string, CommandSender from) {
        return getString(languageManager.getLanguage(), "messages." + string, from);
    }

    public static String getMessage(String string, CommandSender from, boolean replacePersonalPlaceholders) {
        return getString(languageManager.getLanguage(), "messages." + string, from, replacePersonalPlaceholders);
    }

    public static String getMessage(String string, CommandSender from, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        return getString(languageManager.getLanguage(), "messages." + string, from, replacePersonalPlaceholders, replacePAPI);
    }

    public static String getString(String string, CommandSender from) {
        return replacePlaceholders(plugin.getConfig().get(string), from, true);
    }

    public static String getString(String string, CommandSender from, boolean replacePersonalPlaceholders) {
        return replacePlaceholders(plugin.getConfig().get(string), from, replacePersonalPlaceholders);
    }

    public static String getString(FileConfiguration file, String string, CommandSender from) {
        return replacePlaceholders(file.get(string), from, true);
    }

    public static String getString(FileConfiguration file, String string, CommandSender from, boolean replacePersonalPlaceholders) {
        return replacePlaceholders(file.get(string), from, replacePersonalPlaceholders);
    }

    public static String getString(FileConfiguration file, String string, CommandSender from, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        return replacePlaceholders(file.get(string), from, replacePersonalPlaceholders, replacePAPI);
    }

    public static String replacePlaceholders(Object originalMsg, CommandSender from, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        if (originalMsg == null) {
            plugin.getLogger().warning(org.bukkit.ChatColor.RED + "Your language file[s] is/are corrupted or old. Please reset or update them.");
            return "";
        }

        String msg = originalMsg instanceof List ? listToString((List<?>) originalMsg) : (String) originalMsg;
        msg = placeholderManager.replacePlaceholders(Strings.format(msg)).replace("%nl%", "\n");

        if (replacePersonalPlaceholders) {
            msg = msg.replace("%player%", matchName(from));
        }
        if (isPAPIEnabled && replacePAPI) {
            return PlaceholderAPI.setPlaceholders(from instanceof Player ? (Player) from : null, msg);
        }
        return msg;
    }

    public static String replacePlaceholders(Object msg, CommandSender from, boolean replacePersonalPlaceholders) {
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

    public static void sendWinMessage(MessageType messageType, Player winner, Player loser, String itemAmount, String betPrice) {
        Player[] players = new Player[]{winner, loser};
        String toBroadcast = replaceWinnerAndLoser(
                Utils.getMessage("duel.win.broadcast", null, false, false)
                        .replace("%itemamount%", itemAmount)
                        .replace("%betprice%", betPrice),
                winner, loser);
        if (!toBroadcast.isEmpty()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(Utils.replacePlaceholders(toBroadcast, player, true));
                }
            });
        }
        for (Player p : players) {
            String chat = replaceWinnerAndLoser(getMessage("duel.win.chat", p), winner, loser)
                    .replace("%itemamount%", itemAmount)
                    .replace("%betprice%", betPrice);
            String actionBar = replaceWinnerAndLoser(getMessage("duel.win.actionbar", p), winner, loser)
                    .replace("%itemamount%", itemAmount)
                    .replace("%betprice%", betPrice);
            switch (messageType) {
                case CHAT:
                    p.sendMessage(chat);
                    break;
                case TITLE:
                    sendTitle(players, winner, loser);
                    break;
                case ACTIONBAR:
                    sendActionBar(p, actionBar);
                    break;
                case CHAT_AND_BAR:
                    p.sendMessage(chat);
                    sendActionBar(p, actionBar);
                    break;
                case BAR_AND_TITLE:
                    sendActionBar(p, actionBar);
                    sendTitle(players, winner, loser);
                    break;
                case CHAT_AND_TITLE:
                    p.sendMessage(chat);
                    sendTitle(players, winner, loser);
                    break;
                case ALL:
                    p.sendMessage(chat);
                    sendTitle(players, winner, loser);
                    sendActionBar(p, actionBar);
                    break;
            }
        }
    }

    public static void sendTitle(Player[] players, Player winner, Player loser) {
        for (Player p : players) {
            String title = replaceWinnerAndLoser(getMessage("duel.win.title.title", p), winner, loser);
            String subtitle = replaceWinnerAndLoser(getMessage("duel.win.title.subtitle", p), winner, loser);
            Titles.sendTitle(p, title, subtitle, getInt("titles.fade-in"), getInt("titles.stay"), getInt("titles-fade-out"));
        }
    }

    public static String arrayToString(String[] array, CommandSender sender, boolean replacePersonalPlaceholders, boolean replacePAPI) {
        String str = String.join(" ", array);
        return replacePlaceholders(str, sender, replacePersonalPlaceholders, replacePAPI);
    }

    public static String listToString(List<?> list) {
        StringBuilder builder = new StringBuilder();
        int max = list.size() - 1;
        int i = 0;
        for (Object s : list) {
            builder.append((String) s);
            if (i != max) {
                builder.append('\n');
                i++;
            }
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

    public static boolean hasSpace(Inventory inventory, ItemStack itemStack) {
        int totalCount = 0;
        for (int slot = 0; slot < inventory.getSize() * 9; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null) {
                return true;
            } else if (item.isSimilar(itemStack)) {
                totalCount = totalCount + itemStack.getMaxStackSize() - item.getAmount();
                if (totalCount > itemStack.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int nextEmptySlot(int[] side, Inventory inventory) {
        for (int i : side) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) {
                return i;
            }
        }
        return -1;
    }

    public static List<String> getHeadInfo(CommandSender from, int money) {
        List<String> strings = new ArrayList<>();
        for (String value : languageManager.getLanguage().getStringList("messages.duel.request-gui.heads-lore")) {
            strings.add(replacePlaceholders(value.replace("%money%", String.valueOf(money)), from, true, true));
        }
        return strings;
    }

    /** GELECEK NESİLLER İÇİN BU METODU AÇIKLIYORUM, KURCALAYANLARA SELAM OLSUN
     * current = BET OLARAK ORTAYA KOYULMUŞ PARA
     * toAdd = OYUNCUNUN GUIDEN SEÇİP BET'İNE EKLEMEK İSTEDİĞİ PARA
     * player = ÖZNEMİZ
     * currentMoney = OYUNCUNUN HESABINDA BULUNAN TOPLAM PARA
     * EĞER current, currentMoney'e EŞİT İSE DAHA FAZLA KOYAMAZ, KOYARSA HESABI - BAKİYEYE DÜŞER
     * EĞER toAdd değişkenini, current değişkenine EKLEDİKTEN SONRA ÇIKAN SONUÇ, MEVCUT PARADAN BÜYÜKSE DAHA FAZLA KOYAMAZ, KOYARSA HESABI - BAKİYEYE DÜŞER
    **/
    public static boolean canPutMoreMoney(int current, int toAdd, Player player) {
        double currentMoney = economy.getMoney(player);
        return current != currentMoney && current + toAdd <= currentMoney;
    }

    public static boolean shouldCancelEvent(String what, Player player) {
        InGameSettingMode mode = InGameSettingMode.valueOf(getString("in-game-settings." + what));
        if (mode == InGameSettingMode.NEVER) {
            return true;
        } else if (mode == InGameSettingMode.EVERYONE) {
            return false;
        }
        return !(player.hasPermission("bduels." + what));
    }

    public static String formatTime(long millis) {
        long secs = millis / 1000;
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
    }

}