package wtf.devil.cengbot.utils.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Wooclap answer order per user, lower answers first. Separate from the token so /deltoken keeps it.
@Entity
@Table(name = "wooclap_priorities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for Hibernate
public class WooclapPriority {

    @Id
    @Column(name = "discord_id")
    private long discordId;

    @Column(nullable = false)
    private int priority;

    public WooclapPriority(long discordId, int priority) {
        this.discordId = discordId;
        this.priority = priority;
    }
}
