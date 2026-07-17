package com.whatsoeversky.minder.agent.mcp.client;

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonDefaults;

import java.io.File;

public class ClientStdioWebflux {

    public static void main(String[] args) {

        System.out.println(new File(".").getAbsolutePath());

        var stdioParams = ServerParameters.builder("/home/whatsoeversky/programs/jdk-25_linux-x64_bin/jdk-25.0.1/bin/java")
                .args("-Dspring.main.banner-mode=off", "-Dspring.ai.mcp.server.stdio=true", "-Dspring.main.web-application-type=none",
                        "-Dlogging.pattern.console=", "-jar",
                        "/home/whatsoeversky/code/test-mcp-server/test-mcp-server-webflux/target/test-mcp-server-webflux-1.0-SNAPSHOT.jar")
                .build();

        var transport = new StdioClientTransport(stdioParams, McpJsonDefaults.getMapper());

        new SampleClient(transport).run();
    }

}
