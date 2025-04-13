package io.cdap.wrangler.directive;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AggregateStatsTest {

    private static final double MB_DIVISOR = 1024.0 * 1024.0;
    private static final double KB_DIVISOR = 1024.0;

    @Test
    public void testSimpleTotalAggregation() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("data_transfer_size", "1MB").add("response_time", "100ms"),
                new Row("data_transfer_size", "512KB").add("response_time", "0.5s"),
                new Row("data_transfer_size", "2097152").add("response_time", "1500ms")
        );

        String[] recipe = {
          "aggregate-stats data_transfer_size response_time total_size_mb total_time_sec size_unit:MB time_unit:s time_mode:total"
        };

        double expectedTotalSizeInMB = 1.0 + (512.0 / 1024.0) + (2097152.0 / MB_DIVISOR); // 1 + 0.5 + 2 = 3.5
        double expectedTotalTimeInSeconds = 0.1 + 0.5 + 1.5; // 2.1

        List<Row> results = TestingRig.execute(recipe, rows);

        Assert.assertEquals(1, results.size());
        Row resultRow = results.get(0);
        Assert.assertEquals(expectedTotalSizeInMB, (Double) resultRow.getValue("total_size_mb"), 0.001);
        Assert.assertEquals(expectedTotalTimeInSeconds, (Double) resultRow.getValue("total_time_sec"), 0.001);
        Assert.assertEquals(2, resultRow.width());
    }

    @Test
    public void testAverageTimeAggregation() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("size", "10KB").add("time", "1s"),
                new Row("size", "10KB").add("time", "2000ms"),
                new Row("size", "10KB").add("time", "0.5s"),
                new Row("size", "10KB").add("time", "1.5s")
        );

         String[] recipe = {
           "aggregate-stats size time total_kb avg_time_sec size_unit:KB time_unit:s time_mode:average"
         };

        double expectedTotalSizeInKB = 4 * 10.0;
        double expectedAverageTimeInSeconds = (1.0 + 2.0 + 0.5 + 1.5) / 4.0; // 5 / 4 = 1.25

        List<Row> results = TestingRig.execute(recipe, rows);

        Assert.assertEquals(1, results.size());
        Row resultRow = results.get(0);
        Assert.assertEquals(expectedTotalSizeInKB, (Double) resultRow.getValue("total_kb"), 0.001);
        Assert.assertEquals(expectedAverageTimeInSeconds, (Double) resultRow.getValue("avg_time_sec"), 0.001);
    }

    @Test
    public void testDifferentUnitsAndNulls() throws Exception {
         List<Row> rows = Arrays.asList(
                new Row("bytes", "1GB").add("latency", "1m"),
                new Row("bytes", "1024MB").add("latency", "60s"),
                new Row("bytes", null).add("latency", "120000ms"),
                new Row("bytes", "0.5GB").add("latency", null)
         );

         String[] recipe = {
           "aggregate-stats bytes latency total_size_gb total_time_min size_unit:GB time_unit:minutes time_mode:total"
         };

         double expectedTotalSizeGB = 1.0 + 1.0 + 0.5; // 2.5
         double expectedTotalTimeMinutes = (60.0 + 60.0 + 120.0) / 60.0; // 240 / 60 = 4.0

         List<Row> results = TestingRig.execute(recipe, rows);

         Assert.assertEquals(1, results.size());
         Row resultRow = results.get(0);
         Assert.assertEquals(expectedTotalSizeGB, (Double) resultRow.getValue("total_size_gb"), 0.001);
         Assert.assertEquals(expectedTotalTimeMinutes, (Double) resultRow.getValue("total_time_min"), 0.001);
    }

     @Test
    public void testNoRowsInput() throws Exception {
         List<Row> rows = Collections.emptyList();

         String[] recipe = {
           "aggregate-stats size time total_b avg_ms size_unit:BYTES time_unit:ms time_mode:average"
         };

         List<Row> results = TestingRig.execute(recipe, rows);
         Assert.assertEquals(0, results.size());
    }
}