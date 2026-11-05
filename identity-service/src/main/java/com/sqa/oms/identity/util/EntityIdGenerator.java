package com.sqa.oms.identity.util;

import com.sqa.oms.identity.constants.Constants;

import java.security.SecureRandom;

/**
 * Generates the random business keys stored in the {@code entity_id} columns.
 */
public final class EntityIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private EntityIdGenerator() {
    }

    /**
     * Generates an uppercase alphanumeric business key.
     *
     * @return a newly generated entity id
     */
    public static String generate() {
        StringBuilder builder = new StringBuilder(Constants.ENTITY_ID_LENGTH);
        for (int i = 0; i < Constants.ENTITY_ID_LENGTH; i++) {
            builder.append(Constants.ENTITY_ID_ALPHABET.charAt(RANDOM.nextInt(
                    Constants.ENTITY_ID_ALPHABET.length())));
        }

        return builder.toString();
    }
}
