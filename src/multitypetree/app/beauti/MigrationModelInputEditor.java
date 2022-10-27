/*
 * Copyright (C) 2015 Tim Vaughan (tgvaughan@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package multitypetree.app.beauti;

import beast.base.core.BEASTInterface;
import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import beastfx.app.inputeditor.BEASTObjectInputEditor;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.inputeditor.InputEditor;
import beastfx.app.util.Alert;
import beastfx.app.util.FXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import multitypetree.evolution.tree.SCMigrationModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A BEAUti input editor for MigrationModels.
 *
 * @author Tim Vaughan (tgvaughan@gmail.com)
 */
public class MigrationModelInputEditor extends BEASTObjectInputEditor { //extends InputEditor.Base {

    private ObservableList<Double> popSize;
    private ObservableList<double[]> rateMatrix;
    private ObservableList<String> fullTypeListModel, additionalTypeListModel;
    private MultipleSelectionModel<String> additionalTypeListSelectionModel;
    private SCMigrationModel migModel;

    private Button addTypeButton, remTypeButton, addTypesFromFileButton;
    private Button loadPopSizesFromFileButton, loadMigRatesFromFileButton;

    private CheckBox popSizeEstCheckBox, popSizeScaleFactorEstCheckBox;
    private CheckBox rateMatrixEstCheckBox, rateMatrixScaleFactorEstCheckBox, rateMatrixForwardTimeCheckBox;

    boolean fileLoadInProgress = false;

    List<String> rowNames = new ArrayList<>();

    public MigrationModelInputEditor(BeautiDoc doc) {
        super(doc);
    }

    @Override
    public Class<?> type() {
        return SCMigrationModel.class;
    }

    @Override
    public void init(Input<?> input, BEASTInterface beastObject, int itemNr,
        ExpandOption isExpandOption, boolean addButtons) {
        // TODO too much works to do if not call super
        super.init(input, beastObject, itemNr, ExpandOption.TRUE, addButtons);

        pane = new HBox();
        pane.setPadding(new Insets(5));

        // Set up fields
//        m_bAddButtons = addButtons;
//        m_input = input;
//        m_beastObject = beastObject;
//		this.itemNr = itemNr;

        // Adds label to left of input editor
        addInputLabel();

        // Create component models and fill them with data from input
        migModel = (SCMigrationModel) input.get();
        fullTypeListModel = FXCollections.observableArrayList();;
        additionalTypeListModel = FXCollections.observableArrayList();
//        popSizeModel =  popSizeTable.getSelectionModel();
//        rateMatrixModel = new DefaultTableModel() {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return row != column && column != migModel.getNTypes();
//            }
//        };
        popSizeEstCheckBox = new CheckBox("estimate pop. sizes");
        rateMatrixEstCheckBox = new CheckBox("estimate mig. rates");
        popSizeScaleFactorEstCheckBox = new CheckBox("estimate scale factor");
        rateMatrixScaleFactorEstCheckBox = new CheckBox("estimate scale factor");
        rateMatrixForwardTimeCheckBox = new CheckBox("forward-time rate matrix");

        Label label = new Label("<html><body>Type list:</body></html>");
        label.setPadding(new Insets(3, 3, 3, 3));

//        GridPane panel = new GridPane();
//        panel.setBorder(new EtchedBorder());
        VBox box = new VBox();

        HBox tlBox = FXUtils.newHBox();
        VBox tlBoxLeft = FXUtils.newVBox();

        Label labelLeft = new Label("All types");
        tlBoxLeft.getChildren().add(labelLeft);
        ListView<String> jlist = new ListView<>();
        jlist.setOrientation(Orientation.HORIZONTAL);
        jlist.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//        jlist.setSelectionModel(new DefaultListSelectionModel() {
//            @Override
//            public void setSelectionInterval(int index0, int index1) {
//                super.setSelectionInterval(-1, -1);
//            }
//        });
        ScrollPane listScrollPane = new ScrollPane(jlist);
        listScrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
        tlBoxLeft.getChildren().add(listScrollPane);
        tlBox.getChildren().add(tlBoxLeft);

        VBox tlBoxRight = FXUtils.newVBox();
        Label labelRight = new Label("Additional types");
        tlBoxRight.getChildren().add(labelRight);
        ListView<String> jlistAdditional = new ListView<>();
        jlistAdditional.setOrientation(Orientation.HORIZONTAL);
        jlistAdditional.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        additionalTypeListSelectionModel = jlistAdditional.getSelectionModel();

        additionalTypeListSelectionModel.selectedItemProperty().addListener(e -> {
            if (additionalTypeListSelectionModel.isEmpty())
                remTypeButton.setDisable(true);
            else
                remTypeButton.setDisable(false);
        });

        listScrollPane = new ScrollPane(jlistAdditional);
        listScrollPane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
        tlBoxRight.getChildren().add(listScrollPane);
        HBox addRemBox = FXUtils.newHBox();
        addTypeButton = new Button("+");
        remTypeButton = new Button("-");
        remTypeButton.setDisable(true);
        addTypesFromFileButton = new Button("Add from file...");
        addRemBox.getChildren().add(addTypeButton);
        addRemBox.getChildren().add(remTypeButton);
        addRemBox.getChildren().add(addTypesFromFileButton);
        tlBoxRight.getChildren().add(addRemBox);
        tlBox.getChildren().add(tlBoxRight);

//        c.gridx = 1;
//        c.gridy = 0;
//        c.weightx = 1.0;
//        c.anchor = GridBagConstraints.LINE_START;
//        panel.add(tlBox, 1, 0);
        box.getChildren().add(tlBox);
        // Population size table
//        c.gridx = 0;
//        c.gridy = 1;
//        c.weightx = 0.0;
//        c.anchor = GridBagConstraints.LINE_END;
        VBox  psBox = FXUtils.newVBox();
        psBox.getChildren().add(new Label("Population sizes: "));
        loadPopSizesFromFileButton = new Button("Load from file...");
        psBox.getChildren().add(loadPopSizesFromFileButton);
//        panel.add(psBox, 0, 1);
        box.getChildren().add(psBox);

        TableView<Double> popSizeTable = new TableView<>();
        popSizeTable.setEditable(true);
        popSizeTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        popSize = FXCollections.observableArrayList(List.of(1.0, 1.0));//TODO
        // add columns
        for (int i = 0; i < popSize.size(); i++) {
            TableColumn<Double, Object> column = new TableColumn<>();
            popSizeTable.getColumns().add(column);
        }
        // add data
        popSizeTable.setItems(popSize);

//        Pane header = (Pane) popSizeTable.lookup("TableHeaderRow");
//        header.setVisible(false);
//        popSizeTable.setLayoutY(-header.getHeight());
//        popSizeTable.autosize();


//            @Override
//            public TableCellRenderer getCellRenderer(int row, int column) {
//                return new DefaultTableCellRenderer() {
//                    @Override
//                    public Component getTableCellRendererComponent(
//                            JTable table, Object value, boolean isSelected,
//                            boolean hasFocus, int row, int column) {
//                        setHorizontalAlignment(SwingConstants.CENTER);
//                        return super.getTableCellRendererComponent(
//                                table, value, isSelected, hasFocus, row, column);
//                    }
//                };
//            }

//        popSizeTable.setShowVerticalLines(true);
//        popSizeTable.setCellSelectionEnabled(true);
//        popSizeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        popSizeTable.setMaximumSize(new Dimension(100, Short.MAX_VALUE));

//        c.gridx = 1;
//        c.gridy = 1;
//        c.weightx = 1.0;
//        c.anchor = GridBagConstraints.LINE_START;
//        panel.add(popSizeTable, 1, 1);
        box.getChildren().add(popSizeTable);

        popSizeEstCheckBox.setSelected(((RealParameter)migModel.popSizesInput.get()).isEstimatedInput.get());
        popSizeScaleFactorEstCheckBox.setSelected(((RealParameter)migModel.popSizesScaleFactorInput.get()).isEstimatedInput.get());
//        c.gridx = 2;
//        c.gridy = 1;
//        c.anchor = GridBagConstraints.LINE_END;
//        c.weightx = 1.0;
        VBox estBox = FXUtils.newVBox();
        estBox.getChildren().add(popSizeEstCheckBox);
        estBox.getChildren().add(popSizeScaleFactorEstCheckBox);
//        panel.add(estBox, 2, 1);
        box.getChildren().add(estBox);

        // Migration rate table
        // (Uses custom cell renderer to grey out diagonal elements.)
//        c.gridx = 0;
//        c.gridy = 2;
//        c.weightx = 0.0;
//        c.anchor = GridBagConstraints.LINE_END;
        VBox mrBox = FXUtils.newVBox();
        mrBox.getChildren().add(new Label("Migration rates: "));
        loadMigRatesFromFileButton = new Button("Load from file...");
        mrBox.getChildren().add(loadMigRatesFromFileButton);
//        panel.add(mrBox, 0, 2);
        box.getChildren().add(mrBox);

        TableView<double[]> rateMatrixTable = new TableView<>();
        rateMatrixTable.setEditable(true);
        rateMatrixTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        header = (Pane) rateMatrixTable.lookup("TableHeaderRow");
//        header.setVisible(false);
//        rateMatrixTable.setLayoutY(-header.getHeight());
//        rateMatrixTable.autosize();

        //TODO
        rateMatrix = FXCollections.observableArrayList();
        for (int i=0; i < migModel.getNTypes(); i++) {
            double[] rate1row = new double[migModel.getNTypes()];
            for (int j=0; j < migModel.getNTypes(); j++) {
                if (i == j)
                    continue;

                rate1row[j] = migModel.getBackwardRate(i, j);
            }
            rateMatrix.add(rate1row);
        }

        // add columns
        for (int i = 0; i < rateMatrix.size(); i++) {
            TableColumn<double[], Double> column = new TableColumn<>();
            rateMatrixTable.getColumns().add(column);

            // add data
            rateMatrixTable.getItems().add(rateMatrix.get(i));
        }

        loadFromMigrationModel();

//            @Override
//            public TableCellRenderer getCellRenderer(int row, int column) {
//
//                return new DefaultTableCellRenderer() {
//                            @Override
//                            public Component getTableCellRendererComponent(
//                                    JTable table, Object value, boolean isSelected,
//                                    boolean hasFocus, int row, int column) {
//
//                                if (row == column) {
//                                    Label label = new Label();
//                                    label.setOpaque(true);
//                                    label.setBackground(Color.GRAY);
//
//                                    return label;
//
//                                } else {
//
//                                    Component c = super.getTableCellRendererComponent(
//                                        table, value, isSelected, hasFocus, row, column);
//
//                                    if (column == migModel.getNTypes()) {
//                                        c.setBackground(panel.getBackground());
//                                        c.setForeground(Color.gray);
//                                        setHorizontalAlignment(SwingConstants.LEFT);
//                                    } else {
//                                        int l = 1, r = 1, t = 1, b=1;
//                                        if (column>0)
//                                            l = 0;
//                                        if (row>0)
//                                            t = 0;
//
//                                        setBorder(BorderFactory.createMatteBorder(t, l, b, r, Color.GRAY));
//                                        setHorizontalAlignment(SwingConstants.CENTER);
//                                    }
//                                    return c;
//                                }
//                            }
//                };
//            }
//
//        rateMatrixTable.setShowGrid(false);
//        rateMatrixTable.setIntercellSpacing(new Dimension(0,0));
//        rateMatrixTable.setCellSelectionEnabled(true);
//        rateMatrixTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        rateMatrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        TableColumn col = rateMatrixTable.getColumnModel().getColumn(migModel.getNTypes());
//
//        FontMetrics metrics = new Canvas().getFontMetrics(getFont());
//        int maxWidth = 0;
//        for (String rowName : rowNames)
//            maxWidth = Math.max(maxWidth, metrics.stringWidth(rowName + "M"));
//        col.setPreferredWidth(maxWidth);

//        c.gridx = 1;
//        c.gridy = 2;
//        c.anchor = GridBagConstraints.LINE_START;
//        c.weightx = 1.0;
//        panel.add(rateMatrixTable, 1, 2);
        box.getChildren().add(rateMatrixTable);

        rateMatrixEstCheckBox.setSelected(((RealParameter)migModel.rateMatrixInput.get()).isEstimatedInput.get());
        rateMatrixScaleFactorEstCheckBox.setSelected(((RealParameter)migModel.rateMatrixScaleFactorInput.get()).isEstimatedInput.get());
        rateMatrixForwardTimeCheckBox.setSelected(migModel.useForwardMigrationRatesInput.get());
//        c.gridx = 2;
//        c.gridy = 2;
//        c.anchor = GridBagConstraints.LINE_END;
//        c.weightx = 1.0;
        estBox = FXUtils.newVBox();
        estBox.getChildren().add(rateMatrixEstCheckBox);
        estBox.getChildren().add(rateMatrixScaleFactorEstCheckBox);
        estBox.getChildren().add(rateMatrixForwardTimeCheckBox);
//        panel.add(estBox, 2,2);
        box.getChildren().add(estBox);

//        c.gridx = 1;
//        c.gridy = 3;
//        c.anchor = GridBagConstraints.LINE_START;
//        c.weightx = 1.0;
        Label l = new Label("Rows: sources, columns: sinks (backwards in time)");
//        panel.add(l, 1,3);
        box.getChildren().add(l);

//        c.gridx = 1;
//        c.gridy = 4;
//        c.anchor = GridBagConstraints.LINE_START;
//        c.weightx = 1.0;
        Label multilineLabel = new Label("<html><body>Correspondence between row/col indices<br>"
                + "and deme names shown to right of matrix.</body></html>");
//        panel.add(multilineLabel, 1, 4);
        box.getChildren().add(multilineLabel);

        box.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
        HBox.setHgrow(box, Priority.ALWAYS);
        pane.getChildren().add(box);
        getChildren().add(pane);
//        pane.getChildren().add(panel);
//        m_expansionBox = box;


        // Event handlers
//        popSizeTable.addTableModelListener(e -> {
//            if (e.getType() != TableModelEvent.UPDATE)
//                return;
//
//            if (!fileLoadInProgress)
//                saveToMigrationModel();
//        });

        popSizeEstCheckBox.selectedProperty().addListener(e -> saveToMigrationModel());

        popSizeScaleFactorEstCheckBox.selectedProperty().addListener(e -> saveToMigrationModel());

//        rateMatrixModel.addTableModelListener(e -> {
//            if (e.getType() != TableModelEvent.UPDATE)
//                return;
//
//            if (!fileLoadInProgress)
//                saveToMigrationModel();
//        });

        rateMatrixEstCheckBox.selectedProperty().addListener(e -> saveToMigrationModel());

        rateMatrixScaleFactorEstCheckBox.selectedProperty().addListener(e -> saveToMigrationModel());

        rateMatrixForwardTimeCheckBox.selectedProperty().addListener(e -> saveToMigrationModel());

        addTypeButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Name of type");
            dialog.showAndWait().ifPresent(newTypeName -> {
                if (migModel.getTypeSet().containsTypeWithName(newTypeName)) {
                    Alert.showMessageDialog(pane,
                            "Type with this name already present.",
                            "Error",
                            Alert.ERROR_MESSAGE);
                } else {
                    additionalTypeListModel.add(additionalTypeListModel.size(), newTypeName);
                    saveToMigrationModel();
                }
            });
        });

        addTypesFromFileButton.setOnAction(e -> {
            File file = FXUtils.getLoadFile("Choose file containing type names (one per line)");
            if (file != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty())
                            additionalTypeListModel.add(additionalTypeListModel.size(), line);
                    }

                    saveToMigrationModel();

                } catch (IOException e1) {
                    Alert.showMessageDialog(pane,
                            "<html>Error reading from file:<br>" + e1.getMessage() + "</html>",
                            "Error",
                            Alert.ERROR_MESSAGE);
                }
            }
        });


        remTypeButton.setOnAction(e -> {
            additionalTypeListModel.removeAll(additionalTypeListSelectionModel.getSelectedItems());
            additionalTypeListSelectionModel.clearSelection();

            saveToMigrationModel();
        });

        loadPopSizesFromFileButton.setOnAction(e -> {
            File file = FXUtils.getLoadFile("Choose file containing population sizes (one per line)");
            if (file != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                    List<Double> popSizes = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty())
                            popSizes.add(Double.parseDouble(line));
                    }

                    if (popSizes.size() == migModel.getNTypes()) {
                        fileLoadInProgress = true;

                        for (int i=0; i<popSizes.size(); i++)
                            popSize.set(i, popSizes.get(i));

                        fileLoadInProgress = false;

                        saveToMigrationModel();
                    } else {
                        Alert.showMessageDialog(pane,
                                "<html>File must contain exactly one population<br> size for each type/deme.</html>",
                                "Error",
                                Alert.ERROR_MESSAGE);
                    }

                } catch (IOException ex) {
                    Alert.showMessageDialog(pane,
                            "<html>Error reading from file:<br>" + ex.getMessage() + "</html>",
                            "Error",
                            Alert.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    Alert.showMessageDialog(pane,
                            "<html>File contains non-numeric line. Every line must contain<br> exactly one population size.</html>",
                            "Error",
                            Alert.ERROR_MESSAGE);
                }
            }
        });

        loadMigRatesFromFileButton.setOnAction(e -> {
            File file = FXUtils.getLoadFile("Choose CSV file containing migration rate matrix (diagonal ignored)");
            if (file != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                    List<Double> migRates = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        for (String field : line.split(",")) {
                            if (!field.isEmpty())
                                migRates.add(Double.parseDouble(field));
                        }
                    }

                    boolean diagonalsPresent = (migRates.size() == migModel.getNTypes()*migModel.getNTypes());
                    if (diagonalsPresent || migRates.size() == migModel.getNTypes()*(migModel.getNTypes()-1)) {

                        fileLoadInProgress = true;

                        for (int i=0; i<migModel.getNTypes(); i++) {
                            double[] tmp = rateMatrix.get(0);
                            for (int j=0; j<migModel.getNTypes(); j++) {
                                if (i==j)
                                    continue;

                                int offset;
                                if (diagonalsPresent)
                                    offset = i*migModel.getNTypes() + j;
                                else {
                                    offset = i * (migModel.getNTypes() - 1) + j;
                                    if (j>i)
                                        offset -= 1;
                                }
                                tmp[j] = migRates.get(offset);
                            }

                            rateMatrix.set(i, tmp);
                        }

                        fileLoadInProgress = false;

                        saveToMigrationModel();
                    } else {
                        Alert.showMessageDialog(pane,
                                "<html>CSV file must contain a square matrix with exactly one<br>" +
                                        "row for each type/deme.</html>", "Error",
                                Alert.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    Alert.showMessageDialog(pane,
                            "<html>Error reading from file:<br>" + ex.getMessage() + "</html>",
                            "Error", Alert.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    Alert.showMessageDialog(pane,
                            "<html>CSV file contains non-numeric element.</html>", "Error",
                            Alert.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadFromMigrationModel() {
        migModel.getTypeSet().initAndValidate();

        additionalTypeListModel.clear();
        if (migModel.getTypeSet().valueInput.get() != null) {
            for (String typeName : migModel.getTypeSet().valueInput.get().split(","))
                if (!typeName.isEmpty())
                    additionalTypeListModel.add(additionalTypeListModel.size(), typeName);
        }

//        popSizeModel.setRowCount(1);
//        popSizeModel.setColumnCount(migModel.getNTypes());
//        rateMatrixModel.setRowCount(migModel.getNTypes());
//        rateMatrixModel.setColumnCount(migModel.getNTypes()+1);

        List<String> typeNames = migModel.getTypeSet().getTypesAsList();
        fullTypeListModel.clear();
        for (String typeName : typeNames)
            fullTypeListModel.add(fullTypeListModel.size(), typeName);

        rowNames.clear();
        for (int i = 0; i < migModel.getNTypes(); i++) {
        if (i < typeNames.size())
            rowNames.add(" " + typeNames.get(i) + " (" + String.valueOf(i) + ") ");
        else
            rowNames.add(" (" + String.valueOf(i) + ") ");
        }

        for (int i=0; i<migModel.getNTypes(); i++) {
            popSize.set(i, migModel.getPopSize(i));
            double[] tmp = rateMatrix.get(0);
            for (int j=0; j<migModel.getNTypes(); j++) {
                if (i == j)
                    continue;
                tmp[j] = migModel.getBackwardRate(i, j);
//                rateMatrixModel.setValueAt(migModel.getBackwardRate(i, j), i, j);
            }
            rateMatrix.set(i, tmp);
            //TODO
//            rateMatrixModel.setValueAt(rowNames.get(i), i, migModel.getNTypes());
        }

        popSizeEstCheckBox.setSelected(((RealParameter)migModel.popSizesInput.get()).isEstimatedInput.get());
        rateMatrixEstCheckBox.setSelected(((RealParameter)migModel.rateMatrixInput.get()).isEstimatedInput.get());
        rateMatrixForwardTimeCheckBox.setSelected(migModel.useForwardMigrationRatesInput.get());
    }

    private void saveToMigrationModel() {

        StringBuilder sbAdditionalTypes = new StringBuilder();
        for (int i=0; i<additionalTypeListModel.size(); i++) {
            if (i > 0)
                sbAdditionalTypes.append(",");
            sbAdditionalTypes.append(additionalTypeListModel.get(i));
        }

        migModel.typeSetInput.get().valueInput.setValue(
                sbAdditionalTypes.toString(),
                migModel.typeSetInput.get());
        migModel.typeSetInput.get().initAndValidate();

        StringBuilder sbPopSize = new StringBuilder();
        for (int i=0; i<migModel.getNTypes(); i++) {
            if (i>0)
                sbPopSize.append(" ");

            if (i < popSize.size() && popSize.get(i) != null)
                sbPopSize.append(popSize.get(i));
            else
                sbPopSize.append("1.0");
        }
        ((RealParameter)migModel.popSizesInput.get()).setDimension(migModel.getNTypes());
        ((RealParameter)migModel.popSizesInput.get()).valuesInput.setValue(
            sbPopSize.toString(),
                (RealParameter)migModel.popSizesInput.get());

        StringBuilder sbRateMatrix = new StringBuilder();
        boolean first = true;
        for (int i=0; i<migModel.getNTypes(); i++) {
            for (int j=0; j<migModel.getNTypes(); j++) {
                if (i == j)
                    continue;

                if (first)
                    first = false;
                else
                    sbRateMatrix.append(" ");

                // if (i<rateMatrixModel.getRowCount() && j<rateMatrixModel.getColumnCount()-1 && rateMatrixModel.getValueAt(i, j) != null)
                if (i<rateMatrix.size() && rateMatrix.get(i) != null && j<rateMatrix.get(i).length-1 )
                    sbRateMatrix.append(rateMatrix.get(i)[j]);
                else
                    sbRateMatrix.append("1.0");
            }
        }
        ((RealParameter)migModel.rateMatrixInput.get()).setDimension(
            migModel.getNTypes()*(migModel.getNTypes()-1));
        ((RealParameter)migModel.rateMatrixInput.get()).valuesInput.setValue(
            sbRateMatrix.toString(),
                (RealParameter)migModel.rateMatrixInput.get());

        ((RealParameter)migModel.popSizesInput.get()).isEstimatedInput.setValue(
            popSizeEstCheckBox.isSelected(), (RealParameter)migModel.popSizesInput.get());
        ((RealParameter)migModel.popSizesScaleFactorInput.get()).isEstimatedInput.setValue(
                popSizeScaleFactorEstCheckBox.isSelected(), (RealParameter)migModel.popSizesScaleFactorInput.get());
        ((RealParameter)migModel.rateMatrixInput.get()).isEstimatedInput.setValue(
            rateMatrixEstCheckBox.isSelected(), (RealParameter)migModel.rateMatrixInput.get());
        ((RealParameter)migModel.rateMatrixScaleFactorInput.get()).isEstimatedInput.setValue(
                rateMatrixScaleFactorEstCheckBox.isSelected(), (RealParameter)migModel.rateMatrixScaleFactorInput.get());
        migModel.useForwardMigrationRatesInput.setValue(
                rateMatrixForwardTimeCheckBox.isSelected(), migModel);

        try {
            ((RealParameter)migModel.rateMatrixInput.get()).initAndValidate();
            ((RealParameter)migModel.popSizesInput.get()).initAndValidate();
            migModel.initAndValidate();
        } catch (Exception ex) {
            System.err.println("Error updating migration model state.");
        }

        refreshPanel();
    }

//    private void registerAsListener(Node node) {
//        if (node instanceof InputEditor) {
//            ((InputEditor)node).addValidationListener(_this);
//        }
//        if (node instanceof Pane) {
//            for (Node child : ((Pane)node).getChildren()) {
//                registerAsListener(child);
//            }
//        }
//    }

    class PopSize {
        double popSize;

        public PopSize(double popSize) {
            this.popSize = popSize;
        }

        public double getPopSize() {
            return popSize;
        }

        public void setPopSize(double popSize) {
            this.popSize = popSize;
        }
    }

}
