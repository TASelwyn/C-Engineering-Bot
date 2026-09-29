package wtf.devil.cengbot.commands.dev;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.utils.database.repo.UserRepo;

public class TestCommand {

    private static final UserRepo userRepo = new UserRepo();

    public TestCommand(MessageReceivedEvent event, String[] params) {
        User author = event.getAuthor();
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
            //event.getChannel().sendMessage("Are you in the DB? " + userRepo.existsById(author.getIdLong()));

            //if (params.length > 0) {
                //long value = Parsers.parseStringToLong(params[0]);
                //UserDatabase.setValue(author.getId(), "cash", String.valueOf(value));
            //}

            event.getChannel().sendMessage("You have $ " + Constants.numFormatter.format(userRepo.findOrCreate(author.getIdLong()).getCash())).queue();



        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
