package org.coode.dlquery;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLClassExpressionComparator;
import org.protege.editor.owl.ui.framelist.ExplainButton;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.inference.OWLReasoner;
import org.semanticweb.owlapi.inference.OWLReasonerAdapter;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
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
 * Bio-Health Informatics Group<br>
 * Date: 27-Feb-2007<br><br>
 */
public class ResultsList extends MList implements LinkedObjectComponent, Copyable {

    private OWLEditorKit owlEditorKit;

    private boolean showSuperClasses;

    private boolean showAncestorClasses;

    private boolean showDescendantClasses;

    private boolean showSubClasses;

    private boolean showInstances;

    private boolean showEquivalentClasses;

    private LinkedObjectComponentMediator mediator;

    private List<ChangeListener> copyListeners = new ArrayList<ChangeListener>();


    public ResultsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        setCellRenderer(new DLQueryListCellRenderer(owlEditorKit));
        explainButton.add(new ExplainButton(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        }));
        mediator = new LinkedObjectComponentMediator(owlEditorKit, this);

        getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : copyListeners){
                    l.stateChanged(ev);
                }
            }
        });
    }


    public boolean isShowAncestorClasses() {
        return showAncestorClasses;
    }


    public void setShowAncestorClasses(boolean showAncestorClasses) {
        this.showAncestorClasses = showAncestorClasses;
    }


    public boolean isShowDescendantClasses() {
        return showDescendantClasses;
    }


    public void setShowDescendantClasses(boolean showDescendantClasses) {
        this.showDescendantClasses = showDescendantClasses;
    }


    public boolean isShowInstances() {
        return showInstances;
    }


    public void setShowInstances(boolean showInstances) {
        this.showInstances = showInstances;
    }


    public boolean isShowSubClasses() {
        return showSubClasses;
    }


    public void setShowSubClasses(boolean showSubClasses) {
        this.showSubClasses = showSubClasses;
    }


    public boolean isShowSuperClasses() {
        return showSuperClasses;
    }


    public void setShowSuperClasses(boolean showSuperClasses) {
        this.showSuperClasses = showSuperClasses;
    }


    public boolean isShowEquivalentClasses() {
        return showEquivalentClasses;
    }


    public void setShowEquivalentClasses(boolean showEquivalentClasses) {
        this.showEquivalentClasses = showEquivalentClasses;
    }


    private List<OWLClass> toSortedList(Set<OWLClass> clses) {
        OWLClassExpressionComparator descriptionComparator = new OWLClassExpressionComparator(owlEditorKit.getModelManager());
        List<OWLClass> list = new ArrayList<OWLClass>(clses);
        Collections.sort(list, descriptionComparator);
        return list;
    }


    public void setOWLClassExpression(OWLClassExpression description) {
        try {
            List data = new ArrayList();
            OWLReasoner reasoner = owlEditorKit.getModelManager().getReasoner();
            if (showEquivalentClasses) {
                final List<OWLClass> results = toSortedList(reasoner.getEquivalentClasses(description));
                data.add(new DLQueryResultsSection("Equivalent classes (" + results.size() + ")"));
                for (OWLClass cls : results) {
                    data.add(new DLQueryResultsSectionItem(cls));
                }
            }
            if (showAncestorClasses) {
                final List<OWLClass> results = toSortedList(OWLReasonerAdapter.flattenSetOfSets(reasoner.getAncestorClasses(description)));
                data.add(new DLQueryResultsSection("Ancestor classes (" + results.size() + ")"));
                for (OWLClass superClass : results) {
                    data.add(new DLQueryResultsSectionItem(superClass));
                }
            }
            if (showSuperClasses) {
                final List<OWLClass> results = toSortedList(OWLReasonerAdapter.flattenSetOfSets(reasoner.getSuperClasses(description)));
                data.add(new DLQueryResultsSection("Super classes (" + results.size() + ")"));
                for (OWLClass superClass : results) {
                    data.add(new DLQueryResultsSectionItem(superClass));
                }
            }
            if (showSubClasses) {
                // flatten and filter out owl:Nothing
                OWLClass owlNothing = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
                final Set<OWLClass> resultSet = new HashSet<OWLClass>();
                for (Set<OWLClass> clsSet : reasoner.getSubClasses(description)){
                    if (!clsSet.contains(owlNothing)){
                        resultSet.addAll(clsSet);
                    }
                }
                final List<OWLClass> results = toSortedList(resultSet);
                data.add(new DLQueryResultsSection("Sub classes (" + results.size() + ")"));
                for (OWLClass subClass : results) {
                    data.add(new DLQueryResultsSectionItem(subClass));
                }
            }
            if (showDescendantClasses) {
                // flatten and filter out owl:Nothing
                OWLClass owlNothing = owlEditorKit.getOWLModelManager().getOWLDataFactory().getOWLNothing();
                final Set<OWLClass> resultSet = new HashSet<OWLClass>();
                for (Set<OWLClass> clsSet : reasoner.getDescendantClasses(description)){
                    if (!clsSet.contains(owlNothing)){
                        resultSet.addAll(clsSet);
                    }
                }
                final List<OWLClass> results = toSortedList(resultSet);
                data.add(new DLQueryResultsSection("Descendant classes (" + results.size() + ")"));
                for (OWLClass cls : results) {
                    data.add(new DLQueryResultsSectionItem(cls));
                }
            }

            if (showInstances) {
                final Set<OWLNamedIndividual> results = reasoner.getIndividuals(description, false);
                data.add(new DLQueryResultsSection("Instances (" + results.size() + ")"));
                for (OWLIndividual ind : results) {
                    data.add(new DLQueryResultsSectionItem(ind));
                }
            }
            setListData(data.toArray());
        }
        catch (OWLReasonerException e) {
            throw new OWLRuntimeException(e);
        }
    }


    private List<MListButton> explainButton = new ArrayList<MListButton>();


    protected List<MListButton> getButtons(Object value) {
        // TODO: Would be nice :)
//        if (value instanceof DLQueryResultsSectionItem) {
//            return explainButton;
//        }
//        else {
        return Collections.emptyList();
//        }
    }


    public JComponent getComponent() {
        return this;
    }


    public OWLObject getLinkedObject() {
        return mediator.getLinkedObject();
    }


    public Point getMouseCellLocation() {
        Rectangle r = getMouseCellRect();
        if (r == null) {
            return null;
        }
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            return null;
        }
        return new Point(mousePos.x - r.x, mousePos.y - r.y);
    }


    public Rectangle getMouseCellRect() {
        Point mousePos = getMousePosition();
        if (mousePos == null) {
            return null;
        }
        int sel = locationToIndex(mousePos);
        if (sel == -1) {
            return null;
        }
        return getCellBounds(sel, sel);
    }


    public void setLinkedObject(OWLObject object) {
        mediator.setLinkedObject(object);
    }


    public boolean canCopy() {
        return getSelectedIndices().length > 0;
    }


    public List<OWLObject> getObjectsToCopy() {
        List<OWLObject> copyObjects = new ArrayList<OWLObject>();
        for (Object sel : getSelectedValues()){
            if (sel instanceof DLQueryResultsSectionItem){
                copyObjects.add(((DLQueryResultsSectionItem)sel).getOWLObject());
            }
        }
        return copyObjects;
    }


    public void addChangeListener(ChangeListener changeListener) {
        copyListeners.add(changeListener);
    }


    public void removeChangeListener(ChangeListener changeListener) {
        copyListeners.remove(changeListener);
    }
}
