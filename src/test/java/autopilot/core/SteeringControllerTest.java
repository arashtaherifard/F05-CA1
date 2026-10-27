package autopilot.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SteeringControllerTest {

    private static final double TOLERANCE = 1e-9;

    private static final double NORMAL_SPEED_KMH = 50.0;
    private static final double HIGH_SPEED_THRESHOLD_KMH = 120.0;
    private static final double JUST_ABOVE_HIGH_SPEED_THRESHOLD_KMH = 120.001;

    private static final double PHYSICAL_LIMIT_DEGREES = 450.0;
    private static final double HIGH_SPEED_LIMIT_DEGREES = 90.0;
    private static final double MAX_CHANGE_PER_TICK_DEGREES = 60.0;

    private SteeringController steeringController;

    @BeforeEach
    void setUp() {
        steeringController = new SteeringController();
    }

    @Test
    void update_whenRequestedAngleIsWithinOneTick_reachesRequestedAngle() {
        double requestedAngle = 30.0;

        double actualAngle =
                steeringController.update(requestedAngle, NORMAL_SPEED_KMH);

        assertEquals(requestedAngle, actualAngle, TOLERANCE);
    }

    @Test
    void update_whenPositiveRequestedAngleIsFar_movesByMaximumChange() {
        double requestedAngle = 200.0;

        double actualAngle =
                steeringController.update(requestedAngle, NORMAL_SPEED_KMH);

        assertEquals(
                MAX_CHANGE_PER_TICK_DEGREES,
                actualAngle,
                TOLERANCE
        );
    }

    @Test
    void update_whenNegativeRequestedAngleIsFar_movesByMaximumChange() {
        double requestedAngle = -200.0;

        double actualAngle =
                steeringController.update(requestedAngle, NORMAL_SPEED_KMH);

        assertEquals(
                -MAX_CHANGE_PER_TICK_DEGREES,
                actualAngle,
                TOLERANCE
        );
    }

    @Test
    void update_atHighSpeedThreshold_usesPhysicalSteeringLimit() {
        double requestedAngle = 500.0;

        steeringController.update(
                requestedAngle,
                HIGH_SPEED_THRESHOLD_KMH
        );

        assertEquals(
                PHYSICAL_LIMIT_DEGREES,
                steeringController.getTargetAngleDegrees(),
                TOLERANCE
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {450.0, -450.0})
    void update_aboveHighSpeedThreshold_clampsTargetToHighSpeedLimit(
            double requestedAngle
    ) {
        steeringController.update(
                requestedAngle,
                JUST_ABOVE_HIGH_SPEED_THRESHOLD_KMH
        );

        double expectedTarget =
                Math.copySign(HIGH_SPEED_LIMIT_DEGREES, requestedAngle);

        assertEquals(
                expectedTarget,
                steeringController.getTargetAngleDegrees(),
                TOLERANCE
        );
    }

    @Test
    void selfCenter_whenCurrentAngleIsFarFromZero_movesByMaximumChange() {
        steeringController.update(180.0, NORMAL_SPEED_KMH);
        steeringController.update(180.0, NORMAL_SPEED_KMH);
        steeringController.update(180.0, NORMAL_SPEED_KMH);

        double actualAngle = steeringController.selfCenter();

        assertEquals(120.0, actualAngle, TOLERANCE);
    }

    @Test
    void selfCenter_whenCurrentAngleIsWithinOneTick_reachesZero() {
        steeringController.update(30.0, NORMAL_SPEED_KMH);

        double actualAngle = steeringController.selfCenter();

        assertEquals(0.0, actualAngle, TOLERANCE);
    }

    @Test
    void update_whenRequestedAngleIsNaN_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> steeringController.update(
                        Double.NaN,
                        NORMAL_SPEED_KMH
                )
        );
    }

    @Test
    void update_whenRequestedAngleIsInfinite_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> steeringController.update(
                        Double.POSITIVE_INFINITY,
                        NORMAL_SPEED_KMH
                )
        );
    }

    @Test
    void update_whenSpeedIsNegative_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> steeringController.update(0.0, -1.0)
        );
    }

    @Test
    void update_whenSpeedIsNaN_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> steeringController.update(
                        0.0,
                        Double.NaN
                )
        );
    }

    @Test
    void update_whenSpeedIsInfinite_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> steeringController.update(
                        0.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }
}
