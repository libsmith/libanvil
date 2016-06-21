package org.libsmith.anvil.reflection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 20.03.16 6:17
 */
public class InheritanceDistance {
    protected final Class<?> referenceType;
    protected Class<?> foundType;
    protected int distance = -1;

    protected InheritanceDistance(Class<?> referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getDistance() {
        return distance == -1 ? null : distance;
    }

    public Class<?> getType() {
        return foundType;
    }

    protected void applyMinDistance(Integer value, Class<?> type) {
        if (value != null && (distance == -1 || distance >= value)) {
            foundType = type;
            distance = value;
        }
    }

    public static ForwardMinimumDistance fromType(Class<?> from) {
        return new ForwardMinimumDistance(from);
    }

    public static BackwardMinimumDistance toType(Class<?> to) {
        return new BackwardMinimumDistance(to);
    }

    public static class ForwardMinimumDistance extends InheritanceDistance {
        protected ForwardMinimumDistance(Class<?> referenceType) {
            super(referenceType);
        }

        public ForwardMinimumDistance to(Class<?> type) {
            applyMinDistance(calculateDistance(type, referenceType), type);
            return this;
        }

        public ForwardMinimumDistance to(Iterable<Class<?>> types) {
            for (Class<?> type : types) {
                to(type);
            }
            return this;
        }
    }

    public static class BackwardMinimumDistance extends InheritanceDistance {
        protected BackwardMinimumDistance(Class<?> referenceType) {
            super(referenceType);
        }

        public BackwardMinimumDistance from(Class<?> type) {
            applyMinDistance(calculateDistance(referenceType, type), type);
            return this;
        }

        public BackwardMinimumDistance from(Iterable<Class<?>> types) {
            for (Class<?> type : types) {
                from(type);
            }
            return this;
        }
    }

    public static @Nullable Integer calculateDistance(Class<?> subclass, Class<?> parent) {
        int distance = 0;
        if (subclass.equals(parent)) {
            return 0;
        }
        if (parent.isInterface()) {
            distance += 1;
            List<Class<?>> row = new ArrayList<>();
            row.addAll(Arrays.asList(subclass.getInterfaces()));
            while (!row.isEmpty()) {
                List<Class<?>> newRow = new ArrayList<>();
                for (Class<?> iface : row) {
                    if (parent.equals(iface)) {
                        return distance;
                    }
                    newRow.addAll(Arrays.asList(iface.getInterfaces()));
                }
                distance += 1;
                row = newRow;
            }
        }
        else {
            Class<?> current = subclass;
            while (current != null) {
                if (current.equals(parent)) {
                    return distance;
                }
                current = current.getSuperclass();
                distance += 1;
            }
        }
        if (parent == Object.class) {
            return distance;
        }
        return null;
    }
}
