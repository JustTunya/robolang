package robolang.model;

import java.util.List;
import java.util.Set;

/**
 * A puzzle level (SPEC §5).
 *
 * @param id file id, e.g. {@code 03-long-hall}
 * @param title display title
 * @param briefing objective text
 * @param keywords upper-case keywords the level allows
 * @param parInstructions instruction count for the second star
 * @param parActions total action count for the third star
 * @param variants one or more grid variants
 * @param referenceSolution demo-mode solution source, may be null
 */
public record Level(String id, String title, String briefing, Set<String> keywords, int parInstructions,
                    int parActions, List<Variant> variants, String referenceSolution) {

    /** Makes {@code keywords} and {@code variants} immutable. */
    public Level {
        keywords = Set.copyOf(keywords);
        variants = List.copyOf(variants);
    }
}
