package com.jaycode.framework.cloud.boot.core.entity;

import com.jaycode.framework.cloud.boot.core.classmeta.ClassMeta;
import com.jaycode.framework.cloud.boot.core.classmeta.ObjectAccessor;
import com.jaycode.framework.cloud.boot.core.entity.persistence.Archived;
import com.jaycode.framework.cloud.boot.core.entity.persistence.EntitySchema;
import com.jaycode.framework.cloud.boot.core.entity.persistence.PreArchived;
import com.jaycode.framework.cloud.boot.core.support.EntityUtils;
import com.jaycode.framework.cloud.boot.core.support.StringUtils;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体元素数据统一缓存管理，避免性能消耗
 *
 * @author jinlong.wang
 */
@SuppressWarnings("unchecked")
public class EntityMeta {
    //POST注解方法列表
    private final Map<Class<? extends Annotation>, List<Method>> postMethods = new HashMap<>();
    /**
     * 可用的字段缓存
     * 可用定义： 有Column注解并且insertable 为false的字段定义
     */
    private final Map<String, ColumnProperty> persistentColumnPropertyMap = new HashMap<>();
    //支持POST的注解
    private static final Class<? extends Annotation>[] POST_ANNOTATIONS = new Class[]{PrePersist.class, PreUpdate.class, PreArchived.class};

    //所有字段定义
    private final List<ColumnProperty> allColumnProperties = new ArrayList<>();
    //归档字段定义
    private ColumnProperty primaryArchiveColumnProperty;
    //归档受影响字段定义
    private List<ColumnProperty> archiveColumnProperties = new ArrayList<>();
    //主键字段定义
    private ColumnProperty idColumnProperty;
    //数据库乐观锁字段
    private ColumnProperty versionColumnProperty;

    private EntitySchema entitySchema;

    private Class<?> modelClass;


    protected EntityMeta(Class<?> modelClass) {
        ClassMeta classMeta = ClassMeta.of(modelClass);
        //解析字段
        classMeta.filterAllFieldsByAnnotation(Column.class)
                .forEach(fieldDecorate -> {
                    Field field = fieldDecorate.getField();
                    Column column = (Column) fieldDecorate.getDecorate();
                    ColumnProperty columnProperty = new ColumnProperty(fieldDecorate);
                    allColumnProperties.add(columnProperty);
                    if (field.isAnnotationPresent(Archived.class) && field.getAnnotation(Archived.class).primary()) {
                        primaryArchiveColumnProperty = columnProperty;
                    }
                    if (field.isAnnotationPresent(Archived.class) && !field.getAnnotation(Archived.class).primary()) {
                        archiveColumnProperties.add(columnProperty);
                    }
                    if (field.isAnnotationPresent(Id.class)) {
                        idColumnProperty = columnProperty;
                    }
                    if (field.isAnnotationPresent(Version.class)) {
                        versionColumnProperty = columnProperty;
                    }
                    //插入的字段才允许查询，否则没有意义，即 insertable 为false的字段不参与到crud中
                    if (column.insertable()) {
                        persistentColumnPropertyMap.put(columnProperty.getField().getName(), columnProperty);
                    }
                });

        if (idColumnProperty == null) {
            throw new EntityPersistException("\r\n实体对象必须存在ID\r\n" +
                    "考虑在" + modelClass.getName() + "上添加@Id和@Column注解声明ID字段");
        }
        if (primaryArchiveColumnProperty == null && (archiveColumnProperties != null && archiveColumnProperties.size() > 0)) {
            //如果没有主归档字段则忽略其他归档资源
            archiveColumnProperties = new ArrayList<>();
        }
        //解析方法
        List<Method> methods = classMeta.getAllDeclaredMethods();
        for (Class<? extends Annotation> post : POST_ANNOTATIONS) {
            //拦截post 操作
            postMethods.put(post, methods.stream().filter(m -> m.isAnnotationPresent(post)).collect(Collectors.toList()));
        }
        this.entitySchema = modelClass.getAnnotation(EntitySchema.class);
        this.modelClass = modelClass;
    }

    public ColumnProperty getIdColumnField() {
        return idColumnProperty;
    }

    public List<ColumnProperty> getAllColumnFields() {
        return allColumnProperties;
    }

    public ColumnProperty getPrimaryArchiveColumnField() {
        return primaryArchiveColumnProperty;
    }

    public ColumnProperty getVersionColumnField() {
        return versionColumnProperty;
    }

    public String getPersistentColumnName(String propertyName) {
        ColumnProperty columnProperty = persistentColumnPropertyMap.get(propertyName);
        if (columnProperty != null) {
            return columnProperty.getName();
        }
        return null;
    }

    public ColumnProperty getPersistentColumnField(String key) {
        return persistentColumnPropertyMap.get(key);
    }

    public Set<String> getPersistentColumnNames(Set<String> selectPropertyNames) {
        Set<String> c = new HashSet<>();
        for (String c2 : selectPropertyNames) {
            Optional.ofNullable(getPersistentColumnName(c2)).ifPresent(c::add);
        }
        return c;
    }

    /**
     * 获取持久化字段属性名称
     *
     * @return 属性名称集合
     */
    public Set<String> getPersistentColumnPropertyNames() {
        //通过构建hashset实例,copy对象，避免外部remove 操作影响链表内部逻辑
        return new HashSet<>(persistentColumnPropertyMap.keySet());
    }

    public List<ColumnProperty> getArchiveColumnSiblingsFields() {
        return archiveColumnProperties;
    }

    public void invokePostMethod(Class<? extends Annotation> postAnnotation, Object paramObject) {
        List<Method> methods = postMethods.get(postAnnotation);
        methods.forEach((method) -> {
            try {
                method.invoke(paramObject);
            } catch (Exception e) {
                throw new EntityPersistException("pre " + postAnnotation.getSimpleName() + " error", e);
            }
        });
    }

    public void generateId(Object sourceObject) {
        IdGenerateStrategy idGenerateStrategy = IdGenerateStrategy.UUID;
        if (entitySchema != null) {
            idGenerateStrategy = entitySchema.idGenerateStrategy();
        }
        if (idColumnProperty != null &&
                idColumnProperty.getField().isAnnotationPresent(GeneratedValue.class)
        ) {
            GeneratedValue generatedValue = idColumnProperty.getField().getAnnotation(GeneratedValue.class);
            //如果ID生成策略为自动，则在持久化时候自动产生ID信息
            if (GenerationType.AUTO.equals(generatedValue.strategy())) {
                String idFieldName = idColumnProperty.getField().getName();
                ObjectAccessor objectAccessor = ObjectAccessor.of(sourceObject);
                String idValue = objectAccessor.getValue(idFieldName);
                if (StringUtils.isEmpty(idValue)) {
                    objectAccessor.setValue(idFieldName,
                            (IdGenerateStrategy.SNOW_FLAKE.equals(idGenerateStrategy) ? EntityUtils.generateSnowFlakeId() : EntityUtils.generateUuid())
                    );
                }
            }


        }
    }

    public String getTableName() {
        Entity entity = modelClass.getAnnotation(Entity.class);
        if (entity != null && com.jaycode.framework.cloud.boot.core.support.StringUtils.hasLength(entity.name())) {
            return entity.name();
        }

        return null;
    }
}
