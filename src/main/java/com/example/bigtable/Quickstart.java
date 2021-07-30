/*
 * Copyright 2019 Google LLC
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bigtable;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowCell;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.protobuf.ByteString;

import java.io.IOException;

public class Quickstart {
    private static final String COLUMN_FAMILY_NAME = "test_column";

    public static void main(String... args) {

        String projectId = "project-1";  // my-gcp-project-id
        String instanceId = "instance-1"; // my-bigtable-instance-id
        String tableId = "table-2";    // my-bigtable-table-id

        createTestTable(projectId, instanceId, tableId);
        writeSimple(projectId, instanceId, tableId);
        quickstart(projectId, instanceId, tableId);

    }

    public static void quickstart(String projectId, String instanceId, String tableId) {

        BigtableDataSettings settings = BigtableDataSettings.newBuilderForEmulator(8086).
                setProjectId(projectId).setInstanceId(instanceId).build();

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.

        try (BigtableDataClient dataClient = BigtableDataClient.create(settings)) {
            System.out.println("\nReading a single row by row key");

            Row row = dataClient.readRow(tableId, "phone#4c410523#20190501");
            System.out.println("Row: " + row.getKey().toStringUtf8());

            for (RowCell cell : row.getCells()) {
                System.out.printf(
                        "Family: %s    Qualifier: %s    Value: %s%n",
                        cell.getFamily(), cell.getQualifier().toStringUtf8(), cell.getValue().toStringUtf8());
            }
        } catch (NotFoundException e) {
            System.err.println("Failed to read from a non-existent table: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error during quickstart: \n" + e.toString());
        }
    }

    public static BigtableTableAdminSettings createTestTable(String projectId, String instanceId, String tableId) {

        // Creates the settings to configure a bigtable table admin client.
        BigtableTableAdminSettings adminSettings =
                null;
        try {
            adminSettings = BigtableTableAdminSettings.newBuilderForEmulator(8086)
                    .setProjectId(projectId)
                    .setInstanceId(instanceId)
                    .build();


            // Creates a bigtable table admin client.
            BigtableTableAdminClient adminClient = BigtableTableAdminClient.create(adminSettings);

            // Checks if table exists, creates table if does not exist.
            if (!adminClient.exists(tableId)) {
                System.out.println("Creating table: " + tableId);

                CreateTableRequest createTableRequest =
                        CreateTableRequest.of(tableId).addFamily(COLUMN_FAMILY_NAME);
                adminClient.createTable(createTableRequest);

                System.out.printf("Table %s created successfully%n", tableId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return adminSettings;
    }

    public static void writeSimple(String projectId, String instanceId, String tableId) {

        try (BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId)) {
            long timestamp = System.currentTimeMillis() * 1000;

            String rowkey = "phone#4c410523#20190501";

            RowMutation rowMutation =
                    RowMutation.create(tableId, rowkey)
                            .setCell(
                                    COLUMN_FAMILY_NAME,
                                    ByteString.copyFrom("connected_cell".getBytes()),
                                    timestamp,
                                    1)
                            .setCell(
                                    COLUMN_FAMILY_NAME,
                                    ByteString.copyFrom("connected_wifi".getBytes()),
                                    timestamp,
                                    1)
                            .setCell(COLUMN_FAMILY_NAME, "os_build", timestamp, "PQ2A.190405.003");

            dataClient.mutateRow(rowMutation);
            System.out.printf("Successfully wrote row %s", rowkey);

        } catch (Exception e) {
            System.out.println("Error during WriteSimple: \n" + e.toString());
        }
    }
}
