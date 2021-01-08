package moe.tristan.kmdah.configuration;

import java.lang.reflect.Field;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.stereotype.Component;

@Component
public class JettyLoomCustomizer implements JettyServerCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyLoomCustomizer.class);

    @Override
    public void customize(Server server) {
        try {
            Field tp = server.getClass().getDeclaredField("_threadPool");
            tp.setAccessible(true);
            tp.set(server, new JettyLoomThreadPool());
            LOGGER.info("Set Jetty threadpool to Project Loom");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
