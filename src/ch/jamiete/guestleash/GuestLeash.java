package ch.jamiete.guestleash;

import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GuestLeash extends JavaPlugin implements CommandExecutor, Listener {
    private ArrayList<LeashSession> sessions;

    public Player getPlayer(final String who, final CommandSender whom) {
        final ArrayList<Player> matches = new ArrayList<Player>();
        final Player forwhom = whom instanceof Player ? (Player) whom : null;
        final boolean check = forwhom != null;

        for (final Player player : this.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().contains(who.toLowerCase())) {
                if (check && forwhom.canSee(player)) {
                    matches.add(player);
                }
            }
        }

        if (matches.size() == 0) {
            whom.sendMessage(ChatColor.RED + "Couldn't find a player matching that query.");
            return null;
        } else if (matches.size() == 1) {
            return matches.get(0);
        } else {
            final StringBuilder players = new StringBuilder();
            for (final Player player : matches) {
                players.append(player.getName() + ", ");
            }
            players.setLength(players.length() - 2);

            whom.sendMessage(ChatColor.RED + "That query matched too many players: " + players.toString());
            return null;
        }
    }

    public LeashSession getSession(final Player player) {
        for (final LeashSession session : this.sessions) {
            if (session.getCaller() == player || session.getGuest() == player) {
                return session;
            }
        }
        return null;
    }

    public boolean hasSession(final Player player) {
        for (final LeashSession session : this.sessions) {
            if (session.getCaller() == player || session.getGuest() == player) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You cannot use this plugin from console.");
            return true;
        }

        final Player player = (Player) sender;

        if (args.length == 0) {
            player.performCommand("leash help");
        } else {
            if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage(new String[] { ChatColor.GOLD + "=============", ChatColor.GOLD + "= GuestLeash =", ChatColor.GOLD + "=============", ChatColor.AQUA + "/leash request <name>" + ChatColor.DARK_AQUA + " - request a player to come to you on a leash", ChatColor.AQUA + "/leash accept" + ChatColor.DARK_AQUA + " - accept a leash request", ChatColor.AQUA + "/leash deny" + ChatColor.DARK_AQUA + " - deny a leash request", ChatColor.AQUA + "/leash force <name>" + ChatColor.DARK_AQUA + " - force a player to come to you on a leash", ChatColor.AQUA + "/leash leave" + ChatColor.DARK_AQUA + " - leave a leash" });
            } else if (args[0].equalsIgnoreCase("request")) {
                if (player.hasPermission("leash.request")) {
                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "/leash request <name>");
                    } else {
                        final Player match = this.getPlayer(args[1], sender);
                        if (match != null) {
                            if (match == player) {
                                player.sendMessage(ChatColor.RED + "You can't initiate a session with yourself...");
                            } else if (!match.hasPermission("leash.respond")) {
                                player.sendMessage(ChatColor.RED + "That player can't accept.");
                            } else if (this.hasSession(match)) {
                                player.sendMessage(ChatColor.RED + "The requested user already has an active session!");
                            } else if (this.hasSession(player)) {
                                player.sendMessage(ChatColor.RED + "You already have an active session!");
                            } else {
                                final LeashSession session = new LeashSession(player, match, false);
                                this.sessions.add(session);
                                match.sendMessage(ChatColor.GREEN + player.getName() + " requested to teleport you to them on a leash.");
                                match.sendMessage(ChatColor.GRAY + "Use /leash accept or /leash deny to respond.");
                                player.sendMessage(ChatColor.GREEN + match.getName() + " was invited to teleport to you.");
                            }
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission");
                }
            } else if (args[0].equalsIgnoreCase("accept")) {
                if (player.hasPermission("leash.respond")) {
                    if (this.hasSession(player)) {
                        final LeashSession session = this.getSession(player);
                        session.setAccepted(true);
                        session.setPrevious(player.getLocation());
                        player.teleport(session.getCaller());
                        session.sendMessage(ChatColor.GREEN + "Leash session activated.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have an active session request!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission");
                }
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (player.hasPermission("leash.respond")) {
                    if (this.hasSession(player)) {
                        final LeashSession session = this.getSession(player);
                        session.sendMessage(ChatColor.RED + "Leash session denied.");
                        this.sessions.remove(session);
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have an active session request!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission");
                }
            } else if (args[0].equalsIgnoreCase("force")) {
                if (player.hasPermission("leash.force")) {
                    if (args.length != 2) {
                        player.sendMessage(ChatColor.RED + "/leash force <name>");
                    } else {
                        final Player match = this.getPlayer(args[1], sender);

                        if (match != null) {
                            if (match == player) {
                                player.sendMessage(ChatColor.RED + "You can't initiate a session with yourself...");
                            } else if (this.hasSession(match) || this.hasSession(player)) {
                                final LeashSession session = this.getSession(player);
                                session.end();
                                this.sessions.remove(session);
                            }

                            final LeashSession session = new LeashSession(player, match, true);
                            session.setAccepted(true);
                            session.setPrevious(match.getLocation());
                            match.teleport(player);
                            this.sessions.add(session);
                            session.sendMessage(ChatColor.GREEN + "Leash session forced successfully.");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission");
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (player.hasPermission("leash.respond")) {
                    if (this.hasSession(player)) {
                        final LeashSession session = this.getSession(player);

                        if (session.isForced()) {
                            if (session.getCaller() == player) {
                                session.end();
                                session.sendMessage(ChatColor.RED + "Leash session ended.");
                                this.sessions.remove(session);
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot end a forced session.");
                            }
                        } else {
                            if (session.isAccepted()) {
                                session.end();
                            }
                            session.sendMessage(ChatColor.RED + "Leash session ended.");
                            this.sessions.remove(session);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have an active session!");
                    }
                } else if (args[0].equalsIgnoreCase("has")) {
                    player.sendMessage(this.hasSession(player) ? ChatColor.GREEN + "You have an active session!" : ChatColor.RED + "You don't have an active session!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission");
            }
        }

        return true;
    }

    @Override
    public void onEnable() {
        Config.load(this);
        this.sessions = new ArrayList<LeashSession>();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            @Override
            public void run() {
                final Iterator<LeashSession> iterator = GuestLeash.this.sessions.iterator();
                while (iterator.hasNext()) {
                    final LeashSession session = iterator.next();

                    if (session.isAccepted()) {
                        final Player one = session.getCaller();
                        final Player two = session.getGuest();

                        if (one.getLocation().distanceSquared(two.getLocation()) > Config.LEASH_LENGTH) {
                            two.teleport(one);
                            two.sendMessage(ChatColor.translateAlternateColorCodes('&', Config.WARNING_MESSAGE));
                        }
                    }

                    if (!session.isAccepted() && System.currentTimeMillis() - session.getCreation() >= 120000) {
                        session.sendMessage(ChatColor.RED + "Leash session expired. Try creating a new one.");
                        iterator.remove();
                    }
                }
            }

        }, 20, 20);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (this.hasSession(event.getPlayer())) {
            final LeashSession session = this.getSession(event.getPlayer());
            if (session.isAccepted()) {
                session.end();
                session.sendMessage(ChatColor.RED + "Leash session ended because a player disconnected.");
                this.sessions.remove(session);
            }
        }
    }

}
