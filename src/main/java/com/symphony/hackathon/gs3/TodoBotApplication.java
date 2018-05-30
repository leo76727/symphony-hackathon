package com.symphony.hackathon.gs3;

import com.symphony.hackathon.gs3.bot.TodoBot;
import com.symphony.hackathon.gs3.symphony.utils.SymphonyAuth;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.symphonyoss.client.SymphonyClient;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class TodoBotApplication extends Application<TodoBotConfiguration> {

    public static void main(String[] args) throws Exception {
        new TodoBotApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<TodoBotConfiguration> bootstrap) {
        bootstrap.setObjectMapper(JsonObjectMapper.get());
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );


    }

    @Override
    public void run(TodoBotConfiguration configuration, Environment environment) throws Exception {
        // Enable CORS headers
//        final FilterRegistration.Dynamic cors =
//                environment.servlets().addFilter("CORS", CrossOriginFilter.class);
//
//        // Configure CORS parameters
//        cors.setInitParameter("allowedOrigins", "*");
//        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
//        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("crossOriginRequsts", CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        SymphonyClient symClient = new SymphonyAuth().init(configuration);
        TodoBot bot = TodoBot.getInstance(symClient,configuration);
    }
}