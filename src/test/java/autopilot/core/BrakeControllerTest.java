package autopilot.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static autopilot.core.GearBox.Gear.D;
import static autopilot.core.GearBox.Gear.P;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrakeControllerTest {

    private static final double TOLERANCE = 1e-9;

    private BrakeController brakeController;

    @BeforeEach
    void setUp() {
        brakeController = new BrakeController();
    }

    @ParameterizedTest
    @CsvSource({
            "40.0, 20.0, 40.0",
            "80.0, 31.0, 80.0",
            "81.0, 30.0, 81.0",
            "81.0, 30.001, 90.0",
            "120.0, 20.0, 100.0"
    })
    void apply_forDifferentPressureAndSpeedConditions_returnsExpectedPressure(
            double requestedPressure,
            double speedKmh,
            double expectedPressure
    ) {
        double actualPressure =
                brakeController.apply(requestedPressure, speedKmh, D);

        assertEquals(expectedPressure, actualPressure, TOLERANCE);
    }

    @Test
    void apply_whenAbsRemainsActive_alternatesBetweenHighAndLowPulse() {
        brakeController.apply(81.0, 31.0, D);

        double secondPulse = brakeController.apply(81.0, 31.0, D);

        assertEquals(60.0, secondPulse, TOLERANCE);
    }

    @Test
    void apply_afterLeavingAbsCondition_resetsNextAbsPulseToHigh() {
        brakeController.apply(81.0, 31.0, D);
        brakeController.apply(50.0, 31.0, D);

        double pressureAfterReenteringAbs =
                brakeController.apply(81.0, 31.0, D);

        assertEquals(90.0, pressureAfterReenteringAbs, TOLERANCE);
    }

    @Test
    void apply_whileGearIsPark_throwsIllegalStateException() {
        assertThrows(
                IllegalStateException.class,
                () -> brakeController.apply(50.0, 20.0, P)
        );
    }

    @Test
    void apply_withNullGear_throwsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> brakeController.apply(50.0, 20.0, null)
        );
    }

    @Test
    void apply_withNegativePressure_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> brakeController.apply(-1.0, 20.0, D)
        );
    }

    @Test
    void apply_withNaNPressure_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> brakeController.apply(Double.NaN, 20.0, D)
        );
    }

    @Test
    void apply_withInfinitePressure_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> brakeController.apply(
                        Double.POSITIVE_INFINITY,
                        20.0,
                        D
                )
        );
    }

    @Test
    void apply_withNegativeSpeed_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> brakeController.apply(50.0, -1.0, D)
        );
    }

    @Test
    void apply_withNaNSpeed_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> brakeController.apply(
                        50.0,
                        Double.NaN,
                        D
                )
        );
    }

    @Test
    void apply_withInfiniteSpeed_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> brakeController.apply(
                        50.0,
                        Double.POSITIVE_INFINITY,
                        D
                )
        );
    }

    @Test
    void emergencyBrake_setsPressureToMaximum() {
        brakeController.emergencyBrake(D);

        assertEquals(100.0, brakeController.getPressure(), TOLERANCE);
    }

    @Test
    void emergencyBrake_activatesWarning() {
        assertFalse(brakeController.isWarningActive());

        brakeController.emergencyBrake(D);

        assertTrue(brakeController.isWarningActive());
    }

    @Test
    void emergencyBrake_whileGearIsPark_throwsIllegalStateException() {
        assertThrows(
                IllegalStateException.class,
                () -> brakeController.emergencyBrake(P)
        );
    }
}
