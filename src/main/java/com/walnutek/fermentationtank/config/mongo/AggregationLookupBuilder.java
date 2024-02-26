package com.walnutek.fermentationtank.config.mongo;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.service.Utils.SFunction;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.walnutek.fermentationtank.model.service.Utils.field;
import static com.walnutek.fermentationtank.model.service.Utils.mappingField;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

public class AggregationLookupBuilder<S, T, V> {

	private Class<S> sourceEntity;

	private Class<T> targetEntity;

	private Class<V> voEntity;

	private String localField;

	private String foreignField;

	private Boolean preserveNullAndEmptyArrays = false;

	private Boolean isUnwind = true;

	private String tempFieldName;

	private List<AddFieldsOperation> addFieldsOperationList = new ArrayList<>();

	private AggregationLookupBuilder() {
		tempFieldName = UUID.randomUUID().toString().replaceAll("-", "");
	}

	public AggregationLookupBuilder<S, T, V> as(String customAsField) {
		tempFieldName = customAsField;
		return this;
	}

	public static <S> AggregationLookupFromBuilder<S> from(Class<S> sourceEntity) {
		return AggregationLookupFromBuilder.from(sourceEntity);
	}

	public AggregationLookupBuilder<S, T, V> asArrayField() {
		isUnwind = false;
		return this;
	}

	public AggregationLookupBuilder<S, T, V> mapping(SFunction<T, ?> targetEntityFieldGetter, SFunction<V, ?> voEntityFieldGetter) {
		var addField = addFields().addField(field(voEntityFieldGetter))
						.withValue(mappingField(tempFieldName, field(targetEntityFieldGetter)))
						.build();
		addFieldsOperationList.add(addField);
		return this;
	}

	public AggregationLookupBuilder<S, T, V> mapping(SFunction<V, ?> voEntityFieldGetter) {
		var addField = addFields().addField(field(voEntityFieldGetter))
						.withValue(mappingField(tempFieldName))
						.build();
		addFieldsOperationList.add(addField);
		return this;
	}

	public AggregationLookupBuilder<S, T, V> mappingArray(SFunction<V, ?> voEntityFieldGetter) {
		var addField = addFields().addField(field(voEntityFieldGetter))
						.withValue(mappingField(tempFieldName))
						.build();
		addFieldsOperationList.add(addField);
		return this;
	}

	public List<AggregationOperation> build() {
		List<AggregationOperation> resultList = new ArrayList<>();

		var lookup = lookup().from(getCollectionName(targetEntity))
						.localField(localField)
						.foreignField(foreignField)
						.as(tempFieldName);

		resultList.add(lookup);

		if(isUnwind) {
			var unwind = unwind(tempFieldName, preserveNullAndEmptyArrays);
			resultList.add(unwind);
		}

		addFieldsOperationList.forEach(resultList::add);

		return resultList;
	}

	private static <T> String getCollectionName(Class<T> entity) {
		var collection = "";

		var document = entity.getAnnotation(Document.class);
		if(Objects.nonNull(document) && StringUtils.hasText(document.value())) {
			collection = document.value();
		}

		return collection;
	}

	public static class AggregationLookupFromBuilder<S> {

		private Class<S> sourceEntity;

		private static <S> AggregationLookupFromBuilder<S> from(Class<S> sourceEntity) {
			if(StringUtils.hasText(getCollectionName(sourceEntity))) {
				var builder = new AggregationLookupFromBuilder<S>();
				builder.sourceEntity = sourceEntity;

				return builder;
			} else {
				throw new AppException(AppException.Code.E000, "Entity collection config is invalid");
			}
		}

		public <T> AggregationLookupJoinBuilder<S, T> outerJoin(Class<T> targetEntity) {
			if(StringUtils.hasText(getCollectionName(sourceEntity))) {
				var builder = new AggregationLookupJoinBuilder<S, T>();
				builder.sourceEntity = sourceEntity;
				builder.targetEntity = targetEntity;
				builder.preserveNullAndEmptyArrays = true;

				return builder;
			} else {
				throw new AppException(AppException.Code.E000, "Entity collection config is invalid");
			}
		}

		public <T> AggregationLookupJoinBuilder<S, T> innerJoin(Class<T> targetEntity) {
			var builder = new AggregationLookupJoinBuilder<S, T>();
			builder.sourceEntity = sourceEntity;
			builder.targetEntity = targetEntity;
			builder.preserveNullAndEmptyArrays = false;

			return builder;
		}

	}

	public static class AggregationLookupJoinBuilder<S, T> {

		private Class<S> sourceEntity;

		private Class<T> targetEntity;

		private String localField;

		private String foreignField;

		private Boolean preserveNullAndEmptyArrays = false;

		public AggregationLookupJoinBuilder<S, T> on(SFunction<S, ?> sourceEntityFieldGetter, SFunction<T, ?> targetEntityFieldGetter) {
			localField = field(sourceEntityFieldGetter);
			foreignField = field(targetEntityFieldGetter);

			return this;
		}

		public <V> AggregationLookupBuilder<S, T, V> mappingTo(Class<V> voEntity) {
			if(StringUtils.hasText(localField) && StringUtils.hasText(foreignField)) {
				var builder = new AggregationLookupBuilder<S, T, V>();
				builder.sourceEntity = sourceEntity;
				builder.targetEntity = targetEntity;
				builder.voEntity = voEntity;
				builder.localField = localField;
				builder.foreignField = foreignField;
				builder.preserveNullAndEmptyArrays = preserveNullAndEmptyArrays;

				return builder;
			} else {
				throw new AppException(AppException.Code.E000, "mapping on field is empty");
			}
		}

	}

}
