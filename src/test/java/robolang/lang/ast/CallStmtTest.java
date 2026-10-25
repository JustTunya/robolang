package robolang.lang.ast;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CallStmtTest {
    private static final SourceRange R = new SourceRange(1, 1, 1, 5);

    @Test
    void bindLinksTheTargetOnce() {
        CallStmt call = new CallStmt("go", R);
        assertNull(call.target());
        ProcDef def = new ProcDef("go", new BlockStmt(List.of(), R), R);
        call.bind(def);
        assertSame(def, call.target());
        assertThrows(IllegalStateException.class, () -> call.bind(def));
    }
}
