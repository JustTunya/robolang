package robolang.app;

/**
 * The best result stored for a level.
 *
 * @param stars stars earned
 * @param instructions instruction count
 * @param actions action count
 */
public record BestResult(int stars, int instructions, int actions) {
}
