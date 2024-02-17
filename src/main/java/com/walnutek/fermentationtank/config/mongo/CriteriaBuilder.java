package com.walnutek.fermentationtank.config.mongo;

import com.walnutek.fermentationtank.model.service.Utils.SFunction;
import org.bson.BsonRegularExpression;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Shape;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static com.walnutek.fermentationtank.model.service.Utils.field;

public class CriteriaBuilder {

	private Criteria criteria;

	/**
	 * 最終是否輸出Criteria, 搭配where, build, 及null檢查使用
	 */
	private Boolean needBuild = true;

	private Boolean chainByOr = false;

	private List<Criteria> orCriteriaList = new ArrayList<>();

	private CriteriaBuilder() {}

	/**
	 * Static factory method to create a Criteria using the provided key
	 *
	 * @param fieldGetter
	 * @return new instance of {@link Criteria}.
	 */
	public static <T> CriteriaBuilder where(SFunction<T,?> fieldGetter) {
		var builder = new CriteriaBuilder();
		builder.criteria = Criteria.where(field(fieldGetter));
		return builder;
	}

	/**
	 * Static factory method to create a Criteria using the provided key
	 *
	 * @param fieldGetter
	 * @return new instance of {@link Criteria}.
	 */
	public static <T> CriteriaBuilder where(Boolean condition, SFunction<T,?> fieldGetter) {
		var builder = new CriteriaBuilder();
		builder.needBuild = condition;
		builder.criteria = Criteria.where(field(fieldGetter));
		return builder;
	}

	/**
	 * Static factory method to create a Criteria using the provided keys
	 *
	 * @param sourceFieldGetter
	 * @param targetFieldGetter
	 * @return new instance of {@link Criteria}.
	 */
	public static <S, T> CriteriaBuilder where(SFunction<S,T> sourceFieldGetter, SFunction<T,?> targetFieldGetter) {
		var builder = new CriteriaBuilder();
		builder.criteria = Criteria.where(String.join(".", field(sourceFieldGetter), field(targetFieldGetter)));
		return builder;
	}

	public static <T> CriteriaBuilder where(String key) {
		var builder = new CriteriaBuilder();
		builder.criteria = Criteria.where(key);
		return builder;
	}

	/**
	 * Static factory method to create a {@link Criteria} matching a documents against the given {@link MongoExpression
	 * expression}.
	 * <p>
	 * The {@link MongoExpression expression} can be either something that directly renders to the store native
	 * representation like
	 *
	 * <pre class="code">
	 * expr(() -> Document.parse("{ $gt : [ '$spent', '$budget'] }")))
	 * </pre>
	 *
	 * or an {@link org.springframework.data.mongodb.core.aggregation.AggregationExpression} which will be subject to
	 * context (domain type) specific field mapping.
	 *
	 * <pre class="code">
	 * expr(valueOf("amountSpent").greaterThan("budget"))
	 * </pre>
	 *
	 * @param expression must not be {@literal null}.
	 * @return new instance of {@link Criteria}.
	 * @since 4.1
	 */
	public static CriteriaBuilder expr(MongoExpression expression) {
		var builder = new CriteriaBuilder();
		builder.criteria = Criteria.expr(expression);
		return builder;
	}

	/**
	 * Static factory method to create a Criteria using the provided key
	 *
	 * @return new instance of {@link Criteria}.
	 */
	public <T> CriteriaBuilder and(SFunction<T,?> fieldGetter) {
		criteria = criteria.and(field(fieldGetter));
		return this;
	}

	/**
	 * Creates a criterion using equality
	 *
	 * @param value can be {@literal null}.
	 * @return this.
	 */
	public CriteriaBuilder is(@Nullable Object value) {
		criteria = criteria.is(value);
		return this;
	}

	/**
	 * Creates a criterion using {@literal null} equality comparison which matches documents that either contain the item
	 * field whose value is {@literal null} or that do not contain the item field. <br />
	 * Use {@link #isNullValue()} to only query for documents that contain the field whose value is equal to
	 * {@link org.bson.BsonType#NULL}. <br />
	 * Use {@link #exists(boolean)} to query for documents that do (not) contain the field.
	 *
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/tutorial/query-for-null-fields/#equality-filter">Query for Null or
	 *      Missing Fields: Equality Filter</a>
	 * @since 3.3
	 */
	public CriteriaBuilder isNull() {
		return is(null);
	}

	/**
	 * Creates a criterion using a {@link org.bson.BsonType} comparison which matches only documents that contain the item
	 * field whose value is equal to {@link org.bson.BsonType#NULL}. <br />
	 * Use {@link #isNull()} to query for documents that contain the field with a {@literal null} value or do not contain
	 * the field at all. <br />
	 * Use {@link #exists(boolean)} to query for documents that do (not) contain the field.
	 *
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/tutorial/query-for-null-fields/#type-check">Query for Null or Missing
	 *      Fields: Type Check</a>
	 * @since 3.3
	 */
	public CriteriaBuilder isNullValue() {
		criteria = criteria.isNullValue();
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $ne} operator.
	 *
	 * @param value can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/ne/">MongoDB Query operator: $ne</a>
	 */
	public CriteriaBuilder ne(@Nullable Object value) {
		criteria = criteria.ne(value);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $lt} operator.
	 *
	 * @param value must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/lt/">MongoDB Query operator: $lt</a>
	 */
	public CriteriaBuilder lt(Object value) {
		criteria = criteria.lt(value);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $lte} operator.
	 *
	 * @param value must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/lte/">MongoDB Query operator: $lte</a>
	 */
	public CriteriaBuilder lte(Object value) {
		criteria = criteria.lte(value);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $gt} operator.
	 *
	 * @param value must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/gt/">MongoDB Query operator: $gt</a>
	 */
	public CriteriaBuilder gt(Object value) {
		criteria = criteria.gt(value);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $gte} operator.
	 *
	 * @param value can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/gte/">MongoDB Query operator: $gte</a>
	 */
	public CriteriaBuilder gte(Object value) {
		criteria = criteria.gte(value);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $in} operator.
	 *
	 * @param values the values to match against
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/in/">MongoDB Query operator: $in</a>
	 */
	public CriteriaBuilder in(Object... values) {
		criteria = criteria.in(values);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $in} operator.
	 *
	 * @param values the collection containing the values to match against
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/in/">MongoDB Query operator: $in</a>
	 */
	public CriteriaBuilder in(Collection<?> values) {
		criteria = criteria.in(values);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $nin} operator.
	 *
	 * @param values
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/nin/">MongoDB Query operator: $nin</a>
	 */
	public CriteriaBuilder nin(Object... values) {
		return nin(Arrays.asList(values));
	}

	/**
	 * Creates a criterion using the {@literal $nin} operator.
	 *
	 * @param values must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/nin/">MongoDB Query operator: $nin</a>
	 */
	public CriteriaBuilder nin(Collection<?> values) {
		criteria = criteria.nin(values);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $mod} operator.
	 *
	 * @param value must not be {@literal null}.
	 * @param remainder must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/mod/">MongoDB Query operator: $mod</a>
	 */
	public CriteriaBuilder mod(Number value, Number remainder) {
		criteria = criteria.mod(value, remainder);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $all} operator.
	 *
	 * @param values must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/all/">MongoDB Query operator: $all</a>
	 */
	public CriteriaBuilder all(Object... values) {
		return all(Arrays.asList(values));
	}

	/**
	 * Creates a criterion using the {@literal $all} operator.
	 *
	 * @param values must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/all/">MongoDB Query operator: $all</a>
	 */
	public CriteriaBuilder all(Collection<?> values) {
		criteria = criteria.all(values);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $size} operator.
	 *
	 * @param size
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/size/">MongoDB Query operator: $size</a>
	 */
	public CriteriaBuilder size(int size) {
		criteria = criteria.size(size);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $exists} operator.
	 *
	 * @param value
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/exists/">MongoDB Query operator: $exists</a>
	 */
	public CriteriaBuilder exists(boolean value) {
		criteria = criteria.exists(value);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $sampleRate} operator.
	 *
	 * @param sampleRate sample rate to determine number of documents to be randomly selected from the input. Must be
	 *          between {@code 0} and {@code 1}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/aggregation/sampleRate/">MongoDB Query operator:
	 *      $sampleRate</a>
	 * @since 3.3
	 */
	public CriteriaBuilder sampleRate(double sampleRate) {
		criteria = criteria.sampleRate(sampleRate);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $type} operator.
	 *
	 * @param typeNumber
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/type/">MongoDB Query operator: $type</a>
	 */
	public CriteriaBuilder type(int typeNumber) {
		criteria = criteria.type(typeNumber);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $type} operator.
	 *
	 * @param types must not be {@literal null}.
	 * @return this.
	 * @since 2.1
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/type/">MongoDB Query operator: $type</a>
	 */
	public CriteriaBuilder type(Type... types) {
		return type(Arrays.asList(types));
	}

	/**
	 * Creates a criterion using the {@literal $type} operator.
	 *
	 * @param types must not be {@literal null}.
	 * @return this.
	 * @since 3.2
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/type/">MongoDB Query operator: $type</a>
	 */
	public CriteriaBuilder type(Collection<Type> types) {
		criteria = criteria.type(types);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $not} meta operator which affects the clause directly following
	 *
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/not/">MongoDB Query operator: $not</a>
	 */
	public CriteriaBuilder not() {
		criteria = criteria.not();
		return this;
	}

	public CriteriaBuilder like(Object value) {
		return Objects.nonNull(value) ? regex(String.format(".*%s.*", value), "i") : this;
	}

	public CriteriaBuilder likeLeft(Object value) {
		return Objects.nonNull(value) ? regex(String.format(".*%s", value)) : this;
	}

	public CriteriaBuilder likeRight(Object value) {
		return Objects.nonNull(value) ? regex(String.format(".*%s", value)) : this;
	}

	/**
	 * Creates a criterion using a {@literal $regex} operator.
	 *
	 * @param regex must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/regex/">MongoDB Query operator: $regex</a>
	 */
	public CriteriaBuilder regex(String regex) {
		return regex(regex, null);
	}

	/**
	 * Creates a criterion using a {@literal $regex} and {@literal $options} operator.
	 *
	 * @param regex must not be {@literal null}.
	 * @param options can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/regex/">MongoDB Query operator: $regex</a>
	 */
	public CriteriaBuilder regex(String regex, @Nullable String options) {
		criteria = criteria.regex(regex, options);
		return this;
	}

	/**
	 * Syntactical sugar for {@link #is(Object)} making obvious that we create a regex predicate.
	 *
	 * @param pattern must not be {@literal null}.
	 * @return this.
	 */
	public CriteriaBuilder regex(Pattern pattern) {
		criteria = criteria.regex(pattern);
		return this;
	}

	/**
	 * Use a MongoDB native {@link BsonRegularExpression}.
	 *
	 * @param regex must not be {@literal null}.
	 * @return this.
	 */
	public CriteriaBuilder regex(BsonRegularExpression regex) {
		criteria = criteria.regex(regex);
		return this;
	}

	/**
	 * Creates a geospatial criterion using a {@literal $geoWithin $centerSphere} operation. This is only available for
	 * Mongo 2.4 and higher.
	 *
	 * @param circle must not be {@literal null}
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/geoWithin/">MongoDB Query operator:
	 *      $geoWithin</a>
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/centerSphere/">MongoDB Query operator:
	 *      $centerSphere</a>
	 */
	public CriteriaBuilder withinSphere(Circle circle) {
		criteria = criteria.withinSphere(circle);
		return this;
	}

	/**
	 * Creates a geospatial criterion using a {@literal $geoWithin} operation.
	 *
	 * @param shape must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/geoWithin/">MongoDB Query operator:
	 *      $geoWithin</a>
	 */
	public CriteriaBuilder within(Shape shape) {
		criteria = criteria.within(shape);
		return this;
	}

	/**
	 * Creates a geospatial criterion using a {@literal $near} operation.
	 *
	 * @param point must not be {@literal null}
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/near/">MongoDB Query operator: $near</a>
	 */
	public CriteriaBuilder near(Point point) {
		criteria = criteria.near(point);
		return this;
	}

	/**
	 * Creates a geospatial criterion using a {@literal $nearSphere} operation. This is only available for Mongo 1.7 and
	 * higher.
	 *
	 * @param point must not be {@literal null}
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/nearSphere/">MongoDB Query operator:
	 *      $nearSphere</a>
	 */
	public CriteriaBuilder nearSphere(Point point) {
		criteria = criteria.nearSphere(point);
		return this;
	}

	/**
	 * Creates criterion using {@code $geoIntersects} operator which matches intersections of the given {@code geoJson}
	 * structure and the documents one. Requires MongoDB 2.4 or better.
	 *
	 * @param geoJson must not be {@literal null}.
	 * @return this.
	 * @since 1.8
	 */
	@SuppressWarnings("rawtypes")
	public CriteriaBuilder intersects(GeoJson geoJson) {
		criteria = criteria.intersects(geoJson);
		return this;
	}

	/**
	 * Creates a geo-spatial criterion using a {@literal $maxDistance} operation, for use with {@literal $near} or
	 * {@literal $nearSphere}.
	 * <p>
	 * <strong>NOTE:</strong> The unit of measure for distance may depends on the used coordinate representation (legacy
	 * vs. geoJson) as well as the target operation.
	 *
	 * @param maxDistance radians or meters
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/maxDistance/">MongoDB Query operator:
	 *      $maxDistance</a>
	 */
	public CriteriaBuilder maxDistance(double maxDistance) {
		criteria = criteria.maxDistance(maxDistance);
		return this;
	}

	/**
	 * Creates a geospatial criterion using a {@literal $minDistance} operation, for use with {@literal $near} or
	 * {@literal $nearSphere}.
	 * <p>
	 * <strong>NOTE:</strong> The unit of measure for distance may depends on the used coordinate representation (legacy
	 * vs. geoJson) as well as the target operation.
	 *
	 * @param minDistance radians or meters
	 * @return this.
	 * @since 1.7
	 */
	public CriteriaBuilder minDistance(double minDistance) {
		criteria = criteria.minDistance(minDistance);
		return this;
	}

	/**
	 * Creates a criterion using the {@literal $elemMatch} operator
	 *
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/query/elemMatch/">MongoDB Query operator:
	 *      $elemMatch</a>
	 */
	public CriteriaBuilder elemMatch(Criteria criteria) {
		criteria = criteria.elemMatch(criteria);
		return this;
	}

	public CriteriaBuilder or(CriteriaBuilder builder) {
		chainByOr = true;
		orCriteriaList.add(builder.build());
		return this;
	}

	/**
	 * Creates a criteria using the {@code $or} operator for all of the provided criteria.
	 * <p>
	 * Note that MongoDB doesn't support an {@code $nor} operator to be wrapped in a {@code $not} operator.
	 *
	 * @throws IllegalArgumentException if this method follows a {@link #not()} call directly.
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 */
	public CriteriaBuilder orOperator(Criteria... criterias) {
		return orOperator(Arrays.asList(criterias));
	}

	/**
	 * Creates a criteria using the {@code $or} operator for all of the provided criteria.
	 * <p>
	 * Note that MongoDB doesn't support an {@code $nor} operator to be wrapped in a {@code $not} operator.
	 *
	 * @throws IllegalArgumentException if this method follows a {@link #not()} call directly.
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 * @since 3.2
	 */
	public CriteriaBuilder orOperator(Collection<Criteria> criteriaList) {
		criteria = criteria.orOperator(criteriaList);
		return this;
	}

	/**
	 * Creates a criteria using the {@code $nor} operator for all of the provided criteria.
	 * <p>
	 * Note that MongoDB doesn't support an {@code $nor} operator to be wrapped in a {@code $not} operator.
	 *
	 * @throws IllegalArgumentException if this method follows a {@link #not()} call directly.
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 */
	public CriteriaBuilder norOperator(Criteria... criterias) {
		return norOperator(Arrays.asList(criterias));
	}

	/**
	 * Creates a criteria using the {@code $nor} operator for all of the provided criteria.
	 * <p>
	 * Note that MongoDB doesn't support an {@code $nor} operator to be wrapped in a {@code $not} operator.
	 *
	 * @throws IllegalArgumentException if this method follows a {@link #not()} call directly.
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 * @since 3.2
	 */
	public CriteriaBuilder norOperator(Collection<Criteria> criteriaList) {
		criteria = criteria.norOperator(criteriaList);
		return this;
	}

	/**
	 * Creates a criteria using the {@code $and} operator for all of the provided criteria.
	 * <p>
	 * Note that MongoDB doesn't support an {@code $and} operator to be wrapped in a {@code $not} operator.
	 *
	 * @throws IllegalArgumentException if this method follows a {@link #not()} call directly.
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 */
	public CriteriaBuilder andOperator(Criteria... criterias) {
		return andOperator(Arrays.asList(criterias));
	}

	/**
	 * Creates a criteria using the {@code $and} operator for all of the provided criteria.
	 * <p>
	 * Note that MongoDB doesn't support an {@code $and} operator to be wrapped in a {@code $not} operator.
	 *
	 * @throws IllegalArgumentException if this method follows a {@link #not()} call directly.
	 * @param criteria must not be {@literal null}.
	 * @return this.
	 * @since 3.2
	 */
	public CriteriaBuilder andOperator(Collection<Criteria> criteriaList) {
		criteria = criteria.andOperator(criteriaList);
		return this;
	}

	public Criteria build() {
		return needBuild ?
				chainByOr ? orBuild() : criteria :
				null;
	}

	private Criteria orBuild() {
		List<Criteria> orCriteriaList = new ArrayList<>();
		orCriteriaList.add(criteria);
		orCriteriaList.addAll(this.orCriteriaList);

		return new Criteria().orOperator(orCriteriaList);
	}

}
