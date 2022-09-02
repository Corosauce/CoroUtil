package com.corosus.coroutil.lazydfu;

import com.mojang.datafixers.*;

import java.util.concurrent.Executor;

public class LazyDataFixerBuilder extends DataFixerBuilder {

    private static final Executor NO_OP_EXECUTOR = command -> {};

    public LazyDataFixerBuilder(int dataVersion) {
        super(dataVersion);
    }

    @Override
    public DataFixer build(Executor executor) {
        return super.build(NO_OP_EXECUTOR);
    }
}