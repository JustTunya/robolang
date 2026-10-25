package robolang.ui.menu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import robolang.lang.ast.AstNode;
import robolang.lang.ast.BoolExpression;
import robolang.lang.ast.Statement;
import robolang.lang.runtime.Context;
import robolang.theme.ThemeFactory;
import robolang.theme.ThemeRegistry;

/** The table behind Help → Pattern map (FR-60): which class plays which pattern role, read from the code. */
public final class PatternMap {

    /**
     * One table row.
     *
     * @param pattern the design pattern
     * @param role the GoF role
     * @param className the class that plays the role
     */
    public record Row(String pattern, String role, String className) {
    }

    private static final String INTERPRETER = "Interpreter";
    private static final String FACTORY = "Abstract Factory";

    private PatternMap() {
    }

    /**
     * Reads the pattern roles from the sealed hierarchies and the theme registry.
     *
     * @return the rows, Interpreter first
     */
    public static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row(INTERPRETER, "AbstractExpression (common)", AstNode.class.getSimpleName()));
        for (Class<?> abstractExpression : List.of(Statement.class, BoolExpression.class)) {
            rows.add(new Row(INTERPRETER, "AbstractExpression", abstractExpression.getSimpleName()));
            for (Class<?> node : abstractExpression.getPermittedSubclasses()) {
                String role = hasChildNodes(node) ? "NonterminalExpression" : "TerminalExpression";
                rows.add(new Row(INTERPRETER, role + " (" + abstractExpression.getSimpleName() + ")",
                        node.getSimpleName()));
            }
        }
        rows.add(new Row(INTERPRETER, "Context", Context.class.getSimpleName()));
        rows.add(new Row(FACTORY, "AbstractFactory", ThemeFactory.class.getSimpleName()));
        List<Method> creators = new ArrayList<>();
        for (Method m : ThemeFactory.class.getMethods()) {
            if (m.getName().startsWith("create")) {
                creators.add(m);
            }
        }
        creators.sort(Comparator.comparing(Method::getName));
        for (Method m : creators) {
            rows.add(new Row(FACTORY, "AbstractProduct", m.getReturnType().getSimpleName()));
        }
        for (ThemeFactory factory : ThemeRegistry.all()) {
            rows.add(new Row(FACTORY, "ConcreteFactory", factory.getClass().getSimpleName()));
            for (Method m : creators) {
                rows.add(new Row(FACTORY, "ConcreteProduct (" + factory.name() + ")", productClass(m, factory)));
            }
        }
        return rows;
    }

    // A node is a nonterminal when it holds child nodes: a node-typed field or a list of them.
    private static boolean hasChildNodes(Class<?> node) {
        for (Field field : node.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()
                    && (AstNode.class.isAssignableFrom(field.getType())
                            || List.class.isAssignableFrom(field.getType()))) {
                return true;
            }
        }
        return false;
    }

    private static String productClass(Method creator, ThemeFactory factory) {
        try {
            return creator.invoke(factory).getClass().getSimpleName();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
