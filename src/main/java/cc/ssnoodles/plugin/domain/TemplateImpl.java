package cc.ssnoodles.plugin.domain;

import cc.ssnoodles.db.domain.Config;
import cc.ssnoodles.db.domain.Table;
import cc.ssnoodles.db.handler.Template;

import java.sql.SQLException;

/**
 * @author ssnoodles
 * @version 1.0
 * Create at 2020/2/3 17:04
 */
public class TemplateImpl implements Template {

    @Override
    public String render(Config config, Table table) throws SQLException {
        return null;
    }

    @Override
    public void write(Config config) throws SQLException {

    }

    @Override
    public String className(String s, String s1) {
        return null;
    }
}
