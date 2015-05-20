package com.gentics.mesh.demo;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.core.data.model.Language;
import com.gentics.mesh.core.data.model.MeshNode;
import com.gentics.mesh.core.data.model.MeshRoot;
import com.gentics.mesh.core.data.model.ObjectSchema;
import com.gentics.mesh.core.data.model.Project;
import com.gentics.mesh.core.data.model.PropertyType;
import com.gentics.mesh.core.data.model.PropertyTypeSchema;
import com.gentics.mesh.core.data.model.Tag;
import com.gentics.mesh.core.data.model.auth.AuthRelationships;
import com.gentics.mesh.core.data.model.auth.GraphPermission;
import com.gentics.mesh.core.data.model.auth.Group;
import com.gentics.mesh.core.data.model.auth.Role;
import com.gentics.mesh.core.data.model.auth.User;
import com.gentics.mesh.core.data.service.GroupService;
import com.gentics.mesh.core.data.service.LanguageService;
import com.gentics.mesh.core.data.service.MeshNodeService;
import com.gentics.mesh.core.data.service.MeshRootService;
import com.gentics.mesh.core.data.service.ObjectSchemaService;
import com.gentics.mesh.core.data.service.ProjectService;
import com.gentics.mesh.core.data.service.RoleService;
import com.gentics.mesh.core.data.service.TagService;
import com.gentics.mesh.core.data.service.UserService;
import com.gentics.mesh.etc.MeshSpringConfiguration;

@Component
public class DemoDataProvider {

	private static final Logger log = LoggerFactory.getLogger(DemoDataProvider.class);

	public static final String PROJECT_NAME = "dummy";
	public static final String TAG_CATEGORIES_SCHEMA_NAME = "tagCategories";
	public static final String TAG_DEFAULT_SCHEMA_NAME = "tag";

	private static SecureRandom random = new SecureRandom();

	@Autowired
	private UserService userService;

	@Autowired
	private MeshRootService rootService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private LanguageService languageService;

	@Autowired
	private MeshNodeService nodeService;

	@Autowired
	private TagService tagService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ObjectSchemaService objectSchemaService;

	@Autowired
	private GraphDatabaseService graphDb;

	@Autowired
	private Neo4jTemplate neo4jTemplate;

	@Autowired
	protected MeshSpringConfiguration springConfig;

	@Autowired
	private BootstrapInitializer bootstrapInitializer;

	// References to dummy data

	private Language english;

	private Language german;

	private Project project;

	private UserInfo userInfo;

	private MeshRoot root;

	private Map<String, ObjectSchema> schemas = new HashMap<>();
	private Map<String, MeshNode> folders = new HashMap<>();
	private Map<String, MeshNode> contents = new HashMap<>();
	private Map<String, Tag> tags = new HashMap<>();
	private Map<String, User> users = new HashMap<>();
	private Map<String, Role> roles = new HashMap<>();
	private Map<String, Group> groups = new HashMap<>();

	private DemoDataProvider() {
	}

	public void setup(int multiplicator) throws JsonParseException, JsonMappingException, IOException {
		bootstrapInitializer.initMandatoryData();

		contents.clear();
		folders.clear();
		tags.clear();
		users.clear();
		roles.clear();
		groups.clear();

		addUserGroupRoleProject(multiplicator);
		addSchemas();
		addFolderStructure();
		addTags();
		addContents(multiplicator);
		updatePermissions();
	}

	private void addContents(int multiplicator) {

		ObjectSchema contentSchema = schemas.get("content");

		for (int i = 0; i < 12 * multiplicator; i++) {
			addContent(folders.get("news2014"), "News_2014_" + i, "News " + i + "!", "Neuigkeiten " + i + "!", contentSchema);
		}

		addContent(folders.get("news"), "News Overview", "News Overview", "News Übersicht", contentSchema);

		addContent(folders.get("deals"), "Super Special Deal 2015", "Buy two get nine!", "Kauf zwei und nimm neun mit!", contentSchema);
		for (int i = 0; i < 12 * multiplicator; i++) {
			addContent(folders.get("deals"), "Special Deal June 2015 - " + i, "Buy two get three! " + i, "Kauf zwei und nimm drei mit!" + i,
					contentSchema);
		}

		addContent(folders.get("news2015"), "Special News_2014", "News!", "Neuigkeiten!", contentSchema);
		for (int i = 0; i < 12 * multiplicator; i++) {
			addContent(folders.get("news2015"), "News_2015_" + i, "News" + i + "!", "Neuigkeiten " + i + "!", contentSchema);
		}

		MeshNode porsche911 = addContent(
				folders.get("products"),
				"Porsche 911",
				"997 is the internal designation for the Porsche 911 model manufactured and sold by German manufacturer Porsche between 2004 (as Model Year 2005) and 2012.",
				"Porsche 997 ist die interne Modellbezeichnung von Porsche für das von 2004 bis Ende 2012 produzierte 911-Modell.", contentSchema);
		porsche911.addTag(tags.get("vehicle"));
		porsche911.addTag(tags.get("car"));
		contents.put("Porsche 911", porsche911);

		MeshNode nissanGTR = addContent(
				folders.get("products"),
				"Nissan GT-R",
				"The Nissan GT-R is a 2-door 2+2 sports coupé produced by Nissan and first released in Japan in 2007",
				"Der Nissan GT-R ist ein seit Dezember 2007 produziertes Sportcoupé des japanischen Automobilherstellers Nissan und der Nachfolger des Nissan Skyline GT-R R34.",
				contentSchema);
		nissanGTR.addTag(tags.get("vehicle"));
		nissanGTR.addTag(tags.get("car"));
		contents.put("Nissan GTR", nissanGTR);
		nissanGTR.addTag(tags.get("green"));

		MeshNode bmwM3 = addContent(
				folders.get("products"),
				"BMW M3",
				"The BMW M3 (first launched in 1986) is a high-performance version of the BMW 3-Series, developed by BMW's in-house motorsport division, BMW M.",
				"Der BMW M3 ist ein Sportmodell der 3er-Reihe von BMW, das seit Anfang 1986 hergestellt wird. Dabei handelt es sich um ein Fahrzeug, welches von der BMW-Tochterfirma BMW M GmbH entwickelt und anfangs (E30 und E36) auch produziert wurde.",
				contentSchema);
		bmwM3.addTag(tags.get("vehicle"));
		bmwM3.addTag(tags.get("car"));
		bmwM3.addTag(tags.get("blue"));
		contents.put("BMW M3", bmwM3);

		MeshNode concorde = addContent(
				folders.get("products"),
				"Concorde",
				"Aérospatiale-BAC Concorde is a turbojet-powered supersonic passenger jet airliner that was in service from 1976 to 2003.",
				"Die Aérospatiale-BAC Concorde 101/102, kurz Concorde (französisch und englisch für Eintracht, Einigkeit), ist ein Überschall-Passagierflugzeug, das von 1976 bis 2003 betrieben wurde.",
				contentSchema);
		concorde.addTag(tags.get("plane"));
		concorde.addTag(tags.get("twinjet"));
		concorde.addTag(tags.get("red"));
		contents.put("Concorde", concorde);

		MeshNode boeing737 = addContent(
				folders.get("products"),
				"Boeing 737",
				"The Boeing 737 is a short- to medium-range twinjet narrow-body airliner. Originally developed as a shorter, lower-cost twin-engined airliner derived from Boeing's 707 and 727, the 737 has developed into a family of nine passenger models with a capacity of 85 to 215 passengers.",
				"Die Boeing 737 des US-amerikanischen Flugzeugherstellers Boeing ist die weltweit meistgebaute Familie strahlgetriebener Verkehrsflugzeuge.",
				contentSchema);
		boeing737.addTag(tags.get("plane"));
		boeing737.addTag(tags.get("twinjet"));
		contents.put("Boeing 737", boeing737);

		MeshNode a300 = addContent(
				folders.get("products"),
				"Airbus A300",
				"The Airbus A300 is a short- to medium-range wide-body twin-engine jet airliner that was developed and manufactured by Airbus. Released in 1972 as the world's first twin-engined widebody, it was the first product of Airbus Industrie, a consortium of European aerospace manufacturers, now a subsidiary of Airbus Group.",
				"Der Airbus A300 ist das erste zweistrahlige Großraumflugzeug der Welt, produziert vom europäischen Flugzeughersteller Airbus.",
				contentSchema);
		a300.addTag(tags.get("plane"));
		a300.addTag(tags.get("twinjet"));
		a300.addTag(tags.get("red"));

		contents.put("Airbus A300", a300);

		MeshNode wrangler = addContent(
				folders.get("products"),
				"Jeep Wrangler",
				"The Jeep Wrangler is a compact and mid-size (Wrangler Unlimited models) four-wheel drive off-road and sport utility vehicle (SUV), manufactured by American automaker Chrysler, under its Jeep marque – and currently in its third generation.",
				"Der Jeep Wrangler ist ein Geländewagen des US-amerikanischen Herstellers Jeep innerhalb des Chrysler-Konzerns.", contentSchema);
		wrangler.addTag(tags.get("vehicle"));
		wrangler.addTag(tags.get("jeep"));
		contents.put("Jeep Wrangler", wrangler);

		MeshNode volvo = addContent(folders.get("products"), "Volvo B10M",
				"The Volvo B10M was a mid-engined bus and coach chassis manufactured by Volvo between 1978 and 2003.", null, contentSchema);
		volvo.addTag(tags.get("vehicle"));
		volvo.addTag(tags.get("bus"));
		contents.put("Volvo B10M", volvo);

		MeshNode hondact90 = addContent(folders.get("products"), "Honda CT90",
				"The Honda CT90 was a small step-through motorcycle manufactured by Honda from 1966 to 1979.", null, contentSchema);
		hondact90.addTag(tags.get("vehicle"));
		hondact90.addTag(tags.get("motorcycle"));
		contents.put("Honda CT90", hondact90);

		MeshNode hondaNR = addContent(
				folders.get("products"),
				"Honda NR",
				"The Honda NR (New Racing) was a V-four motorcycle engine series started by Honda in 1979 with the 500cc NR500 Grand Prix racer that used oval pistons.",
				"Die NR750 ist ein Motorrad mit Ovalkolben-Motor des japanischen Motorradherstellers Honda, von dem in den Jahren 1991 und 1992 300 Exemplare gebaut wurden.",
				contentSchema);
		hondaNR.addTag(tags.get("vehicle"));
		hondaNR.addTag(tags.get("motorcycle"));
		hondaNR.addTag(tags.get("green"));
		contents.put("Honda NR", hondaNR);

	}

	private void addFolderStructure() {

		MeshNode rootNode = new MeshNode();
		rootNode = nodeService.save(rootNode);
		rootNode.setCreator(userInfo.getUser());
		rootNode.addProject(project);
		project.setRootNode(rootNode);
		project = projectService.save(project);

		MeshNode news = addFolder(rootNode, "News", "Neuigkeiten");
		addFolder(news, "2015", null);

		MeshNode news2014 = addFolder(news, "2014", null);
		addFolder(news2014, "March", null);

		addFolder(rootNode, "products", "Produkte");
		addFolder(rootNode, "Deals", "Angebote");

	}

	private void addTags() {

		ObjectSchema colorSchema = schemas.get("colors");
		ObjectSchema categoriesSchema = schemas.get("categories");

		// Tags for categories
		addTag("Vehicle", "Fahrzeug", categoriesSchema);
		addTag("Car", "Auto", categoriesSchema);
		addTag("Jeep", null, categoriesSchema);
		addTag("Bike", "Fahrrad", categoriesSchema);
		addTag("Motorcycle", "Motorrad", categoriesSchema);
		addTag("Bus", "Bus", categoriesSchema);
		addTag("Plane", "Flugzeug", categoriesSchema);
		addTag("JetFigther", "Düsenjäger", categoriesSchema);
		addTag("Twinjet", "Zweistrahliges Flugzeug", categoriesSchema);

		// Tags for colors
		addTag("red", null, colorSchema);
		addTag("blue", null, colorSchema);
		addTag("green", null, colorSchema);

	}

	public UserInfo createUserInfo(String username, String firstname, String lastname) {

		String password = "test123";
		String email = firstname.toLowerCase().substring(0, 1) + "." + lastname.toLowerCase() + "@spam.gentics.com";

		User user = new User(username);
		user.setUuid("UUIDOFUSER1");
		userService.setPassword(user, password);
		log.info("Creating user with username: " + username + " and password: " + password);
		user.setFirstname(firstname);
		user.setLastname(lastname);
		user.setEmailAddress(email);
		userService.save(user);
		users.put(username, user);

		String roleName = username + "_role";
		Role role = new Role(roleName);
		roleService.save(role);
		roles.put(roleName, role);

		String groupName = username + "_group";
		Group group = new Group(groupName);
		group.addUser(user);
		group.addRole(role);
		group = groupService.save(group);
		groups.put(groupName, group);

		UserInfo userInfo = new UserInfo(user, group, role, password);
		return userInfo;

	}

	private void addUserGroupRoleProject(int multiplicator) {
		// User, Groups, Roles
		userInfo = createUserInfo("joe1", "Joe", "Doe");

		project = new Project(PROJECT_NAME);
		project.setCreator(userInfo.getUser());
		project = projectService.save(project);

		root = rootService.findRoot();
		root.addUser(userInfo.getUser());
		rootService.save(root);

		english = languageService.findByLanguageTag("en");
		german = languageService.findByLanguageTag("de");

		// Guest Group / Role
		Role guestRole = new Role("guest_role");
		roleService.save(guestRole);
		roles.put(guestRole.getName(), guestRole);

		Group guests = new Group("guests");
		guests.addRole(guestRole);
		guests = groupService.save(guests);
		groups.put("guests", guests);

		// Extra User
		for (int i = 0; i < 12 * multiplicator; i++) {
			User user = new User("guest_" + i);
			// userService.setPassword(user, "guestpw" + i);
			user.setFirstname("Guest Firstname");
			user.setLastname("Guest Lastname");
			user.setEmailAddress("guest_" + i + "@spam.gentics.com");
			user = userService.save(user);
			guests.addUser(user);
			guests = groupService.save(guests);
			users.put(user.getUsername(), user);
		}
		// Extra Groups
		for (int i = 0; i < 12 * multiplicator; i++) {
			Group group = new Group("extra_group_" + i);
			group = groupService.save(group);
			groups.put(group.getName(), group);
		}

		// Extra Roles
		for (int i = 0; i < 12 * multiplicator; i++) {
			Role role = new Role("extra_role_" + i);
			roleService.save(role);
			roles.put(role.getName(), role);
		}
	}

	private void addSchemas() {

		// tag
		ObjectSchema tagSchema = objectSchemaService.findByName("tag");
		tagSchema.addProject(project);
		tagSchema = objectSchemaService.save(tagSchema);
		schemas.put("tag", tagSchema);

		// folder
		ObjectSchema folderSchema = objectSchemaService.findByName("folder");
		folderSchema.addProject(project);
		folderSchema = objectSchemaService.save(folderSchema);
		schemas.put("folder", folderSchema);

		// content
		ObjectSchema contentSchema = objectSchemaService.findByName("content");
		contentSchema.addProject(project);
		contentSchema = objectSchemaService.save(contentSchema);
		schemas.put("content", contentSchema);

		// colors
		ObjectSchema colorSchema = new ObjectSchema("colors");
		colorSchema.setDescription("Colors");
		colorSchema.setDescription("Colors");
		PropertyTypeSchema nameProp = new PropertyTypeSchema(ObjectSchema.NAME_KEYWORD, PropertyType.I18N_STRING);
		nameProp.setDisplayName("Name");
		nameProp.setDescription("The name of the category.");
		colorSchema.addPropertyTypeSchema(nameProp);
		objectSchemaService.save(colorSchema);
		schemas.put("color", colorSchema);

		// category
		ObjectSchema categoriesSchema = new ObjectSchema(TAG_CATEGORIES_SCHEMA_NAME);
		categoriesSchema.addProject(project);
		categoriesSchema.setDisplayName("Category");
		categoriesSchema.setDescription("Custom schema for tag categories");
		categoriesSchema.setCreator(userInfo.getUser());
		nameProp = new PropertyTypeSchema(ObjectSchema.NAME_KEYWORD, PropertyType.I18N_STRING);
		nameProp.setDisplayName("Name");
		nameProp.setDescription("The name of the category.");
		categoriesSchema.addPropertyTypeSchema(nameProp);

		PropertyTypeSchema filenameProp = new PropertyTypeSchema(ObjectSchema.FILENAME_KEYWORD, PropertyType.I18N_STRING);
		filenameProp.setDisplayName("Filename");
		filenameProp.setDescription("The filename property of the category.");
		categoriesSchema.addPropertyTypeSchema(filenameProp);

		PropertyTypeSchema contentProp = new PropertyTypeSchema(ObjectSchema.CONTENT_KEYWORD, PropertyType.I18N_STRING);
		contentProp.setDisplayName("Content");
		contentProp.setDescription("The main content html of the category.");
		categoriesSchema.addPropertyTypeSchema(contentProp);
		objectSchemaService.save(categoriesSchema);
		schemas.put("category", categoriesSchema);

	}

	private void updatePermissions() {
		// // Add Permissions
		// // Add admin permissions to all nodes
		// int i = 0;
		// for (GenericNode currentNode : genericNodeService.findAll()) {
		// currentNode = genericNodeService.reload(currentNode);
		// log.info("Adding BasicPermission to node {" + currentNode.getId() + "}");
		// if (adminRole.getId() == currentNode.getId()) {
		// log.info("Skipping role");
		// continue;
		// }
		// roleService.addPermission(adminRole, currentNode, CREATE, READ, UPDATE, DELETE);
		// adminRole = roleService.save(adminRole);
		// log.info("Added permissions to {" + i + "} objects.");
		// i++;
		// }

		// TODO determine why this is not working when using sdn
		// Add Permissions
		Node roleNode = neo4jTemplate.getPersistentState(userInfo.getRole());
		for (Node node : GlobalGraphOperations.at(graphDb).getAllNodes()) {

			if (roleNode.getId() == node.getId()) {
				log.info("Skipping own role");
				continue;
			}
			Relationship rel = roleNode.createRelationshipTo(node, AuthRelationships.TYPES.HAS_PERMISSION);
			rel.setProperty("__type__", GraphPermission.class.getSimpleName());
			rel.setProperty("permissions-read", true);
			rel.setProperty("permissions-delete", true);
			rel.setProperty("permissions-create", true);
			rel.setProperty("permissions-update", true);
			// GenericNode sdnNode = neo4jTemplate.projectTo(node, GenericNode.class);
			// roleService.addPermission(adminRole, sdnNode, CREATE, READ, UPDATE, DELETE);
			// genericNodeService.save(node);

		}
		log.info("Added BasicPermissions to nodes");

	}

	public MeshNode addFolder(MeshNode rootNode, String englishName, String germanName) {
		MeshNode folderNode = new MeshNode();
		folderNode.setParent(rootNode);
		folderNode.getProjects().add(project);
		if (germanName != null) {
			nodeService.setName(folderNode, german, germanName);
		}
		if (englishName != null) {
			nodeService.setName(folderNode, english, englishName);
		}
		folderNode.setCreator(userInfo.getUser());
		folderNode.setSchema(schemas.get("folder"));
		nodeService.save(folderNode);
		folders.put(englishName.toLowerCase(), folderNode);
		return folderNode;
	}

	public Tag addTag(String englishName, String germanName) {
		return addTag(englishName, germanName, schemas.get("tag"));
	}

	public Tag addTag(String englishName, String germanName, ObjectSchema schema) {
		Tag tag = new Tag();
		if (englishName != null) {
			tagService.setName(tag, english, englishName);
		}
		if (germanName != null) {
			tagService.setName(tag, german, germanName);
		}
		tag.addProject(project);
		tag.setSchema(schema);
		tag.setCreator(userInfo.getUser());
		tag = tagService.save(tag);
		tags.put(englishName.toLowerCase(), tag);
		return tag;
	}

	private MeshNode addContent(MeshNode parentNode, String name, String englishContent, String germanContent, ObjectSchema schema) {
		MeshNode node = new MeshNode();
		nodeService.setName(node, english, name + " english");
		nodeService.setFilename(node, english, name + ".en.html");
		nodeService.setContent(node, english, englishContent);

		if (germanContent != null) {
			nodeService.setName(node, german, name + " german");
			nodeService.setFilename(node, german, name + ".de.html");
			nodeService.setContent(node, german, germanContent);
		}
		// TODO maybe set project should be done inside the save?
		node.addProject(project);
		node.setCreator(userInfo.getUser());
		node.setSchema(schema);
		node.setOrder(42);
		node.setParent(parentNode);
		node = nodeService.save(node);
		// Add the content to the given tag
		//		parentTag.addContent(content);
		//		parentTag = tagService.save(parentTag);

		contents.put(name.toLowerCase(), node);
		return node;
	}

	/**
	 * Returns the path to the tag for the given language.
	 * 
	 * @param language
	 * @return
	 */
	public String getPathForNews2015Tag(Language language) {

		String name = nodeService.getName(folders.get("news"), language);
		String name2 = nodeService.getName(folders.get("news2015"), language);
		return name + "/" + name2;
	}

	public Language getEnglish() {
		return english;
	}

	public Language getGerman() {
		return german;
	}

	public Project getProject() {
		return project;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public MeshNode getFolder(String name) {
		return folders.get(name);
	}

	public MeshNode getContent(String name) {
		return contents.get(name);
	}

	public Tag getTag(String name) {
		return tags.get(name);
	}

	public ObjectSchema getSchema(String name) {
		return schemas.get(name);
	}

	public Map<String, Tag> getTags() {
		return tags;
	}

	public Map<String, MeshNode> getContents() {
		return contents;
	}

	public Map<String, MeshNode> getFolders() {
		return folders;
	}

	public Map<String, User> getUsers() {
		return users;
	}

	public Map<String, Group> getGroups() {
		return groups;
	}

	public Map<String, Role> getRoles() {
		return roles;
	}

	public Map<String, ObjectSchema> getSchemas() {
		return schemas;
	}

	public MeshRoot getMeshRoot() {
		return root;

	}

	public int getNodeCount() {
		return folders.size() + contents.size();
	}
}
