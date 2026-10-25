package robolang.ui.menu;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import robolang.lang.ast.BoolExpression;
import robolang.lang.ast.Statement;
import robolang.theme.ThemeFactory;
import robolang.theme.ThemeRegistry;

class PatternMapTest {
    private final List<PatternMap.Row> rows = PatternMap.rows();

    private Set<String> classesWithRole(String rolePrefix) {
        return rows.stream().filter(r -> r.role().startsWith(rolePrefix))
                .map(PatternMap.Row::className).collect(Collectors.toSet());
    }

    private String roleOf(String className) {
        return rows.stream().filter(r -> r.className().equals(className)).findFirst().orElseThrow().role();
    }

    @Test
    void listsEveryPermittedNode() {
        Set<String> expected = new java.util.HashSet<>();
        for (Class<?> c : Statement.class.getPermittedSubclasses()) {
            expected.add(c.getSimpleName());
        }
        for (Class<?> c : BoolExpression.class.getPermittedSubclasses()) {
            expected.add(c.getSimpleName());
        }
        Set<String> listed = classesWithRole("TerminalExpression");
        listed.addAll(classesWithRole("NonterminalExpression"));
        assertTrue(listed.containsAll(expected), "missing: " + expected.stream().filter(c -> !listed.contains(c)).toList());
        assertTrue(expected.size() >= 14);
    }

    @Test
    void separatesTerminalFromNonterminalNodes() {
        for (String leaf : List.of("MoveStmt", "TurnStmt", "PickStmt", "WallAheadCond", "OnItemCond", "AtGoalCond")) {
            assertTrue(roleOf(leaf).startsWith("TerminalExpression"), leaf);
        }
        for (String inner : List.of("BlockStmt", "RepeatStmt", "WhileStmt", "IfStmt", "CallStmt", "NotCond",
                "AndCond", "OrCond")) {
            assertTrue(roleOf(inner).startsWith("NonterminalExpression"), inner);
        }
    }

    @Test
    void listsFactoriesAndProductsFromTheRegistry() {
        Set<String> factories = classesWithRole("ConcreteFactory");
        for (ThemeFactory f : ThemeRegistry.all()) {
            assertTrue(factories.contains(f.getClass().getSimpleName()), f.name());
        }
        assertTrue(classesWithRole("AbstractFactory").contains("ThemeFactory"));
        assertTrue(classesWithRole("AbstractProduct").containsAll(
                Set.of("RobotView", "WallTile", "FloorTile", "Collectible", "GoalTile")));
        assertTrue(rows.stream().filter(r -> r.role().startsWith("ConcreteProduct")).count()
                >= 5L * ThemeRegistry.all().size());
    }
}
