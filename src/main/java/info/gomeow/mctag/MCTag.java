package info.gomeow.mctag;

import info.gomeow.mctag.matches.Match;
import info.gomeow.mctag.util.Metrics;
import info.gomeow.mctag.util.Updater;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MCTag extends JavaPlugin {

    public static MCTag instance;

    Manager manager;
    private File dataFile;
    private YamlConfiguration data;
    private File statsFile;
    private YamlConfiguration stats;

    public void onEnable() {
        d("Enabling.");
        instance = this;
        saveDefaultConfig();
        loadData();
        loadStats();
        manager = new Manager(this);
        getServer().getPluginManager().registerEvents(new Listeners(this), this);
        getCommand("tag").setExecutor(new CommandHandler(this));
        getCommand("leave").setExecutor(new CommandHandler(this));
        checkUpdate();
        startMetrics();
    }

    public void onDisable() {
        d("Disabling");
        int debug = 0;
        for (Match match : manager.getMatches()) {
            debug++;
            match.broadcast(ChatColor.DARK_RED + "Match Interrupted by Server Stop/Reload");
            match.reset(true);
        }
        d(debug + " match(es) force stopped.");
    }

    public void loadData() {
        d("Loading data.");
        File f = new File(getDataFolder(), "data.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataFile = f;
        data = YamlConfiguration.loadConfiguration(f);
    }

    public YamlConfiguration getData() {
        return data;
    }

    public void saveData() {
        d("Saving data.");
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadStats() {
        d("Loading stats.");
        File f = new File(getDataFolder(), "stats.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        statsFile = f;
        stats = YamlConfiguration.loadConfiguration(f);
    }

    public YamlConfiguration getStats() {
        return stats;
    }

    public void saveStats() {
        d("Saving stats.");
        try {
            stats.save(statsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Manager getManager() {
        return manager;
    }

    /**
     * Checks for available updates.
     */
    public void checkUpdate() {
        d("Checking for updates.");
        new BukkitRunnable() {

            public void run() {
                if (getConfig().getBoolean("check-update")) {
                    Updater updater = new Updater(MCTag.instance, 40098, getFile(), Updater.UpdateType.DEFAULT, true);
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void startMetrics() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Metrics metrics = new Metrics(MCTag.this);
                    metrics.start();
                } catch (IOException ex) {
                    getLogger().warning("Failed to load metrics :(");
                }
            }
        }.runTaskAsynchronously(this);
    }

    public void d(Object o) { // Debug
        if (getConfig().getBoolean("debug-mode", false)) {
            getLogger().info(o.toString());
        }
    }
}
