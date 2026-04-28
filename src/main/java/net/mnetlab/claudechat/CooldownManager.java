package net.mnetlab.claudechat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final ClaudeChatPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public CooldownManager(ClaudeChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("claudechat.bypass-cooldown")) return false;
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) return false;
        return getRemainingSeconds(player) > 0;
    }

    public long getRemainingSeconds(Player player) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) return 0;
        long cooldownMs = plugin.getConfig().getLong("cooldown-seconds", 30) * 1000L;
        long elapsed = System.currentTimeMillis() - cooldowns.get(uuid);
        return Math.max(0, (cooldownMs - elapsed) / 1000);
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
