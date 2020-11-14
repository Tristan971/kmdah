package moe.tristan.kmdah.operator.service.vacuum;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VacuumJob {

    private final VacuumService vacuumService;

    public VacuumJob(VacuumService vacuumService) {
        this.vacuumService = vacuumService;
    }

    // once per hour
    @Scheduled(fixedDelay = 3600 * 1000)
    public void execute() {
        vacuumService.vacuumUntilUnderCacheMaxSize();
    }

}
