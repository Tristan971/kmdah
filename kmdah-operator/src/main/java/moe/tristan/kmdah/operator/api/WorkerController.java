package moe.tristan.kmdah.operator.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.common.internal.Worker;
import moe.tristan.kmdah.common.internal.WorkerConfig;
import moe.tristan.kmdah.common.internal.WorkerShutdownConfig;
import moe.tristan.kmdah.operator.service.workers.WorkerPoolService;

@RestController
public class WorkerController {

    private final WorkerPoolService workerPoolService;

    public WorkerController(WorkerPoolService workerPoolService) {
        this.workerPoolService = workerPoolService;
    }

    @PutMapping("/worker")
    public WorkerConfig heartbeat(@RequestBody Worker worker) {
        return workerPoolService.heartbeat(worker);
    }

    @DeleteMapping("/worker")
    public WorkerShutdownConfig disconnect(@RequestBody Worker worker) {
        return workerPoolService.disconnect(worker);
    }

}
