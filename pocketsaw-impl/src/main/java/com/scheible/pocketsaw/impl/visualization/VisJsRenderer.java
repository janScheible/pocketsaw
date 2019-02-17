package com.scheible.pocketsaw.impl.visualization;

import com.scheible.pocketsaw.impl.dependency.DependencyGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scheible.pocketsaw.impl.dependency.Dependency;
import java.io.File;
import java.io.IOException;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonObject;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonValue;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VisJsRenderer {

	private static final String TEMPLATE_FILE = "visualization-template.html";
	
	static class Node {

		private enum Type {
			SUB_MODULE, EXTERNAL_FUNCTIONALITY
		}

		final int id;
		final String label;
		final String title;
		final String color;
		private final Type type;

		Node(final int id, final String label, final String title, final String color, Type type) {
			this.id = id;
			this.label = label;
			this.title = title;
			this.color = color;
			this.type = type;
		}

		public JsonObject toJsonObject() {
			return Json.object().add("id", id).add("label", label).add("title", title).add("color", color).add("type", type.name());
		}
	}

	static class Color {

		final String color;

		public Color(String color) {
			this.color = color;
		}

		public JsonObject toJsonObject() {
			return Json.object().add("color", color);
		}

	}

	static class Edge {

		final int from;
		final int to;
		final String arrows;
		final Color color;
		final Optional<String> label;
		final float width;

		public Edge(final int from, final int to, final Color color, Optional<String> label, float width) {
			this.from = from;
			this.to = to;
			this.arrows = "to";
			this.color = color;
			this.label = label;
			this.width = width;
		}

		public JsonObject toJsonObject() {
			JsonObject result = Json.object().add("from", from).add("to", to).add("arrows", arrows)
					.add("color", color.toJsonObject()).add("width", width);
			if(label.isPresent()) {
				result.add("label", label.get());
			}
			return result;
		}
	}

	public static class VisJsJson {

		private final String nodeDataSet;
		private final String edgeDataSet;
		private final String usedSubModuleTypesJson;

		public VisJsJson(String nodeDataSet, String edgeDataSet, String usedSubModuleTypesJson) {
			this.nodeDataSet = nodeDataSet;
			this.edgeDataSet = edgeDataSet;
			this.usedSubModuleTypesJson = usedSubModuleTypesJson;
		}

		public String getEdgeDataSet() {
			return edgeDataSet;
		}

		public String getNodeDataSet() {
			return nodeDataSet;
		}

		public String getUsedSubModuleTypesJson() {
			return usedSubModuleTypesJson;
		}
	}

	private static VisJsJson serialize(DependencyGraph dependencyGraph) {
		Map<PackageGroupDescriptor, Integer> packageGroupToIdMapping = new HashMap<>();
		int idCounter = 1;

		List<Node> nodes = new ArrayList<>();
		List<Edge> edges = new ArrayList<>();

		for (PackageGroupDescriptor descriptor : dependencyGraph.getPackageGroups()) {
			packageGroupToIdMapping.put(descriptor, idCounter);
			nodes.add(new Node(idCounter, descriptor.getName(), 
					descriptor.getPackageMatchPatterns().stream().collect(Collectors.joining(", ")), descriptor.getColor(),
					descriptor instanceof SubModuleDescriptor ? Node.Type.SUB_MODULE : Node.Type.EXTERNAL_FUNCTIONALITY));
			idCounter++;
		}

		for (Dependency dependency : dependencyGraph.getDependencies()) {
			int sourceId = packageGroupToIdMapping.get(dependency.getSource());
			int targetId = packageGroupToIdMapping.get(dependency.getTarget());

			final float width = dependency.getCodeDependencyCount() == 0 ? 1 
					: (float)(Math.log(dependency.getCodeDependencyCount()) / Math.log(0.5) * -1 + 1);
			final Optional<String> label = dependency.getCodeDependencyCount() == 0 ? Optional.empty() 
					: Optional.of(Integer.toString(dependency.getCodeDependencyCount()));
			edges.add(new Edge(sourceId, targetId, new Color(dependency.hasCodeOrigin() && dependency.hasDescriptorOrigin()
					? "green" : dependency.hasDescriptorOrigin() && !dependency.hasCodeOrigin() ? "gray" : "red"),
					label, width));
		}

		String nodesJson = nodes.stream().map(node -> node.toJsonObject()).collect(JsonArrayCollector.toJson());
		String edgesJson = edges.stream().map(edge -> edge.toJsonObject()).collect(JsonArrayCollector.toJson());
		String usedSubModuleTypesJson = dependencyGraph.getUsedSubModuleTypes().entrySet().stream()
				.sorted((first, second) -> first.getKey().getName().compareTo(second.getKey().getName()))
				.map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey().getName(), (JsonValue)Json.array(
						e.getValue().stream().sorted().collect(Collectors.toList()).toArray(new String[0]))))
				.collect(JsonMapCollector.toJson());

		return new VisJsJson(nodesJson, edgesJson, usedSubModuleTypesJson);
	}

	public static void render(DependencyGraph dependencyGraph, File outputFile) {
		try {
			final VisJsJson data = serialize(dependencyGraph);

			final String html = readTemplateContent(VisJsRenderer.class.getResourceAsStream(TEMPLATE_FILE))
					.replace("/*{nodes-array}*/", data.getNodeDataSet())
					.replace("/*{edges-array}*/", data.getEdgeDataSet())
					.replace("/*{used-sub-module-types-map}*/", data.getUsedSubModuleTypesJson());

			Files.write(outputFile.toPath(), html.getBytes("UTF8"));
		} catch (IOException ex) {
			throw new UncheckedIOException("Error while writing the graph HTML to '" + outputFile + "'!", ex);
		}
	}

	private static String readTemplateContent(InputStream input) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, "UTF8"))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}
}
