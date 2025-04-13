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

package io.cdap.directives.row;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.directives.parser.JsParser;
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
import io.cdap.wrangler.api.parser.ColumnNameList;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A directive that Flattens a record
 */
@Plugin(type = Directive.TYPE)
@Name(Flatten.NAME)
@Categories(categories = { "row"})
@Description("Separates array elements of one or more columns into individual records, copying the other columns.")
public class Flatten implements Directive, Lineage {
  public static final String NAME = "flatten";
  // Column within the input row that needs to be parsed as Json
  private String[] columns;
  private int[] locations;
  private int count = 0;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME_LIST);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    List<String> cols = ((ColumnNameList) args.value("column")).value();
    columns = new String[cols.size()];
    columns = cols.toArray(columns);
    locations = new int[columns.length];
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    List<Row> results = new ArrayList<>();

    // Iterate through the rows.
    for (Row row : rows) {
      count = 0;
      // Only once find the location of the columns to be flatten within
      // the row. It's assumed that all rows to passed to this
      // instance are same.
      for (String column : columns) {
        locations[count] = row.find(column);
        ++count;
      }
      // For each row we find the maximum number of
      // values in each of the columns specified to be
      // flattened.
      int max = Integer.MIN_VALUE;
      for (int i = 0; i < count; ++i) {
        if (locations[i] != -1) {
          Object value = row.getValue(locations[i]);
          int m = -1;
          if (value instanceof JsonArray) {
            m = ((JsonArray) value).size();
          } else if (value instanceof List) {
            m = ((List) value).size();
          } else {
            m = 1;
          }
          if (m > max) {
            max = m;
          }
        }
      }

      if (max == 0) {
        Row newRow = new Row(row);
        for (int i = 0; i < count; ++i) {
          if (locations[i] != -1) {
            newRow.setValue(locations[i], null);
          }
        }
        results.add(newRow);
        continue;
      }

      // We iterate through the arrays and populate all the columns.
      for (int k = 0; k < max; ++k) {
        Row r = new Row(row);
        for (int i = 0; i < count; ++i) {
          if (locations[i] != -1) {
            Object value = row.getValue(locations[i]);
            if (value == null) {
              r.add(columns[i], null);
            } else {
              Object v = null;
              if (value instanceof JsonArray) {
                JsonArray array = (JsonArray) value;
                if (k < array.size()) {
                  v = array.get(k);
                }
              } else if (value instanceof List) {
                List<Object> array = (List) value;
                if (k < array.size()) {
                  v = array.get(k);
                }
              } else {
                v = value;
              }
              if (v == null) {
                r.addOrSet(columns[i], null);
              } else {
                if (v instanceof JsonElement) {
                  r.setValue(locations[i], JsParser.getValue((JsonElement) v));
                } else {
                  r.setValue(locations[i], v);
                }
              }
            }
          } else {
            r.addOrSet(columns[i], null);
          }
        }
        results.add(r);
      }
    }
    return results;
  }

  @Override
  public Mutation lineage() {
    return Mutation.builder()
      .readable("Expanded to individual records based on values in columns '%s''", Arrays.asList(columns))
      .all(Many.columns(columns), Many.columns(columns))
      .build();
  }
}
