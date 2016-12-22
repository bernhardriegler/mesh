package com.gentics.mesh.search;

import static com.gentics.mesh.util.MeshAssert.assertSuccess;
import static com.gentics.mesh.util.MeshAssert.failingLatch;
import static com.gentics.mesh.util.MeshAssert.latchFor;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.codehaus.jettison.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.SchemaListResponse;
import com.gentics.mesh.graphdb.NoTx;
import com.gentics.mesh.parameter.impl.PagingParametersImpl;
import com.gentics.mesh.rest.client.MeshResponse;
import com.gentics.mesh.test.performance.TestUtils;
import com.gentics.mesh.util.MeshAssert;

import io.vertx.core.DeploymentOptions;

public class SchemaSearchEndpointTest extends AbstractSearchEndpointTest implements BasicSearchCrudTestcases {

	@Before
	public void setupWorkerVerticle() throws Exception {
		DeploymentOptions options = new DeploymentOptions();
		options.setWorker(true);
		vertx.deployVerticle(meshDagger.nodeMigrationVerticle(), options);
	}

	@After
	public void setopWorkerVerticle() throws Exception {
		meshDagger.nodeMigrationVerticle().stop();
	}

	@Test
	public void testSearchSchema() throws Exception {
		try (NoTx noTx = db.noTx()) {
			fullIndex();
		}

		MeshResponse<SchemaListResponse> future = getClient().searchSchemas(getSimpleQuery("folder"), new PagingParametersImpl().setPage(1).setPerPage(2))
				.invoke();
		latchFor(future);
		assertSuccess(future);
		SchemaListResponse response = future.result();
		assertEquals(1, response.getData().size());

		future = getClient().searchSchemas(getSimpleQuery("blub"), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		response = future.result();
		assertEquals(0, response.getData().size());

		future = getClient().searchSchemas(getSimpleTermQuery("name", "folder"), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		response = future.result();
		assertEquals(1, response.getData().size());
	}

	@Test
	@Override
	public void testDocumentCreation() throws Exception {
		final String newName = "newschema";
		Schema schema = createSchema(newName);
		try (NoTx noTx = db.noTx()) {
			MeshAssert.assertElement(boot.schemaContainerRoot(), schema.getUuid(), true);
		}
		MeshResponse<SchemaListResponse> future = getClient()
				.searchSchemas(getSimpleTermQuery("name", newName), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		SchemaListResponse response = future.result();
		assertEquals(1, response.getData().size());
	}

	@Test
	@Override
	public void testDocumentDeletion() throws InterruptedException, JSONException {
		final String schemaName = "newschemaname";
		Schema schema = createSchema(schemaName);

		MeshResponse<SchemaListResponse> future = getClient()
				.searchSchemas(getSimpleTermQuery("name", schemaName), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		assertEquals(1, future.result().getData().size());

		deleteSchema(schema.getUuid());
		future = getClient().searchSchemas(getSimpleTermQuery("name", schemaName), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		assertEquals(0, future.result().getData().size());
	}

	@Test
	@Override
	@Ignore
	public void testDocumentUpdate() throws Exception {
		CountDownLatch latch = TestUtils.latchForMigrationCompleted(getClient());

		// 1. Create a new schema
		final String schemaName = "newschemaname";
		Schema schema = createSchema(schemaName);

		// 2. Setup latch for migration/schema update
		String newSchemaName = "updatedschemaname";
		updateSchema(schema.getUuid(), newSchemaName);

		// 3. Wait for migration to complete
		failingLatch(latch);

		// 4. Search for the original schema
		MeshResponse<SchemaListResponse> future = getClient()
				.searchSchemas(getSimpleTermQuery("name", schemaName), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		assertEquals("The schema with the old name {" + schemaName + "} was found but it should not have been since we updated it.", 0,
				future.result().getData().size());

		// 5. Search for the updated schema
		future = getClient().searchSchemas(getSimpleTermQuery("name", newSchemaName), new PagingParametersImpl().setPage(1).setPerPage(2)).invoke();
		latchFor(future);
		assertSuccess(future);
		assertEquals("The schema with the updated name was not found.", 1, future.result().getData().size());
	}
}