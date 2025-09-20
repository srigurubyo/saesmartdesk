package com.sae.smartdesk.common.utils;

import java.time.Clock;

public final class ClockProvider {

    private static Clock clock = Clock.systemUTC();

    private ClockProvider() {
    }

    public static Clock getClock() {
        return clock;
    }

    public static void setClock(Clock newClock) {
        clock = newClock;
    }
}
