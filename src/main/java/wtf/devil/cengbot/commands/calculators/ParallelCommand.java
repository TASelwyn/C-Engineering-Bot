package wtf.devil.cengbot.commands.calculators;

import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.utils.modules.MathCalculators;

import java.text.DecimalFormat;

public class ParallelCommand {

    public ParallelCommand(MessageCreateEvent event, String[] params) {

        if (params.length >= 1) {
            String msg = String.join(" ", params);

            String[] resistors = msg.replaceAll("`", "").replace("||", " ").split(" ");

            double eqvResistance = new MathCalculators().parallelResistance(resistors);

            DecimalFormat df = new DecimalFormat("##.###"); //df.format(eqvResistance)
            event.getChannel().sendMessage("Equivalence Resistance: " + eqvResistance + " \u03A9"); // Ω
        } else {
            event.getChannel().sendMessage("Not sure what that was..... try `c.help parallel`");
        }


    }
}
