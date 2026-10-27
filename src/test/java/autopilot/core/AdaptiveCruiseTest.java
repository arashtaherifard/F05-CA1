package autopilot.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static autopilot.core.AdaptiveCruise.Mode.ACCELERATE;
import static autopilot.core.AdaptiveCruise.Mode.BRAKE;
import static autopilot.core.AdaptiveCruise.Mode.EMERGENCY_BRAKE;
import static autopilot.core.AdaptiveCruise.Mode.HOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdaptiveCruiseTest {

    private static final double TOLERANCE = 1e-9;
    private static final double TARGET_SPEED_KMH = 120.0;

    static Stream<Arguments> updateScenarios() {
        return Stream.of(
                Arguments.of(
                        100.0,
                        25.0,
                        EMERGENCY_BRAKE,
                        100.0
                ),
                Arguments.of(
                        100.0,
                        40.0,
                        BRAKE,
                        20.0
                ),
                Arguments.of(
                        100.0,
                        50.0,
                        HOLD,
                        0.0
                ),
                Arguments.of(
                        100.0,
                        70.0,
                        ACCELERATE,
                        5.0
                ),
                Arguments.of(
                        118.0,
                        70.0,
                        ACCELERATE,
                        2.0
                ),
                Arguments.of(
                        120.0,
                        70.0,
                        HOLD,
                        0.0
                ),
                Arguments.of(
                        130.0,
                        70.0,
                        HOLD,
                        0.0
                )
        );
    }

    @ParameterizedTest
    @MethodSource("updateScenarios")
    void update_forDifferentDrivingConditions_returnsExpectedDecision(
            double speedKmh,
            double distanceMeters,
            AdaptiveCruise.Mode expectedMode,
            double expectedAmount
    ) {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        AdaptiveCruise.Decision decision =
                adaptiveCruise.update(speedKmh, distanceMeters);

        assertEquals(expectedMode, decision.mode());
        assertEquals(expectedAmount, decision.amount(), TOLERANCE);
    }

    @Test
    void update_afterEnteringBrakeMode_belowReleaseDistanceKeepsBraking() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        adaptiveCruise.update(100.0, 40.0);

        AdaptiveCruise.Decision decision =
                adaptiveCruise.update(100.0, 55.0);

        assertEquals(BRAKE, decision.mode());
        assertEquals(10.0, decision.amount(), TOLERANCE);
    }

    @Test
    void update_afterEnteringBrakeMode_atReleaseDistanceReleasesBrake() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        adaptiveCruise.update(100.0, 40.0);

        AdaptiveCruise.Decision decision =
                adaptiveCruise.update(100.0, 60.0);

        assertEquals(ACCELERATE, decision.mode());
        assertEquals(5.0, decision.amount(), TOLERANCE);
    }

    @Test
    void safeDistanceFor_returnsHalfOfSpeed() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        double safeDistance =
                adaptiveCruise.safeDistanceFor(100.0);

        assertEquals(50.0, safeDistance, TOLERANCE);
    }

    @Test
    void safeDistanceFor_withNegativeSpeed_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.safeDistanceFor(-1.0)
        );
    }

    @Test
    void update_withNegativeSpeed_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.update(-1.0, 50.0)
        );
    }

    @Test
    void update_withNaNSpeed_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.update(Double.NaN, 50.0)
        );
    }

    @Test
    void update_withInfiniteSpeed_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.update(
                        Double.POSITIVE_INFINITY,
                        50.0
                )
        );
    }

    @Test
    void update_withNegativeDistance_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.update(100.0, -1.0)
        );
    }

    @Test
    void update_withNaNDistance_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.update(
                        100.0,
                        Double.NaN
                )
        );
    }

    @Test
    void update_withInfiniteDistance_throwsIllegalArgumentException() {
        AdaptiveCruise adaptiveCruise =
                new AdaptiveCruise(TARGET_SPEED_KMH);

        assertThrows(
                IllegalArgumentException.class,
                () -> adaptiveCruise.update(
                        100.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }
}
