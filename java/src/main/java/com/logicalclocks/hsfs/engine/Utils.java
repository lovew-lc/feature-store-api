/*
 * Copyright (c) 2020 Logical Clocks AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.logicalclocks.hsfs.engine;

import com.logicalclocks.hsfs.Feature;
import com.logicalclocks.hsfs.FeatureStoreException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

  // TODO(Fabio): make sure we keep save the feature store/feature group for serving
  public List<Feature> parseSchema(Dataset<Row> dataset) {
    return Arrays.stream(dataset.schema().fields())
        // TODO(Fabio): unit test this one for complext types
        .map(f -> new Feature(f.name(), f.dataType().catalogString()))
        .collect(Collectors.toList());
  }

  // TODO(Fabio): keep into account the sorting - needs fixing in Hopsworks as well
  public void schemaMatches(Dataset<Row> dataset, List<Feature> features) throws FeatureStoreException {
    StructType tdStructType = new StructType(features.stream().map(
        f -> new StructField(f.getName(),
            // What should we do about the nullables
            new CatalystSqlParser(null).parseDataType(f.getType()), true, Metadata.empty())
    ).toArray(StructField[]::new));

    if (!dataset.schema().equals(tdStructType)) {
      throw new FeatureStoreException("The Dataframe schema: " + dataset.schema() +
          " does not match the training dataset schema: " + tdStructType);
    }
  }
}