package org.coode.dlquery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/09/2013
 */
public enum ResultsSection {

    DIRECT_SUPER_CLASSES("Direct superclasses"),

    DIRECT_SUB_CLASSES("Direct subclasses"),

    SUPER_CLASSES("Superclasses"),

    SUB_CLASSES("Subclasses"),

    EQUIVALENT_CLASSES("Equivalent classes"),

    INSTANCES("Instances");


    private final String displayName;

    private ResultsSection(String displayName) {
        this.displayName = checkNotNull(displayName);
    }

    public String getDisplayName() {
        return displayName;
    }
}
