package com.gentics.mesh.core.data.service;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.RoutingContext;

import java.awt.print.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.Result;
import com.gentics.mesh.core.data.model.root.ProjectRoot;
import com.gentics.mesh.core.data.model.tinkerpop.Project;
import com.gentics.mesh.core.data.model.tinkerpop.User;
import com.gentics.mesh.core.data.service.generic.GenericNodeServiceImpl;
import com.gentics.mesh.core.rest.project.response.ProjectResponse;
import com.gentics.mesh.paging.PagingInfo;

@Component
public class ProjectServiceImpl extends GenericNodeServiceImpl<Project> implements ProjectService {

	private static Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

	@Autowired
	protected UserService userService;

	@Override
	public Project findByName(String projectName) {
		return null;
	}

	@Override
	public Project findByUUID(String uuid) {
		return null;
	}

	@Override
	public Result<Project> findAll() {
		return null;
	}

	@Override
	public void deleteByName(String name) {
	}

	@Override
	public ProjectResponse transformToRest(RoutingContext rc, Project project) {
		ProjectResponse projectResponse = new ProjectResponse();
		projectResponse.setUuid(project.getUuid());
		projectResponse.setName(project.getName());
		projectResponse.setPerms(userService.getPerms(rc, project));

		//		MeshNode rootNode = neo4jTemplate.fetch(project.getRootNode());
		//		if (rootNode != null) {
		//			projectResponse.setRootNodeUuid(rootNode.getUuid());
		//		} else {
		//			log.info("Inconsistency detected. Project {" + project.getUuid() + "} has no root node.");
		//		}
		//		return projectResponse;
		return null;
	}

	@Override
	public Page<Project> findAllVisible(User requestUser, PagingInfo pagingInfo) {
		//		return projectRepository.findAll(requestUser, new MeshPageRequest(pagingInfo));
		return null;
	}

	public Page<Project> findAll(User requestUser, Pageable pageable) {

		//	@Query(value = "MATCH (requestUser:User)-[:MEMBER_OF]->(group:Group)<-[:HAS_ROLE]-(role:Role)-[perm:HAS_PERMISSION]->(project:Project) where id(requestUser) = {0} and perm.`permissions-read` = true return project ORDER BY project.name", countQuery = "MATCH (requestUser:User)-[:MEMBER_OF]->(group:Group)<-[:HAS_ROLE]-(role:Role)-[perm:HAS_PERMISSION]->(project:Project) where id(requestUser) = {0} and perm.`permissions-read` = true return count(project)")
		return null;

	}

	public ProjectRoot findRoot() {
		//		@Query("MATCH (n:ProjectRoot) return n")
		return null;
	}

//	@Override
//	public Project save(Project project) {
		//		ProjectRoot root = projectRepository.findRoot();
		//		if (root == null) {
		//			throw new NullPointerException("The project root node could not be found.");
		//		}
		//		project = neo4jTemplate.save(project);
		//		root.getProjects().add(project);
		//		neo4jTemplate.save(root);
		//		return project;
//		return null;
//	}

	@Override
	public Project create(String name) {
		Project project = framedGraph.addVertex(Project.class);
		project.setName(name);
		return project;
	}

	@Override
	public ProjectRoot createRoot() {
		return framedGraph.addVertex(ProjectRoot.class);
	}

}
