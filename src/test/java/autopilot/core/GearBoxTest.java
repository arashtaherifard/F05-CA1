package autopilot.core;

import autopilot.exception.IllegalGearChangeException;
import autopilot.sensor.SpeedSensor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static autopilot.core.GearBox.Gear.D;
import static autopilot.core.GearBox.Gear.N;
import static autopilot.core.GearBox.Gear.P;
import static autopilot.core.GearBox.Gear.R;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GearBoxTest {

    @Test
    void changeTo_fromParkToReverseBelowSpeedLimit_changesGear() {
        SpeedSensor speedSensor = () -> 4.999;
        GearBox gearBox = new GearBox(speedSensor);

        gearBox.changeTo(R);

        assertEquals(R, gearBox.getCurrentGear());
    }

    @Test
    void changeTo_fromParkToReverseAtSpeedLimit_throwsIllegalGearChangeException() {
        SpeedSensor speedSensor = () -> 5.0;
        GearBox gearBox = new GearBox(speedSensor);

        assertThrows(
                IllegalGearChangeException.class,
                () -> gearBox.changeTo(R)
        );
    }

    @ParameterizedTest
    @EnumSource(value = GearBox.Gear.class, names = {"N", "D"})
    void changeTo_fromParkToUnsupportedGear_throwsIllegalGearChangeException(
            GearBox.Gear targetGear
    ) {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);

        assertThrows(
                IllegalGearChangeException.class,
                () -> gearBox.changeTo(targetGear)
        );
    }

    @Test
    void changeTo_fromReverseToNeutral_changesGear() {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);
        gearBox.changeTo(R);

        gearBox.changeTo(N);

        assertEquals(N, gearBox.getCurrentGear());
    }

    @Test
    void changeTo_fromNeutralToDrive_changesGear() {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);
        gearBox.changeTo(R);
        gearBox.changeTo(N);

        gearBox.changeTo(D);

        assertEquals(D, gearBox.getCurrentGear());
    }

    @Test
    void changeTo_fromReverseToDriveAtZeroSpeed_changesGear() {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);
        gearBox.changeTo(R);

        gearBox.changeTo(D);

        assertEquals(D, gearBox.getCurrentGear());
    }

    @Test
    void changeTo_fromReverseToDriveAboveZeroSpeed_throwsIllegalGearChangeException() {
        double[] speedKmh = {0.0};
        SpeedSensor speedSensor = () -> speedKmh[0];
        GearBox gearBox = new GearBox(speedSensor);
        gearBox.changeTo(R);

        speedKmh[0] = 0.001;

        assertThrows(
                IllegalGearChangeException.class,
                () -> gearBox.changeTo(D)
        );
    }

    @Test
    void changeTo_fromDriveToReverseAtZeroSpeed_changesGear() {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);
        gearBox.changeTo(R);
        gearBox.changeTo(N);
        gearBox.changeTo(D);

        gearBox.changeTo(R);

        assertEquals(R, gearBox.getCurrentGear());
    }

    @Test
    void changeTo_fromDriveToReverseAboveZeroSpeed_throwsIllegalGearChangeException() {
        double[] speedKmh = {0.0};
        SpeedSensor speedSensor = () -> speedKmh[0];
        GearBox gearBox = new GearBox(speedSensor);
        gearBox.changeTo(R);
        gearBox.changeTo(N);
        gearBox.changeTo(D);

        speedKmh[0] = 0.001;

        assertThrows(
                IllegalGearChangeException.class,
                () -> gearBox.changeTo(R)
        );
    }

    @Test
    void changeTo_sameGear_keepsCurrentGear() {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);

        gearBox.changeTo(P);

        assertEquals(P, gearBox.getCurrentGear());
    }

    @Test
    void changeTo_nullGear_throwsNullPointerException() {
        SpeedSensor speedSensor = () -> 0.0;
        GearBox gearBox = new GearBox(speedSensor);

        assertThrows(
                NullPointerException.class,
                () -> gearBox.changeTo(null)
        );
    }

    @Test
    void changeTo_whenSpeedIsNegative_throwsIllegalArgumentException() {
        SpeedSensor speedSensor = () -> -1.0;
        GearBox gearBox = new GearBox(speedSensor);

        assertThrows(
                IllegalArgumentException.class,
                () -> gearBox.changeTo(R)
        );
    }

    @Test
    void changeTo_whenSpeedIsNaN_throwsIllegalArgumentException() {
        SpeedSensor speedSensor = () -> Double.NaN;
        GearBox gearBox = new GearBox(speedSensor);

        assertThrows(
                IllegalArgumentException.class,
                () -> gearBox.changeTo(R)
        );
    }

    @Test
    void changeTo_whenSpeedIsInfinite_throwsIllegalArgumentException() {
        SpeedSensor speedSensor = () -> Double.POSITIVE_INFINITY;
        GearBox gearBox = new GearBox(speedSensor);

        assertThrows(
                IllegalArgumentException.class,
                () -> gearBox.changeTo(R)
        );
    }
}
