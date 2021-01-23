package cc.ssnoodles.plugin.ui;

import cc.ssnoodles.db.constant.DbType;
import cc.ssnoodles.db.constant.TemplateType;
import cc.ssnoodles.db.domain.*;
import cc.ssnoodles.db.handler.Template;
import cc.ssnoodles.db.util.*;
import cc.ssnoodles.plugin.domain.TemplateImpl;
import cc.ssnoodles.plugin.domain.TreeData;
import cc.ssnoodles.plugin.service.*;
import cc.ssnoodles.plugin.util.UiUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.awt.event.*;
import java.util.*;

public class MainDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField jdbcUrl;
    private JTextField username;
    private JPasswordField password;
    private JTextField author;
    private JRadioButton isOverwriteFiles;
    private Tree schemaTree;
    private JTextField singleRename;
    private TextFieldWithBrowseButton outPath;
    private JButton loadButton;
    private JTextArea customTemplate;
    private JBList<String> templateTypes;

    private Project project;
    private AnActionEvent anActionEvent;
    private Db2jCeStateService db2jCeStateService;
    private Config config;

    public static final String ICONS = "/icons/";

    public MainDialog(AnActionEvent anActionEvent) {
        this.project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        db2jCeStateService = Db2jCeStateService.getInstance(project);

        UiUtil.centerDialog(this,"Db2j-CE", 1200, 800);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // init config、dialog data
        init();

        pack();
        setVisible(true);
    }

    private void init() {
        // init multiple selectList
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement(TemplateType.JPA.getType());
        listModel.addElement(TemplateType.DTO.getType());
        listModel.addElement(TemplateType.POJO.getType());
        listModel.addElement(TemplateType.REPOSITORY.getType());
        templateTypes.setModel(listModel);
        ListSelectionModel listSelect = templateTypes.getSelectionModel();
        listSelect.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        templateTypes.setSelectionModel(listSelect);

        // init tree root node
        List<Schema> schemas = db2jCeStateService.getSchemas();
        if (schemas != null) {
            schemaTree.setModel(toTreeNode(schemas));
        } else {
            DefaultMutableTreeNode database = new DefaultMutableTreeNode("Database");
            schemaTree.setModel(new DefaultTreeModel(database));
        }

        // set tree multiple
        TreeSelectionModel treeSelect = schemaTree.getSelectionModel();
        treeSelect.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        schemaTree.setSelectionModel(treeSelect);

        Config config = db2jCeStateService.getConfig();
        if (config != null) {
            jdbcUrl.setText(config.getUrl());
            username.setText(config.getUsername());
            password.setText(config.getPassword());
            singleRename.setText(config.getSingleTableRename());
            author.setText(config.getAuthor());
            outPath.setText(config.getOutPath());
            isOverwriteFiles.setSelected(config.isOverwriteFiles());
            if (config.getTemplates() != null && config.getTemplates().length > 0) {
                for (TemplateType template : config.getTemplates()) {
                    templateTypes.setSelectedIndex(template.ordinal());
                }
            }
            customTemplate.setText(config.getCustomTemplate());
        } else {
            config = FileUtil.PROPERTIES;
            // init out path
            config.setOutPath(project.getBasePath());
            outPath.setText(project.getBasePath());
            templateTypes.setSelectedIndex(0);
        }
        this.config = config;

        jdbcUrl.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                MainDialog.this.config.setUrl(jdbcUrl.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                MainDialog.this.config.setUrl(jdbcUrl.getText());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                MainDialog.this.config.setUrl(jdbcUrl.getText());
            }
        });
        username.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                MainDialog.this.config.setUsername(username.getText());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                MainDialog.this.config.setUsername(username.getText());
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                MainDialog.this.config.setUsername(username.getText());
            }
        });
        password.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                MainDialog.this.config.setPassword(new String(password.getPassword()));
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                MainDialog.this.config.setPassword(new String(password.getPassword()));
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                MainDialog.this.config.setPassword(new String(password.getPassword()));
            }
        });
        loadButton.setIcon(IconLoader.getIcon(ICONS + "reload.png", MainDialog.class));
        loadButton.addActionListener(e -> {
            TemplateImpl template = new TemplateImpl();
            try {
                MainDialog.this.config.setDbType(DbType.getFromUrl(jdbcUrl.getText()));
                template.init(this.config);
            } catch (Exception ex) {
                Messages.showErrorDialog(project, ex.getMessage(), "Database Collection Error");
                return;
            }
            schemaTree.setModel(toTreeNode(TemplateImpl.SCHEMAS));
            db2jCeStateService.setSchemas(TemplateImpl.SCHEMAS);
        });
        schemaTree.setCellRenderer(new MyDefaultTreeCellRenderer());
    }

    private TreeModel toTreeNode(List<Schema> schemas) {
        String title;
        if (schemas != null && schemas.size() > 0) {
            String dateTime = schemas.get(0).getTimestamp() == 0 ? "" : TimeUtil.DATE_TIME_SS.format(TimeUtil.timestampToLocalDateTime(schemas.get(0).getTimestamp()));
            title = "Database(" + schemas.size() + ") Refreshed " + dateTime;
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeData<>(title));
            for (Schema schema : schemas) {
                DefaultMutableTreeNode schemaNode = new DefaultMutableTreeNode(new TreeData<>(schema));
                for (Table table : schema.getTables()) {
                    DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new TreeData<>(table));
                    for (Column column : table.getColumns()) {
                        DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(new TreeData<>(column));
                        tableNode.add(columnNode);
                    }
                    schemaNode.add(tableNode);
                }
                root.add(schemaNode);
            }
            return new DefaultTreeModel(root);
        }
        title = "Database(0) Refreshed " + TimeUtil.getTimeSS();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeData<>(title));
        return new DefaultTreeModel(root);
    }

    private void onOK() {
        config.setAuthor(author.getText().trim());
        config.setOutPath(outPath.getText().trim());
        config.setSingleTableRename(singleRename.getText().trim());
        config.setOverwriteFiles(isOverwriteFiles.isSelected());
        config.setTemplates(templateTypes.getSelectedValuesList().stream().map(TemplateType::get).toArray(TemplateType[]::new));
        config.setCustomTemplate(customTemplate.getText().trim());
        // save store
        db2jCeStateService.setConfig(config);
        // get select table
        DefaultMutableTreeNode[] selectedNodes = schemaTree.getSelectedNodes(DefaultMutableTreeNode.class, null);
        if (selectedNodes.length <= 0) {
            Messages.showErrorDialog(project, "Please select at least one tables", "Error");
            return;
        }
        GeneratorService generatorService = GeneratorService.of();
        Set<Table> selectTables = new HashSet<>();
        Map<Table, List<Column>> columnSet = new HashMap<>();
        for (DefaultMutableTreeNode selectedNode : selectedNodes) {
            TreeData<?> treeData = (TreeData<?>)selectedNode.getUserObject();
            if (treeData.getData() instanceof Table) {
                Template.SCHEMAS.forEach(schema -> schema.getTables().stream().filter(t -> t.equals(treeData.getData())).findFirst().ifPresent(selectTables::add));
            }
            // custom table columns
            if (treeData.getData() instanceof Column) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
                Table table = (Table)((TreeData<?>)parent.getUserObject()).getData();
                Column column = (Column)((TreeData<?>)selectedNode.getUserObject()).getData();
                Template.SCHEMAS.forEach(schema -> schema.getTables().stream().filter(t -> t.equals(table)).findFirst()
                        .flatMap(t -> t.getColumns().stream().filter(c -> c.equals(column)).findFirst())
                        .ifPresent(c -> columnSet.computeIfAbsent(table, k -> new ArrayList<>()).add(c)));
            }
        }
        if (columnSet.size() > 0) {
            columnSet.forEach((k, v) -> {
                if (!selectTables.contains(k)) {
                    selectTables.add(k);
                }
            });
            // generate code
            selectTables.stream().map(table -> {
                Table newTable = new Table();
                newTable.setName(table.getName());
                newTable.setClassName(table.getClassName());
                newTable.setRemarks(table.getRemarks());
                newTable.setTimestamp(table.getTimestamp());
                newTable.setPrimaryKey(table.isPrimaryKey());
                newTable.setPrimaryKeys(table.getPrimaryKeys());
                newTable.setColumns(columnSet.get(table));
                return newTable;
            }).forEach(table -> generatorService.create(project, config, table));
        } else {
            selectTables.forEach(table -> generatorService.create(project, config, table));
        }

        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

}
