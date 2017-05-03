package main;

import java.util.ArrayList;

public final class ParametersFactory {

    private ParametersFactory() {
        // Empty by design
    }

    /**
     * Skapar {@link ParametersFactory.Parameters} som hanterar inparametrar i ett metodanrop.
     * <p/>
     * <pre>
     *  // void aMethod(String);
     *  msgui.webmuui.ParametersFactory.getArgs("string");
     *
     *  // void aMethod(String, String);
     *  msgui.webmuui.ParametersFactory.getArgs("string", "string2");
     *
     *  // void aMethod(String, Integer);
     *  msgui.webmuui.ParametersFactory.getArgs("string", 1);
     *
     *  // void aMethod(String[], Integer);
     *  msgui.webmuui.ParametersFactory.getArgs({"string", "string2"}, 1);
     * </pre>
     * <p/>
     * Om en inparameter är null ska {@link ParametersFactory.NULL} användas.
     * <p/>
     * <pre>
     * // void aMethod(Integer, String);
     * msgui.webmuui.ParametersFactory.getArgs(1, msgui.webmuui.ParametersFactory.NULL(String.class));
     * </pre>
     * <p/>
     * <p/>
     * Om inparametern är ENDAST en array måste den först "paketeras" i en Object[]. See
     * {@link ParametersFactory#ARRAY(Object[])}
     * <p/>
     * <pre>
     * // void aMethod(String[]);
     * msgui.webmuui.ParametersFactory.getArgs(msgui.webmuui.ParametersFactory.ARRAY(new String[] { &quot;string1&quot;, &quot;string2&quot; }));
     * </pre>
     * <p/>
     * Om en signatur tar ett inteface, men det anropade värdet är en implementation av interfacet så ska
     * ParameterBucket användas.
     *
     * @param inParemeters de parametrarna som ska ingå i metodanropet.
     * @return
     * @see ParametersFactory#getParameterBucket(Object, Class)
     */
    public static Parameters getArgs(final Object... inParemeters) {

        final ArrayList<Class<?>> clazz = new ArrayList<>();
        final ArrayList<Object> parameters = new ArrayList<>();

        for (final Object object : inParemeters) {
            if (object instanceof ParameterBucket) {
                final ParameterBucket bucket = (ParameterBucket) object;
                clazz.add(bucket.getClazz());
                parameters.add(getObject(bucket.getValue()));
            } else {
                checkForNull(object);
                clazz.add(getClass(object));
                parameters.add(getObject(object));
            }
        }

        return new ParametersImpl(clazz.toArray(new Class[clazz.size()]), parameters.toArray(new Object[parameters
                .size()]));
    }

    /**
     * En behållare för en enskild parameter. Om man behöver specificera en specifik signatur, som inte går att härleda
     * från objekten, exempelvis om signaturen har ingående interface så måste denna metod använda.
     * <p/>
     * <pre>
     *
     * getArgs(getParameterBucket(listObjArrayImpl, List.class))
     *
     * <pre>
     *
     * @param value Värdet.
     * @param clazz Metodparameterns Class. Ska matcha parameterns interface.
     * @return
     */
    @SuppressWarnings("synthetic-access")
    public static ParameterBucket getParameterBucket(final Object value, final Class<?> clazz) {
        return new ParametersFactory().new ParameterBucket(value, clazz);
    }

    /**
     * Skapar upp ett {@link ParametersFactory.NULL} för hanteringen av null
     * <p/>
     * <pre>
     * msgui.webmuui.ParametersFactory.NULL(String.class)
     * </pre>
     *
     * @param args vilken {@link Class} som ska representera null
     * @return
     */
    public static NULL NULL(final Class<?> args) {
        return new NULL(args);
    }

    public static boolean isNull(Object o){
        return o instanceof NULL;
    }

    /**
     * Kapslar in angiven array i en {@link Object}[].
     * <p/>
     * <pre>
     * msgui.webmuui.ParametersFactory.ARRAY(new String[] { string1, string2 })
     * </pre>
     *
     * @param inParameter som ska kaplsas in
     * @return
     */
    public static Object[] ARRAY(final Object[] inParameter) {
        return new Object[]{inParameter};
    }

    private static Object getObject(final Object object) {
        return object instanceof NULL ? null : object;
    }

    private static Class<?> getClass(final Object object) {
        return object instanceof NULL ? ((NULL) object).getType() : object.getClass();
    }

    private static void checkForNull(final Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Null values must be represended by NULL object");
        }

    }

    public interface Parameters {
        Class<?>[] getTypes();

        Object[] getValues();

    }

    private static class NULL {
        private transient final Class<?> type;

        public NULL(final Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
    }

    private static class ParametersImpl implements Parameters {
        private transient final Class<?>[] types;
        private transient final Object[] values;

        public ParametersImpl(final Class<?>[] types, final Object[] values) {
            this.types = types.clone();
            this.values = values.clone();
        }

        @Override
        public Class<?>[] getTypes() {
            return types.clone();
        }

        @Override
        public Object[] getValues() {
            return values.clone();
        }
    }

    public class ParameterBucket {
        private final Object value;
        private final Class<?> clazz;

        @SuppressWarnings("hiding")
        private ParameterBucket(final Object value, final Class<?> clazz) {
            checkForNull(value);
            checkForNull(clazz);
            this.value = value;
            this.clazz = clazz;
        }

        public Object getValue() {
            return value;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        private void checkForNull(final Object object) {
            if (object == null) {
                throw new IllegalArgumentException("Null values are not allowed");
            }
        }
    }
}
