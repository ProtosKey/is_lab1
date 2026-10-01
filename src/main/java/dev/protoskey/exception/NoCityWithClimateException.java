package dev.protoskey.exception;

import dev.protoskey.entity.Climate;

public class NoCityWithClimateException extends Exception {
    public NoCityWithClimateException(Climate climate) {
        super("Городов с климатом " + climate + " нет");
    }
}
