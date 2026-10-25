package robolang.lang.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import robolang.lang.ast.SourceRange;

class ErrorTest {
    private static final SourceRange R = new SourceRange(3, 12, 3, 14);

    @Test
    void formattedPrefixesLineAndColumn() {
        assertEquals("3:12 Expected '{' after REPEAT 4", new ParseException("Expected '{' after REPEAT 4", R).formatted());
        assertEquals("3:12 x", new LexException("x", R).formatted());
        assertEquals("3:12 x", new SemanticException("x", R).formatted());
    }

    @Test
    void formattedWithoutRangeIsJustTheMessage() {
        assertEquals("boom", new SemanticException("boom", null).formatted());
        assertEquals("Execution stopped", new ExecutionStoppedException(null).formatted());
    }

    @Test
    void executionFailuresCarryTheVariant() {
        ExecutionFailure[] all = {
            new CrashException("c", R, 1), new EmptyPickException("e", R, 1),
            new TickLimitException("t", R, 1), new RecursionLimitException("r", R, 1)};
        for (ExecutionFailure f : all) {
            assertEquals(1, f.variantIndex());
            assertEquals(R, f.range());
            assertInstanceOf(RoboLangException.class, f);
            assertInstanceOf(RuntimeException.class, f);
        }
    }

    @Test
    void stoppedKeepsItsRange() {
        assertEquals(R, new ExecutionStoppedException(R).range());
        assertNull(new ExecutionStoppedException(null).range());
    }
}
