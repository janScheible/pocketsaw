package com.scheible.pocketsaw.impl.visualization;

import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonObject;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonValue;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 *
 * @author sj
 */
class JsonMapCollector implements Collector<Entry<String, JsonValue>, JsonObject, String> {
	
	static JsonMapCollector toJson() {
		return new JsonMapCollector();
	}

	@Override
	public Supplier<JsonObject> supplier() {
		return () -> Json.object();
	}

	@Override
	public BiConsumer<JsonObject, Entry<String, JsonValue>> accumulator() {
		return (obj, e) -> {
			obj.set(e.getKey(), e.getValue());
		};
	}

	@Override
	public BinaryOperator<JsonObject> combiner() {
		return (a, b) -> {
			return a.merge(b);
		};
	}

	@Override
	public Function<JsonObject, String> finisher() {
		return o -> o.toString();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.noneOf(Characteristics.class);
	}
}
