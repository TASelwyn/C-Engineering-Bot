package wtf.devil;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public class Main {

    public static void main(String[] args) {
        final DiscordClient client = DiscordClient.create("TOKEN HERE");
        final GatewayDiscordClient gateway = client.login().block();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            if ("!sigfig".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();
                channel.createMessage("#FuckSigFigRules!").block();
            }
            if ("!bry".equals(message.getContent())) {
                final MessageChannel channel = message.getChannel().block();
                for (int i = 0; i < 5; i++) {
                    channel.createMessage("TEST: " + message.getAuthor().toString());
                    channel.createMessage("SPAM <@166022239815204864> ;)").block();
                }
            }
        });

        gateway.onDisconnect().block();
    }
}
