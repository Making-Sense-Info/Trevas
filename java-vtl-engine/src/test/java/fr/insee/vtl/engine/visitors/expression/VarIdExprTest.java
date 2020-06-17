package fr.insee.vtl.engine.visitors.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

public class VarIdExprTest {

    private ScriptEngine engine;

    @BeforeEach
    public void setUp() {
        engine = new ScriptEngineManager().getEngineByName("vtl");
    }

    @Test
    public void testVariableExpression() throws ScriptException {
        ScriptContext context = engine.getContext();
        context.setAttribute("foo", 123, ScriptContext.ENGINE_SCOPE);
        engine.eval("bar := foo;");
        assertThat(context.getAttribute("bar"))
                .isSameAs(context.getAttribute("foo"));
    }
}