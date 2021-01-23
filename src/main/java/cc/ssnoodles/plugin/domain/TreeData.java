package cc.ssnoodles.plugin.domain;

import cc.ssnoodles.db.domain.Column;
import cc.ssnoodles.db.domain.Schema;
import cc.ssnoodles.db.domain.Table;

public class TreeData<T> {
    private T data;

    public TreeData() {
    }

    public TreeData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if (data instanceof String) {
            return (String)data;
        } else if (data instanceof Schema) {
            Schema schema = (Schema)data;
            return schema.getName() + " " + nullFormat(schema.getRemarks());
        } else if (data instanceof Table) {
            Table table = (Table)data;
            return table.getName() + " " + nullFormat(table.getRemarks());
        } else if (data instanceof Column) {
            Column column = (Column)data;
            return column.getName() + " " + column.getType() + " " + nullFormat(column.getRemarks());
        }
        return "Unknown";
    }

    private String nullFormat(String remarks) {
        return remarks == null ? "" : remarks;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
