package io.cdap.wrangler.api.parser;


import io.cdap.wrangler.api.UsageDefinition;
import io.cdap.wrangler.api.annotations.Directive;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import javax.annotation.Nullable;

@Directive(
    name = "aggregate-stats",
    description = "Aggregates byte size and time duration columns."
)
public class AggregateStats implements io.cdap.wrangler.api.Directive {

    private ColumnName sizeColumn;
    private ColumnName timeColumn;
    private ColumnName targetSizeColumn;
    private ColumnName targetTimeColumn;
    @Nullable
    private String outputSizeUnit;
    @Nullable
    private String outputTimeUnit;
    @Nullable
    private String aggregationType;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
            .use(":<size-column> :<time-column> :<target-size-column> :<target-time-column> [:output-size-unit] [:output-time-unit] [:aggregation-type]")
            .withArgs(
                ColumnName.of("size-column", TokenType.COLUMN_NAME),
                ColumnName.of("time-column", TokenType.COLUMN_NAME),
                ColumnName.of("target-size-column", TokenType.COLUMN_NAME),
                ColumnName.of("target-time-column", TokenType.COLUMN_NAME),
                UsageDefinition.optional("output-size-unit", TokenType.STRING),
                UsageDefinition.optional("output-time-unit", TokenType.STRING),
                UsageDefinition.optional("aggregation-type", TokenType.STRING) 
            )
            .build();
    }

    
}