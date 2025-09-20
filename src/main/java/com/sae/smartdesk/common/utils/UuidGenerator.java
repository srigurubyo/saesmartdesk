package com.sae.smartdesk.common.utils;

import java.util.UUID;

public final class UuidGenerator {

    private UuidGenerator() {
    }

    public static UUID randomUuid() {
        return UUID.randomUUID();
    }
}
