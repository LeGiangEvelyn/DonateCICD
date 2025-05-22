package com.leap.donate.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.servlet.SlackAppServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackAppServletConfig {

    @Bean
    public ServletRegistrationBean<SlackAppServlet> slackEventsServlet(App slackApp) {
        SlackAppServlet servlet = new SlackAppServlet(slackApp);
        return new ServletRegistrationBean<>(servlet, "/api/slack/events");
    }
} 