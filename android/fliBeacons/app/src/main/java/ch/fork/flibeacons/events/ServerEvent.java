package ch.fork.flibeacons.events;

import com.google.gson.JsonElement;

/**
 * Created by fork on 01.07.14.
 */
public class ServerEvent {
    private final String event;
    private final JsonElement[] args;

    public ServerEvent(String event, JsonElement[] args) {
        this.event = event;
        this.args = args;
    }

    public String getEvent() {
        return event;
    }

    public JsonElement[] getArgs() {
        return args;
    }
}
