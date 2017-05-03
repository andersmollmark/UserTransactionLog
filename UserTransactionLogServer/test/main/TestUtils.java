package main;

import java.lang.reflect.*;

/**
 * Class to simplify testing.
 * 
 */
public final class TestUtils {

	private TestUtils() {
		// Empty by design
	}


     public static <T> T getInstanceFromPrivateConstructor(final Class<T> remoteFacadeFactoryClazz) {
        try {
            final Constructor<T> constructor = remoteFacadeFactoryClazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * To set a private field in given class, you can't change the value of the final field!
     *
     * @param nameOfField name of the field to set
     * @param valueToSet  the new value
     * @param target      instance of the object
     */
    public static void setField(final String nameOfField, final Object valueToSet, final Object target) {
        try {
            checkParam(nameOfField, valueToSet, target);
            settingTheField(nameOfField, valueToSet, target);
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <X> X getFieldValue(final String nameOfField, final Class<?> target, Object targetInstance) throws NoSuchFieldException, IllegalAccessException {
        Field field = getField(nameOfField, target);
        field.setAccessible(true);
        return (X)field.get(targetInstance);
    }

    private static void settingTheField(final String nameOfField, final Object valueToSet, final Object target)
            throws NoSuchFieldException, IllegalAccessException {

        final Field field = getField(nameOfField, target.getClass());
        checkIfFieldIsFinal(field);
        field.setAccessible(true);
        if(ParametersFactory.isNull(valueToSet)){
            field.set(target, null);
        }
        else {
            field.set(target, valueToSet);
        }


    }

    private static Field getField(final String nameOfField, final Class<?> target) throws NoSuchFieldException {
        try {
            return target.getDeclaredField(nameOfField);
        } catch (final NoSuchFieldException e) {
            final Class<?> superclass = target.getSuperclass();
            if (superclass != null) {
                return getField(nameOfField, superclass);
            }
            throw e;
        }
    }

    /**
     * To execute a private method for testing
     *
     * @param <X>          the returnValue of the method
     * @param nameOfMethod name of the method to execute
     * @param target       instace of the object where the method is
     * @param parameters   to the method {@link ParametersFactory}
     */
    @SuppressWarnings("unchecked")
    public static <X> X runMethod(final String nameOfMethod, final Object target, final ParametersFactory.Parameters parameters) {
        try {
            final Method method = getMethod(nameOfMethod, target.getClass(), parameters);
            method.setAccessible(true);
            return (X) method.invoke(target, parameters == null ? null : parameters.getValues());
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <X> X runMethodWithoutArguments(final String nameOfMethod, final Object target) {
        return runMethod(nameOfMethod, target, null);
    }

    public static <X> X runMethodThrowOriginalExceptionIfAny(final String nameOfMethod, final Object target,
                                                             final ParametersFactory.Parameters parameters) throws Throwable {

        try {
            return runMethod(nameOfMethod, target, parameters);
        } catch (final RuntimeException e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }

    }

    private static Method getMethod(final String nameOfMethod, final Class<?> target, final ParametersFactory.Parameters parameters)
            throws NoSuchMethodException {
        try {
            return target.getDeclaredMethod(nameOfMethod, parameters == null ? null : parameters.getTypes());
        } catch (final NoSuchMethodException e) {
            final Class<?> superclass = target.getSuperclass();
            if (superclass != null) {
                return getMethod(nameOfMethod, superclass, parameters);
            }
            throw e;
        }
    }

    private static void checkIfFieldIsFinal(final Field field) {
        if (Modifier.isFinal(field.getModifiers())) {
            throw new IllegalStateException("Can't change a final field: " + field.getName());
        }
    }

    private static void checkParam(final String nameOfField, final Object valueToSet, final Object target) {
        checkNotNull(nameOfField);
        checkNotNull(valueToSet);
        checkNotNull(target);
        checkNotEmptyString(nameOfField);

    }

    private static void checkNotEmptyString(final String nameOfField) {
        if (nameOfField.isEmpty()) {
            throw new IllegalArgumentException("Name of field can not be empty");
        }

    }

    private static void checkNotNull(final Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Null values is not allowed");
        }
    }

}
