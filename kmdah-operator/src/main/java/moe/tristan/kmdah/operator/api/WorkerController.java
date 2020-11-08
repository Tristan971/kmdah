package moe.tristan.kmdah.operator.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.common.api.worker.Worker;
import moe.tristan.kmdah.common.api.worker.WorkerConfiguration;
import moe.tristan.kmdah.common.api.worker.WorkerShutdown;
import moe.tristan.kmdah.operator.service.workers.WorkerPool;

@RestController
public class WorkerController {

    private final WorkerPool workerPool;

    public WorkerController(WorkerPool workerPool) {
        this.workerPool = workerPool;
    }

    @PutMapping("/worker")
    public WorkerConfiguration heartbeat(@RequestBody Worker worker) {
        return workerPool.heartbeat(worker);
    }

    @DeleteMapping("/worker")
    public WorkerShutdown disconnect(@RequestBody Worker worker) {
        return workerPool.disconnect(worker);
    }

}
