package cc.ssnoodles.plugin.ui;

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
        if (0 == node.getLevel()) {
            this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "database.png"));
        } else if (1 == node.getLevel()) {
            this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "table.png"));
        } else {
            this.setIcon(IconLoader.getIcon(MainDialog.ICONS + "file.png"));
        }
        return this;
    }
}
