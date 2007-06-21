package org.protege.owl.dlquery;

import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;

import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 11-Oct-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface ReasonerQueryInvoker<O extends OWLObject> {

    public Set<O> getAnswer(OWLReasoner reasoner, OWLDescription description);
}
