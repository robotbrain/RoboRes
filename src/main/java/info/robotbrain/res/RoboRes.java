package info.robotbrain.res;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoboRes extends JavaPlugin implements Listener
{
    private static Pattern[] resSyntaxes = { Pattern.compile("res\\[(?<name>[a-zA-Z0-9_.]+)]"), Pattern.compile("\\./res tp (?<name>[a-zA-Z0-9_.]+)") };
    private static Pattern[] warpSyntaxes = { Pattern.compile("warp\\[(?<name>[a-zA-Z0-9_]+)]") };

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        String msg = event.getMessage();
        ArrayList<String> reses = new ArrayList<String>();
        find(msg, resSyntaxes, reses);
        if (reses.size() > 0) {
            link(reses, "Res", "/res tp %s");
        }
        ArrayList<String> warps = new ArrayList<String>();
        find(msg, warpSyntaxes, warps);
        if (warps.size() > 0) {
            link(warps, "Warp", "/warp %s");
        }
    }

    private void find(String msg, Pattern[] patterns, ArrayList<String> warps)
    {
        for (Pattern syntax : patterns) {
            Matcher matcher = syntax.matcher(msg);
            if (matcher.matches()) {
                while (matcher.find()) {
                    warps.add(matcher.group("name"));
                }
            }
        }
    }

    private void link(ArrayList<String> reses, String text, String command)
    {
        text += " link";
        if (reses.size() > 1) {
            text += "s: ";
        } else {
            text += ": ";
        }
        ComponentBuilder builder = new ComponentBuilder(text);
        for (int i = 0; i < reses.size(); i++) {
            String name = reses.get(i);
            builder.append(name, FormatRetention.NONE);
            builder.color(ChatColor.LIGHT_PURPLE);
            builder.event(new ClickEvent(Action.RUN_COMMAND, String.format(command, name)));
            if (i + 1 < reses.size()) {
                builder.append(", ", FormatRetention.NONE);
            }
        }
        getServer().spigot().broadcast(builder.create());
    }

    private boolean doLink(String line, Pattern[] syntaxes, Player player, String command)
    {
        for (Pattern resSyntax : syntaxes) {
            Matcher matcher = resSyntax.matcher(line);
            if (matcher.matches()) {
                player.chat(String.format(command, matcher.group("name")));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            String[] lines = sign.getLines();
            for (String line : lines) {
                if (doLink(line, resSyntaxes, event.getPlayer(), "/res tp %s")) {
                    return;
                }
                doLink(line, warpSyntaxes, event.getPlayer(), "/warp %s");
            }
        }
    }
}
