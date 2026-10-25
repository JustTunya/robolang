package robolang.lang.runtime;

/**
 * Guards against runaway programs (SPEC §4.6).
 *
 * @param maxTicks ticks allowed per variant
 * @param maxCallDepth procedure call depth allowed
 */
public record ExecutionLimits(int maxTicks, int maxCallDepth) {
    /** The limits of the specification: 100 000 ticks, call depth 64. */
    public static final ExecutionLimits DEFAULT = new ExecutionLimits(100_000, 64);
}
