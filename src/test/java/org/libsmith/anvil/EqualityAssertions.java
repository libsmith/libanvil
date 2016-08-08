package org.libsmith.anvil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitriy Balakin <balakin@0x0000.ru>
 * @created 18.07.2016 19:03
 */
public class EqualityAssertions {

    private final List<Object> equalitySubjects;
    private final boolean hashCodeAssertions;
    private final boolean reverseEquality;
    private final int repeatCount;

    private EqualityAssertions(List<Object> equalitySubjects, boolean hashCodeAssertions,
                               boolean reverseEquality, int repeatCount) {
        this.equalitySubjects = equalitySubjects;
        this.hashCodeAssertions = hashCodeAssertions;
        this.reverseEquality = reverseEquality;
        this.repeatCount = repeatCount;
    }

    public static EqualityAssertions assertThat(Object object) {
        assertNotNull(object);
        return new EqualityAssertions(Collections.singletonList(object), true, true, 3);
    }

    public EqualityAssertions withoutHashCodeAssertions() {
        return new EqualityAssertions(equalitySubjects, false, reverseEquality, repeatCount);
    }

    public EqualityAssertions withRepeatCount(int repeatCount) {
        return new EqualityAssertions(equalitySubjects, hashCodeAssertions, reverseEquality, repeatCount);
    }

    public EqualityAssertions and(Object object) {
        assertNotNull(object);
        List<Object> equalitySubjects = new ArrayList<>(this.equalitySubjects);
        equalitySubjects.add(object);
        return new EqualityAssertions(equalitySubjects, hashCodeAssertions, reverseEquality, repeatCount);
    }

    public EqualityAssertions equalsTogether() {
        return equalsTogetherAssertion(true);
    }

    public EqualityAssertions notEqualsTogether() {
        return equalsTogetherAssertion(false);
    }

    public EqualityAssertions equalsTo(Object object) {
        return toEqualsOrNotToEqualsTo(object, true);
    }

    public EqualityAssertions notEqualsTo(Object object) {
        return toEqualsOrNotToEqualsTo(object, false);
    }

    private EqualityAssertions toEqualsOrNotToEqualsTo(Object object, boolean equalAssertion) {

        assertNotNull(object);
        if (equalitySubjects.size() < 1) {
            throw new AssertionError("Insufficient data for equality assertion");
        }

        assertThatSubjectSatisfyGeneralContract(object);

        for (int take = 0; take < repeatCount; take++) {
            for (int i = 0; i < equalitySubjects.size(); i++) {
                String message = "Object at index " + i + " must be " + (equalAssertion ? "" : "not ") +
                                 "equal to object '" + object + "'" +
                                 (take > 0 ? ", take " + (take + 1) + " of " + repeatCount + ")" : "");
                Object other = equalitySubjects.get(i);
                assertThatSubjectSatisfyGeneralContract(other);
                if (equalAssertion) {
                    assertEquals(message, other, object);
                    if (reverseEquality) {
                        assertEquals(message, object, other);
                    }
                }
                else {
                    assertNotEquals(message, other, object);
                    if (reverseEquality) {
                        assertNotEquals(message, object, other);
                    }
                }
            }
            if (hashCodeAssertions && equalAssertion) {
                int referenceHashCode = object.hashCode();
                for (int i = 0; i < equalitySubjects.size(); i++) {
                    Object other = equalitySubjects.get(i);
                    String message = "Hash code of object at index " + i +
                                     " must be equal to hash code of object '" + object + "'" +
                                     (take > 0 ? ", take " + (take + 1) + " of " + repeatCount + ")" : "");
                    assertEquals(message, referenceHashCode, other.hashCode());
                }
            }
        }

        return this;
    }

    private EqualityAssertions equalsTogetherAssertion(boolean equalAssertion) {

        if (equalitySubjects.size() < 2) {
            throw new AssertionError("Insufficient data for equality assertion");
        }

        for (int take = 0; take < repeatCount; take++) {
            for (int i = 0; i < equalitySubjects.size(); i++) {
                assertThatSubjectSatisfyGeneralContract(equalitySubjects.get(i));
                for (int j = 0; j < equalitySubjects.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Object a = equalitySubjects.get(i);
                    Object b = equalitySubjects.get(j);
                    String message = "Object at index " + i + " must be " + (equalAssertion ? "" : "not ") +
                                     "equal to object at index " + j +
                                     (take > 0 ? ", take " + (take + 1) + " of " + repeatCount + ")" : "");
                    if (equalAssertion) {
                        assertEquals(message, a, b);
                    }
                    else {
                        assertNotEquals(message, a, b);
                    }
                }
            }
            if (hashCodeAssertions && equalAssertion) {
                Object referenceObject = equalitySubjects.get(0);
                int referenceHashCode = referenceObject.hashCode();
                for (int i = 0; i < equalitySubjects.size(); i++) {
                    Object other = equalitySubjects.get(i);
                    String message = "Hashcode of object at index " + i + "(" + other + ") must be equal to " +
                                     "hash code of reference object '" + referenceHashCode + "'" +
                                     (take > 0 ? ", take " + (take + 1) + " of " + repeatCount + ")" : "");
                    assertEquals(message, referenceHashCode, other.hashCode());
                }
            }
        }
        return this;
    }

    @SuppressWarnings({ "EqualsWithItself", "ObjectEqualsNull" })
    private void assertThatSubjectSatisfyGeneralContract(Object subject) {
        assertEquals("Object must be equal to self", subject, subject);
        assertNotEquals("Object must not equal to null", null, subject);
        assertNotEquals("Object must not equal to unknown object", new Object(), subject);
        if (hashCodeAssertions) {
            assertEquals("Object must have same hashCode between calls", subject.hashCode(), subject.hashCode());
        }
    }
    
    private static void assertNotNull(Object object) {
        if (object == null) {
            throw new AssertionError("Object must be not null");
        }
    }
    
    private static void assertEquals(String message, Object expected, Object actual) {
        if (!actual.equals(expected)) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotEquals(String message, Object expected, Object actual) {
        if (actual.equals(expected)) {
            throw new AssertionError(message);
        }
    }
}

