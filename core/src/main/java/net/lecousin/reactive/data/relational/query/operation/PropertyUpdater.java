package net.lecousin.reactive.data.relational.query.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.relational.core.sql.AssignValue;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.SQL;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.core.sql.Update;

import net.lecousin.reactive.data.relational.query.SqlQuery;
import reactor.core.publisher.Mono;

class PropertyUpdater extends AbstractProcessor<PropertyUpdater.Request> {
	
	private Map<RelationalPersistentEntity<?>, Map<RelationalPersistentProperty, Map<Object, Request>>> requests = new HashMap<>();
	
	static class Request extends AbstractProcessor.Request {
		RelationalPersistentProperty property;
		Object whereValueIs;
		Object newValue;
		
		Request(RelationalPersistentEntity<?> entityType, RelationalPersistentProperty property, Object whereValueIs, Object newValue) {
			super(entityType);
			this.property = property;
			this.whereValueIs = whereValueIs;
			this.newValue = newValue;
		}
	}
	
	Request update(RelationalPersistentEntity<?> entityType, RelationalPersistentProperty property, Object whereValueIs, Object newValue) {
		Map<RelationalPersistentProperty, Map<Object, Request>> map = requests.computeIfAbsent(entityType, e -> new HashMap<>());
		Map<Object, Request> map2 = map.computeIfAbsent(property, p -> new HashMap<>());
		return map2.computeIfAbsent(whereValueIs, e -> new Request(entityType, property, whereValueIs, newValue));
	}

	@Override
	protected Mono<Void> executeRequests(Operation op) {
		List<Mono<Void>> calls = new LinkedList<>();
		for (Map.Entry<RelationalPersistentEntity<?>, Map<RelationalPersistentProperty, Map<Object, Request>>> entity : requests.entrySet()) {
			for (Map.Entry<RelationalPersistentProperty, Map<Object, Request>> property : entity.getValue().entrySet()) {
				Map<Object, Set<Object>> reverseMap = new HashMap<>();
				List<Request> ready = new LinkedList<>();
				for (Map.Entry<Object, Request> entry : property.getValue().entrySet()) {
					if (!canExecuteRequest(entry.getValue()))
						continue;
					Set<Object> set = reverseMap.computeIfAbsent(entry.getValue().newValue, e -> new HashSet<>());
					set.add(entry.getKey());
					ready.add(entry.getValue());
				}
				if (reverseMap.isEmpty())
					continue;
				Table table = Table.create(entity.getKey().getTableName());
				for (Map.Entry<Object, Set<Object>> update : reverseMap.entrySet()) {
					SqlQuery<Update> query = new SqlQuery<>(op.lcClient);
					List<Expression> values = new ArrayList<>(update.getValue().size());
					for (Object value : update.getValue())
						values.add(query.marker(value));
					query.setQuery(
						Update.builder().table(table)
						.set(AssignValue.create(Column.create(property.getKey().getColumnName(), table), update.getKey() != null ? query.marker(update.getKey()) : SQL.nullLiteral()))
						.where(Conditions.in(Column.create(property.getKey().getColumnName(), table), values))
						.build()
					);
					calls.add(query.execute().then().doOnSuccess(v -> ready.forEach(r -> r.executed = true)));
				}
			}
		}
		if (calls.isEmpty())
			return null;
		return Mono.when(calls);
	}

}
