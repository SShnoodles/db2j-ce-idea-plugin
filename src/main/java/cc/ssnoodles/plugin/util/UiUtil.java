package cc.ssnoodles.plugin.util;

import cc.ssnoodles.plugin.ui.MainDialog;

import java.awt.*;

public class UiUtil {

    public static void centerDialog(MainDialog dialog, String title, int width, int height) {
        dialog.setTitle(title);
        dialog.setPreferredSize(new Dimension(width, height));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation(screenSize.width / 2 - width / 2, screenSize.height / 2 - height / 2);
    }

    public static void centerDialog(MainDialog dialog, String title) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setTitle(title);
        dialog.setPreferredSize(new Dimension(screenSize.width, screenSize.height));
    }
}