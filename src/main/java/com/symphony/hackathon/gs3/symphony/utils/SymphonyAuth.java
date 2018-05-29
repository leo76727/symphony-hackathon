package com.symphony.hackathon.gs3.symphony.utils;


import com.symphony.hackathon.gs3.TodoBotConfiguration;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.symphony.clients.AuthenticationClient;


public class SymphonyAuth {

    public SymphonyClient init(TodoBotConfiguration config) throws Exception{

        SymphonyClient symClient;

        symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.V4);

        AuthenticationClient authClient = new AuthenticationClient(config.getSessionAuthURL(), config.getKeyAuthUrl());

        System.setProperty("javax.net.ssl.keyStore", config.getBotCertPath());
        System.setProperty("javax.net.ssl.keyStorePassword", config.getBotCertPassword());
        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
        SymAuth symAuth = authClient.authenticate();


        //Set agent and pod clients if custom are needed


        SymphonyClientConfig symphonyClientConfig = new SymphonyClientConfig(false);
        symphonyClientConfig.set(SymphonyClientConfigID.AGENT_URL, config.getAgentAPIEndpoint());
        symphonyClientConfig.set(SymphonyClientConfigID.POD_URL,config.getPodAPIEndpoint());

        symClient.init(symAuth,symphonyClientConfig);

        return symClient;
    }
}
