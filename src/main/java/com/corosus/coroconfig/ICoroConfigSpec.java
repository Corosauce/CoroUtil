package com.corosus.coroconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

public interface ICoroConfigSpec<T extends ICoroConfigSpec<T>> extends UnmodifiableConfig {
    @SuppressWarnings("unchecked")
    default T self() {
        return (T) this;
    }

    void acceptConfig(CommentedConfig data);

    boolean isCorrecting();

    boolean isCorrect(CommentedConfig commentedFileConfig);

    int correct(CommentedConfig commentedFileConfig);

    void afterReload();
}
