package com.symphony.hackathon.gs3.symphony.utils;


import com.symphony.hackathon.gs3.TodoBotConfiguration;
import com.symphony.hackathon.gs3.symphony.model.AppAuthResponse;
import com.symphony.hackathon.gs3.symphony.model.PodCert;
import com.symphony.hackathon.gs3.symphony.model.VerifyRequest;
import org.glassfish.jersey.client.ClientConfig;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.symphonyoss.client.impl.CustomHttpClient;
import org.symphonyoss.symphony.authenticator.invoker.ApiClient;
import org.symphonyoss.symphony.authenticator.invoker.Configuration;
import org.symphonyoss.symphony.authenticator.invoker.Pair;

import javax.security.auth.login.LoginException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;

public class AppAuthClient {

    private TodoBotConfiguration config;
    private ApiClient apiClient;

    public AppAuthClient(TodoBotConfiguration config) {
        this.config = config;
        this.apiClient = Configuration.getDefaultApiClient();   }

    public AppAuthResponse authenticate(String podId) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        Client appAuthHttpClient = CustomHttpClient.getClient(config.getAppCertPath(),config.getAppCertPassword(),config.getLocalKeystorePath(),config.getLocalKeystorePassword(),clientConfig);
        apiClient.setBasePath(this.config.getAppAuthBase());

        Configuration.getDefaultApiClient().setHttpClient(appAuthHttpClient);

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        String localVarPostBody = "{\"appToken\":\""+randomUUIDString+"\"}";
        String localVarPath = this.config.getAppAuthPath();
        List<Pair> localVarQueryParams = new ArrayList();
        Map<String, String> localVarHeaderParams = new HashMap();
        Map<String, Object> localVarFormParams = new HashMap();
        String[] localVarAccepts = new String[]{"application/json"};
        String localVarAccept = this.apiClient.selectHeaderAccept(localVarAccepts);
        String[] localVarContentTypes = new String[0];
        String localVarContentType = this.apiClient.selectHeaderContentType(localVarContentTypes);
        String[] localVarAuthNames = new String[0];
        GenericType<AppAuthResponse> localVarReturnType = new GenericType<AppAuthResponse>() {};
        AppAuthResponse appToken = this.apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        resetToDefaultHTTPClient();
        return appToken;
    }

    public Object verify(VerifyRequest request) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        Client appAuthHttpClient = CustomHttpClient.getClient(config.getAppCertPath(),config.getAppCertPassword(),config.getLocalKeystorePath(),config.getLocalKeystorePassword(),clientConfig);
        apiClient.setBasePath(this.config.getSymphCertBaseURL());

        Configuration.getDefaultApiClient().setHttpClient(appAuthHttpClient);

        String localVarPostBody = null;
        String localVarPath = this.config.getSymphCertPathURL();
        List<Pair> localVarQueryParams = new ArrayList();
        Map<String, String> localVarHeaderParams = new HashMap();
        Map<String, Object> localVarFormParams = new HashMap();
        String[] localVarAccepts = new String[]{"application/json"};
        String localVarAccept = this.apiClient.selectHeaderAccept(localVarAccepts);
        String[] localVarContentTypes = new String[0];
        String localVarContentType = this.apiClient.selectHeaderContentType(localVarContentTypes);
        String[] localVarAuthNames = new String[0];
        GenericType<PodCert> localVarReturnType = new GenericType<PodCert>() {};
        PodCert podCert = this.apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        resetToDefaultHTTPClient();

        // Get the public key from the cert
        PublicKey publicKey;
        try {
            X509Certificate x509Certificate = SecurityKeyUtils.parseX509Certificate(podCert.getCertificate());
            publicKey = x509Certificate.getPublicKey();
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setVerificationKey(publicKey)
                    .setSkipAllValidators()
                    .build();

            // validate and decode the jwt
            JwtClaims jwtDecoded = jwtConsumer.processToClaims(request.getJwt());
            return jwtDecoded.getClaimValue("user");

            //String username = jwtDecoded.getStringClaimValue("username");
        } catch (GeneralSecurityException e) {
            throw new LoginException("Couldn't parse cert string from Symphony into X509Certificate object:" + e.getMessage());
        }

    }

    private void resetToDefaultHTTPClient() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        Client appAuthHttpClient = CustomHttpClient.getClient(config.getBotCertPath(),config.getBotCertPassword(),config.getLocalKeystorePath(),config.getLocalKeystorePassword(),clientConfig);
        apiClient.setBasePath(this.config.getAppAuthBase());

        Configuration.getDefaultApiClient().setHttpClient(appAuthHttpClient);
    }
}
