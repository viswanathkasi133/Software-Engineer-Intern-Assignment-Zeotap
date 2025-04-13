package io.cdap.wrangler.parser; 

import io.cdap.wrangler.TestingRig; 
import io.cdap.wrangler.api.RecipeParser;
import io.cdap.wrangler.api.RecipeParserException;
import org.junit.Test;

public class RecipeCompilerTest { 

    @Test
    public void testAggregateStatsDirectiveParsing() throws Exception {
        String[] recipe = {
            "aggregate-stats :size :time :total_mb :avg_sec size_unit:MB time_unit:s time_mode:average",
            "aggregate-stats size_col time_col target_size target_time"
        };
        RecipeParser recipeParser = new TestingRig.Compiler(); // Or appropriate instantiation
        recipeParser.parse(String.join("\n", recipe)); // Should parse without exception
    }

    @Test(expected = RecipeParserException.class)
    public void testAggregateStatsDirectiveInvalidArgs() throws Exception {
         String[] recipe = {
            "aggregate-stats :size :time"
         };
         RecipeParser recipeParser = new TestingRig.Compiler();
         recipeParser.parse(String.join("\n", recipe));
    }

     @Test(expected = RecipeParserException.class) // Expect parse exception due to invalid unit value
    public void testAggregateStatsDirectiveInvalidOptionValue() throws Exception {
         String[] recipe = {
             "aggregate-stats s t ts tt time_unit:years"
         };
         RecipeParser recipeParser = new TestingRig.Compiler();
         recipeParser.parse(String.join("\n", recipe));
     }

     
}