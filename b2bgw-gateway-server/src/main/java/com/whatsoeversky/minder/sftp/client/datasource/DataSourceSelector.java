package com.whatsoeversky.minder.sftp.client.datasource;

import com.whatsoeversky.minder.sftp.client.datasource.handler.DataSourceHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DataSourceSelector {
    private final Map<String, DataSourceHandler> dataSourceHandlerMap;

    public DataSourceSelector(List<DataSourceHandler> dataSourceHandlerList) {
        dataSourceHandlerMap = dataSourceHandlerList.stream().collect(Collectors.toMap(DataSourceHandler::getType, Function.identity()));
    }

    public DataSourceHandler getDataSourceHandler(String type) {
        return dataSourceHandlerMap.get(type);
    }
}
