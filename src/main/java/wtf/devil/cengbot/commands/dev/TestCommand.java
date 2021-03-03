package wtf.devil.cengbot.commands.dev;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.utils.modules.Parsers;
import wtf.devil.cengbot.utils.database.UserDatabase;

public class TestCommand {

    public TestCommand(MessageCreateEvent event, String[] params, long startTimestamp) {
        MessageAuthor author = event.getMessage().getAuthor();
        //event.getChannel().sendMessage("Empty test");
        /*DiscordApi api = new DiscordApi();

        User user = event.getMessage().getAuthor().asUser().get();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Bot Info")
                .addField("Shard", api.getCurrentShard(), true)
                .addField("Name + Discriminator", , true)
                .addField("User Id", , true);

        event.getChannel().sendMessage(embed)
                .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));*/
        //event.getChannel().addMessageCreateListener()
        //user.addMessageCreateListener(...).removeAfter(15, SECONDS).addRemoveHandler(...)

        //user.addMessageCreateListener(...).removeAfter(15, TimeUnit.SECONDS).addRemoveHandler(() -> event.getChannel().sendMessage("Proceeding to the next step!"));

        //long cash;
        try {
            //long cash = UserDatabase.getCash(author.getId());
            //String cash = UserDatabase.getCash(author.getId());
            //event.getChannel().sendMessage("Your SQL DB Cash pile is at: " + cash);
            boolean isAlive = UserDatabase.doesUserExistInDB(author.getId());

            //event.getChannel().sendMessage("Are you in the DB? " + isAlive);
            if (!isAlive) {
                UserDatabase.createUserInDB(author.getId());
            }

            //if (params.length > 0) {
                //long value = Parsers.parseStringToLong(params[0]);
                //UserDatabase.setValue(author.getId(), "cash", String.valueOf(value));
            //}

            event.getChannel().sendMessage("You have $ " + Constants.numFormatter.format(UserDatabase.getLong(author.getId(), "cash")));



        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            //event.getChannel().sendMessage("TIME TOOK: " + (Calendar.getInstance().getTimeInMillis() - startTimestamp) + "ms");
        }
    }
}
