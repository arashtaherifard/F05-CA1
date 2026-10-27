package autopilot.core;

import autopilot.exception.SensorFailureException;
import autopilot.sensor.LaneSensor;
import autopilot.sensor.ObstacleSensor;
import autopilot.sensor.SpeedSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoDriverTest {

    private static final double DEFAULT_SPEED_KMH = 80.0;
    private static final double DEFAULT_OBSTACLE_DISTANCE_METERS = 100.0;
    private static final double DEFAULT_LANE_OFFSET_METERS = 0.0;
    private static final double TARGET_SPEED_KMH = 80.0;
    private static final double SAFE_MODE_BRAKE_PRESSURE = 30.0;
    private static final double TOLERANCE = 1e-9;

    private SpeedSensor speedSensor;
    private ObstacleSensor obstacleSensor;
    private LaneSensor laneSensor;
    private AutoDriver autoDriver;

    @BeforeEach
    void setUp() {
        speedSensor = () -> DEFAULT_SPEED_KMH;
        obstacleSensor = () -> DEFAULT_OBSTACLE_DISTANCE_METERS;
        laneSensor = () -> DEFAULT_LANE_OFFSET_METERS;

        autoDriver = new AutoDriver(
                speedSensor,
                obstacleSensor,
                laneSensor,
                TARGET_SPEED_KMH
        );
    }

    @Test
    void tick_withHealthySensors_returnsNormalDrivingResult() {
        AutoDriver.DriveResult result =
                autoDriver.tick(GearBox.Gear.D);

        AutoDriver.DriveResult expected =
                new AutoDriver.DriveResult(
                        0.0,
                        0.0,
                        AdaptiveCruise.Mode.HOLD,
                        false,
                        false
                );

        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "/autopilot/core/lane-correction.csv",
            numLinesToSkip = 1
    )
    void tick_withLaneDeviation_appliesExpectedSteeringCorrection(
            double laneOffsetMeters,
            double expectedSteeringAngleDegrees
    ) {
        LaneSensor deviatedLaneSensor = () -> laneOffsetMeters;

        AutoDriver driver = new AutoDriver(
                speedSensor,
                obstacleSensor,
                deviatedLaneSensor,
                TARGET_SPEED_KMH
        );

        AutoDriver.DriveResult result =
                driver.tick(GearBox.Gear.D);

        assertEquals(
                expectedSteeringAngleDegrees,
                result.steeringAngleDegrees(),
                TOLERANCE
        );
    }

    @Test
    void tick_whenSpeedSensorFails_returnsSafeModeResult() {
        SpeedSensor failedSpeedSensor = () -> {
            throw new SensorFailureException("Speed sensor failed");
        };

        AutoDriver driver = new AutoDriver(
                failedSpeedSensor,
                obstacleSensor,
                laneSensor,
                TARGET_SPEED_KMH
        );

        AutoDriver.DriveResult result =
                driver.tick(GearBox.Gear.D);

        assertEquals(expectedSafeModeResult(), result);
    }

    @Test
    void tick_whenObstacleSensorFails_returnsSafeModeResult() {
        ObstacleSensor failedObstacleSensor = () -> {
            throw new SensorFailureException("Obstacle sensor failed");
        };

        AutoDriver driver = new AutoDriver(
                speedSensor,
                failedObstacleSensor,
                laneSensor,
                TARGET_SPEED_KMH
        );

        AutoDriver.DriveResult result =
                driver.tick(GearBox.Gear.D);

        assertEquals(expectedSafeModeResult(), result);
    }

    private AutoDriver.DriveResult expectedSafeModeResult() {
        return new AutoDriver.DriveResult(
                0.0,
                SAFE_MODE_BRAKE_PRESSURE,
                AdaptiveCruise.Mode.BRAKE,
                false,
                true
        );
    }
}
