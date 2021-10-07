package io.github.bilektugrul.bduels.features.language;

import io.github.bilektugrul.bduels.BDuels;
import me.despical.commons.configuration.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.Locale;

public class LanguageManager {

    private FileConfiguration language;
    private String languageString;

    private final BDuels plugin;

    public LanguageManager(BDuels plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        languageString = plugin.getConfig().getString("language").toLowerCase(Locale.ROOT);
        String s = File.separator;
        try {
            language = ConfigUtils.getConfig(plugin, "language" + s + "language_" + languageString);
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().warning("Olmayan bir seçtiniz, mevcut dilleri kontrol etmek için eklentinin .jar dosyasını açıp languages klasörüne göz atın");
            plugin.getLogger().warning("Eklenti Türkçe dilini kullanacak.");
            languageString = "tr";
            language = ConfigUtils.getConfig(plugin, "language" + s + "language_" + languageString);
        }
    }

    public FileConfiguration getLanguage() {
        return language;
    }

    public String getLanguageName() {
        return languageString;
    }

    public String getString(String string) {
        return language.getString(string);
    }

    public boolean getBoolean(String string) {
        return getBoolean(string, false);
    }

    public boolean getBoolean(String string, boolean def) {
        return language.getBoolean(string, def);
    }

}
