package fr.insee.vtl.model;

import java.util.Map;

/**
 * The <code>LongExpression</code> class is an abstract representation of an expression of type <code>Long</code>.
 */
public abstract class LongExpression extends NumberExpression {

    public static LongExpression of(Long value) {
        return new LongExpression() {
            @Override
            public Long resolve(Map<String, Object> context) {
                return value;
            }
        };
    }

    @Override
    public abstract Long resolve(Map<String, Object> context);

    @Override
    public Class<Long> getType() {
        return Long.class;
    }

    /**
     * Returns the result of applying a function of type <code>Long</code> to a given dataset context.
     *
     * @param func A function applicable to a dataset context and yielding a <code>Long</code> result.
     * @return The result of applying the given function to the dataset context.
     */
    public static LongExpression of(VtlFunction<Map<String, Object>, Long> func) {
        return new LongExpression() {
            @Override
            public Long resolve(Map<String, Object> context) {
                return func.apply(context);
            }
        };
    }

    /**
     * Returns the result of the cast operator on an expression
     *
     * @param expr        A <code>ResolvableExpression</code> to cast.
     * @param outputClass The type to cast expression.
     * @return The casted <code>ResolvableExpression</code>.
     */
    public static ResolvableExpression castTo(ResolvableExpression expr, Class outputClass) {
        if (outputClass.equals(String.class))
            return StringExpression.of(context -> {
                Long exprValue = (Long) expr.resolve(context);
                if (exprValue == null) return null;
                return exprValue.toString();
            });
        if (outputClass.equals(Long.class))
            return LongExpression.of(context -> {
                Long exprValue = (Long) expr.resolve(context);
                if (exprValue == null) return null;
                return exprValue;
            });
        if (outputClass.equals(Double.class))
            return DoubleExpression.of(context -> {
                Long exprValue = (Long) expr.resolve(context);
                if (exprValue == null) return null;
                return Double.valueOf(exprValue);
            });
        if (outputClass.equals(Boolean.class))
            return BooleanExpression.of(context -> {
                Long exprValue = (Long) expr.resolve(context);
                if (exprValue == null) return null;
                return exprValue.equals(0L) ? false : true;
            });
        throw new ClassCastException("Cast Long to " + outputClass + " is not supported");
    }
}