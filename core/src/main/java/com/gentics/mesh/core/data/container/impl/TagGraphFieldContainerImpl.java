package com.gentics.mesh.core.data.container.impl;

import com.gentics.mesh.core.data.TagGraphFieldContainer;
import com.gentics.mesh.graphdb.spi.Database;

public class TagGraphFieldContainerImpl extends AbstractBasicGraphFieldContainerImpl implements TagGraphFieldContainer {

	public static void checkIndices(Database database) {
		database.addVertexType(TagGraphFieldContainerImpl.class);
	}

	public String getName() {
		return getProperty("name");
	}

	public TagGraphFieldContainer setName(String name) {
		setProperty("name", name);
		return this;
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}

}