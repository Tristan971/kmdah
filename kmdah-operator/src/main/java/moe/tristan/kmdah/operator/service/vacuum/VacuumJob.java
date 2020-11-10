package moe.tristan.kmdah.operator.service.vacuum;

import org.springframework.stereotype.Component;

@Component
public class VacuumJob {

    private final VacuumService vacuumService;

    public VacuumJob(VacuumService vacuumService) {
        this.vacuumService = vacuumService;
    }

    //@Scheduled(fixedDelay = 15 * 60 * 1000)
    public void execute() {
        vacuumService.vacuumUntilUnderCacheMaxSize();
    }

}
