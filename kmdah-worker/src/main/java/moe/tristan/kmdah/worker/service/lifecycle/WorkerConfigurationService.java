package moe.tristan.kmdah.worker.service.lifecycle;

import org.springframework.stereotype.Service;

@Service
public class WorkerConfigurationService {

    private String imageServer;

    public String getImageServer() {
        return imageServer;
    }

    public void setImageServer(String imageServer) {
        this.imageServer = imageServer;
    }

}
