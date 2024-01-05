package com.walnutek.fermentationtank.config.mongo;

import com.walnutek.fermentationtank.model.service.Utils.SFunction;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Update.Position;
import org.springframework.lang.Nullable;

import static com.walnutek.fermentationtank.model.service.Utils.field;

public class UpdateBuilder {

	private Update update = new Update();
	
	public static UpdateBuilder newInstance() {
		return new UpdateBuilder();
	}
	
	/**
	 * Update using the {@literal $set} update modifier
	 *
	 * @param fieldGetter
	 * @param value can be {@literal null}. In this case the property remains in the db with a {@literal null} value. To
	 *          remove it use {@link #unset(String)}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/operator/update/set/">MongoDB Update operator: $set</a>
	 */
	public <T> UpdateBuilder set(SFunction<T,?> fieldGetter, @Nullable Object value) {
		update.set(field(fieldGetter), value);
		return this;
	}
	
	/**
	 * Update using the {@literal $setOnInsert} update modifier
	 *
	 * @param fieldGetter
	 * @param value can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/setOnInsert/">MongoDB Update operator:
	 *      $setOnInsert</a>
	 */
	public <T> UpdateBuilder setOnInsert(SFunction<T,?> fieldGetter, @Nullable Object value) {
		update.setOnInsert(field(fieldGetter), value);
		return this;
	}
	
	/**
	 * Update using the {@literal $unset} update modifier
	 *
	 * @param fieldGetter
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/unset/">MongoDB Update operator: $unset</a>
	 */
	public <T> UpdateBuilder unset(SFunction<T,?> fieldGetter) {
		update.unset(field(fieldGetter));
		return this;
	}
	
	/**
	 * Update using the {@literal $inc} update modifier
	 *
	 * @param fieldGetter
	 * @param inc must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/inc/">MongoDB Update operator: $inc</a>
	 */
	public <T> UpdateBuilder inc(SFunction<T,?> fieldGetter, Number inc) {
		update.inc(field(fieldGetter), inc);
		return this;
	}
	
	/**
	 * Update using the {@literal $push} update modifier
	 *
	 * @param fieldGetter
	 * @param value can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/push/">MongoDB Update operator: $push</a>
	 */
	public <T> UpdateBuilder push(SFunction<T,?> fieldGetter, @Nullable Object value) {
		update.push(field(fieldGetter), value);
		return this;
	}
	
	/**
	 * Update using the {@literal $addToSet} update modifier
	 *
	 * @param fieldGetter
	 * @param value can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/addToSet/">MongoDB Update operator:
	 *      $addToSet</a>
	 */
	public <T> UpdateBuilder addToSet(SFunction<T,?> fieldGetter, @Nullable Object value) {
		update.addToSet(field(fieldGetter), value);
		return this;
	}
	
	/**
	 * Update using the {@literal $pop} update modifier
	 *
	 * @param fieldGetter
	 * @param pos must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/pop/">MongoDB Update operator: $pop</a>
	 */
	public <T> UpdateBuilder pop(SFunction<T,?> fieldGetter, Position pos) {
		update.pop(field(fieldGetter), pos);
		return this;
	}
	
	/**
	 * Update using the {@literal $pull} update modifier
	 *
	 * @param fieldGetter
	 * @param value can be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/pull/">MongoDB Update operator: $pull</a>
	 */
	public <T> UpdateBuilder pull(SFunction<T,?> fieldGetter, @Nullable Object value) {
		update.pull(field(fieldGetter), value);
		return this;
	}
	
	/**
	 * Update using the {@literal $pullAll} update modifier
	 *
	 * @param fieldGetter
	 * @param values must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/pullAll/">MongoDB Update operator:
	 *      $pullAll</a>
	 */
	public <T> UpdateBuilder pullAll(SFunction<T,?> fieldGetter, Object[] values) {
		update.pullAll(field(fieldGetter), values);
		return this;
	}
	
	/**
	 * Update given key to current date using {@literal $currentDate} modifier.
	 *
	 * @param fieldGetter
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/currentDate/">MongoDB Update operator:
	 *      $currentDate</a>
	 */
	public <T> UpdateBuilder currentDate(SFunction<T,?> fieldGetter) {
		update.currentDate(field(fieldGetter));
		return this;
	}
	
	/**
	 * Update given key to current date using {@literal $currentDate : &#123; $type : "timestamp" &#125;} modifier.
	 *
	 * @param fieldGetter
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/currentDate/">MongoDB Update operator:
	 *      $currentDate</a>
	 */
	public <T> UpdateBuilder currentTimestamp(SFunction<T,?> fieldGetter) {
		update.currentTimestamp(field(fieldGetter));
		return this;
	}
	
	/**
	 * Multiply the value of given key by the given number.
	 *
	 * @param fieldGetter
	 * @param multiplier must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/mul/">MongoDB Update operator: $mul</a>
	 */
	public <T> UpdateBuilder multiply(SFunction<T,?> fieldGetter, Number multiplier) {
		update.multiply(field(fieldGetter), multiplier);
		return this;
	}
	
	/**
	 * Update given key to the {@code value} if the {@code value} is greater than the current value of the field.
	 *
	 * @param fieldGetter
	 * @param value must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/bson-type-comparison-order/">Comparison/Sort Order</a>
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/max/">MongoDB Update operator: $max</a>
	 */
	public <T> UpdateBuilder max(SFunction<T,?> fieldGetter, Object value) {
		update.max(field(fieldGetter), value);
		return this;
	}
	
	/**
	 * Update given key to the {@code value} if the {@code value} is less than the current value of the field.
	 *
	 * @param fieldGetter
	 * @param value must not be {@literal null}.
	 * @return this.
	 * @see <a href="https://docs.mongodb.com/manual/reference/bson-type-comparison-order/">Comparison/Sort Order</a>
	 * @see <a href="https://docs.mongodb.org/manual/reference/operator/update/min/">MongoDB Update operator: $min</a>
	 */
	public <T> UpdateBuilder min(SFunction<T,?> fieldGetter, Object value) {
		update.min(field(fieldGetter), value);
		return this;
	}
	
	public Update build() {
		return update;
	}
	
}
