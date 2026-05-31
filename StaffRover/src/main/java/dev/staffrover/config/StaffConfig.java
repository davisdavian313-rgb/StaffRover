package dev.staffrover.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StaffConfig {
    private final Path dataDirectory;
    private final Path configPath;

    public StaffConfig(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.yml");
    }

    public void loadOrCreate() throws IOException {
        Files.createDirectories(dataDirectory);
        if (!Files.exists(configPath)) {
            Files.writeString(configPath, defaults(), StandardCharsets.UTF_8);
        }
    }

    public Path configPath() {
        return configPath;
    }

    private String defaults() {
        return """
                # STAFF configuration
                # This file is generated once. Restart the proxy after editing.

                branding:
                  name: "STAFF"
                  prefix: "[STAFF]"
                  primary-color: "#31D7FF"
                  accent-color: "#FFD166"
                  success-color: "#7CFFB2"
                  error-color: "#FF5C7A"
                  muted-color: "#A7B0C0"

                staff-chat:
                  enabled: true
                  show-luckperms-prefix: true
                  allow-legacy-colors: true
                  allow-hex-colors: true
                  allow-gradients: true
                  format: "[SC] {prefix}{staff} [{server}] {tags} > {message}"

                gui:
                  enabled: true
                  command: "staffgui"
                  target-placeholder: "<player>"
                  panels:
                    main: true
                    punish: true
                    reports: true
                    lookup: true
                    tools: true
                    recreation: true

                staffhub:
                  primary-server: "staffhub"
                  fallback-server: "hub"

                punishment-commands:
                  ban-default-duration: "permanent"
                  tempban-requires-duration: true
                  mute-requires-duration: true
                  teamipban-requires-duration: true

                blacklist:
                  server-scoped: true
                  disconnect-if-current-server: true

                evidence:
                  require-for-cheating: true
                  allow-bypass-permission: "staffrover.evidence.bypass"

                ban-evasion:
                  alert-risk-threshold: 50
                  high-risk-threshold: 75
                  new-account-risk-points: 10
                  exact-ip-risk-points: 60
                  shared-ip-risk-points: 25
                  timing-risk-points: 20
                """;
    }
}
