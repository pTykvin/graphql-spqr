package io.leangen.graphql.metadata.strategy.type;

import io.leangen.graphql.annotations.types.GraphQLInterface;
import io.leangen.graphql.annotations.types.GraphQLType;
import io.leangen.graphql.annotations.types.GraphQLUnion;
import io.leangen.graphql.metadata.messages.MessageBundle;
import io.leangen.graphql.util.ClassUtils;
import io.leangen.graphql.util.Utils;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Bojan Tomic (kaqqao)
 */
public class DefaultTypeInfoGenerator implements TypeInfoGenerator {

    @Override
    public String generateTypeName(AnnotatedType type, MessageBundle messageBundle) {
        if (type instanceof AnnotatedParameterizedType) {
            String baseName = generateSimpleName(type, messageBundle);
            StringBuilder genericName = new StringBuilder(baseName);
            Arrays.stream(((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments())
                    .map(t -> generateSimpleName(t, messageBundle))
                    .forEach(argName -> genericName.append("_").append(argName));
            return genericName.toString();
        }
        return generateSimpleName(type, messageBundle);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String generateTypeDescription(AnnotatedType type, MessageBundle messageBundle) {
        Optional<String>[] descriptions = new Optional[]{
                Optional.ofNullable(type.getAnnotation(GraphQLUnion.class))
                        .map(GraphQLUnion::description),
                Optional.ofNullable(type.getAnnotation(GraphQLInterface.class))
                        .map(GraphQLInterface::description),
                Optional.ofNullable(type.getAnnotation(GraphQLType.class))
                        .map(GraphQLType::description)
        };
        return messageBundle.interpolate(getFirstNonEmptyOrDefault(descriptions, ""));
    }

    @Override
    public String[] getFieldOrder(AnnotatedType type, MessageBundle messageBundle) {
        return Utils.or(
                Optional.ofNullable(type.getAnnotation(GraphQLInterface.class))
                        .map(GraphQLInterface::fieldOrder),
                Optional.ofNullable(type.getAnnotation(GraphQLType.class))
                        .map(GraphQLType::fieldOrder))
                .orElse(Utils.emptyArray());
    }

    @SuppressWarnings("unchecked")
    private String generateSimpleName(AnnotatedType type, MessageBundle messageBundle) {
        Optional<String>[] names = new Optional[]{
                Optional.ofNullable(type.getAnnotation(GraphQLUnion.class))
                        .map(GraphQLUnion::name),
                Optional.ofNullable(type.getAnnotation(GraphQLInterface.class))
                        .map(GraphQLInterface::name),
                Optional.ofNullable(type.getAnnotation(GraphQLType.class))
                        .map(GraphQLType::name)
        };
        return messageBundle.interpolate(getFirstNonEmptyOrDefault(names, ClassUtils.getRawType(type.getType()).getSimpleName()));
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
    private String getFirstNonEmptyOrDefault(Optional<String>[] optionals, String defaultValue) {
        return Arrays.stream(optionals)
                .map(opt -> opt.filter(Utils::isNotEmpty))
                .reduce(Utils::or)
                .map(opt -> opt.orElse(defaultValue))
                .get();
    }
}
