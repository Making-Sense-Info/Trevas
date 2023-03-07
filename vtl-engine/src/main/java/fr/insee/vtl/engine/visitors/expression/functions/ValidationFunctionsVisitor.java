package fr.insee.vtl.engine.visitors.expression.functions;

import fr.insee.vtl.engine.VtlScriptEngine;
import fr.insee.vtl.engine.exceptions.InvalidArgumentException;
import fr.insee.vtl.engine.exceptions.UndefinedVariableException;
import fr.insee.vtl.engine.exceptions.VtlRuntimeException;
import fr.insee.vtl.engine.visitors.expression.ExpressionVisitor;
import fr.insee.vtl.model.DataPointRuleset;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.DatasetExpression;
import fr.insee.vtl.model.ProcessingEngine;
import fr.insee.vtl.model.ResolvableExpression;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.parser.VtlBaseVisitor;
import fr.insee.vtl.parser.VtlParser;

import java.util.Objects;

import static fr.insee.vtl.engine.utils.TypeChecking.assertTypeExpression;

/**
 * <code>ValidationFunctionsVisitor</code> is the base visitor for expressions involving validation functions.
 */
public class ValidationFunctionsVisitor extends VtlBaseVisitor<ResolvableExpression> {

    private final ExpressionVisitor expressionVisitor;
    private final ProcessingEngine processingEngine;
    private final VtlScriptEngine engine;

    /**
     * Constructor taking an expression visitor and a processing engine.
     *
     * @param expressionVisitor A visitor for the expression corresponding to the validation function.
     * @param processingEngine  The processing engine.
     */
    public ValidationFunctionsVisitor(ExpressionVisitor expressionVisitor,
                                      ProcessingEngine processingEngine,
                                      VtlScriptEngine engine) {
        this.expressionVisitor = Objects.requireNonNull(expressionVisitor);
        this.processingEngine = Objects.requireNonNull(processingEngine);
        this.engine = Objects.requireNonNull(engine);
    }

    /**
     * Visits a DataPointRuleset expression to validate.
     *
     * @param ctx The scripting context for the expression...
     * @return A <code>ResolvableExpression</code> resolving to...
     */
    @Override
    public ResolvableExpression visitValidateDPruleset(VtlParser.ValidateDPrulesetContext ctx) {
        // get DataPointRuleset
        String dprName = ctx.dpName.getText();
        Object dprObject = engine.getContext().getAttribute((dprName));
        String output = getValidationOutput(ctx.validationOutput());
        if (!(dprObject instanceof DataPointRuleset))
            throw new VtlRuntimeException(new UndefinedVariableException(ctx.IDENTIFIER()));
        DataPointRuleset dpr = (DataPointRuleset) dprObject;

        DatasetExpression ds = (DatasetExpression) assertTypeExpression(expressionVisitor.visit(ctx.op),
                Dataset.class, ctx.op);

        // check if dpr variables are in ds structure
        Structured.DataStructure dataStructure = ds.getDataStructure();
        dpr.getVariables().forEach(v -> {
            if (!dataStructure.containsKey(v)) {
                throw new VtlRuntimeException(
                        new InvalidArgumentException("Variable " + v +
                                " not contained in " + ctx.op.getText(), ctx.op)
                );
            }
        });

        // check if dpr alias are not in ds
        dpr.getAlias().values().forEach(
                v -> {
                    if (dataStructure.containsKey(v)) {
                        throw new VtlRuntimeException(
                                new InvalidArgumentException("Alias " + v +
                                        " from " + dprName + " ruleset already defined in " +
                                        ctx.op.getText(), ctx.op)
                        );
                    }
                }
        );

        return processingEngine.executeValidateDPruleset(dpr, ds, output);
    }

    /**
     * Visits a datasets to validate.
     *
     * @param ctx The scripting context for the expression...
     * @return A <code>ResolvableExpression</code> resolving to...
     */
    @Override
    public ResolvableExpression visitValidationSimple(VtlParser.ValidationSimpleContext ctx) {
        DatasetExpression dsExpression = (DatasetExpression) assertTypeExpression(expressionVisitor.visit(ctx.expr()),
                Dataset.class, ctx.expr());
        ResolvableExpression erCodeExpression = expressionVisitor.visit(ctx.erCode());
        ResolvableExpression erLevelExpression = expressionVisitor.visit(ctx.erLevel());
        DatasetExpression imbalanceExpression = (DatasetExpression) assertTypeExpression(expressionVisitor.visit(ctx.imbalanceExpr()),
                Dataset.class, ctx.imbalanceExpr());
        String output = ctx.output != null ? ctx.output.getText() : null;
        return processingEngine.executeValidationSimple(dsExpression, erCodeExpression, erLevelExpression, imbalanceExpression, output);
    }

    private String getValidationOutput(VtlParser.ValidationOutputContext voc) {
        if (null == voc) return null;
        return voc.getText();
    }
}
