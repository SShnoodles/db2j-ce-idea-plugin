package cc.ssnoodles.plugin.ui;

import cc.ssnoodles.db.domain.Column;
import cc.ssnoodles.db.domain.Schema;
import cc.ssnoodles.db.domain.Table;
import cc.ssnoodles.plugin.domain.TreeData;
import com.intellij.openapi.util.IconLoader;

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
            this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "database.png", MyDefaultTreeCellRenderer.class));
        } else if (data.getData() instanceof Schema) {
            this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "schema.png", MyDefaultTreeCellRenderer.class));
        } else if (data.getData() instanceof Table) {
            this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "table.png", MyDefaultTreeCellRenderer.class));
        } else if (data.getData() instanceof Column) {
            Column column = (Column)data.getData();
            if (column.isPrimaryKey()) {
                this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "key.png", MyDefaultTreeCellRenderer.class));
            } else {
                this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "file.png", MyDefaultTreeCellRenderer.class));
            }
        }
        return this;
    }
}
