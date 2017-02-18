package app.we.go.oracle.obd2.commands;

/**
 * Created by Vadim on 12.01.2017.
 */

public enum OBDProtocols {
    AUTO('0'),
    SAE_J1850_PW('1'),
    SAE_J1850_VPW('2'),
    ISO_9141_2('3'),
    ISO_14230_4_KWP('4'),
    ISO_14230_4_KWP_FAST('5'),
    ISO_15765_4_CAN('6'),
    ISO_15765_4_CAN_B('7'),
    ISO_15765_4_CAN_C('8'),
    ISO_15765_4_CAN_D('9'),
    SAE_J1939_CAN('A'),
    USER1_CAN('B'),
    USER2_CAN('C');

    private final  char value;

    OBDProtocols(char value) {
        this.value=value;
    }

    public char getValue() {

        return value;
    }
}
