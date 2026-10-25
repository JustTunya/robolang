package robolang.lang.runtime;

import robolang.lang.ast.SourceRange;

/**
 * Result of running a program on all variants of a level.
 *
 * @param success true if every variant succeeded
 * @param failure failure kind; null on success
 * @param message failure message; null on success
 * @param location failing source location; null on success
 * @param failedVariant zero-based index of the failing variant; -1 on success
 * @param instructionCount static instruction count (SPEC §3.5)
 * @param totalActions actions performed over all variants
 * @param totalTicks ticks consumed over all variants
 * @param stars stars earned, 0 to 3
 */
public record Outcome(boolean success, FailureKind failure, String message, SourceRange location,
                      int failedVariant, int instructionCount, int totalActions, long totalTicks, int stars) {
}
