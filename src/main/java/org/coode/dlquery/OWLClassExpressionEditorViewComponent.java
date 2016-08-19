package org.coode.dlquery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.*;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.cache.OWLExpressionUserCache;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.OWLReasonerManager;
import org.protege.editor.owl.model.inference.ReasonerUtilities;
import org.protege.editor.owl.ui.CreateDefinedClassPanel;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static org.coode.dlquery.ResultsSection.*;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 22-Aug-2006<br><br>
 *
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLClassExpressionEditorViewComponent extends AbstractOWLViewComponent {


    private static final Marker marker = MarkerFactory.getMarker("DL Query");

    private static final Logger logger = LoggerFactory.getLogger(OWLClassExpressionEditorViewComponent.class);



    public static final String SHOW_OWL_THING_IN_RESULTS_KEY = "showOWLThingInResults";

    public static final String SHOW_OWL_NOTHING_IN_RESULTS_KEY = "showOWLNothingInResults";


    private final JCheckBox showOWLThingInResults = new JCheckBox("<html><body>Display owl:Thing<br><span style=\"color: #808080; font-size: 0.8em;\">(in superclass results)</span></body></html>");

    private final JCheckBox showOWLNothingInResults = new JCheckBox("<html><body>Display owl:Nothing<br><span style=\"color: #808080; font-size: 0.8em;\">(in subclass results)</span></body></html>");


    private ExpressionEditor<OWLClassExpression> owlDescriptionEditor;

    private ResultsList resultsList;

    private final JCheckBox showDirectSuperClassesCheckBox = new JCheckBox(DIRECT_SUPER_CLASSES.getDisplayName());

    private final JCheckBox showSuperClassesCheckBox = new JCheckBox(SUPER_CLASSES.getDisplayName());

    private final JCheckBox showEquivalentClassesCheckBox = new JCheckBox(EQUIVALENT_CLASSES.getDisplayName());

    private final JCheckBox showDirectSubClassesCheckBox = new JCheckBox(DIRECT_SUB_CLASSES.getDisplayName());

    private final JCheckBox showSubClassesCheckBox = new JCheckBox(SUB_CLASSES.getDisplayName());

    private final JCheckBox showIndividualsCheckBox = new JCheckBox(INSTANCES.getDisplayName());

    private final JButton executeButton = new JButton("Execute");

    private final JButton addButton = new JButton("Add to ontology");

    private final OWLModelManagerListener listener = event -> {
        if (event.isType(EventType.ONTOLOGY_CLASSIFIED)) {
            doQuery();
        }
    };

    private boolean requiresRefresh = false;

    private final Predicate<OWLClass> supersFilter = cls -> !cls.isOWLThing() || showOWLThingInResults.isSelected();

    private final Predicate<OWLClass> subsFilter = cls -> !cls.isOWLNothing() || showOWLNothingInResults.isSelected();


    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout(10, 10));

        showOWLThingInResults.setVerticalTextPosition(SwingConstants.TOP);
        showOWLNothingInResults.setVerticalTextPosition(SwingConstants.TOP);

        JComponent editorPanel = createQueryPanel();
        JComponent resultsPanel = createResultsPanel();
        JComponent optionsBox = createOptionsBox();
        JPanel optionsBoxHolder = new JPanel(new BorderLayout());
        optionsBoxHolder.add(optionsBox, BorderLayout.NORTH);
        resultsPanel.add(optionsBoxHolder, BorderLayout.EAST);

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, resultsPanel);
        splitter.setDividerLocation(0.3);

        add(splitter, BorderLayout.CENTER);

        updateGUI();

        getOWLModelManager().addListener(listener);

        addHierarchyListener(event -> {
            if (requiresRefresh && isShowing()) {
                doQuery();
            }
        });
    }


    private JComponent createQueryPanel() {
        JPanel editorPanel = new JPanel(new BorderLayout());

        final OWLExpressionChecker<OWLClassExpression> checker = getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker();
        owlDescriptionEditor = new ExpressionEditor<>(getOWLEditorKit(), checker);
        owlDescriptionEditor.addStatusChangedListener(newState -> {
            executeButton.setEnabled(newState);
            addButton.setEnabled(newState);
        });
        owlDescriptionEditor.setPreferredSize(new Dimension(100, 50));

        editorPanel.add(ComponentFactory.createScrollPane(owlDescriptionEditor), BorderLayout.CENTER);
        JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.LEFT));
        executeButton.addActionListener(e -> doQuery());

        addButton.addActionListener(e -> doAdd());

        buttonHolder.add(executeButton);
        buttonHolder.add(addButton);

        editorPanel.add(buttonHolder, BorderLayout.SOUTH);
        editorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(),
                        "Query (class expression)"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        return editorPanel;
    }


    private JComponent createResultsPanel() {
        JComponent resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Query results"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        resultsList = new ResultsList(getOWLEditorKit());
        resultsPanel.add(ComponentFactory.createScrollPane(resultsList));
        return resultsPanel;
    }


    private JComponent createOptionsBox() {
        Box optionsBox = new Box(BoxLayout.Y_AXIS);
        showDirectSuperClassesCheckBox.addActionListener(e -> {
            resultsList.setResultsSectionVisible(DIRECT_SUPER_CLASSES, showDirectSuperClassesCheckBox.isSelected());
            doQuery();
        });
        optionsBox.add(showDirectSuperClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showSuperClassesCheckBox.addActionListener(e -> {
            resultsList.setResultsSectionVisible(SUPER_CLASSES, showSuperClassesCheckBox.isSelected());
            doQuery();
        });
        showSuperClassesCheckBox.setSelected(false);
        optionsBox.add(showSuperClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showEquivalentClassesCheckBox.addActionListener(e -> {
            resultsList.setResultsSectionVisible(EQUIVALENT_CLASSES, showEquivalentClassesCheckBox.isSelected());
            doQuery();
        });
        optionsBox.add(showEquivalentClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showDirectSubClassesCheckBox.addActionListener(e -> {
            resultsList.setResultsSectionVisible(DIRECT_SUB_CLASSES, showDirectSubClassesCheckBox.isSelected());
            doQuery();
        });
        optionsBox.add(showDirectSubClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showSubClassesCheckBox.addActionListener(e -> {
            resultsList.setResultsSectionVisible(SUB_CLASSES, showSubClassesCheckBox.isSelected());
            doQuery();
        });
        showSubClassesCheckBox.setSelected(false);
        optionsBox.add(showSubClassesCheckBox);
        optionsBox.add(Box.createVerticalStrut(3));

        showIndividualsCheckBox.addActionListener(e -> {
            resultsList.setResultsSectionVisible(INSTANCES, showIndividualsCheckBox.isSelected());
            doQuery();
        });

        optionsBox.add(showIndividualsCheckBox);
        optionsBox.add(Box.createVerticalStrut(20));
        optionsBox.add(new JSeparator());
        optionsBox.add(Box.createVerticalStrut(20));
        optionsBox.add(showOWLThingInResults);
        Preferences preferences = PreferencesManager.getInstance().getApplicationPreferences("DLQuery");

        showOWLThingInResults.setSelected(preferences.getBoolean(SHOW_OWL_THING_IN_RESULTS_KEY, true));
        showOWLThingInResults.addActionListener(e -> {
            preferences.putBoolean(SHOW_OWL_THING_IN_RESULTS_KEY, showOWLThingInResults.isSelected());
            doQuery();
        });
        optionsBox.add(Box.createVerticalStrut(3));
        optionsBox.add(showOWLNothingInResults);
        showOWLNothingInResults.setSelected(preferences.getBoolean(SHOW_OWL_NOTHING_IN_RESULTS_KEY, true));
        showOWLNothingInResults.addActionListener(e -> {
            preferences.putBoolean(SHOW_OWL_NOTHING_IN_RESULTS_KEY, showOWLNothingInResults.isSelected());
            doQuery();
        });
        return optionsBox;
    }


    protected void disposeOWLView() {
        getOWLModelManager().removeListener(listener);
    }


    private void updateGUI() {
        showDirectSuperClassesCheckBox.setSelected(resultsList.isResultsSectionVisible(DIRECT_SUPER_CLASSES));
        showSuperClassesCheckBox.setSelected(resultsList.isResultsSectionVisible(SUPER_CLASSES));
        showEquivalentClassesCheckBox.setSelected(resultsList.isResultsSectionVisible(EQUIVALENT_CLASSES));
        showDirectSubClassesCheckBox.setSelected(resultsList.isResultsSectionVisible(DIRECT_SUB_CLASSES));
        showSubClassesCheckBox.setSelected(resultsList.isResultsSectionVisible(SUB_CLASSES));
        showIndividualsCheckBox.setSelected(resultsList.isResultsSectionVisible(INSTANCES));
    }


    private void doQuery() {
        if (isShowing()) {
            try {
                OWLReasonerManager reasonerManager = getOWLModelManager().getOWLReasonerManager();
                ReasonerUtilities.warnUserIfReasonerIsNotConfigured(this, reasonerManager);

                OWLClassExpression desc = owlDescriptionEditor.createObject();
                if (desc != null) {
                    OWLExpressionUserCache.getInstance(getOWLModelManager()).add(desc, owlDescriptionEditor.getText());
                    resultsList.setSuperClassesResultFilter(supersFilter);
                    resultsList.setDirectSuperClassesResultFilter(supersFilter);

                    resultsList.setDirectSubClassesResultFilter(subsFilter);
                    resultsList.setSubClassesResultFilter(subsFilter);

                    resultsList.setOWLClassExpression(desc);
                }
            } catch (OWLException e) {
                logger.error(marker, "An error occurred whilst executing the DL query: {}", e.getMessage(), e);
            }
            requiresRefresh = false;
        }
        else {
            requiresRefresh = true;
        }
    }


    private void doAdd() {
        try {
            OWLClassExpression desc = owlDescriptionEditor.createObject();
            OWLEntityCreationSet<OWLClass> creationSet = CreateDefinedClassPanel.showDialog(desc, getOWLEditorKit());
            if (creationSet != null) {
                List<OWLOntologyChange> changes = new ArrayList<>(creationSet.getOntologyChanges());
                OWLDataFactory factory = getOWLModelManager().getOWLDataFactory();
                OWLAxiom equiv = factory.getOWLEquivalentClassesAxiom(creationSet.getOWLEntity(), desc);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), equiv));
                getOWLModelManager().applyChanges(changes);
                if (isSynchronizing()) {
                    getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(creationSet.getOWLEntity());
                }
            }
        } catch (OWLException e) {
            logger.error(marker, "An error occurred whilst adding the class definition: {}", e.getMessage(), e);
        }
    }
}
