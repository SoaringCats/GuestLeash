package ch.jamiete.guestleash;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LeashSession {
    private final long creation;
    private final Player caller;
    private final Player guest;
    private Location previous;
    private final boolean forced;
    private boolean accepted;

    public LeashSession(final Player caller, final Player guest, final boolean forced) {
        this.creation = System.currentTimeMillis();
        this.caller = caller;
        this.guest = guest;
        this.forced = forced;
    }

    public void end() {
        this.guest.teleport(this.previous);
    }

    public Player getCaller() {
        return this.caller;
    }

    public long getCreation() {
        return this.creation;
    }

    public Player getGuest() {
        return this.guest;
    }

    public Location getPrevious() {
        return this.previous;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public boolean isForced() {
        return this.forced;
    }

    public void sendMessage(final String message) {
        this.caller.sendMessage(message);
        this.guest.sendMessage(message);
    }

    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

    public void setPrevious(final Location previous) {
        this.previous = previous;
    }

}
