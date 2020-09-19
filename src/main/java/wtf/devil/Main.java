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
            final MessageChannel channel = message.getChannel().block();

            if ("!ping".equals(message.getContent())) {

                long originalMsgTimestamp = message.getTimestamp().toEpochMilli();

                final Message pong = channel.createMessage("Pong!").block();
                // TODO
                // Take msg after it's sent, compare timestamps.
                // Then edit the msg that's sent, edit in the timestamp difference.
                // Pong! 123ms
            }

            if ("sigfig".contains(message.getContent())) {
                channel.createMessage("#FuckSigFigRules!").block();
            }

            if ("!bry".equals(message.getContent())) {

                for (int i = 0; i < 10; i++) {
                    // Just a Bry spammer LOL (Sorry dude)
                    channel.createMessage("SPAM <@166022239815204864> ;)").block();
                }
            }
        });

        gateway.onDisconnect().block();
    }
}
