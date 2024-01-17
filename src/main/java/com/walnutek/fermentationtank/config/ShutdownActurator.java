package com.walnutek.fermentationtank.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.exec-init-script", havingValue = "true")
public class ShutdownActurator implements ApplicationContextAware  {

	private ApplicationContext context;

	@Autowired
	private InitService initService;

	@Scheduled(initialDelay = 5000L, fixedDelay = 1000L)
	public void shutdown() {
		initService.init();
		int exitCode = SpringApplication.exit(context, () -> 0);
		System.exit(exitCode);
	}

	@Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }

}
