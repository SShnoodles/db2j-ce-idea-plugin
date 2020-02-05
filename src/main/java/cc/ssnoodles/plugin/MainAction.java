package cc.ssnoodles.plugin;

import cc.ssnoodles.plugin.ui.MainDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author ssnoodles
 * @version 1.0
 * Create at 2019-01-31 13:13
 */
public class MainAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        new MainDialog(e);
    }
}
