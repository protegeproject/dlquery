package org.coode.dlquery;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.base.Stopwatch;
import org.protege.editor.core.log.LogBanner;
import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLEditorKitShortFormProvider;
import org.protege.editor.owl.ui.OWLClassExpressionComparator;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.framelist.ExplainButton;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.view.Copyable;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.coode.dlquery.ResultsSection.*;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 27-Feb-2007<br><br>
 */
public class ResultsList extends MList implements LinkedObjectComponent, Copyable {

    private final static Logger logger = LoggerFactory.getLogger(ResultsList.class);

    private final OWLEditorKit owlEditorKit;

    private final Set<ResultsSection> visibleResultsSections = EnumSet.of(SUB_CLASSES);

    private final LinkedObjectComponentMediator mediator;

    private final List<ChangeListener> copyListeners = new ArrayList<>();

    private Predicate<OWLClass> superClassesResultFilter = (OWLClass) -> true;

    private Predicate<OWLClass> directSuperClassesResultFilter = (OWLClass) -> true;

    private Predicate<OWLClass> equivalentClassesResultFilter = (OWLClass) -> true;

    private Predicate<OWLClass> directSubClassesResultFilter = (OWLClass) -> true;

    private Predicate<OWLClass> subClassesResultFilter = (OWLClass) -> true;

    private Predicate<OWLNamedIndividual> instancesResultFilter = (OWLNamedIndividual) -> true;

    private String nameFilter = "";

    private Predicate<OWLEntity> nameResultFilter;

    public ResultsList(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        this.nameResultFilter = (e) -> nameFilter.isEmpty() || owlEditorKit.getOWLModelManager().getRendering(e).contains(nameFilter);
        setCellRenderer(new DLQueryListCellRenderer(owlEditorKit));
        mediator = new LinkedObjectComponentMediator(owlEditorKit, this);
        getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                ChangeEvent ev = new ChangeEvent(ResultsList.this);
                for (ChangeListener l : new ArrayList<>(copyListeners)) {
                    l.stateChanged(ev);
                }
            }
        });
    }

    public void setSuperClassesResultFilter(Predicate<OWLClass> superClassesResultFilter) {
        this.superClassesResultFilter = checkNotNull(superClassesResultFilter);
    }

    public void setDirectSuperClassesResultFilter(Predicate<OWLClass> directSuperClassesResultFilter) {
        this.directSuperClassesResultFilter = checkNotNull(directSuperClassesResultFilter);
    }

    public void setDirectSubClassesResultFilter(Predicate<OWLClass> directSubClassesResultFilter) {
        this.directSubClassesResultFilter = checkNotNull(directSubClassesResultFilter);
    }

    public void setSubClassesResultFilter(Predicate<OWLClass> subClassesResultFilter) {
        this.subClassesResultFilter = checkNotNull(subClassesResultFilter);
    }

    public void setInstancesFilter(Predicate<OWLNamedIndividual> instancesResultFilter) {
        this.instancesResultFilter = checkNotNull(instancesResultFilter);
    }

    public boolean isResultsSectionVisible(ResultsSection section) {
        return visibleResultsSections.contains(section);
    }

    public void setResultsSectionVisible(ResultsSection section, boolean b) {
        if (b) {
            visibleResultsSections.add(section);
        } else {
            visibleResultsSections.remove(section);
        }
    }

    private List<OWLClass> toSortedList(Set<OWLClass> clses) {
        OWLClassExpressionComparator descriptionComparator = new OWLClassExpressionComparator(owlEditorKit.getModelManager());
        List<OWLClass> list = new ArrayList<>(clses);
        Collections.sort(list, descriptionComparator);
        return list;
    }

    public void setOWLClassExpression(OWLClassExpression description) {

        logger.info(LogBanner.start("Executing DL Query"));
        List<Object> data = new ArrayList<>();
        OWLDataFactory factory = owlEditorKit.getOWLModelManager().getOWLDataFactory();
        OWLReasoner reasoner = owlEditorKit.getModelManager().getReasoner();

        addSectionIfVisible(
                EQUIVALENT_CLASSES,
                () -> reasoner.getEquivalentClasses(description).getEntities() ,
                equivalentClassesResultFilter,
                (cls) -> factory.getOWLEquivalentClassesAxiom(description, cls),
                data
        );
        addSectionIfVisible(
                SUPER_CLASSES,
                () -> reasoner.getSuperClasses(description, false).getFlattened(),
                superClassesResultFilter,
                (superCls) -> factory.getOWLSubClassOfAxiom(description, superCls),
                data
        );
        addSectionIfVisible(
                DIRECT_SUPER_CLASSES,
                () -> reasoner.getSuperClasses(description, true).getFlattened(),
                directSuperClassesResultFilter,
                (superCls) -> factory.getOWLSubClassOfAxiom(description, superCls),
                data
        );
        addSectionIfVisible(
                DIRECT_SUB_CLASSES,
                () -> reasoner.getSubClasses(description, true).getFlattened(),
                directSubClassesResultFilter,
                (subCls) -> factory.getOWLSubClassOfAxiom(subCls, description),
                data
        );
        addSectionIfVisible(
                SUB_CLASSES,
                () -> reasoner.getSubClasses(description, false).getFlattened(),
                subClassesResultFilter,
                (subCls) -> factory.getOWLSubClassOfAxiom(subCls, description),
                data
        );
        addSectionIfVisible(
                INSTANCES,
                () -> reasoner.getInstances(description, false).getFlattened(),
                instancesResultFilter,
                (instance) -> factory.getOWLClassAssertionAxiom(description, instance),
                data
        );
        setListData(data.toArray());
        logger.info(LogBanner.end());
    }

    private <E extends OWLEntity> void addSectionIfVisible(ResultsSection section, Supplier<Collection<E>> reasoner, Predicate<E> filter, Function<E, OWLAxiom> axiomFactory, List<Object> data) {
        if(!isResultsSectionVisible(section)) {
            return;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        Collection<E> results = reasoner.get();
        stopwatch.stop();
        logger.info("Computed results for {} in {} ms", section.getDisplayName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        List<Object> resultsList = results.stream()
                .filter(filter)
                .filter(nameResultFilter)
                .sorted(new OWLEntityComparator(new OWLEditorKitShortFormProvider(owlEditorKit)))
                .map(e -> new DLQueryResultsSectionItem(e, axiomFactory.apply(e)))
                .collect(toList());
        data.add(new DLQueryResultsSection(String.format("%s (%d of %d)", section.getDisplayName(), resultsList.size(), results.size())));
        data.addAll(resultsList);
    }

    protected List<MListButton> getButtons(Object value) {
        if (!(value instanceof DLQueryResultsSectionItem)) {
            return Collections.emptyList();
        }
        final OWLAxiom axiom = ((DLQueryResultsSectionItem) value).getAxiom();
        ExplanationManager explanationManager = owlEditorKit.getOWLModelManager().getExplanationManager();
        if (!explanationManager.hasExplanation(axiom)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(
                new ExplainButton(e -> {
                    Frame parent = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, ResultsList.this);
                    explanationManager.handleExplain(parent, axiom);
                })
        );
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
        List<OWLObject> copyObjects = new ArrayList<>();
        for (Object sel : getSelectedValuesList()) {
            if (sel instanceof DLQueryResultsSectionItem) {
                copyObjects.add(((DLQueryResultsSectionItem) sel).getOWLObject());
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

    public void setNameFilter(String nameFilter) {
        this.nameFilter = checkNotNull(nameFilter);
    }
}
