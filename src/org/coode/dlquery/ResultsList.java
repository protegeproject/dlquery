package org.coode.dlquery;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLDescriptionComparator;
import org.protege.editor.owl.ui.framelist.ExplainButton;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * Bio-Health Informatics Group<br>
 * Date: 27-Feb-2007<br><br>
 */
public class ResultsList extends MList implements LinkedObjectComponent {

    private OWLEditorKit owlEditorKit;

    private boolean showSuperClasses;

    private boolean showAncestorClasses;

    private boolean showDescendantClasses;

    private boolean showSubClasses;

    private boolean showInstances;

    private boolean showEquivalentClasses;

    private LinkedObjectComponentMediator mediator;


    public ResultsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        setCellRenderer(new DLQueryListCellRenderer(owlEditorKit));
        explainButton.add(new ExplainButton(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        }));
        mediator = new LinkedObjectComponentMediator(owlEditorKit, this);
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
        OWLDescriptionComparator descriptionComparator = new OWLDescriptionComparator(owlEditorKit.getOWLModelManager());
        List<OWLClass> list = new ArrayList<OWLClass>(clses);
        Collections.sort(list, descriptionComparator);
        return list;
    }


    public void setOWLDescription(OWLDescription description) {
        try {
            List data = new ArrayList();
            OWLReasoner reasoner = owlEditorKit.getOWLModelManager().getReasoner();
            if (showEquivalentClasses) {
                data.add(new DLQueryResultsSection("Equivalent classes"));
                for (OWLClass cls : toSortedList(reasoner.getEquivalentClasses(description))) {
                    data.add(new DLQueryResultsSectionItem(cls));
                }
            }
            if (showAncestorClasses) {
                data.add(new DLQueryResultsSection("Ancestor classes"));
                for (OWLClass superClass : toSortedList(OWLReasonerAdapter.flattenSetOfSets(reasoner.getAncestorClasses(
                        description)))) {
                    data.add(new DLQueryResultsSectionItem(superClass));
                }
            }
            if (showSuperClasses) {
                data.add(new DLQueryResultsSection("Super classes"));
                for (OWLClass superClass : toSortedList(OWLReasonerAdapter.flattenSetOfSets(reasoner.getSuperClasses(
                        description)))) {
                    data.add(new DLQueryResultsSectionItem(superClass));
                }
            }
            if (showSubClasses) {
                data.add(new DLQueryResultsSection("Sub classes"));
                for (OWLClass subClass : toSortedList(OWLReasonerAdapter.flattenSetOfSets(reasoner.getSubClasses(
                        description)))) {
                    data.add(new DLQueryResultsSectionItem(subClass));
                }
            }

            if (showDescendantClasses) {
                data.add(new DLQueryResultsSection("Descendant classes"));
                for (OWLClass cls : toSortedList(OWLReasonerAdapter.flattenSetOfSets(reasoner.getDescendantClasses(
                        description)))) {
                    data.add(new DLQueryResultsSectionItem(cls));
                }
            }

            if (showInstances) {
                data.add(new DLQueryResultsSection("Instances"));
                for (OWLIndividual ind : reasoner.getIndividuals(description, false)) {
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
}
