package wtf.devil.cengbot.utils.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// No @Data: its equals/hashCode/toString don't play well with Hibernate entities
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for Hibernate
public class BotUser {

    @Id
    @Column(name = "discord_id")
    private long discordId;

    @Setter
    private String nickname;

    @Setter
    @Column(nullable = false)
    private long cash = 0;

    @Setter
    @Column(nullable = false)
    private long bank = 0;

    @Setter
    @Column(nullable = false)
    private double multiplier = 1;

    @Column(nullable = false)
    private int level = 1;

    @Column(name = "vault_level", nullable = false)
    private int vaultLevel = 1;

    public BotUser(long discordId) {
        this.discordId = discordId;
    }
}
