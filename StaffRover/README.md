# STAFF

STAFF is a Velocity proxy staff plugin for reports, punishments, evidence, ban-evasion checks, player lookup, and staff utilities.

## Commands

- `/punish <player>` opens the section/offense punishment menu.
- `/ban <player> <reason>`
- `/tempban <player> <duration> <reason>`
- `/mute <player> <duration> <reason>`
- `/warn <player> <reason>`
- `/banip <player> <reason>`
- `/teamipban <player> <duration> <reason>` also has `/tempipban`.
- `/blacklist <player> <reason>` blocks the player from joining the server the staff member is currently on.
- `/punishment revoke <id>` revokes a logged punishment.
- `/appeal <punishmentId> <upheld|reduced|accepted|denied>` marks appeal status.
- `/unban <player>`, `/unmute <player>`, `/unwarn <player>` revoke the latest matching active punishment.
- `/report <player> <reason>` files a proxy-wide report.
- `/reports` lists open reports.
- `/reportclaim <id>` claims a report.
- `/reportclose <id> <reason>` closes a report.
- `/evidence add <player> <url> [note]`
- `/evidence view <player>`
- `/whois <player>` shows registered date, last login, last logout, server, reports, evidence, punishments, and linked accounts.
- `/seen <player>` shows a quick last-seen line.
- `/alts <player>` shows accounts sharing known IPs.
- `/evasion <player>` calculates possible ban-evasion risk.
- `/history <player>` lists punishments.
- `/case <player>` shows a full investigation summary.
- `/appealinfo <player>` shows punishment/evidence details for appeal review.
- `/notes <player>` lists staff notes.
- `/note add <player> <note>` adds a staff note.
- `/freeze <player>` toggles a proxy-side freeze state and staff alert.
- `/watch <player> <reason>` alerts staff when a player joins or changes servers.
- `/watch remove <player>` removes an active watch.
- `/staffgui [panel] [player]` opens the clickable STAFF chat GUI.
- `/staffchat <message>` sends a staff-only proxy chat message.
- `/staffbroadcast <message>` sends a staff notice.
- `/staffmode` toggles staff mode state.
- `/staffhub` sends staff to the `staffhub` server, or `hub` if `staffhub` does not exist.
- `/vanish` toggles proxy-side vanish state.
- `/stafflist` lists online staff.
- `/staffstats [onlineStaff]` shows staff activity stats.
- `/staffroll [max]` rolls a staff-only random number.
- `/staffcoinflip` flips a staff-only coin.
- `/auditlog` shows recent STAFF actions.
- `/staffhelp` lists STAFF commands.

## Notes

Velocity alone cannot open Bukkit inventory menus. The first `/punish` implementation is a clickable proxy chat menu with categories, offenses, confirmation, evidence checks, escalation tiers, and command execution. A Paper companion can be added later for a true inventory GUI.

`/staffgui` is also a clickable proxy chat GUI. For a real chest/inventory GUI, add a Paper companion plugin on backend servers and communicate with this proxy plugin over plugin messaging.

Velocity alone also cannot fully hide a player inside backend Paper servers. `/vanish` tracks proxy-side vanish state and staff notifications; true in-game vanish should be backed by a Paper companion or an existing backend vanish plugin.

The same is true for full movement freezing and inventory-based staff mode items. The proxy plugin tracks state and alerts staff, while a Paper companion can enforce movement, inventory, vanish visibility, and real chest GUIs.

Staff chat supports LuckPerms prefixes when LuckPerms is installed. It also supports restrained color codes like `&b`, `&#31D7FF`, and full-message gradients in the form `<gradient:#31D7FF:#FFD166>message</gradient>`.
