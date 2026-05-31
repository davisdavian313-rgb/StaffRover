package dev.staffrover.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerProfile {
    private final UUID uuid;
    private String lastName;
    private Instant registeredAt;
    private Instant lastLoginAt;
    private Instant lastLogoutAt;
    private String lastServer = "unknown";
    private String firstIp = "unknown";
    private String lastIp = "unknown";
    private final Set<String> knownIps = new LinkedHashSet<>();

    public PlayerProfile(UUID uuid, String lastName) {
        this.uuid = uuid;
        this.lastName = lastName;
    }

    public UUID uuid() {
        return uuid;
    }

    public String lastName() {
        return lastName;
    }

    public void lastName(String lastName) {
        this.lastName = lastName;
    }

    public Instant registeredAt() {
        return registeredAt;
    }

    public void registeredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant lastLoginAt() {
        return lastLoginAt;
    }

    public void lastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant lastLogoutAt() {
        return lastLogoutAt;
    }

    public void lastLogoutAt(Instant lastLogoutAt) {
        this.lastLogoutAt = lastLogoutAt;
    }

    public String lastServer() {
        return lastServer;
    }

    public void lastServer(String lastServer) {
        this.lastServer = lastServer;
    }

    public String firstIp() {
        return firstIp;
    }

    public void firstIp(String firstIp) {
        this.firstIp = firstIp;
    }

    public String lastIp() {
        return lastIp;
    }

    public void lastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public Set<String> knownIps() {
        return knownIps;
    }
}
