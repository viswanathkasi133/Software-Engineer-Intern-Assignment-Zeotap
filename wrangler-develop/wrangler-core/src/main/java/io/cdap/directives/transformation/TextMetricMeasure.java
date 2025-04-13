/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.transformation;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Many;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;

import java.util.List;

/**
 * A directive for implementing the directive for measuring the metrics between two sequence of characters.
 */
@Plugin(type = Directive.TYPE)
@Name(TextMetricMeasure.NAME)
@Categories(categories = { "transform"})
@Description("Calculates the metric for comparing two string values.")
public class TextMetricMeasure implements Directive, Lineage {
  public static final String NAME = "text-metric";
  private String column1;
  private String column2;
  private String destination;
  private StringMetric metric;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("method", TokenType.TEXT);
    builder.define("column1", TokenType.COLUMN_NAME);
    builder.define("column2", TokenType.COLUMN_NAME);
    builder.define("destination", TokenType.COLUMN_NAME);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    String method = ((Text) args.value("method")).value();
    this.column1 = ((ColumnName) args.value("column1")).value();
    this.column2 = ((ColumnName) args.value("column2")).value();
    this.destination = ((ColumnName) args.value("destination")).value();
    // defaults to : cosineSimilarity
    switch(method.toLowerCase()) {
      case "euclidean":
        metric = StringMetrics.euclideanDistance();
        break;

      case "block-metric":
        metric = StringMetrics.blockDistance();
        break;

      case "identity":
        metric = StringMetrics.identity();
        break;

      case "block":
        metric = StringMetrics.blockDistance();
        break;

      case "dice":
        metric = StringMetrics.dice();
        break;

      case "longest-common-subsequence":
        metric = StringMetrics.longestCommonSubsequence();
        break;

      case "longest-common-substring":
        metric = StringMetrics.longestCommonSubstring();
        break;

      case "overlap-cofficient":
        metric = StringMetrics.overlapCoefficient();
        break;

      case "jaccard":
        metric = StringMetrics.jaccard();
        break;

      case "damerau-levenshtein":
        metric = StringMetrics.damerauLevenshtein();
        break;

      case "generalized-jaccard":
        metric = StringMetrics.generalizedJaccard();
        break;

      case "jaro":
        metric = StringMetrics.jaro();
        break;

      case "simon-white":
        metric = StringMetrics.simonWhite();
        break;

      case "levenshtein":
        metric = StringMetrics.levenshtein();
        break;

      case "cosine":
      default:
        metric = StringMetrics.cosineSimilarity();
        break;
    }
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      int idx1 = row.find(column1);
      int idx2 = row.find(column2);

      if (idx1 == -1 || idx2 == -1) {
        row.add(destination, 0.0f);
        continue;
      }

      Object object1 = row.getValue(idx1);
      Object object2 = row.getValue(idx2);

      if (object1 == null || object2 == null) {
        row.add(destination, 0.0f);
        continue;
      }

      if (!(object1 instanceof String) || !(object2 instanceof String)) {
        row.add(destination, 0.0f);
        continue;
      }

      if (((String) object1).isEmpty() || ((String) object2).isEmpty()) {
        row.add(destination, 0.0f);
        continue;
      }

      row.add(destination, metric.compare((String) object1, (String) object2));
    }

    return rows;
  }

  @Override
  public Mutation lineage() {
    return Mutation.builder()
      .readable("Compared text in columns '%s' and '%s' and saved it in column '%s'",
                column1, column2, destination)
      .relation(Many.columns(column1, column2), destination)
      .relation(column1, column1)
      .relation(column2, column2)
      .build();
  }
}
