package net.mnetlab.claudechat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ClaudeCommand implements CommandExecutor, TabCompleter {

    private final ClaudeChatPlugin plugin;

    public ClaudeCommand(ClaudeChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(msg("messages.player-only", null));
            return true;
        }

        if (!player.hasPermission("claudechat.use")) {
            player.sendMessage(msg("messages.no-permission", null));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(msg("messages.usage", null));
            return true;
        }

        if (plugin.getCooldownManager().isOnCooldown(player)) {
            long remaining = plugin.getCooldownManager().getRemainingSeconds(player);
            player.sendMessage(msg("messages.cooldown", null)
                    .replaceText(b -> b.matchLiteral("{time}").replacement(String.valueOf(remaining))));
            return true;
        }

        String question = String.join(" ", args);
        int maxLen = plugin.getConfig().getInt("max-question-length", 500);
        if (question.length() > maxLen) {
            player.sendMessage(msg("messages.question-too-long", null)
                    .replaceText(b -> b.matchLiteral("{max}").replacement(String.valueOf(maxLen))));
            return true;
        }

        plugin.getCooldownManager().setCooldown(player);
        player.sendMessage(msg("messages.waiting", null));

        plugin.getApiClient().ask(player.getName(), question).thenAccept(result -> {
            if (!player.isOnline()) return;

            Component response = switch (result.type) {
                case SUCCESS -> {
                    String prefix = plugin.getConfig().getString("chat-prefix", "&b[Claude] &f");
                    yield color(prefix + result.text);
                }
                case MISSING_KEY -> msg("messages.api-key-missing", null);
                case API_ERROR -> msg("messages.api-error", null)
                        .replaceText(b -> b.matchLiteral("{code}").replacement(String.valueOf(result.httpCode)));
                case CONNECTION_ERROR -> msg("messages.connection-error", null);
            };

            player.sendMessage(response);
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("<pytanie>");
        }
        return Collections.emptyList();
    }

    private Component msg(String path, String fallback) {
        String raw = plugin.getConfig().getString(path, fallback != null ? fallback : path);
        return color(raw);
    }

    private Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
