package cc.ssnoodles.plugin.ui;

import cc.ssnoodles.db.domain.Column;
import cc.ssnoodles.db.domain.Schema;
import cc.ssnoodles.db.domain.Table;
import cc.ssnoodles.plugin.domain.TreeData;
import icons.MyIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author ssnoodles
 * @version 1.0
 * Create at 2020/2/6 12:40
 */
public class MyDefaultTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        TreeData<?> data = (TreeData<?>)node.getUserObject();
        if (data.getData() instanceof String) {
            this.setIcon(MyIcons.DATABASE);
        } else if (data.getData() instanceof Schema) {
            this.setIcon(MyIcons.SCHEMA);
        } else if (data.getData() instanceof Table) {
            this.setIcon(MyIcons.TABLE);
        } else if (data.getData() instanceof Column) {
            Column column = (Column)data.getData();
            if (column.isPrimaryKey()) {
                this.setIcon(MyIcons.KEY);
            } else {
                this.setIcon(MyIcons.FILE);
            }
        }
        return this;
    }
}
