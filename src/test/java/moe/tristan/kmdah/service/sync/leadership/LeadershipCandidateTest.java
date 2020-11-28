package moe.tristan.kmdah.service.sync.leadership;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import moe.tristan.kmdah.service.sync.InstanceIdSettings;
import moe.tristan.kmdah.service.sync.InstanceIdSettings.IdGenerationMethod;

class LeadershipCandidateTest {

    @Test
    void fromHostname() throws UnknownHostException {
        InstanceIdSettings instanceIdSettings = new InstanceIdSettings(IdGenerationMethod.HOSTNAME);

        LeadershipCandidate leadershipCandidate = new LeadershipCandidate(instanceIdSettings);

        String expected = Inet4Address.getLocalHost().getHostName();
        assertThat(leadershipCandidate.getId()).isEqualTo(expected);
    }

    @Test
    void fromUuid() {
        UUID expected = UUID.randomUUID();
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            //noinspection ResultOfMethodCallIgnored
            uuid.when(UUID::randomUUID).thenReturn(expected);

            InstanceIdSettings instanceIdSettings = new InstanceIdSettings(IdGenerationMethod.RANDOM_UUID);

            LeadershipCandidate leadershipCandidate = new LeadershipCandidate(instanceIdSettings);

            assertThat(leadershipCandidate.getId()).isEqualTo(expected.toString());
        }
    }

}
