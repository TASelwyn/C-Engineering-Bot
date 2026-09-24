package wtf.devil.cengbot.commands.calculators;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.modules.MathCalculators;

import java.text.DecimalFormat;

public class ParallelCommand {

    public ParallelCommand(MessageReceivedEvent event, String[] params) {

        if (params.length >= 1) {
            String msg = String.join(" ", params);

            String[] resistors = msg.replaceAll("`", "").replace("||", " ").split(" ");

            double eqvResistance = new MathCalculators().parallelResistance(resistors);

            DecimalFormat df = new DecimalFormat("##.###"); //df.format(eqvResistance)
            event.getChannel().sendMessage("Equivalence Resistance: " + eqvResistance + " Ω").queue(); // Ω
        } else {
            event.getChannel().sendMessage("Not sure what that was..... try `c.help parallel`").queue();
        }


    }
}
