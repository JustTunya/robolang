package robolang;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;
import robolang.theme.ThemeRegistry;

/** Enforces the layering rules of AGENTS.md §3 and SPEC §10. */
class ArchitectureTest {
    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("robolang");

    private static final ArchRule langIndependent = noClasses().that().resideInAPackage("robolang.lang..")
            .should().dependOnClassesThat().resideInAnyPackage("robolang.theme..", "robolang.ui..", "robolang.app..");

    private static final ArchRule themeIndependent = noClasses().that().resideInAPackage("robolang.theme..")
            .should().dependOnClassesThat().resideInAnyPackage("robolang.lang..", "robolang.ui..", "robolang.app..");

    private static final ArchRule modelIndependent = noClasses().that().resideInAPackage("robolang.model..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "robolang.lang..", "robolang.theme..", "robolang.ui..", "robolang.app..");

    private static final ArchRule langAndModelHeadless = noClasses()
            .that().resideInAnyPackage("robolang.lang..", "robolang.model..")
            .should().dependOnClassesThat().resideInAnyPackage("java.awt..", "javax.swing..");

    private static final ArchRule themeNoSwing = noClasses().that().resideInAPackage("robolang.theme..")
            .should().dependOnClassesThat().resideInAPackage("javax.swing..");

    private static ArchRule productsPrivate(String subPackage) {
        String pkg = "robolang.theme." + subPackage + "..";
        DescribedPredicate<JavaClass> allowed = JavaClass.Predicates.resideInAPackage(pkg)
                .or(JavaClass.Predicates.equivalentTo(ThemeRegistry.class));
        return classes().that().resideInAPackage(pkg)
                .should().onlyBeAccessed().byClassesThat(allowed)
                .allowEmptyShould(true);
    }

    private static final ArchRule spaceProductsHidden = productsPrivate("space");

    private static final ArchRule jungleProductsHidden = productsPrivate("jungle");

    private static final ArchRule retroProductsHidden = productsPrivate("retro");

    private static final ArchRule noPackageCycles = SlicesRuleDefinition.slices().matching("robolang.(*)..")
            .should().beFreeOfCycles();

    @Test
    void langIndependent() {
        langIndependent.check(CLASSES);
    }

    @Test
    void themeIndependent() {
        themeIndependent.check(CLASSES);
    }

    @Test
    void modelIndependent() {
        modelIndependent.check(CLASSES);
    }

    @Test
    void langAndModelHeadless() {
        langAndModelHeadless.check(CLASSES);
    }

    @Test
    void themeNoSwing() {
        themeNoSwing.check(CLASSES);
    }

    @Test
    void spaceProductsHidden() {
        spaceProductsHidden.check(CLASSES);
    }

    @Test
    void jungleProductsHidden() {
        jungleProductsHidden.check(CLASSES);
    }

    @Test
    void retroProductsHidden() {
        retroProductsHidden.check(CLASSES);
    }

    @Test
    void noPackageCycles() {
        noPackageCycles.check(CLASSES);
    }
}
