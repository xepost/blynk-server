package cc.blynk.server.core.model.widgets.outputs;

import cc.blynk.server.core.model.Pin;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.widgets.FrequencyWidget;
import cc.blynk.server.core.model.widgets.MultiPinWidget;
import cc.blynk.utils.ParseUtil;
import cc.blynk.utils.structure.LimitedArrayDeque;
import io.netty.channel.Channel;

import static cc.blynk.server.core.protocol.enums.Command.APP_SYNC;
import static cc.blynk.server.core.protocol.enums.Command.HARDWARE;
import static cc.blynk.utils.BlynkByteBufUtil.makeUTF8StringMessage;
import static cc.blynk.utils.StringUtils.prependDashIdAndDeviceId;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 21.03.15.
 */
public class LCD extends MultiPinWidget implements FrequencyWidget {

    public boolean advancedMode;

    public String textFormatLine1;
    public String textFormatLine2;

    public boolean textLight;

    private int frequency;

    private transient long lastRequestTS;

    private static final int POOL_SIZE = ParseUtil.parseInt(System.getProperty("lcd.strings.pool.size", "6"));
    private transient final LimitedArrayDeque<String> lastCommands = new LimitedArrayDeque<>(POOL_SIZE);

    private static void sendSyncOnActivate(Pin pin, int dashId, int deviceId, Channel appChannel) {
        if (pin.notEmpty()) {
            String body = prependDashIdAndDeviceId(dashId, deviceId, pin.makeHardwareBody());
            appChannel.write(makeUTF8StringMessage(APP_SYNC, SYNC_DEFAULT_MESSAGE_ID, body), appChannel.voidPromise());
        }
    }

    @Override
    public boolean updateIfSame(int deviceId, byte pinIn, PinType type, String value) {
        boolean isSame = false;
        if (pins != null && this.deviceId == deviceId) {
            for (Pin pin : pins) {
                if (pin.isSame(pinIn, type)) {
                    pin.value = value;
                    isSame = true;
                }
            }
            if (advancedMode && isSame) {
                lastCommands.add(value);
            }
        }
        return isSame;
    }

    @Override
    public void sendAppSync(Channel appChannel, int dashId, int targetId) {
        if (pins == null) {
            return;
        }
        if (targetId == ANY_TARGET || this.deviceId == targetId) {
            if (advancedMode) {
                for (String command : lastCommands) {
                    pins[0].value = command;
                    sendSyncOnActivate(pins[0], dashId, deviceId, appChannel);
                }
            } else {
                for (Pin pin : pins) {
                    sendSyncOnActivate(pin, dashId, deviceId, appChannel);
                }
            }
        }
    }

    @Override
    public boolean isSplitMode() {
        return !advancedMode;
    }

    @Override
    public final int getFrequency() {
        return frequency;
    }

    @Override
    public final long getLastRequestTS() {
        return lastRequestTS;
    }

    @Override
    public final void setLastRequestTS(long now) {
        this.lastRequestTS = now;
    }

    @Override
    public void sendReadingCommand(Session session, int dashId) {
        if (pins == null) {
            return;
        }
        for (Pin pin : pins) {
            session.sendMessageToHardware(dashId, HARDWARE, 7778, Pin.makeReadingHardwareBody(pin.pinType.pintTypeChar, pin.pin), deviceId);
        }
    }

    @Override
    public String getModeType() {
        return "in";
    }

    @Override
    public int getPrice() {
        return 400;
    }
}
