package com.corosus.coroutil.common.core.modconfig;

public class ConfigAddQueue {

    public String modID;
    public IConfigCategory config;

    public ConfigAddQueue(String modID, IConfigCategory config) {
        this.modID = modID;
        this.config = config;
    }
}
