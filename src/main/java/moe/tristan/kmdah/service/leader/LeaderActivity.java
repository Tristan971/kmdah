package moe.tristan.kmdah.service.leader;

import java.time.Duration;

public interface LeaderActivity extends Runnable {

    String getName();

    Duration getInitialDelay();

    Duration getPeriod();

}
