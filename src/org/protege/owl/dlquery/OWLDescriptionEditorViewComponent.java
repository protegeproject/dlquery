package org.protege.owl.dlquery;

import org.protege.core.ui.util.ComponentFactory;
import org.protege.owl.model.description.OWLDescriptionParser;
import org.protege.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.owl.ui.clsdescriptioneditor.OWLDescriptionChecker;
import org.protege.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 22-Aug-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLDescriptionEditorViewComponent extends AbstractOWLViewComponent {

    private ExpressionEditor owlDescriptionEditor;

    private ResultsList resultsList;

    private JCheckBox showSuperClassesCheckBox;

    private JCheckBox showAncestorClassesCheckBox;

    private JCheckBox showEquivalentClassesCheckBox;

    private JCheckBox showSubClassesCheckBox;

    private JCheckBox showDescendantClassesCheckBox;

    private JCheckBox showIndividualsCheckBox;


    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout(10, 10));
        JPanel editorPanel = new JPanel(new BorderLayout());
        owlDescriptionEditor = new ExpressionEditor<OWLDescription>(getOWLEditorKit(),
                                                                    new OWLDescriptionChecker(getOWLEditorKit()));
        owlDescriptionEditor.setPreferredSize(new Dimension(100, 50));
        editorPanel.add(ComponentFactory.createScrollPane(owlDescriptionEditor), BorderLayout.CENTER);
        JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonHolder.add(new JButton(new AbstractAction("Execute") {
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        }), BorderLayout.NORTH);
        editorPanel.add(buttonHolder, BorderLayout.SOUTH);
        editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Query (class expression)"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(editorPanel, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.LIGHT_GRAY), "Query results"), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(resultsPanel);
        resultsList = new ResultsList(getOWLEditorKit());
        resultsPanel.add(ComponentFactory.createScrollPane(resultsList));
        Box optionsBox = new Box(BoxLayout.Y_AXIS);
        resultsPanel.add(optionsBox, BorderLayout.EAST);
        showSuperClassesCheckBox = new JCheckBox(new AbstractAction("Super classes") {
            public void actionPerformed(ActionEvent e) {
                resultsList.setShowSuperClasses(showSuperClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showSuperClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showAncestorClassesCheckBox = new JCheckBox(new AbstractAction("Ancestor classes") {
            public void actionPerformed(ActionEvent e) {
                resultsList.setShowAncestorClasses(showAncestorClassesCheckBox.isSelected());
                doQuery();
            }
        });
        showAncestorClassesCheckBox.setSelected(false);
        optionsBox.add(showAncestorClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showEquivalentClassesCheckBox = new JCheckBox(new AbstractAction("Equivalent classes") {
            public void actionPerformed(ActionEvent e) {
                resultsList.setShowEquivalentClasses(showEquivalentClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showEquivalentClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showSubClassesCheckBox = new JCheckBox(new AbstractAction("Subclasses") {
            public void actionPerformed(ActionEvent e) {
                resultsList.setShowSubClasses(showSubClassesCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showSubClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showDescendantClassesCheckBox = new JCheckBox(new AbstractAction("Descendant classes") {
            public void actionPerformed(ActionEvent e) {
                resultsList.setShowDescendantClasses(showDescendantClassesCheckBox.isSelected());
                doQuery();
            }
        });
        showDescendantClassesCheckBox.setSelected(false);
        optionsBox.add(showDescendantClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showIndividualsCheckBox = new JCheckBox(new AbstractAction("Individuals") {
            public void actionPerformed(ActionEvent e) {
                resultsList.setShowInstances(showIndividualsCheckBox.isSelected());
                doQuery();
            }
        });
        optionsBox.add(showIndividualsCheckBox);
        updateGUI();
    }


    protected void disposeOWLView() {

    }


    private void updateGUI() {

        showSuperClassesCheckBox.setSelected(resultsList.isShowSuperClasses());
        showAncestorClassesCheckBox.setSelected(resultsList.isShowAncestorClasses());
        showEquivalentClassesCheckBox.setSelected(resultsList.isShowEquivalentClasses());
        showSubClassesCheckBox.setSelected(resultsList.isShowSubClasses());
        showDescendantClassesCheckBox.setSelected(resultsList.isShowDescendantClasses());
        showIndividualsCheckBox.setSelected(resultsList.isShowInstances());
    }


    private void doQuery() {
        try {
            if (!getOWLModelManager().getReasoner().isClassified()) {
                JOptionPane.showMessageDialog(this,
                                              "The reasoner is not syncronised.  This may produce misleading results.",
                                              "Reasoner out of sync",
                                              JOptionPane.WARNING_MESSAGE);
            }

            String exp = owlDescriptionEditor.getText();
            System.out.println(exp);

            OWLDescriptionParser parser = getOWLModelManager().getOWLDescriptionParser();
//            if(parser instanceof OWLDescriptionNodeParser) {
//                if(((OWLDescriptionNodeParser) parser).isWellFormedNode(exp)) {
//                    OWLDescriptionNode node = ((OWLDescriptionNodeParser) parser).createOWLDescriptionNode(exp);
//                    handle(node);
//                }
//            }
//            else {
            if (parser.isWellFormed(exp)) {
                OWLDescription desc = parser.createOWLDescription(exp);
                resultsList.setOWLDescription(desc);
            }
//            }

        }
        catch (OWLException e) {
            // Don't need to do anything here except disable the execute button

        }
    }

//    private void handle(OWLDescriptionNode node) {
//        System.out.println("Handle: " + node);
//        try {
//            resultsList.setOWLDescriptionNode(node);
//        } catch (OWLException e) {
//            e.printStackTrace();
//        }
//
//    }

}
