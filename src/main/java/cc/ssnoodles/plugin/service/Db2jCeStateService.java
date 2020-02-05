package cc.ssnoodles.plugin.service;

import cc.ssnoodles.db.domain.Config;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 配置持久化
 * @author ssnoodles
 * @version 1.0
 * Create at 2019-02-10 12:57
 */
@State(name = "Db2jCeStateService", storages = {@Storage("db2j-ce-config.xml")})
public class Db2jCeStateService implements PersistentStateComponent<Db2jCeStateService> {
    private Map<String, Config> config;

    @Nullable
    public static Db2jCeStateService getInstance(Project project) {
        return ServiceManager.getService(project, Db2jCeStateService.class);
    }

    @Nullable
    @Override
    public Db2jCeStateService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Db2jCeStateService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public Map<String, Config> getConfig() {
        return config;
    }

    public Db2jCeStateService setConfig(Map<String, Config> config) {
        this.config = config;
        return this;
    }
}
