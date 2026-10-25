package robolang.lang.runtime;

/** Why a run did not succeed (SPEC §3.4). */
public enum FailureKind {
    /** Moved into a wall or off the grid. */
    CRASH,
    /** Picked where there is no item. */
    EMPTY_PICK,
    /** Tick limit reached. */
    TICK_LIMIT,
    /** Call depth limit exceeded. */
    RECURSION_LIMIT,
    /** Program ended without meeting the success condition. */
    INCOMPLETE,
    /** The player pressed Stop. */
    STOPPED
}
