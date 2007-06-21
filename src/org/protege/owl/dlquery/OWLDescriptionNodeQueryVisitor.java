package org.protege.owl.dlquery;

import org.apache.log4j.Logger;
import org.protege.owl.model.OWLModelManager;
import org.protege.owl.model.description.*;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObject;

import java.util.HashSet;
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
public class OWLDescriptionNodeQueryVisitor<O extends OWLObject> implements OWLDescriptionNodeVisitor {

    private static final Logger logger = Logger.getLogger(OWLDescriptionNodeQueryVisitor.class);

    private OWLModelManager owlModelManager;

    private OWLReasoner reasoner;

    private ReasonerQueryInvoker<O> queryInvoker;

    private Set<O> results;


    public OWLDescriptionNodeQueryVisitor(OWLModelManager manager, OWLReasoner reasoner,
                                          ReasonerQueryInvoker<O> queryInvoker) {
        this.owlModelManager = manager;
        this.reasoner = reasoner;
        this.queryInvoker = queryInvoker;
        results = new HashSet<O>();
    }


    public void reset() {
        results.clear();
    }


    public Set<O> getResults() {
        return new HashSet<O>(results);
    }


    public void visit(OWLDescriptionNodeDifference node) {
        node.getLeftNode().accept(this);
        Set<O> leftResults = results;
        node.getRightNode().accept(this);
        Set<O> rightResults = results;
        results = new HashSet<O>();
        results.addAll(leftResults);
        results.remove(node.getRightNode().getDescription());
        results.removeAll(rightResults);
    }


    public void visit(OWLDescriptionNodeUnion node) {
        node.getLeftNode().accept(this);
        Set<O> leftResults = results;
        node.getRightNode().accept(this);
        Set<O> rightResults = results;
        results = new HashSet<O>();
        results.addAll(leftResults);
        results.addAll(rightResults);
    }


    public void visit(OWLDescriptionNodePossibly node) {
        // LEFTDESC minus not(RIGHTDESC)
        OWLDescription leftDesc = node.getLeftNode().getDescription();
        OWLDescription rightDesc = node.getRightNode().getDescription();
        OWLDescription negRightDesc = owlModelManager.getOWLDataFactory().getOWLObjectComplementOf(rightDesc);

        Set<O> leftResults = queryInvoker.getAnswer(reasoner, leftDesc);
        Set<O> rightResults = queryInvoker.getAnswer(reasoner, negRightDesc);
        results = new HashSet<O>();
        results.addAll(leftResults);
        results.removeAll(rightResults);
    }


    public void visit(OWLDescriptionLeafNode node) {
        results = queryInvoker.getAnswer(reasoner, node.getDescription());
    }
}
