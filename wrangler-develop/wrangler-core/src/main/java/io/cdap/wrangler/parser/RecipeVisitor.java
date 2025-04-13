package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.SyntaxError;
import io.cdap.wrangler.api.parser.TokenGroup; 
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.RecipeParserException; 
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.parser.psi.DirectivesBaseVisitor;
import io.cdap.wrangler.parser.psi.DirectivesParser; 
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class RecipeVisitor extends DirectivesBaseVisitor<Object> { 

    private final TokenGroup tokenGroup;
    private final String recipe; 

    public RecipeVisitor(String recipe, List<SyntaxError> errors) { 
        this.recipe = recipe;
        this.tokenGroup = new TokenGroup(errors); 
    }

    public TokenGroup getTokenGroup() {
        return tokenGroup;
    }




    @Override
    public Object visitValue(DirectivesParser.ValueContext ctx) {
        if (ctx.BYTE_SIZE() != null) {
            try {
                tokenGroup.add(new ByteSize(ctx.BYTE_SIZE().getText()));
            } catch (IllegalArgumentException e) {
                 tokenGroup.getErrors().add(
                     new SyntaxError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                                     ctx.getStop().getCharPositionInLine(), ctx.getText(), e.getMessage())
                 );
                 throw new RecipeParserException("Error parsing byte size: " + e.getMessage(), ctx);
            }
        } else if (ctx.TIME_DURATION() != null) {
             try {
                tokenGroup.add(new TimeDuration(ctx.TIME_DURATION().getText()));
            } catch (IllegalArgumentException e) {
                 tokenGroup.getErrors().add(
                     new SyntaxError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                                     ctx.getStop().getCharPositionInLine(), ctx.getText(), e.getMessage())
                 );
                 throw new RecipeParserException("Error parsing time duration: " + e.getMessage(), ctx);
            }
        } else {
            return super.visitValue(ctx); // Handle other value types
        }
        return null;
    }



    @Override
    public Object visitByteSizeArg(DirectivesParser.ByteSizeArgContext ctx) {
         String byteSizeText = ctx.BYTE_SIZE().getText();
         try {
             tokenGroup.add(new ByteSize(byteSizeText));
         } catch (IllegalArgumentException e) {
             tokenGroup.getErrors().add(
                 new SyntaxError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                                 ctx.getStop().getCharPositionInLine(), ctx.getText(), e.getMessage())
             );
             throw new RecipeParserException("Error parsing byte size argument: " + e.getMessage(), ctx);
         }
         return super.visitByteSizeArg(ctx);
    }

    @Override
    public Object visitTimeDurationArg(DirectivesParser.TimeDurationArgContext ctx) {
         String timeDurationText = ctx.TIME_DURATION().getText();
          try {
             tokenGroup.add(new TimeDuration(timeDurationText));
         } catch (IllegalArgumentException e) {
             tokenGroup.getErrors().add(
                 new SyntaxError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                                 ctx.getStop().getCharPositionInLine(), ctx.getText(), e.getMessage())
             );
             throw new RecipeParserException("Error parsing time duration argument: " + e.getMessage(), ctx);
         }
         return super.visitTimeDurationArg(ctx);
    }



    @Override
    public Object visitAggregateStatsDirective(DirectivesParser.AggregateStatsDirectiveContext ctx) {
       // Assuming DirectiveName token type exists and needs the directive name
       tokenGroup.add(new io.cdap.wrangler.api.parser.DirectiveName(ctx.getStart().getText()));

       // Visit children to add COLUMN and PROPERTY tokens to the tokenGroup
       for (DirectivesParser.ColumnContext colCtx : ctx.column()) {
           visit(colCtx); // Use generic visit or specific visitColumn if overridden
       }
       if (ctx.property() != null) {
          for (DirectivesParser.PropertyContext propCtx : ctx.property()) {
            visit(propCtx); // Use generic visit or specific visitProperty
          }
       }
       return null;
    }

   

    @Override
    public Object visitColumn(DirectivesParser.ColumnContext ctx) {
         // Logic to get text from COLUMN, QUOTED_STRING, or TEXT inside ctx
         String colName = ctx.getText(); // Simplistic; may need refinement based on grammar
         // Assuming COLUMN token type exists
         tokenGroup.add(new io.cdap.wrangler.api.parser.ColumnName(colName));
         return null;
    }

    @Override
    public Object visitProperty(DirectivesParser.PropertyContext ctx) {
        
        tokenGroup.add(new io.cdap.wrangler.api.parser.Property(ctx.getText()));
        return null;
    }

}