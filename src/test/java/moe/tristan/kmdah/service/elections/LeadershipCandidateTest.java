package moe.tristan.kmdah.service.elections;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import moe.tristan.kmdah.service.elections.CandidacySettings.CandidacyIdGenerationMethod;

class LeadershipCandidateTest {

    @Test
    void fromHostname() throws UnknownHostException {
        CandidacySettings candidacySettings = new CandidacySettings(
            CandidacyIdGenerationMethod.HOSTNAME,
            "static-id"
        );

        LeadershipCandidate leadershipCandidate = new LeadershipCandidate(candidacySettings);

        String expected = Inet4Address.getLocalHost().getHostName();
        assertThat(leadershipCandidate.getId()).isEqualTo(expected);
    }

    @Test
    void fromUuid() {
        UUID expected = UUID.randomUUID();
        try (MockedStatic<UUID> uuid = Mockito.mockStatic(UUID.class)) {
            //noinspection ResultOfMethodCallIgnored
            uuid.when(UUID::randomUUID).thenReturn(expected);

            CandidacySettings candidacySettings = new CandidacySettings(CandidacyIdGenerationMethod.RANDOM_UUID, "static-id");

            LeadershipCandidate leadershipCandidate = new LeadershipCandidate(candidacySettings);

            assertThat(leadershipCandidate.getId()).isEqualTo(expected.toString());
        }
    }

    @Test
    void fromStatic() {
        CandidacySettings candidacySettings = new CandidacySettings(
            CandidacyIdGenerationMethod.STATIC,
            "static-id"
        );

        LeadershipCandidate leadershipCandidate = new LeadershipCandidate(candidacySettings);

        assertThat(leadershipCandidate.getId()).isEqualTo("static-id");
    }

}
