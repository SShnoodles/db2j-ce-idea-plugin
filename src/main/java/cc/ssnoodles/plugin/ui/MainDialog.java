package cc.ssnoodles.plugin.ui;

import cc.ssnoodles.db.constant.DbType;
import cc.ssnoodles.db.constant.TemplateType;
import cc.ssnoodles.db.domain.*;
import cc.ssnoodles.db.handler.Template;
import cc.ssnoodles.db.util.*;
import cc.ssnoodles.plugin.service.*;
import cc.ssnoodles.plugin.util.UiUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBList;
import com.intellij.ui.scale.*;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.awt.event.*;
import java.time.LocalDateTime;
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
    private Tree tableTree;
    private JTextField singleRename;
    private JTextField outPath;
    private JButton loadButton;
    private JTextArea customTemplate;
    private JBList<String> templateTypes;

    private Project project;
    private AnActionEvent anActionEvent;
    private Db2jCeStateService db2jCeStateService;
    private Config config;

    private static final String SEPARATOR = " | ";

    private static final String ICONS = "/icons/";

    public MainDialog(AnActionEvent anActionEvent) {
        this.project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        db2jCeStateService = Db2jCeStateService.getInstance(project);

        UiUtil.centerDialog(this,"Db2j-CE", 1200, 600);

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
        List<Table> tables = db2jCeStateService.getTables();
        if (tables != null) {
            tableTree.setModel(toTreeNode(tables));
        } else {
            DefaultMutableTreeNode database = new DefaultMutableTreeNode("Database");
            tableTree.setModel(new DefaultTreeModel(database));
        }

        // set tree multiple
        TreeSelectionModel treeSelect = tableTree.getSelectionModel();
        treeSelect.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tableTree.setSelectionModel(treeSelect);

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
            String defaultOutPath = "src/main/java";
            config.setOutPath(defaultOutPath);
            outPath.setText(defaultOutPath);
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
        loadButton.setIcon(IconLoader.getIcon(ICONS + "reload.png"));
        loadButton.addActionListener(e -> {
            TemplateImpl template = new TemplateImpl();
            try {
                MainDialog.this.config.setDbType(DbType.getFromUrl(jdbcUrl.getText()));
                template.init(this.config);
            } catch (Exception ex) {
                Messages.showErrorDialog(project, ex.getMessage(), "Database Collection Error");
                return;
            }
            tableTree.setModel(toTreeNode(TemplateImpl.TABLES));
            db2jCeStateService.setTables(TemplateImpl.TABLES);
        });
    }

    private TreeModel toTreeNode(List<Table> tables) {
        if (tables != null && tables.size() > 0) {
            String dateTime = tables.get(0).getDateTime() == null ? "" : TimeUtil.DATE_TIME_SS.format(tables.get(0).getDateTime());
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Database" + SEPARATOR + "Refreshed " + dateTime);
            for (Table table : tables) {
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(String.join(SEPARATOR, table.getName(), StringUtil.isEmpty(table.getRemarks()) ? "" :  table.getRemarks()));
                for (Column column : table.getColumns()) {
                    DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(String.join(SEPARATOR, column.getName(), column.getType(), StringUtil.isEmpty(column.getRemarks()) ? "" :  column.getRemarks()));
                    tableNode.add(columnNode);
                }
                root.add(tableNode);
            }
            return new DefaultTreeModel(root);
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Database" + SEPARATOR + "Refreshed " + TimeUtil.getTimeSS());
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
        DefaultMutableTreeNode[] selectedNodes = tableTree.getSelectedNodes(DefaultMutableTreeNode.class, null);
        if (selectedNodes.length <= 0) {
            Messages.showErrorDialog(project, "Please select at least one tables", "Error");
            return;
        }
        GeneratorService generatorService = GeneratorService.of();
        for (DefaultMutableTreeNode selectedNode : selectedNodes) {
            if (1 == selectedNode.getLevel()) {
                String tableName = selectedNode.getUserObject().toString().split(SEPARATOR)[0];
                Template.TABLES.stream().filter(t -> t.getName().equals(tableName)).findFirst().ifPresent(
                        table -> generatorService.create(project, config, table)
                );
            }
            // TODO custom table columns
//            if (2 == selectedNode.getLevel()) {
//                DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
//            }
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
