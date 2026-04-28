package net.mnetlab.claudechat;

import org.bukkit.plugin.java.JavaPlugin;

public class ClaudeChatPlugin extends JavaPlugin {

    private ClaudeApiClient apiClient;
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        apiClient = new ClaudeApiClient(this);
        cooldownManager = new CooldownManager(this);

        ClaudeCommand command = new ClaudeCommand(this);
        getCommand("claude").setExecutor(command);
        getCommand("claude").setTabCompleter(command);

        getLogger().info("ClaudeChat uruchomiony! Model: " + getConfig().getString("model"));
    }

    @Override
    public void onDisable() {
        getLogger().info("ClaudeChat wyłączony.");
    }

    public ClaudeApiClient getApiClient() {
        return apiClient;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}
