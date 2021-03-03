package wtf.devil.cengbot.utils.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wtf.devil.cengbot.Main;
import wtf.devil.cengbot.utils.database.UserDatabase;

import java.sql.SQLException;

public class Economy {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public long getCash(long discordID) {
        try {
            return UserDatabase.getLong(discordID, "cash");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return 0L;
    }

    public long getBank(long discordID) {
        try {
            return UserDatabase.getLong(discordID, "bank");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return 0L;
    }

    public double getMultiplier(long discordID) {
        try {
            return UserDatabase.getDouble(discordID, "multiplier");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return 1;
    }

    public int getLevel(long discordID) {
        try {
            return UserDatabase.getInt(discordID, "level");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public int getVaultLevel(long discordID) {
        try {
            return UserDatabase.getInt(discordID, "vault_level");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return 0;
    }


    public void setCash(long discordID, long cash) {
        try {
            UserDatabase.setValue(discordID, "cash", String.valueOf(cash));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setBank(long discordID, long bank) {
        try {
            UserDatabase.setValue(discordID, "bank", String.valueOf(bank));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setMultiplier(long discordID, double multiplier) {
        try {
            UserDatabase.setValue(discordID, "multiplier", String.valueOf(multiplier));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean doesUserExist(long discordID) {
        return UserDatabase.doesUserExistInDB(discordID);
    }

    public boolean healthCheck(long discordID) {
        return UserDatabase.healthCheck(discordID);
    }

    public void createNewUser(long discordID) {
        try {
            UserDatabase.createUserInDB(discordID);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public long getMaxDepositAmount(long discordID) {
        return getMaxVaultHoldings(discordID) - getBank(discordID);
    }

    public long getMaxVaultHoldings(long discordID) {

        /*int vaultLevel = getVaultLevel(discordID);
        // TODO
        switch (vaultLevel) {
            case 1:
                return 500000;
            case 2:
                return 1000000;
            case 3:
                return 1500000;
            case 4:
                return 2500000;
        }*/
        return (long) getVaultLevel(discordID) * 500000L;
    }

    public double getVaultUsedPercentage(long discordID) {
        double bank = (double) getBank(discordID);
        double vaultHoldings = (double) getMaxVaultHoldings(discordID);
        return bank / vaultHoldings;
    }

    public void depositBalance(long discordID, long moneyToDeposit) {

        if (moneyToDeposit > getMaxDepositAmount(discordID)) {
            moneyToDeposit = getMaxDepositAmount(discordID);
        }

        if (getCash(discordID) < moneyToDeposit) {
            moneyToDeposit = getCash(discordID);
        }

        removeCash(discordID, moneyToDeposit);
        addBank(discordID, moneyToDeposit);
    }

    public void depositMax(long discordID) {
        depositBalance(discordID, getMaxDepositAmount(discordID));
    }

    public void withdrawBalance(long discordID, long moneyToWithdraw) {
        removeBank(discordID, moneyToWithdraw);
        addCash(discordID, moneyToWithdraw);
    }

    public void withdrawMax(long discordID) {
        withdrawBalance(discordID, getBank(discordID));
    }

    public void payCash(long payeeDiscordID, long discordID, long cashToPay) {
        if (healthCheck(payeeDiscordID) && healthCheck(discordID)) {
            removeCash(payeeDiscordID, cashToPay);
            addCash(discordID, cashToPay);
        }
    }

    public void addCash(long discordID, long moneyToAdd) {
        setCash(discordID, getCash(discordID) + moneyToAdd);
    }

    public void addBank(long discordID, long moneyToAdd) {
        setBank(discordID, getBank(discordID) + moneyToAdd);
    }

    public void removeCash(long discordID, long moneyToRemove) {
        setCash(discordID, getCash(discordID) - moneyToRemove);
    }

    public void removeBank(long discordID, long moneyToRemove) {
        setBank(discordID, getBank(discordID) - moneyToRemove);
    }

    public void robUser(long callerDiscordID, long robbedDiscordID, long robbedAmount) {
        if (healthCheck(callerDiscordID) && healthCheck(robbedDiscordID)) {
            removeCash(robbedDiscordID, robbedAmount);
            addCash(callerDiscordID, robbedAmount);
        }
    }
}
