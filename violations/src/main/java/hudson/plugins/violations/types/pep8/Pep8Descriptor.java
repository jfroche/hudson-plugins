package hudson.plugins.violations.types.pep8;

import hudson.plugins.violations.TypeDescriptor;
import hudson.plugins.violations.ViolationsParser;

/**
 * The descriptor class for Pep8 violations type.
 */
public final class Pep8Descriptor  extends TypeDescriptor {

    /** The descriptor for the Pep8 violations type. */
    public static final Pep8Descriptor DESCRIPTOR = new Pep8Descriptor();

    private Pep8Descriptor() {
        super("pep8");
    }

    /**
     * Create a parser for the Pep8 type.
     * @return a new Pep8 parser.
     */
    @Override
    public ViolationsParser createParser() {
        return new Pep8Parser();
    }

}

