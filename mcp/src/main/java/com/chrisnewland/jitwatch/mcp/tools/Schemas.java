package com.chrisnewland.jitwatch.mcp.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small helpers for writing JSON Schema for tool inputs without a pile of nested
 * literals.
 */
public final class Schemas {

	private Schemas() {
	}

	public static Builder object() {
		return new Builder();
	}

	public static final class Builder {

		private final Map<String, Object> properties = new LinkedHashMap<>();

		private final List<String> required = new ArrayList<>();

		public Builder str(String name, String description) {
			return add(name, "string", description, false, null);
		}

		public Builder reqStr(String name, String description) {
			return add(name, "string", description, true, null);
		}

		public Builder enumStr(String name, String description, boolean isRequired, List<String> values) {
			return add(name, "string", description, isRequired, values);
		}

		public Builder integer(String name, String description) {
			return add(name, "integer", description, false, null);
		}

		public Builder bool(String name, String description) {
			return add(name, "boolean", description, false, null);
		}

		public Builder strArray(String name, String description) {
			Map<String, Object> property = new LinkedHashMap<>();
			property.put("type", "array");
			property.put("items", Map.of("type", "string"));
			property.put("description", description);
			properties.put(name, property);
			return this;
		}

		private Builder add(String name, String type, String description, boolean isRequired, List<String> values) {
			Map<String, Object> property = new LinkedHashMap<>();
			property.put("type", type);
			property.put("description", description);

			if (values != null) {
				property.put("enum", values);
			}

			properties.put(name, property);

			if (isRequired) {
				required.add(name);
			}

			return this;
		}

		public Map<String, Object> build() {
			Map<String, Object> schema = new LinkedHashMap<>();
			schema.put("type", "object");
			schema.put("properties", properties);
			schema.put("required", required);
			schema.put("additionalProperties", false);
			return schema;
		}

	}

}
