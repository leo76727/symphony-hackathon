# TODO Bot

Edit server.yml, replacing the bot username with your own user, and pointing to the correct location for the p12 certs.
```
git clone https://github.com/paulcreasey/symphony-hackathon.git
cd symphony-hackathon
mvn clean install
```

The run with arguments `server server.yml`

Try talking to the bot

see `com.symphony.hackathon.gs3.bot.TodoBot.onChatMessage` for details