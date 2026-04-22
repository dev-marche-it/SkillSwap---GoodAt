package it.skillswap.domain.exception;

import it.skillswap.domain.ExchangeStatus;

/**
 * Eccezione lanciata quando viene tentata una transizione di stato non valida.
 */
public class InvalidStateTransitionException extends SkillSwapException {
    public InvalidStateTransitionException(ExchangeStatus from, ExchangeStatus to) {
        super("Transizione di stato non valida: da " + from + " a " + to);
    }
}
