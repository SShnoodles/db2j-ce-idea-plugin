package icons;

import cc.ssnoodles.plugin.ui.MainDialog;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface MyIcons {
    String BASE_PATH = "/icons/";

    Icon DATABASE = IconLoader.getIcon(BASE_PATH + "database.png", MyIcons.class);

    Icon SCHEMA = IconLoader.getIcon(BASE_PATH + "schema.png", MyIcons.class);

    Icon TABLE = IconLoader.getIcon(BASE_PATH + "table.png", MyIcons.class);

    Icon KEY = IconLoader.getIcon(BASE_PATH + "key.png", MyIcons.class);

    Icon FILE = IconLoader.getIcon(BASE_PATH + "file.png", MyIcons.class);

    Icon RELOAD = IconLoader.getIcon(BASE_PATH + "reload.png", MainDialog.class);

    Icon LOGO = IconLoader.getIcon( BASE_PATH + "logo.svg", MainDialog.class);
}
