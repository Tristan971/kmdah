package moe.tristan.kmdah.operator.service.workers;

import org.springframework.stereotype.Component;

@Component
public class WorkerConfigurationHolder {

    private String imageServer;

    public String getImageServer() {
        return imageServer;
    }

    public void setImageServer(String imageServer) {
        this.imageServer = imageServer;
    }

}
