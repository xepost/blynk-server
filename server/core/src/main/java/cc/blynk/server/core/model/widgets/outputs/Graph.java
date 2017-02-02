package cc.blynk.server.core.model.widgets.outputs;

import cc.blynk.server.core.model.Pin;
import cc.blynk.server.core.model.auth.Session;
import cc.blynk.server.core.model.widgets.FrequencyWidget;
import cc.blynk.server.core.model.widgets.OnePinWidget;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.server.core.protocol.enums.Command.HARDWARE;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 21.03.15.
 */
public class Graph extends OnePinWidget implements FrequencyWidget {

    public boolean isBar;

    private int frequency;

    private transient long lastRequestTS;

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
        session.sendMessageToHardware(dashId, HARDWARE, 7778, Pin.makeReadingHardwareBody(pinType.pintTypeChar, pin), deviceId);
    }

    @Override
    public void sendHardSync(ChannelHandlerContext ctx, int msgId, int deviceId) {
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
