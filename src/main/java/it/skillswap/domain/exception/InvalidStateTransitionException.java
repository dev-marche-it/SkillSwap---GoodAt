package it.skillswap.domain.exception;

import it.skillswap.domain.ExchangeStatus;

/**
 * Lanciata quando uno {@link it.skillswap.domain.Exchange} passa a un {@link it.skillswap.domain.ExchangeStatus} incompatibile.
 */
public class InvalidStateTransitionException extends SkillSwapException {
    /**
     * @param from stato attuale
     * @param to   stato target tentato
     */
    public InvalidStateTransitionException(ExchangeStatus from, ExchangeStatus to) {
        super("Transizione di stato non valida: da " + from + " a " + to);
    }
}
