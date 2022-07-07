package fr.insee.vtl.engine.visitors;

import fr.insee.vtl.engine.exceptions.InvalidArgumentException;
import fr.insee.vtl.engine.exceptions.VtlRuntimeException;
import fr.insee.vtl.engine.exceptions.VtlScriptException;
import fr.insee.vtl.engine.visitors.expression.ConstantVisitor;
import fr.insee.vtl.model.Analytics;
import fr.insee.vtl.model.DatasetExpression;
import fr.insee.vtl.model.LongExpression;
import fr.insee.vtl.model.ProcessingEngine;
import fr.insee.vtl.parser.VtlBaseVisitor;
import fr.insee.vtl.parser.VtlParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsVisitor extends VtlBaseVisitor<DatasetExpression> {

    private final ProcessingEngine processingEngine;
    private final DatasetExpression dataset;

    public AnalyticsVisitor(ProcessingEngine processingEngine, DatasetExpression dataset) {
        this.processingEngine = processingEngine;
        this.dataset = dataset;
    }

    private Analytics.Function toFunctionEnum(Token op, ParseTree ctx) {
        switch (op.getType()) {
            case VtlParser.SUM:
                return Analytics.Function.SUM;
            case VtlParser.AVG:
                return Analytics.Function.AVG;
            case VtlParser.COUNT:
                return Analytics.Function.COUNT;
            case VtlParser.MEDIAN:
                return Analytics.Function.MEDIAN;
            case VtlParser.MIN:
                return Analytics.Function.MIN;
            case VtlParser.MAX:
                return Analytics.Function.MAX;
            case VtlParser.STDDEV_POP:
                return Analytics.Function.STDDEV_POP;
            case VtlParser.STDDEV_SAMP:
                return Analytics.Function.STDDEV_SAMP;
            case VtlParser.VAR_POP:
                return Analytics.Function.VAR_POP;
            case VtlParser.VAR_SAMP:
                return Analytics.Function.VAR_SAMP;
            case VtlParser.FIRST_VALUE:
                return Analytics.Function.FIRST_VALUE;
            case VtlParser.LAST_VALUE:
                return Analytics.Function.LAST_VALUE;
            default:
                throw new VtlRuntimeException(
                        new InvalidArgumentException("not an analytic function", ctx)
                );
        }
    }

    private Map<String, Analytics.Order> toOrderBy(VtlParser.OrderByClauseContext orderByCtx) {
        if (orderByCtx == null) {
            return null;
        }
        Map<String, Analytics.Order> orderBy = new LinkedHashMap<>();
        for (VtlParser.OrderByItemContext item : orderByCtx.orderByItem()) {
            String columnName = ClauseVisitor.getName(item.componentID());
            if (item.DESC() != null) {
                orderBy.put(columnName, Analytics.Order.ASC);
            } else {
                orderBy.put(columnName, Analytics.Order.DESC);
            }
        }
        return orderBy;
    }

    private List<String> toPartitionBy(VtlParser.PartitionByClauseContext partition) {
        if (partition == null) {
            return null;
        }
        return partition.componentID().stream()
                .map(ClauseVisitor::getName)
                .collect(Collectors.toList());
    }

    // between -2 following and -2 preceding
    private Long toRangeLong(VtlParser.LimitClauseItemContext ctx) {
        if (ctx.CURRENT() != null) {
            return 0L;
        } else if (ctx.UNBOUNDED() != null && ctx.PRECEDING() != null) {
            return Long.MIN_VALUE;
        } else if (ctx.UNBOUNDED() != null && ctx.FOLLOWING() != null) {
            return Long.MAX_VALUE;
        } else if (ctx.INTEGER_CONSTANT() != null) {
            return Long.parseLong(ctx.getText());
        }
        throw new VtlRuntimeException(new VtlScriptException("invalid range", ctx));
    }

    private Analytics.WindowSpec toWindowSpec(VtlParser.WindowingClauseContext windowing) {
        if (windowing == null) {
            return null;
        }
        Long from = toRangeLong(windowing.from_);
        Long to = toRangeLong(windowing.to_);
        if (windowing.RANGE() != null) {
            return new Analytics.RangeWindow(from, to);
        } else {
            return new Analytics.DataPointWindow(from, to);
        }
    }

    @Override
    public DatasetExpression visitAnSimpleFunction(VtlParser.AnSimpleFunctionContext ctx) {
        return processingEngine.executeAnalytic(
                dataset,
                toFunctionEnum(ctx.op, ctx),
                toPartitionBy(ctx.partition),
                toOrderBy(ctx.orderBy),
                toWindowSpec(ctx.windowing)
        );
    }

    @Override
    public DatasetExpression visitLagOrLeadAn(VtlParser.LagOrLeadAnContext ctx) {
        throw new VtlRuntimeException(new VtlScriptException("not implemented", ctx));
    }

    @Override
    public DatasetExpression visitRatioToReportAn(VtlParser.RatioToReportAnContext ctx) {
        throw new VtlRuntimeException(new VtlScriptException("not implemented", ctx));
    }
}
