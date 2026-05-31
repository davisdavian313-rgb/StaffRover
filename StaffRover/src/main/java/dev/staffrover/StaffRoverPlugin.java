package dev.staffrover;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.staffrover.command.AltsCommand;
import dev.staffrover.command.AppealCommand;
import dev.staffrover.command.AppealInfoCommand;
import dev.staffrover.command.AuditLogCommand;
import dev.staffrover.command.CaseCommand;
import dev.staffrover.command.CommandUtil;
import dev.staffrover.command.DirectPunishmentCommand;
import dev.staffrover.command.EvasionCommand;
import dev.staffrover.command.EvidenceCommand;
import dev.staffrover.command.FreezeCommand;
import dev.staffrover.command.HistoryCommand;
import dev.staffrover.command.NotesCommand;
import dev.staffrover.command.PunishCommand;
import dev.staffrover.command.PunishmentCommand;
import dev.staffrover.command.ReportCommand;
import dev.staffrover.command.ReportClaimCommand;
import dev.staffrover.command.ReportCloseCommand;
import dev.staffrover.command.ReportsCommand;
import dev.staffrover.command.SeenCommand;
import dev.staffrover.command.StaffBroadcastCommand;
import dev.staffrover.command.StaffChatCommand;
import dev.staffrover.command.StaffCoinflipCommand;
import dev.staffrover.command.StaffGuiCommand;
import dev.staffrover.command.StaffHelpCommand;
import dev.staffrover.command.StaffHubCommand;
import dev.staffrover.command.StaffListCommand;
import dev.staffrover.command.StaffModeCommand;
import dev.staffrover.command.StaffRollCommand;
import dev.staffrover.command.StaffStatsCommand;
import dev.staffrover.command.UnpunishCommand;
import dev.staffrover.command.VanishCommand;
import dev.staffrover.command.WatchCommand;
import dev.staffrover.command.WhoisCommand;
import dev.staffrover.config.StaffConfig;
import dev.staffrover.model.PlayerProfile;
import dev.staffrover.model.Punishment;
import dev.staffrover.punish.PunishmentCatalog;
import dev.staffrover.storage.StaffRoverStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(
        id = "staffrover",
        name = "STAFF",
        version = "0.1.0",
        description = "Proxy staff tools for reports, punishments, evidence, ban-evasion checks, and staff utilities."
)
public final class StaffRoverPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final StaffRoverStorage storage;
    private final StaffConfig config;
    private final PunishmentCatalog catalog = new PunishmentCatalog();
    private final Set<UUID> staffModePlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private Object luckPerms;

    @Inject
    public StaffRoverPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.storage = new StaffRoverStorage(dataDirectory);
        this.config = new StaffConfig(dataDirectory);
        try {
            config.loadOrCreate();
            storage.load();
        } catch (IOException exception) {
            logger.error("Unable to load STAFF data", exception);
        }
        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            this.luckPerms = providerClass.getMethod("get").invoke(null);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logger.info("LuckPerms was not available during STAFF startup. Staff chat prefixes will be omitted.");
        }
        registerCommands();
    }

    public ProxyServer server() {
        return server;
    }

    public StaffRoverStorage storage() {
        return storage;
    }

    public PunishmentCatalog catalog() {
        return catalog;
    }

    public Logger logger() {
        return logger;
    }

    public StaffConfig config() {
        return config;
    }

    private void registerCommands() {
        register("punish", new PunishCommand(this));
        register("ban", new DirectPunishmentCommand(this, "ban", "BAN", false, false));
        register("tempban", new DirectPunishmentCommand(this, "tempban", "TEMPBAN", true, false));
        register("mute", new DirectPunishmentCommand(this, "mute", "MUTE", true, false));
        register("banip", new DirectPunishmentCommand(this, "banip", "IPBAN", false, false));
        register("teamipban", new DirectPunishmentCommand(this, "teamipban", "TEMPIPBAN", true, false), "tempipban");
        register("warn", new DirectPunishmentCommand(this, "warn", "WARN", false, false));
        register("blacklist", new DirectPunishmentCommand(this, "blacklist", "BLACKLIST", false, true));
        register("punishment", new PunishmentCommand(this));
        register("report", new ReportCommand(this));
        register("reports", new ReportsCommand(this));
        register("reportclaim", new ReportClaimCommand(this));
        register("reportclose", new ReportCloseCommand(this));
        register("evidence", new EvidenceCommand(this));
        register("whois", new WhoisCommand(this));
        register("seen", new SeenCommand(this));
        register("alts", new AltsCommand(this));
        register("evasion", new EvasionCommand(this));
        register("history", new HistoryCommand(this));
        register("notes", new NotesCommand(this));
        register("note", new NotesCommand(this));
        register("case", new CaseCommand(this));
        register("appealinfo", new AppealInfoCommand(this));
        register("appeal", new AppealCommand(this));
        register("freeze", new FreezeCommand(this));
        register("watch", new WatchCommand(this));
        register("staffhelp", new StaffHelpCommand());
        register("auditlog", new AuditLogCommand(this));
        register("staffchat", new StaffChatCommand(this), "sc");
        register("staffbroadcast", new StaffBroadcastCommand(this), "sbc");
        register("staffgui", new StaffGuiCommand(this), "sgui", "staff");
        register("staffmode", new StaffModeCommand(this));
        register("staffhub", new StaffHubCommand(this));
        register("vanish", new VanishCommand(this), "v");
        register("unban", new UnpunishCommand(this, "staffrover.unpunish.ban", "unban", "BAN", "TEMPBAN"));
        register("unmute", new UnpunishCommand(this, "staffrover.unpunish.mute", "unmute", "MUTE"));
        register("unwarn", new UnpunishCommand(this, "staffrover.unpunish.warn", "unwarn", "WARN"));
        register("staffroll", new StaffRollCommand());
        register("staffcoinflip", new StaffCoinflipCommand());
        register("stafflist", new StaffListCommand(this));
        register("staffstats", new StaffStatsCommand(this));
    }

    private void register(String name, SimpleCommand command, String... aliases) {
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder(name).aliases(aliases).plugin(this).build(),
                command
        );
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        storage.activeBanFor(event.getPlayer().getUniqueId()).ifPresent(punishment -> event.setResult(ResultedEvent.ComponentResult.denied(
                Component.text("You are banned: " + punishment.reason(), NamedTextColor.RED))));
        String ip = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
        storage.activeIpBanFor(ip).ifPresent(punishment -> event.setResult(ResultedEvent.ComponentResult.denied(
                Component.text("You are IP banned: " + punishment.reason(), NamedTextColor.RED))));
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = storage.profile(player.getUniqueId(), player.getUsername());
        Instant now = Instant.now();
        if (profile.registeredAt() == null) {
            profile.registeredAt(now);
        }
        profile.lastLoginAt(now);
        String ip = player.getRemoteAddress().getAddress().getHostAddress();
        if (profile.firstIp().equals("unknown")) {
            profile.firstIp(ip);
        }
        profile.lastIp(ip);
        profile.knownIps().add(ip);
        storage.saveAll();

        int risk = evasionRisk(profile);
        if (risk >= 50) {
            broadcastStaff(CommandUtil.prefix()
                    .append(CommandUtil.danger("Possible ban evader joined: "))
                    .append(CommandUtil.name(player.getUsername()))
                    .append(CommandUtil.detail(" risk " + risk + ". Use /evasion " + player.getUsername())),
                    "staffrover.evasion.alert");
        }
        storage.activeWatchesFor(player.getUniqueId()).forEach(watch -> broadcastStaff(
                CommandUtil.prefix()
                        .append(CommandUtil.action("Watched player joined: "))
                        .append(CommandUtil.name(player.getUsername()))
                        .append(CommandUtil.detail(" | " + watch.reason())),
                "staffrover.watch.alert"));
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        String serverName = event.getOriginalServer().getServerInfo().getName();
        storage.activeServerBlacklist(event.getPlayer().getUniqueId(), serverName).ifPresent(blacklist -> {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            event.getPlayer().sendMessage(CommandUtil.error("You are blacklisted from " + serverName + ": " + blacklist.reason()));
        });
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        storage.profile(player.getUniqueId(), player.getUsername()).lastServer(event.getServer().getServerInfo().getName());
        storage.saveAll();
        storage.activeWatchesFor(player.getUniqueId()).forEach(watch -> broadcastStaff(
                CommandUtil.prefix()
                        .append(CommandUtil.action("Watched player moved: "))
                        .append(CommandUtil.name(player.getUsername()))
                        .append(CommandUtil.detail(" -> " + event.getServer().getServerInfo().getName())),
                "staffrover.watch.alert"));
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = storage.profile(player.getUniqueId(), player.getUsername());
        profile.lastLogoutAt(Instant.now());
        player.getCurrentServer().ifPresent(connection -> profile.lastServer(connection.getServerInfo().getName()));
        storage.saveAll();
    }

    public Optional<PlayerProfile> findProfile(String name) {
        Optional<Player> online = server.getPlayer(name);
        if (online.isPresent()) {
            return Optional.of(storage.profile(online.get().getUniqueId(), online.get().getUsername()));
        }
        return storage.profileByName(name);
    }

    public String senderName(CommandSource source) {
        return source instanceof Player player ? player.getUsername() : "Console";
    }

    public UUID senderUuid(CommandSource source) {
        return source instanceof Player player ? player.getUniqueId() : null;
    }

    public String luckPermsPrefix(Player player) {
        if (luckPerms == null) {
            try {
                Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                luckPerms = providerClass.getMethod("get").invoke(null);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                return "";
            }
        }
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());
            if (user == null) {
                return "";
            }
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            Object prefix = metaData.getClass().getMethod("getPrefix").invoke(metaData);
            return prefix == null ? "" : prefix.toString();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return "";
        }
    }

    public void broadcastStaff(Component message, String permission) {
        server.getAllPlayers().stream()
                .filter(player -> player.hasPermission(permission) || player.hasPermission("staffrover.staff"))
                .forEach(player -> player.sendMessage(message));
        server.getConsoleCommandSource().sendMessage(message);
    }

    public int evasionRisk(PlayerProfile profile) {
        int risk = 0;
        for (PlayerProfile linked : storage.linkedProfiles(profile)) {
            boolean hasBan = storage.punishmentsFor(linked.uuid()).stream()
                    .anyMatch(punishment -> punishment.type().equalsIgnoreCase("BAN")
                            || punishment.type().equalsIgnoreCase("TEMPBAN"));
            if (hasBan) {
                risk += profile.lastIp().equals(linked.lastIp()) ? 60 : 25;
                if (profile.registeredAt() != null && linked.lastLogoutAt() != null
                        && profile.registeredAt().isAfter(linked.lastLogoutAt().minusSeconds(3600))) {
                    risk += 20;
                }
            }
        }
        if (profile.registeredAt() != null && profile.registeredAt().isAfter(Instant.now().minusSeconds(86400))) {
            risk += 10;
        }
        return Math.min(risk, 100);
    }

    public boolean toggleStaffMode(Player player) {
        if (!staffModePlayers.add(player.getUniqueId())) {
            staffModePlayers.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean staffMode(Player player) {
        return staffModePlayers.contains(player.getUniqueId());
    }

    public boolean toggleVanish(Player player) {
        if (!vanishedPlayers.add(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean vanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean toggleFrozen(Player player) {
        if (!frozenPlayers.add(player.getUniqueId())) {
            frozenPlayers.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean frozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public void applyPunishmentAction(Punishment punishment) {
        server.getPlayer(punishment.target()).ifPresent(player -> {
            Component message = CommandUtil.error(punishment.reason());
            if (punishment.type().equalsIgnoreCase("KICK")
                    || punishment.type().equalsIgnoreCase("BAN")
                    || punishment.type().equalsIgnoreCase("TEMPBAN")
                    || punishment.type().equalsIgnoreCase("IPBAN")
                    || punishment.type().equalsIgnoreCase("TEMPIPBAN")
                    || punishment.type().equalsIgnoreCase("FREEZE")) {
                player.disconnect(message);
                return;
            }
            if (punishment.type().equalsIgnoreCase("WARN") || punishment.type().equalsIgnoreCase("MUTE")) {
                player.sendMessage(CommandUtil.error(punishment.reason()));
            }
        });
    }
}
