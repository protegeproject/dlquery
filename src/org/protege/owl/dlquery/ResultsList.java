package org.protege.owl.dlquery;

import org.protege.core.ui.list.MList;
import org.protege.owl.OWLEditorKit;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLRuntimeException;

import java.util.ArrayList;
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
public class ResultsList extends MList {

    private OWLEditorKit owlEditorKit;


    public ResultsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        setCellRenderer(new DLQueryListCellRenderer(owlEditorKit));
    }


    public void setOWLDescription(OWLDescription description) {
        try {
            List data = new ArrayList();
            OWLReasoner reasoner = owlEditorKit.getOWLModelManager().getReasoner();
            data.add(new DLQueryResultsSection("Super classes"));
            for (OWLClass superClass : OWLReasonerAdapter.flattenSetOfSets(reasoner.getSuperClasses(description))) {
                data.add(new DLQueryResultsSectionItem(superClass));
            }
            data.add(new DLQueryResultsSection("Sub classes"));
            for (OWLClass subClass : OWLReasonerAdapter.flattenSetOfSets(reasoner.getSubClasses(description))) {
                data.add(new DLQueryResultsSectionItem(subClass));
            }

            data.add(new DLQueryResultsSection("Instances"));
            for (OWLIndividual ind : reasoner.getIndividuals(description, true)) {
                data.add(new DLQueryResultsSectionItem(ind));
            }
            setListData(data.toArray());
        }
        catch (OWLReasonerException e) {
            throw new OWLRuntimeException(e);
        }
    }
}
