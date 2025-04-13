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

package io.cdap.directives.parser;

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
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;

import java.util.List;

/**
 * A Executor for parsing Apache HTTPD and NGINX log files.
 */
@Plugin(type = Directive.TYPE)
@Name("parse-as-log")
@Categories(categories = { "parser", "logs"})
@Description("Parses Apache HTTPD and NGINX logs.")
public class ParseLog implements Directive, Lineage {
  public static final String NAME = "parse-as-log";
  private String column;
  private String format;
  private LogLine line;
  private Parser<Object> parser;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    builder.define("format", TokenType.TEXT);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.column = ((ColumnName) args.value("column")).value();
    this.format = ((Text) args.value("format")).value();
    this.parser = new ApacheHttpdLoglineParser<>(Object.class, format);
    this.line = new LogLine();
    List<String> paths = this.parser.getPossiblePaths();
    try {
      parser.addParseTarget(LogLine.class.getMethod("setValue", String.class, String.class), paths);
    } catch (NoSuchMethodException e) {
      // This should never happen, as the class is defined within this class.
    }
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    // Iterate through all the rows.
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx != -1) {
        Object object = row.getValue(idx);

        if (object == null) {
          throw new DirectiveExecutionException(
            NAME, String.format("Column '%s' has null value. It should be a non-null 'String' or 'byte array'.",
                                column));
        }

        String log;
        if (object instanceof String) {
          log = (String) object;
        } else if (object instanceof byte[]) {
          log = new String((byte[]) object);
        } else {
          throw new DirectiveExecutionException(
            NAME, String.format("Column '%s' is of invalid type '%s'. It should be of type 'String' or 'byte array'.",
                                column, object.getClass().getSimpleName()));
        }
        line.set(row);
        try {
          parser.parse(line, log);
        } catch (Exception e) {
          row.addOrSet("log.parse.error", 1);
        }
      }
    }
    return rows;
  }

  @Override
  public Mutation lineage() {
    return Mutation.builder()
      .readable("Parsed column '%s' as webserver log using format '%s'", column, format)
      .all(Many.columns(column), Many.columns(column))
      .build();
  }

  /**
   * A log line
   */
  public final class LogLine {
    private Row row;

    public void setValue(String name, String value) {
      String key = name.toLowerCase();
      if (key.contains("original") || key.contains("bytesclf") || key.contains("cookie")) {
        return;
      }
      key = key.replaceAll("[^a-zA-Z0-9_]", "_");
      row.addOrSet(key, value);
    }

    public void set(Row row) {
      this.row = row;
    }

    public Row get() {
      return row;
    }
  }

}
