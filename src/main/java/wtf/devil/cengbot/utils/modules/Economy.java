package wtf.devil.cengbot.utils.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.DevilsBot;
import wtf.devil.cengbot.utils.database.model.BotUser;
import wtf.devil.cengbot.utils.database.repo.UserRepo;

public class Economy {

    private static final Logger logger = LogManager.getLogger(DevilsBot.class);
    private static final UserRepo userRepo = new UserRepo();

    public long getCash(long discordID) {
        return userRepo.findOrCreate(discordID).getCash();
    }

    public long getBank(long discordID) {
        return userRepo.findOrCreate(discordID).getBank();
    }

    public double getMultiplier(long discordID) {
        return userRepo.findOrCreate(discordID).getMultiplier();
    }

    public int getLevel(long discordID) {
        return userRepo.findOrCreate(discordID).getLevel();
    }

    public int getVaultLevel(long discordID) {
        return userRepo.findOrCreate(discordID).getVaultLevel();
    }


    public void setCash(long discordID, long cash) {
        userRepo.update(discordID, user -> user.setCash(cash));
    }

    public void setBank(long discordID, long bank) {
        userRepo.update(discordID, user -> user.setBank(bank));
    }

    public void setMultiplier(long discordID, double multiplier) {
        userRepo.update(discordID, user -> user.setMultiplier(multiplier));
    }

    public boolean doesUserExist(long discordID) {
        return userRepo.existsById(discordID);
    }

    public boolean healthCheck(long discordID) {
        try {
            userRepo.findOrCreate(discordID);
            return true;
        } catch (RuntimeException exception) {
            logger.error("(" + discordID + "-DB) health check failed", exception);
            return false;
        }
    }

    public void createNewUser(long discordID) {
        userRepo.findOrCreate(discordID);
    }

    public long getMaxDepositAmount(long discordID) {
        return maxDepositAmount(userRepo.findOrCreate(discordID));
    }

    public long getMaxVaultHoldings(long discordID) {
        return maxVaultHoldings(userRepo.findOrCreate(discordID));
    }

    public double getVaultUsedPercentage(long discordID) {
        BotUser user = userRepo.findOrCreate(discordID);
        return (double) user.getBank() / (double) maxVaultHoldings(user);
    }

    public void depositBalance(long discordID, long moneyToDeposit) {
        userRepo.update(discordID, user -> {
            long amount = Math.min(moneyToDeposit, Math.min(maxDepositAmount(user), user.getCash()));
            user.setCash(user.getCash() - amount);
            user.setBank(user.getBank() + amount);
        });
    }

    public void depositMax(long discordID) {
        depositBalance(discordID, Long.MAX_VALUE);
    }

    public void withdrawBalance(long discordID, long moneyToWithdraw) {
        userRepo.update(discordID, user -> {
            user.setBank(user.getBank() - moneyToWithdraw);
            user.setCash(user.getCash() + moneyToWithdraw);
        });
    }

    public void withdrawMax(long discordID) {
        userRepo.update(discordID, user -> {
            user.setCash(user.getCash() + user.getBank());
            user.setBank(0);
        });
    }

    public void payCash(long payeeDiscordID, long discordID, long cashToPay) {
        transferCash(payeeDiscordID, discordID, cashToPay);
    }

    public void addCash(long discordID, long moneyToAdd) {
        userRepo.update(discordID, user -> user.setCash(user.getCash() + moneyToAdd));
    }

    public void addBank(long discordID, long moneyToAdd) {
        userRepo.update(discordID, user -> user.setBank(user.getBank() + moneyToAdd));
    }

    public void removeCash(long discordID, long moneyToRemove) {
        addCash(discordID, -moneyToRemove);
    }

    public void removeBank(long discordID, long moneyToRemove) {
        addBank(discordID, -moneyToRemove);
    }

    public void robUser(long callerDiscordID, long robbedDiscordID, long robbedAmount) {
        transferCash(robbedDiscordID, callerDiscordID, robbedAmount);
    }

    private static void transferCash(long fromDiscordID, long toDiscordID, long amount) {
        userRepo.update(fromDiscordID, toDiscordID, (from, to) -> {
            from.setCash(from.getCash() - amount);
            to.setCash(to.getCash() + amount);
        });
    }

    private static long maxVaultHoldings(BotUser user) {
        return (long) user.getVaultLevel() * 500000L;
    }

    private static long maxDepositAmount(BotUser user) {
        return maxVaultHoldings(user) - user.getBank();
    }
}
