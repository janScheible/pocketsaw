package com.scheible.pocketsaw.impl.visualization;

import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonArray;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonValue;
import java.util.EnumSet;
import java.util.Iterator;
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
class JsonArrayCollector implements Collector<JsonValue, JsonArray, String> {
	
	static JsonArrayCollector toJson() {
		return new JsonArrayCollector();
	}

	@Override
	public Supplier<JsonArray> supplier() {
		return () -> Json.array();
	}

	@Override
	public BiConsumer<JsonArray, JsonValue> accumulator() {
		return JsonArray::add;
	}

	@Override
	public BinaryOperator<JsonArray> combiner() {
		return (a, b) -> {
			Iterator<JsonValue> iter = b.iterator();
			while (iter.hasNext()) {
				a.add(iter.next());
			}
			return a;
		};
	}

	@Override
	public Function<JsonArray, String> finisher() {
		return a -> a.toString();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return EnumSet.noneOf(Characteristics.class);
	}
}
