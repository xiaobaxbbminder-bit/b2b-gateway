package com.whatsoeversky.minder.agent.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

public class SampleClient {

	private final McpClientTransport transport;

	public SampleClient(McpClientTransport transport) {
		this.transport = transport;
	}

	public void run() {

		var client = McpClient.sync(this.transport).build();

		client.initialize();

		client.ping();

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