package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.vo.Page;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.model.service.Utils.field;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public abstract class BaseDao<T extends BaseColumns> {

    @SuppressWarnings("unchecked")
	private final Class<T>  TYPE_REF = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), BaseDao.class);

    @Autowired
    private MongoTemplate template;

    @Autowired
    private MongoConverter mongoConverter;

    private MongoRepository<T, String> repository;

    @PostConstruct
    private void init() {
    	initMongoRepository();
    }

    private void initMongoRepository() {

    	if (mongoConverter.getMappingContext() instanceof MongoMappingContext context) {
    		@SuppressWarnings("unchecked")
			var persistentEntity = (MongoPersistentEntity<T>) context.getPersistentEntity(TYPE_REF);

    		if(Objects.nonNull(persistentEntity)) {
    			var mappingMongoEntityInformation = new MappingMongoEntityInformation<T, String>(persistentEntity, String.class);
    			repository = new SimpleMongoRepository<T, String>(mappingMongoEntityInformation, template);
    		}
    	}
    }

    public List<T> selectAll() {
        return template.findAll(TYPE_REF);
    }

    public T selectOne(Query query) {
        return template.findOne(query, TYPE_REF);
    }

    public T selectOne(List<Criteria> criteriaList) {
        var query = new Query();
        criteriaList.forEach(query::addCriteria);

        return template.findOne(query, TYPE_REF);
    }

    public List<T> selectList(Query query) {
        return template.find(query, TYPE_REF);
    }

    public List<T> selectList(List<Criteria> criteriaList) {
        var query = new Query();
        criteriaList.forEach(query::addCriteria);
        return template.find(query, TYPE_REF);
    }

    protected Page<T> search(QueryCondition condition) {
        return search(condition, TYPE_REF);
    }

    protected Page<T> search(QueryCondition condition, Class<T> typeRef) {
        var totalCount = template.count(condition.toQuery(), typeRef);
        var resultList = template.find(condition.toPagedQuery(), typeRef);

        return Page.of(PageableExecutionUtils.getPage(
                resultList,
                condition.pageable,
                () -> totalCount));
    }

    protected <O> Page<O> aggregationSearch(QueryCondition condition, Class<O> to) {
        return aggregationSearch(condition, TYPE_REF, to);
    }

    protected <T, O> Page<O> aggregationSearch(QueryCondition condition, Class<T> from, Class<O> to) {
        var validPageable = getValidPageable(condition.pageable);

        List<AggregationOperation> aggregationList = new ArrayList<>();
        Boolean isBeforeLookupCondition;
        if(Objects.nonNull(condition.isBeforeLookupCondition)){
            isBeforeLookupCondition = condition.isBeforeLookupCondition;
        }else {
            isBeforeLookupCondition = false;
        }
        if(isBeforeLookupCondition){
            condition.criteriaList
                    .stream()
                    .map(Aggregation::match)
                    .forEach(aggregationList::add);
            aggregationList.addAll(getLookupAggregation(to));
        }else {
            aggregationList.addAll(getLookupAggregation(to));
            condition.criteriaList
                    .stream()
                    .map(Aggregation::match)
                    .forEach(aggregationList::add);
        }

        var totalCount = template.aggregate(newAggregation(from, aggregationList), to)
                                 .getMappedResults()
                                 .size();

        aggregationList.add(skip(validPageable.getOffset()));
        aggregationList.add(sort(condition.sort));
        aggregationList.add(limit(validPageable.getPageSize()));

    	var resultList = template.aggregate(newAggregation(from, aggregationList), to)
    							 .getMappedResults();

        return Page.of(PageableExecutionUtils.getPage(
                resultList,
                validPageable,
                () -> totalCount));
    }

    protected <T, O> Page<O> aggregationSearch(QueryCondition beforeLookupCondition,
                                               QueryCondition afterLookupCondition,
                                               Class<T> from,
                                               Class<O> to) {
        var validPageable = getValidPageable(beforeLookupCondition.pageable);

        List<AggregationOperation> aggregationList = new ArrayList<>();
        beforeLookupCondition.criteriaList
                .stream()
                .map(Aggregation::match)
                .forEach(aggregationList::add);
        aggregationList.addAll(getLookupAggregation(to));
        afterLookupCondition.criteriaList
                .stream()
                .map(Aggregation::match)
                .forEach(aggregationList::add);

        var totalCount = template.aggregate(newAggregation(from, aggregationList), to)
                .getMappedResults()
                .size();

        aggregationList.add(skip(validPageable.getOffset()));
        aggregationList.add(sort(beforeLookupCondition.sort));
        aggregationList.add(limit(validPageable.getPageSize()));

        var resultList = template.aggregate(newAggregation(from, aggregationList), to)
                .getMappedResults();

        return Page.of(PageableExecutionUtils.getPage(
                resultList,
                validPageable,
                () -> totalCount));
    }

    public <T, O> List<O> aggregationSelectList(QueryCondition condition, Class<T> from, Class<O> to) {
        List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.addAll(getLookupAggregation(to));
        condition.criteriaList
                .stream()
                .map(Aggregation::match)
                .forEach(aggregationList::add);

        return template.aggregate(newAggregation(from, aggregationList), to)
                .getMappedResults();
    }

    protected Pageable getValidPageable(Pageable pageable) {
        var source = pageable.getPageNumber() - 1;
        var target = source <= 0 ? 0 : source;
        return pageable.withPage(target);
    }

    protected Pageable getPageable(Map<String, Object> paramMap) {
        int currentPage = Integer.parseInt(String.valueOf(paramMap.getOrDefault(Const.PAGE, Const.DEFAULT_PAGE)));
        int limit = Integer.parseInt(String.valueOf(paramMap.getOrDefault(Const.LIMIT, Const.DEFAULT_LIMIT)));

        return PageRequest.of(currentPage, limit);
    }

    protected Sort getSort(Map<String, Object> paramMap) {
        return getSort(paramMap, TYPE_REF);
    }
    protected <O> Sort getSort(Map<String, Object> paramMap, Class<O> typeRef) {
        var sourceSortField = Optional.ofNullable(String.valueOf(paramMap.get(Const.SORT_FIELD_KEY))).orElse("");
        var targetSortField = Stream.concat(
                    Stream.of(typeRef.getDeclaredFields()),
                    Stream.of(BaseColumns.class.getDeclaredFields()))
                .map(field -> field.getName())
                .filter(field -> sourceSortField.equals(field))
                .findFirst()
                .orElse(field(BaseColumns::getCreateTime));

        var sourceSortDirection = Optional.ofNullable(String.valueOf(paramMap.get(Const.SORT_DIRECTION_KEY))).orElse("");
        var targetSortDirection = Stream.of("asc", "desc")
                .filter(direction -> sourceSortDirection.equalsIgnoreCase(direction))
                .findFirst()
                .map(direction -> switch (direction) {
                    case "desc" -> Sort.Direction.DESC;
                    default -> Sort.Direction.ASC;
                })
                .orElse(Sort.Direction.ASC);

        return Sort.by(targetSortDirection, targetSortField);
    }

    public T selectById(String id) {
        return template.findById(id, TYPE_REF);
    }

    public T selectByIdAndStatus(String id, BaseColumns.Status status) {
        var query = new Query();
        query.addCriteria(CriteriaBuilder.where("status").is(status).build());
        query.addCriteria(CriteriaBuilder.where(BaseColumns::getId).is(id).build());
        return template.findOne(query, TYPE_REF);
    }

    public <O> O selectById(String id, Class<O> to) {
    	List<AggregationOperation> aggregationList = new ArrayList<>();
    	aggregationList.add(match(where(field(BaseColumns::getId)).is(id)));
        aggregationList.addAll(getLookupAggregation(to));

        return template.aggregate(newAggregation(TYPE_REF, aggregationList), to)
					   .getMappedResults()
					   .stream()
					   .findFirst()
					   .orElse(null);
    }

    public List<T> selectByIds(List<String> idList) {
        return template.find(Query.query(where(field(BaseColumns::getId)).in(idList)), TYPE_REF);
    }

    public <O> List<O> selectByIds(List<String> idList, Class<O> to) {
        List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.add(match(where(field(BaseColumns::getId)).in(idList)));
        aggregationList.addAll(getLookupAggregation(to));

        return template.aggregate(newAggregation(TYPE_REF, aggregationList), to)
                .getMappedResults();
    }

    public boolean existById(String id) {
        return Objects.nonNull(selectById(id));
    }

    public void insert(T data) {
        data.setCreateTime(LocalDateTime.now());
        Optional.ofNullable(Auth.getAuthUser())
                .map(AuthUser::getUserId)
                .filter(ObjectId::isValid)
                .ifPresent(data::setCreateUser);

        template.save(data);
    }

    public Optional<T> updateById(Object id, Update update) {
        update.set(field(BaseColumns::getUpdateTime), LocalDateTime.now());
        Optional.ofNullable(Auth.getAuthUser())
                .map(AuthUser::getUserId)
                .filter(ObjectId::isValid)
                .map(ObjectId::new)
                .ifPresent(userId -> update.set(field(BaseColumns::getUpdateUser), userId));

        return template.update(TYPE_REF)
                .matching(where(field(BaseColumns::getId)).is(id))
                .apply(update)
                .findAndModify();
    }

    public void updateById(T data) {
        repository.save(data);
    }

    public void deleteById(String id) {
        if(existById(id)) {
            template.remove(selectById(id));
        }
    }

    public long count(List<Criteria> criteriaList) {
        var query = new Query();
        criteriaList.forEach(query::addCriteria);

        return template.count(query, TYPE_REF);
    }


    private <O> List<AggregationOperation> getLookupAggregation(Class<O> target) {
    	List<AggregationOperation> aggregationList = new ArrayList<>();

    	try {
			var method = target.getMethod("getLookupAggregation");

			@SuppressWarnings("unchecked")
			var lookupAggregation = (List<AggregationOperation>) method.invoke(null);
			aggregationList.addAll(lookupAggregation);
        } catch (Exception e) {
//			e.printStackTrace();
		}

    	return aggregationList;
    }

    /**
     * 查詢條件封裝物件
     */
    @Data
    public static class QueryCondition {

        /**
         * 查詢條件
         */
        private List<Criteria> criteriaList;

        /**
         * 排序條件
         */
        private Sort sort;

        /**
         * 分頁物件
         */
        private Pageable pageable;

        /**
         * 是否先過濾再lookup
         */
        private Boolean isBeforeLookupCondition;

        public static QueryCondition of(List<Criteria> criteriaList, Sort sort, Pageable pageable) {
            var vo = new QueryCondition();
            vo.criteriaList = criteriaList;
            vo.sort = sort;
            vo.pageable = pageable;
            return vo;
        }

        public static QueryCondition of(List<Criteria> criteriaList) {
            var vo = new QueryCondition();
            vo.criteriaList = criteriaList;

            return vo;
        }

        public Query toQuery() {
        	var query = new Query();
        	criteriaList.forEach(query::addCriteria);

            return query;
        }

        public Query toPagedQuery() {
        	var query = toQuery();

        	query.with(sort);
            query.with(pageable);

            return query;
        }

    }

}
