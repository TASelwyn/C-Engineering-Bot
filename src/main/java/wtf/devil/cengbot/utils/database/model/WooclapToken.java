package wtf.devil.cengbot.utils.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wooclap_tokens")
public class WooclapToken {

    @Id
    @Column(name = "discord_id")
    private long discordId;

    @Getter
    @Setter
    @Column(name = "auth_token", nullable = false, columnDefinition = "text")
    private String authToken;

    protected WooclapToken() {
        // for Hibernate
    }

    public WooclapToken(long discordId, String authToken) {
        this.discordId = discordId;
        this.authToken = authToken;
    }

    // Never print the token itself
    @Override
    public String toString() {
        return "WooclapToken[discordId=" + discordId + "]";
    }
}
