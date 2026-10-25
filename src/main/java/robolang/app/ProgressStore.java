package robolang.app;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;
import robolang.lang.runtime.Outcome;

/** Persists best results and last programs per level. */
public final class ProgressStore {
    private static final System.Logger LOG = System.getLogger(ProgressStore.class.getName());

    private final Path progressFile;
    private final Path programsDir;

    /**
     * Opens the store.
     *
     * @param dir directory holding the data files
     */
    public ProgressStore(Path dir) {
        this.progressFile = dir.resolve("progress.properties");
        this.programsDir = dir.resolve("programs");
    }

    /**
     * Returns the best result of a level.
     *
     * @param levelId level id
     * @return the best result, if any
     */
    public Optional<BestResult> best(String levelId) {
        Properties p = load(progressFile);
        try {
            String stars = p.getProperty(levelId + ".stars");
            if (stars == null) {
                return Optional.empty();
            }
            return Optional.of(new BestResult(Integer.parseInt(stars),
                    Integer.parseInt(p.getProperty(levelId + ".instructions")),
                    Integer.parseInt(p.getProperty(levelId + ".actions"))));
        } catch (NumberFormatException e) {
            LOG.log(System.Logger.Level.WARNING, "Corrupt progress entry for " + levelId, e);
            return Optional.empty();
        }
    }

    /**
     * Tells whether a level has been solved.
     *
     * @param levelId level id
     * @return true if solved
     */
    public boolean isSolved(String levelId) {
        return best(levelId).isPresent();
    }

    /**
     * Records a run, keeping the better result. Failed runs are ignored.
     *
     * @param levelId level id
     * @param outcome the run outcome
     */
    public void record(String levelId, Outcome outcome) {
        if (!outcome.success()) {
            return;
        }
        BestResult candidate = new BestResult(outcome.stars(), outcome.instructionCount(), outcome.totalActions());
        Optional<BestResult> old = best(levelId);
        if (old.isPresent() && !better(candidate, old.get())) {
            return;
        }
        Properties p = load(progressFile);
        p.setProperty(levelId + ".stars", Integer.toString(candidate.stars()));
        p.setProperty(levelId + ".instructions", Integer.toString(candidate.instructions()));
        p.setProperty(levelId + ".actions", Integer.toString(candidate.actions()));
        write(progressFile, w -> p.store(w, null));
    }

    private static boolean better(BestResult a, BestResult b) {
        return Comparator.comparingInt(BestResult::stars).reversed()
                .thenComparingInt(BestResult::instructions)
                .thenComparingInt(BestResult::actions)
                .compare(a, b) < 0;
    }

    /**
     * Returns the last saved program of a level.
     *
     * @param levelId level id
     * @return the source, if any
     */
    public Optional<String> lastProgram(String levelId) {
        try {
            return Optional.of(Files.readString(programFile(levelId), StandardCharsets.UTF_8));
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            LOG.log(System.Logger.Level.WARNING, "Cannot read program of " + levelId, e);
            return Optional.empty();
        }
    }

    /**
     * Saves the current program of a level.
     *
     * @param levelId level id
     * @param source program text
     */
    public void saveProgram(String levelId, String source) {
        write(programFile(levelId), w -> w.write(source));
    }

    /**
     * Deletes all stored progress and programs; settings are kept.
     */
    public void resetAll() {
        try {
            Files.deleteIfExists(progressFile);
            if (Files.isDirectory(programsDir)) {
                try (Stream<Path> files = Files.list(programsDir)) {
                    for (Path f : (Iterable<Path>) files::iterator) {
                        Files.deleteIfExists(f);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path programFile(String levelId) {
        if (!levelId.matches("[A-Za-z0-9_.-]+") || levelId.contains("..")) {
            throw new IllegalArgumentException("Invalid level id: " + levelId);
        }
        return programsDir.resolve(levelId + ".rl");
    }

    /** Loads a properties file; a missing or corrupt file yields empty properties. */
    static Properties load(Path file) {
        Properties p = new Properties();
        if (!Files.exists(file)) {
            return p;
        }
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(r);
        } catch (IOException | RuntimeException e) {
            LOG.log(System.Logger.Level.WARNING, "Cannot read " + file + ", treating as empty", e);
            return new Properties();
        }
        return p;
    }

    /** Body of an atomic write. */
    interface Body {
        void accept(Writer w) throws IOException;
    }

    /** Writes a file via a temp file and an atomic move. */
    static void write(Path target, Body body) {
        try {
            Files.createDirectories(target.getParent());
            Path tmp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
            try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                body.accept(w);
            }
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
