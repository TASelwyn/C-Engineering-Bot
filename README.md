# C-Engineering Bot
A discord bot for the C-Engineering Chill discord server, mainly created to just mess around/learn jdbc.
This is in no way a good bot, as it was not designed to be.

Made by Thomas Selwyn ([Devil#1337](https://devil.wtf/discord))

## Features

The bot currently supports the following commands:

The default prefix is `c.`, however that can be customized.
Example usage: c.pay @Thomas 500k

### General
- **`help`**
  Lists all available commands and how to use them if you follow the command with another command.
- **`info`**
  Shows some basic information (id, name, etc.) about the user who used this command. Alternatively, you can @ mention someone to get their information.

### Economy
- **`balance`**
  Finds the user's balance (cash & in their bank) for the bot's economy features.
- **`beg`**
  Pretend to be a beggar and get some cash.
- **`rich`**
  Sees the leaderboard of richest people on the server.
- **`rob`**
  Attempts to rob the mentioned user.
- **`deposit`**
  Deposit your cash into your bank's vault.
- **`withdraw`**
  Withdraw money from your vault into cash.
- **`pay`**
  Give a user some cash.
  
### Cheat/Admin Commands
- **`cheat`**
  Cheats in money to a users cash or bank reserves.
  
## Running the bot for testing

To run the bot right from Gradle (just for testing, not for production) you can
do `gradlew run`. You can view the login process by looking at the
[Main.java](https://github.com/TASelwyn/C-Engineering-Bot/blob/master/src/main/java/wtf/devil/cengbot/Main.java)
class.

## Building the bot for production

To get a distributable package you run `gradlew distZip`. The created zip is located
at `build/distributions/cengbot-0.0.4-DEV.zip` and contains all necessary things to run the bot, except the token. The token goes in `data/config.json`, which get's generated on first launch. 
Take a look at the [build.gradle.kts](https://github.com/TASelwyn/C-Engineering-Bot/blob/master/build.gradle.kts) file for additional build parameters. 

## Running the bot for production

After you built the distributable package as described in the previous section, you can copy over the zip file to where
you want to run your bot. There you unzip it where ever you like and run one of the included start scripts.

```shell
unzip cengbot-0.0.4-DEV.zip
cd cengbot-0.0.4-DEV
./cengbot
```

After starting, you'll need to edit `data/config.json` to include your discord bot's access token.
Then restart the bot. A log file will be created in the `data/logs` directory where you execute the last command.
