package com.symphony.hackathon.gs3;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;

public class TodoBotConfiguration extends Configuration {

    @NotEmpty
    private String sessionAuthURL;

    @NotEmpty
    private String botUser;

    @NotEmpty
    private String keyAuthUrl;
    @NotEmpty
    private String localKeystorePath;
    @NotEmpty
    private String localKeystorePassword;
    @NotEmpty
    private String botCertPath;
    @NotEmpty
    private String botCertPassword;
    @NotEmpty
    private String botEmailAddress;

    @NotEmpty
    private String agentAPIEndpoint;
    @NotEmpty
    private String podAPIEndpoint;

    public String appAuthBase;

    public String appAuthPath;

    public String appCertPath;

    public String appCertPassword;

    public String symphCertBaseURL;

    public String symphCertPathURL;

    public String getSymphCertBaseURL() {
        return symphCertBaseURL;
    }

    public void setSymphCertBaseURL(String symphCertBaseURL) {
        this.symphCertBaseURL = symphCertBaseURL;
    }

    public String getSymphCertPathURL() {
        return symphCertPathURL;
    }

    public void setSymphCertPathURL(String symphCertPathURL) {
        this.symphCertPathURL = symphCertPathURL;
    }

    public String getAppCertPath() {
        return appCertPath;
    }

    public void setAppCertPath(String appCertPath) {
        this.appCertPath = appCertPath;
    }

    public String getAppCertPassword() {
        return appCertPassword;
    }

    public void setAppCertPassword(String appCertPassword) {
        this.appCertPassword = appCertPassword;
    }

    public String getAppAuthBase() {
        return appAuthBase;
    }

    public void setAppAuthBase(String appAuthBase) {
        this.appAuthBase = appAuthBase;
    }

    public String getAppAuthPath() {
        return appAuthPath;
    }

    public void setAppAuthPath(String appAuthPath) {
        this.appAuthPath = appAuthPath;
    }

    @NotNull
    private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

    @JsonProperty
    public String getSessionAuthURL() {
        return sessionAuthURL;
    }

    @JsonProperty("sessionAuthURL")
    public void setSessionAuthURL(String sessionAuthURL) {
        this.sessionAuthURL = sessionAuthURL;
    }

    @JsonProperty
    public String getKeyAuthUrl() {
        return keyAuthUrl;
    }

    @JsonProperty("keyAuthUrl")
    public void setKeyAuthUrl(String keyAuthUrl) {
        this.keyAuthUrl = keyAuthUrl;
    }

    @JsonProperty
    public String getLocalKeystorePath() {
        return localKeystorePath;
    }

    @JsonProperty("localKeystorePath")
    public void setLocalKeystorePath(String localKeystorePath) {
        this.localKeystorePath = localKeystorePath;
    }

    @JsonProperty
    public String getLocalKeystorePassword() {
        return localKeystorePassword;
    }

    @JsonProperty("localKeystorePassword")
    public void setLocalKeystorePassword(String localKeystorePassword) {
        this.localKeystorePassword = localKeystorePassword;
    }

    @JsonProperty
    public String getBotCertPath() {
        return botCertPath;
    }

    @JsonProperty("botCertPath")
    public void setBotCertPath(String botCertPath) {
        this.botCertPath = botCertPath;
    }

    @JsonProperty
    public String getBotCertPassword() {
        return botCertPassword;
    }

    @JsonProperty("botCertPassword")
    public void setBotCertPassword(String botCertPassword) {
        this.botCertPassword = botCertPassword;
    }

    @JsonProperty
    public String getBotEmailAddress() {
        return botEmailAddress;
    }

    @JsonProperty("botEmailAddress")
    public void setBotEmailAddress(String botEmailAddress) {
        this.botEmailAddress = botEmailAddress;
    }

    @JsonProperty
    public String getAgentAPIEndpoint() {
        return agentAPIEndpoint;
    }

    @JsonProperty("agentAPIEndpoint")
    public void setAgentAPIEndpoint(String agentAPIEndpoint) {
        this.agentAPIEndpoint = agentAPIEndpoint;
    }

    @JsonProperty
    public String getPodAPIEndpoint() {
        return podAPIEndpoint;
    }

    @JsonProperty("podAPIEndpoint")
    public void setPodAPIEndpoint(String podAPIEndpoint) {
        this.podAPIEndpoint = podAPIEndpoint;
    }

    @JsonProperty
    public String getBotUser() {
        return botUser;
    }
    @JsonProperty("botUser")
    public void setBotUser(String botUser) {
        this.botUser = botUser;
    }
}
