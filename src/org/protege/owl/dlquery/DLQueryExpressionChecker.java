package org.protege.owl.dlquery;

import org.protege.owl.OWLEditorKit;
import org.protege.owl.model.description.OWLDescriptionNode;
import org.protege.owl.model.description.OWLDescriptionNodeParser;
import org.protege.owl.model.description.OWLDescriptionParser;
import org.protege.owl.model.description.OWLExpressionParserException;
import org.protege.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owl.model.OWLException;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 11-Oct-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class DLQueryExpressionChecker implements OWLExpressionChecker<OWLDescriptionNode> {

    private OWLEditorKit owlEditorKit;


    public DLQueryExpressionChecker(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
    }


    public OWLDescriptionNode createObject(String text) throws OWLExpressionParserException, OWLException {
        return ((OWLDescriptionNodeParser) owlEditorKit.getOWLModelManager().getOWLDescriptionParser()).createOWLDescriptionNode(
                text);
    }


    public void check(String text) throws OWLExpressionParserException, OWLException {
        OWLDescriptionParser parser = owlEditorKit.getOWLModelManager().getOWLDescriptionParser();
        if (parser instanceof OWLDescriptionNodeParser) {
            ((OWLDescriptionNodeParser) parser).isWellFormedNode(text);
        }
        else {
            parser.isWellFormed(text);
        }
    }
}
