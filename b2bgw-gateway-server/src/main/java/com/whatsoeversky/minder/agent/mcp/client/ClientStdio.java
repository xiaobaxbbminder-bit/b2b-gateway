package com.whatsoeversky.minder.agent.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

public class ClientStdio {

    public static void main(String[] args) {
        var stdioParams = ServerParameters.builder("/home/whatsoeversky/programs/jdk-25_linux-x64_bin/jdk-25.0.1/bin/java")
                .args("-jar",
                        "/home/whatsoeversky/code/test-mcp-server/test-mcp-server/target/test-mcp-server-1.0-SNAPSHOT.jar"
                )
                .build();

        var transport = new StdioClientTransport(stdioParams, McpJsonDefaults.getMapper());
        var client = McpClient.sync(transport).build();

        client.initialize();

        // List and demonstrate tools
        McpSchema.ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        McpSchema.CallToolResult weatherForcastResult = client.callTool(new McpSchema.CallToolRequest("getWeatherForecastByLocation",
                Map.of("latitude", 47.6062, "longitude", -122.3321)));
        System.out.println("Weather Forcast: " + weatherForcastResult);

        McpSchema.CallToolResult alertResult = client.callTool(new McpSchema.CallToolRequest("getAlerts", Map.of("state", "NY")));
        System.out.println("Alert Response = " + alertResult);

        client.closeGracefully();
    }

}