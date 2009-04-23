package org.coode.dlquery;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.description.*;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLObject;

import java.util.HashSet;
import java.util.Set;
/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


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
        results.remove(node.getRightNode().getClassExpression());
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
        OWLClassExpression leftDesc = node.getLeftNode().getClassExpression();
        OWLClassExpression rightDesc = node.getRightNode().getClassExpression();
        OWLClassExpression negRightDesc = owlModelManager.getOWLDataFactory().getOWLObjectComplementOf(rightDesc);

        Set<O> leftResults = queryInvoker.getAnswer(reasoner, leftDesc);
        Set<O> rightResults = queryInvoker.getAnswer(reasoner, negRightDesc);
        results = new HashSet<O>();
        results.addAll(leftResults);
        results.removeAll(rightResults);
    }


    public void visit(OWLDescriptionLeafNode node) {
        results = queryInvoker.getAnswer(reasoner, node.getClassExpression());
    }
}
