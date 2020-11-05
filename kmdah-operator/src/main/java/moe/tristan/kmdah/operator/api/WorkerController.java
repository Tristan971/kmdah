package moe.tristan.kmdah.operator.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.common.internal.Worker;
import moe.tristan.kmdah.operator.service.workers.WorkerPoolService;

@RestController
public class WorkerController {

    private final WorkerPoolService workerPoolService;

    public WorkerController(WorkerPoolService workerPoolService) {
        this.workerPoolService = workerPoolService;
    }

    @PutMapping("/worker")
    public void register(@RequestBody Worker worker) {
        workerPoolService.registerWorker(worker);
    }

    @DeleteMapping("/worker")
    public void unregister(@RequestBody Worker worker) {
        workerPoolService.unregisterWorker(worker);
    }

}
