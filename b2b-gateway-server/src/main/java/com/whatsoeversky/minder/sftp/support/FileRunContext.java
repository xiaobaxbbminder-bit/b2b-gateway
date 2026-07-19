package com.whatsoeversky.minder.sftp.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Slf4j
public class FileRunContext {
    private Map<String, Object> contextVariables = new LinkedHashMap<>();
    private Path file;
    private String logId;
    private volatile ExpressionParser expressionParser;

    public void putContextVariables(String pluginName, ContextVariable result) {
        contextVariables.put(pluginName, result);
    }

    public ExpressionParser getExpressionParser() {
        if (expressionParser == null) {
            synchronized (this) {
                if (expressionParser == null) {
                    ExpressionParser ep = new ExpressionParser();
                    ep.standardEvaluationContext.setVariables(contextVariables);
                    ep.standardEvaluationContext.setTypeLocator(typeName -> {
                        throw new org.springframework.expression.EvaluationException("已彻底禁用 T() 静态语法");
                    });
                    try {
                        Method uuidMethod = ExpressionFunction.class.getDeclaredMethod("UUID");
                        ep.standardEvaluationContext.registerFunction("UUID", uuidMethod);
                    } catch (NoSuchMethodException e) {
                        log.error("no such method exception", e);
                    }
                    expressionParser = ep;
                }
            }
        }
        return expressionParser;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextVariable {
        private Object args;
        private Object res;
    }

    public static class ExpressionParser {
        private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        private final StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();

        public String parseExpression(String expressionStr) {
            // TemplateParseContext对象能够让模板的格式为“#{}”，而不是直接解析
            Expression expression = spelExpressionParser.parseExpression(expressionStr, new TemplateParserContext());
            Object value = expression.getValue(standardEvaluationContext);
            return value != null ? value.toString() : null;
        }
    }

    public static class ExpressionFunction {
        public static String UUID() {
            return UUID.randomUUID().toString();
        }
    }

    public static void main(String[] args) {
        FileRunContext fileRunContext = new FileRunContext();
        fileRunContext.setContextVariables(Map.of("aaa", "111", "bbb", true));
        ExpressionParser expressionParser = fileRunContext.getExpressionParser();
        System.out.println(expressionParser.parseExpression("#{#aaa}"));
        System.out.println(expressionParser.parseExpression("#{1+1}"));
        System.out.println(expressionParser.parseExpression("#{'hello'.toUpperCase()}"));
        System.out.println(expressionParser.parseExpression("#{'hello'.substring(2)}"));
        System.out.println(expressionParser.parseExpression("#{#UUID()}"));
        System.out.println(expressionParser.parseExpression("#{'生成UUID：'+#UUID()}"));
        String expression = """
                {
                  "name":"#{#UUID()}"
                }
                """;
        System.out.println(expressionParser.parseExpression(expression));
        System.out.println(expressionParser.parseExpression("#{T(java.util.UUID).randomUUID().toString()}"));
        System.out.println(expressionParser.parseExpression("#{T(java.lang.System).getProperty('os.name')}"));
    }
}
