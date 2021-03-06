package org.libsmith.anvil.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * http://habrahabr.ru/blogs/java/66593/ и немного модифицировано
 *
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.03.16 5:06
 */
public class GenericReflection<T> {

    private final Class<T> genericClass;

    protected GenericReflection(Class<T> genericClass) {
        this.genericClass = genericClass;
    }

    public static <T> GenericReflection<T> extractParameterOf(Class<T> genericContainerClass) {
        return new GenericReflection<>(genericContainerClass);
    }

    public Indexed atIndex(int parameterIndex) {
        return new Indexed(parameterIndex);
    }

    public class Indexed {

        private final int parameterIndex;

        protected Indexed(int parameterIndex) {
            this.parameterIndex = parameterIndex;
        }

        public AsType asType() {
            return new AsType();
        }

        public @Nonnull <R> Class<R> from(@Nonnull final T instance) throws IllegalArgumentException {
            return ReflectionUtils.extractClass(GenericReflection.this.from(instance, parameterIndex));
        }

        public @Nonnull <R> Class<R> from(@Nonnull final Type actualType) throws IllegalArgumentException {
            return ReflectionUtils.extractClass(GenericReflection.this.from(actualType, parameterIndex));
        }

        public class AsType {

            public @Nonnull Type from(@Nonnull final T instance) throws IllegalArgumentException {
                return GenericReflection.this.from(instance, parameterIndex);
            }

            public @Nonnull Type from(@Nonnull final Type actualType) throws IllegalArgumentException {
                return GenericReflection.this.from(actualType, parameterIndex);
            }
        }
    }

    @Nonnull Type from(@Nonnull T instance, int parameterIndex) throws IllegalArgumentException {
        return from(instance.getClass(), parameterIndex);
    }

    @Nonnull Type from(@Nonnull final Type actualType, int parameterIndex) throws IllegalArgumentException {
        final Class<?> actualClass = ReflectionUtils.extractClass(actualType);

        // Прекращаем работу если genericClass не является предком
        // actualClass.
        if (!genericClass.isAssignableFrom(actualClass)
                || (genericClass.equals(actualClass) && actualType instanceof Class)) {
            throw new IllegalArgumentException("Class "
                    + genericClass.getName() + " is not a superclass of "
                    + actualClass.getName() + ".");
        }

        final boolean isInterface = genericClass.isInterface();

        // Нам нужно найти класс, для которого непосредственным родителем будет
        // genericClass.
        // Мы будем подниматься вверх по иерархии, пока не найдем интересующий
        // нас класс.
        // В процессе поднятия мы будем сохранять в genericClasses все классы -
        // они нам понадобятся при спуске вниз.

        // Проейденные классы - используются для спуска вниз.
        final Deque<ParameterizedType> genericClasses = new ArrayDeque<>();

        // clazz - текущий рассматриваемый класс
        Type clazz = actualType;

        while (true) {
            final Type genericInterface = isInterface && clazz instanceof Class
                                          ? getGenericInterface((Class<?>) clazz, genericClass)
                                          : null;
            final Type currentType;
            if (genericInterface != null) {
                currentType = genericInterface;
            }
            else {
                if (clazz instanceof Class) {
                    currentType = ((Class<?>) clazz).getGenericSuperclass();
                }
                else {
                    currentType = clazz;
                }
            }

            final boolean isParametrizedType = currentType instanceof ParameterizedType;
            if (isParametrizedType) {
                // Если предок - параметризованный класс, то запоминаем его -
                // возможно он пригодится при спуске вниз.
                genericClasses.push((ParameterizedType) currentType);
            }
            else {
                // В иерархии встретился непараметризованный класс. Все ранее
                // сохраненные параметризованные классы будут бесполезны.
                genericClasses.clear();
            }
            // Проверяем, дошли мы до нужного предка или нет.
            final Type rawType = isParametrizedType ? ((ParameterizedType) currentType).getRawType() : currentType;
            if (!rawType.equals(genericClass)) {
                // genericClass не является непосредственным родителем для
                // текущего класса.
                // Поднимаемся по иерархии дальше.
                clazz = rawType;
            }
            else {
                // Мы поднялись до нужного класса. Останавливаемся.
                break;
            }
        }

        // Нужный класс найден. Теперь мы можем узнать, какими типами он
        // параметризован.
        Type result = genericClasses.pop().getActualTypeArguments()[parameterIndex];

        while (result instanceof TypeVariable && !genericClasses.isEmpty()) {
            // Похоже наш параметр задан где-то ниже по иерархии, спускаемся
            // вниз.

            // Получаем индекс параметра в том классе, в котором он задан.
            final int actualArgumentIndex = getParameterTypeDeclarationIndex((TypeVariable<?>) result);
            // Берем соответствующий класс, содержащий метаинформацию о нашем
            // параметре.
            final ParameterizedType type = genericClasses.pop();
            // Получаем информацию о значении параметра.
            result = type.getActualTypeArguments()[actualArgumentIndex];
        }

        if (result instanceof TypeVariable) {
            // Мы спустились до самого низа, но даже там нужный параметр не
            // имеет явного задания.
            // Следовательно из-за "Type erasure" узнать класс для параметра
            // невозможно.
            throw new IllegalArgumentException("Unable to resolve type variable " + result + "."
                    + " Try to replace instances of parametrized class with its non-parameterized subtype.");
        }

        if (result == null) {
            throw new IllegalArgumentException("Unable to determine actual parameter type for " + actualType + ".");
        }
        result = ReflectionUtils.extractWildcardType(result);
        if (result instanceof WildcardType) {
            WildcardType wt = (WildcardType) result;
            if (wt.getLowerBounds().length == 1) {
                result = wt.getLowerBounds()[0];
            }
            else if (wt.getUpperBounds().length == 1) {
                result = wt.getUpperBounds()[0];
            }
        }
        if (result instanceof Class || result instanceof ParameterizedType) {
            return result;
        }
        throw new IllegalArgumentException("Actual parameter type for " + actualType + " is not a object Class: " + result);
        // Похоже, что параметр - массив, примитивный тип, интерфейс или
        // еще-что-то, что не является классом.
    }

    private static int getParameterTypeDeclarationIndex(final TypeVariable<?> typeVariable) {
        final GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();

        // Ищем наш параметр среди всех параметров того класса, где определен
        // нужный нам параметр.
        final TypeVariable<?>[] typeVariables = genericDeclaration.getTypeParameters();
        Integer actualArgumentIndex = null;
        for (int i = 0; i < typeVariables.length; i++) {
            if (typeVariables[i].equals(typeVariable)) {
                actualArgumentIndex = i;
                break;
            }
        }
        if (actualArgumentIndex != null) {
            return actualArgumentIndex;
        }
        else {
            throw new IllegalArgumentException("Argument " + typeVariable.toString() + " is not found in "
                    + genericDeclaration.toString() + ".");
        }
    }

    private static Type getGenericInterface(final Class<?> sourceClass, final Class<?> genericInterface) {
        final Type[] types = sourceClass.getGenericInterfaces();
        for (final Type type : types) {
            if (type instanceof Class) {
                if (genericInterface.isAssignableFrom((Class<?>) type)) {
                    return type;
                }
            }
            else if (type instanceof ParameterizedType) {
                if (genericInterface.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType())) {
                    return type;
                }
            }
        }
        return null;
    }
}
