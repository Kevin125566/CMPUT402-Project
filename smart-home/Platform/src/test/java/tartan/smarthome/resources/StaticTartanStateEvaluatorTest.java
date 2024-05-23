package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.util.Hashtable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticTartanStateEvaluatorTest {
    private final StaticTartanStateEvaluator stateEvaluator = new StaticTartanStateEvaluator();
    private static final Map<String, Object> oldState = new Hashtable<>();
    private final StringBuffer log = new StringBuffer();

    @BeforeEach
    public void setupTests() {
        oldState.put(IoTValues.DOOR_STATE, false);
        oldState.put(IoTValues.LIGHT_STATE, false);
        oldState.put(IoTValues.PROXIMITY_STATE, false);
        oldState.put(IoTValues.ALARM_STATE, false);
        oldState.put(IoTValues.ALARM_ACTIVE, false);
        oldState.put(IoTValues.HUMIDIFIER_STATE, false);
        oldState.put(IoTValues.CHILLER_STATE, false);
        oldState.put(IoTValues.TEMP_READING, 25);
        oldState.put(IoTValues.TARGET_TEMP, 25);
        oldState.put(IoTValues.HVAC_MODE, "Heater");
        oldState.put(IoTValues.ALARM_PASSCODE, "passcode");
    }

    // R1
    @Test
    public void isLightOff_When_HouseIsVacant() {
        // arrange
        oldState.put(IoTValues.PROXIMITY_STATE, false);
        oldState.put(IoTValues.LIGHT_STATE, true);

        // act
        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);

        // assert
        assertFalse((Boolean) newState.get(IoTValues.LIGHT_STATE));
        assertFalse((Boolean) newState.get(IoTValues.PROXIMITY_STATE));
    }

    @Test
    public void doesLightStayOff_When_HouseIsVacant() {
        // arrange
        oldState.put(IoTValues.PROXIMITY_STATE, false);
        oldState.put(IoTValues.LIGHT_STATE, false);

        // act
        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);

        // assert
        assertFalse((Boolean) newState.get(IoTValues.LIGHT_STATE));
        assertFalse((Boolean) newState.get(IoTValues.PROXIMITY_STATE));
    }

    @Test
    public void doesLightStayOn_When_HouseIsOccupied() {
        // arrange
        oldState.put(IoTValues.PROXIMITY_STATE, true);
        oldState.put(IoTValues.LIGHT_STATE, true);

        // act
        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);

        // assert
        assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE));
        assertTrue((Boolean) newState.get(IoTValues.PROXIMITY_STATE));
    }

    // R3
    @Test
    public void isDoorClosed_When_HouseIsVacant() {
        // arrange
        oldState.put(IoTValues.PROXIMITY_STATE, false);
        oldState.put(IoTValues.DOOR_STATE, true);

        // act
        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);

        // assert that door is closed when house is vacant
        assertFalse((Boolean) newState.get(IoTValues.PROXIMITY_STATE));
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE));
    }


    @Test
    public void doesDoorStayOpen_When_HouseIsOccupied() {
        // arrange
        oldState.put(IoTValues.PROXIMITY_STATE, true);
        oldState.put(IoTValues.DOOR_STATE, true);

        // act
        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);

        // assert that door is open when house is occupied
        assertTrue((Boolean) newState.get(IoTValues.PROXIMITY_STATE));
        assertTrue((Boolean) newState.get(IoTValues.DOOR_STATE));
    }


//    ########################## R9 START ##########################
    @Test
    public void AlarmIsNotDisabled_When_HouseIsEmpty() {
        // set house is empty
        oldState.put(IoTValues.PROXIMITY_STATE, false);

        // set alarm is active(sounding)
        oldState.put(IoTValues.ALARM_ACTIVE, true);

        // disable alarm
        oldState.put(IoTValues.ALARM_STATE, false);

        // correct passcode entered
        oldState.put(IoTValues.GIVEN_PASSCODE, "passcode");

        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is not disabled since house is empty
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
        // alarm is still active
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE));
    }


    @Test
    public void AlarmIsNotDisabled_When_AlarmIsNotActive() {
        // set house is not empty
        oldState.put(IoTValues.PROXIMITY_STATE, true);

        // set alarm is not active
        oldState.put(IoTValues.ALARM_ACTIVE, false);

        // disable alarm
        oldState.put(IoTValues.ALARM_STATE, false);

        // correct passcode entered
        oldState.put(IoTValues.GIVEN_PASSCODE, "passcode");

        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is not disabled since alarm is not active
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
    }


    @Test
    public void AlarmIsNotDisabled_When_HouseIsNotEmpty_And_AlarmIsActive_And_IncorrectPasscode_Entered() {
        // set house is not empty
        oldState.put(IoTValues.PROXIMITY_STATE, true);

        // set alarm is active(sounding)
        oldState.put(IoTValues.ALARM_ACTIVE, true);

        // disable alarm
        oldState.put(IoTValues.ALARM_STATE, false);

        // correct passcode
        oldState.put(IoTValues.GIVEN_PASSCODE, "incorrect_passcode");

        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is not disabled since incorrect passcode is given
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
        // alarm is still active since incorrect passcode is given
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE));
    }

    @Test
    public void AlarmIsDisabled_When_HouseIsNotEmpty_And_AlarmIsActive_And_CorrectPasscode_Entered() {
        // set house is not empty
        oldState.put(IoTValues.PROXIMITY_STATE, true);

        // set alarm is active(sounding)
        oldState.put(IoTValues.ALARM_ACTIVE, true);

        // disable alarm
        oldState.put(IoTValues.ALARM_STATE, false);

        // correct passcode
        oldState.put(IoTValues.GIVEN_PASSCODE, "passcode");

        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is disabled since house is not empty and alarm was active
        assertFalse((Boolean) newState.get(IoTValues.ALARM_STATE));
        // alarm is no longer active since correct passcode is given
        assertFalse((Boolean) newState.get(IoTValues.ALARM_ACTIVE));
    }


    @Test
    public void AlarmIsDisabled_With_CorrectPasscodeOnly() {
        // alarm can be disabled if house is not empty
        oldState.put(IoTValues.PROXIMITY_STATE, true);

        // alarm can be disabled if alarm is active(sounding)
        oldState.put(IoTValues.ALARM_ACTIVE, true);

        // disable alarm
        oldState.put(IoTValues.ALARM_STATE, false);

        // given passcode is incorrect and lexicographically less than correct password
        oldState.put(IoTValues.GIVEN_PASSCODE, "incorrect_passcode1234");
        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is still enabled because wrong passcode provided
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
        // assert alarm is still active
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE));

        // given passcode is different case
        oldState.put(IoTValues.GIVEN_PASSCODE, "PASSCODE");
        newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is still enabled because wrong passcode provided
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
        // assert alarm is still active
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE));

        ////////// The below tests passed after the functionality was fixed //////////
        // given passcode is empty
        oldState.put(IoTValues.GIVEN_PASSCODE, "");
        newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is still enabled because wrong passcode provided
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
        // assert alarm is still active
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE));

        // given passcode is lexicographically greater than correct password
        oldState.put(IoTValues.GIVEN_PASSCODE, "q");
        newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is still enabled because wrong passcode provided
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE));
        // assert alarm is still active
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE));
        ////////// The above tests passed after the functionality was fixed //////////

        // given passcode now correct
        oldState.put(IoTValues.GIVEN_PASSCODE, "passcode");
        newState = stateEvaluator.evaluateState(oldState, log);
        // alarm is now disabled because correct passcode provided
        assertFalse((Boolean) newState.get(IoTValues.ALARM_STATE));
        // assert alarm is no longer active
        assertFalse((Boolean) newState.get(IoTValues.ALARM_ACTIVE));
    }
//    ########################## R9 END ##########################


    // R10
    @Test
    public void turnHeaterOn_If_CurrTempLessThanTargetTemp() {
        // test if heater turns on
        oldState.put(IoTValues.TARGET_TEMP, 25);
        oldState.put(IoTValues.TEMP_READING, 20);

        Map<String, Object> newState = stateEvaluator.evaluateState(oldState, log);

        assertTrue((Boolean) newState.get(IoTValues.HEATER_STATE));

        // test if heater turns off
        oldState.put(IoTValues.TARGET_TEMP, 20);
        oldState.put(IoTValues.TEMP_READING, 25);

        newState = stateEvaluator.evaluateState(oldState, log);

        assertFalse((Boolean) newState.get(IoTValues.HEATER_STATE));
    }
}