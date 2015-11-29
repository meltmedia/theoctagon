package com.meltmedia.resource;

import com.meltmedia.dao.UserDAO;
import com.meltmedia.data.PageLink;
import com.meltmedia.data.Paginate;
import com.meltmedia.data.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.meltmedia.representation.JsonMessageException;
import com.meltmedia.representation.UserRepresentation;
import com.meltmedia.service.ValidationService;
import com.meltmedia.util.BakedBeanUtils;
import com.meltmedia.util.UserUtil;
import com.praxissoftware.rest.core.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. UserResource: jheun Date: 6/26/13
 */

@Path("/user")
@Singleton
public class UserResource {

	@Context
	UriInfo uriInfo;

	static int PAGE_LIMIT = 4;

	private Logger log = LoggerFactory.getLogger(getClass());

	protected UserRepresentation createRepresentation(User user) {

		UserRepresentation rep = new UserRepresentation(user);
		// Link to the full entity
		rep.getLinks()
				.add(new Link(
						uriInfo.getBaseUriBuilder().path(UserResource.class).path(user.getId().toString()).build(),
						"self", MediaType.APPLICATION_JSON));
		return rep;

	}

	@Inject
	ValidationService validationService;
	@Inject
	UserDAO dao;

	@GET
	@Produces("application/json")
	public List<UserRepresentation> getUsers() {
		List<User> users = dao.list();

		List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();

		for (User user : users) {
			userReps.add(createRepresentation(user));
		}

		return userReps;
	}

	@GET
	@Path("/{userId}")
	@Produces("application/json")
	public UserRepresentation getUser(@PathParam("userId") long id) {
		User user = dao.get(id);

		if (user == null) {
			throw new WebApplicationException(404);
		}

		return createRepresentation(user);
	}

	@GET
	@Path("/page")
	@Produces("application/json")
	public HashMap<String, Object> getPage() {

		// get the complete users list
		HashMap<String, Object> returnObject = new HashMap<String, Object>();

		try {
			List<User> users = dao.list(); //retrieve all users
			List<User> filteredUsers = new ArrayList<User>(); 
			List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();

			int start = 0;
			int end = users.size();

			// Step 1. Filter based on the range

			int userStrt = 0;
			int userEnd = users.size();

			//get the userStrt range value from the request
			if (uriInfo.getQueryParameters().containsKey("userStrt")) {
				userStrt = Integer.parseInt(uriInfo.getQueryParameters().getFirst("userStrt"));
			}
			//get the userEnd range value from the request
			if (uriInfo.getQueryParameters().containsKey("userEnd")) {
				userEnd = Integer.parseInt(uriInfo.getQueryParameters().getFirst("userEnd"));
			}
			
			//adjust the start and end based on inputs
			if (userStrt != start || userEnd != end) {
				if (userStrt <= userEnd) {
					start = userStrt;
					end = userEnd;
				} else {
					start = userEnd;
					end = userStrt;
				}

				for (User user : users) {
					if (user.getId() >= start && user.getId() <= end) {
						filteredUsers.add(user); //filter the users list based on the query parameters
					}
				}
			} else {
				//get all users when no filter matches
				filteredUsers.addAll(users);
			}

			// Step 2. Divide the results into pages

			HashMap<Integer, List<User>> pages = new HashMap<Integer, List<User>>();

			int pageLim = PAGE_LIMIT; // default Page Limit
			int pageNo = 1; //default page number set to 1

			//get the page number value from the request
			if (uriInfo.getQueryParameters().containsKey("pageNo")) {
				pageNo = Integer.parseInt(uriInfo.getQueryParameters().getFirst("pageNo"));
			}

			//get the users per page limit from the request
			if (uriInfo.getQueryParameters().containsKey("pageLim")) {
				pageLim = Integer.parseInt(uriInfo.getQueryParameters().getFirst("pageLim"));
			}
			
			if(pageLim < 1){
				pageLim = PAGE_LIMIT;
			}

			int userIndex = 0; //index to iterate over users
			int pageIndex = 1; //index for pages
			int listSize = filteredUsers.size(); // max number of matching users 
			
			while (userIndex < listSize) {
				int pageEnd = userIndex + pageLim - 1;
				if (pageEnd >= listSize) {
					pageEnd = listSize - 1;
				}
				List<User> tempList = new ArrayList<User>();

				for (int j = userIndex; j <= pageEnd; j++) {
					tempList.add(filteredUsers.get(j)); //get users for a page
				}

				userIndex += pageLim;
				pages.put(pageIndex++, tempList); //add a page with index for users list

			}
			
			// adjust page numbers
			if (pageNo >= pageIndex) {
				pageNo = pageIndex - 1;
			}
			if (pageNo <= 0) {
				pageNo = 0;
			}

			//create representations for each user for the selected page to be displayed
			for (User user : pages.get(pageNo)) {
				userReps.add(createRepresentation(user));
			}

			// Step 3. Create other page links
			
			Paginate pageLinks = new Paginate();
			
			//Set Current, Next and previous page numbers
			pageLinks.setCurrentPage(pageNo); 
			if (pageNo + 1 <= pages.size()) {
				pageLinks.setNextPage(pageNo + 1);
			} else {
				pageLinks.setNextPage(pageNo);
			}
			if (pageNo - 1 <= 0) {
				pageLinks.setPreviousPage(pageNo);
			} else {
				pageLinks.setPreviousPage(pageNo - 1);
			}

			
			//Iterate over the pages and create links for each page
			Iterator it = pages.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				// System.out.println(pair.getKey() + " = " + pair.getValue());
				UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(UserResource.class);
				uriBuilder.path("/page");
				//create userStrt parameter if it exists in the request
				if (uriInfo.getQueryParameters().containsKey("userStrt")) {
					uriBuilder.queryParam("userStrt", userStrt);
				}
				//create userEnd parameter if it exists in the request
				if (uriInfo.getQueryParameters().containsKey("userEnd")) {
					uriBuilder.queryParam("userEnd", userEnd);
				}
				uriBuilder.queryParam("pageNo", (Integer) pair.getKey());
				//create pageLim parameter if it exists in the request
				if (uriInfo.getQueryParameters().containsKey("pageLim")) {
					uriBuilder.queryParam("pageLim", pageLim);
				}

				//build the link
				Link link = new Link(uriBuilder.build(), "self", MediaType.APPLICATION_JSON);
				PageLink pageLink = new PageLink((Integer) pair.getKey(), link);
				//add the new link to the list
				pageLinks.getPageLinks().add(pageLink);
				it.remove(); 
			}
			returnObject.put("users", userReps); //result list of user representations
			returnObject.put("pages", pageLinks); // list of relevant pages
		} catch (Exception e) {
			//generic error message in case of issues
			returnObject.put("message", "Unable to retrieve data");
		}

		return returnObject;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public UserRepresentation addUser(UserRepresentation rep) {

		// Validate the new user
		validationService.runValidationForJaxWS(rep);

		User user = new User();

		try {

			// Copy the appropriate properties to the new User object
			BakedBeanUtils.safelyCopyProperties(rep, user);

		} catch (BakedBeanUtils.HalfBakedBeanException ex) {

			log.error("There was an error processing the new user input.", ex);
			throw new JsonMessageException(Response.Status.INTERNAL_SERVER_ERROR,
					"There was an error processing the input.");

		}

		// Set the new password, salting and hashing and all that neat jazz
		UserUtil.setupNewPassword(user, rep.getPassword().toCharArray());

		// Create the user in the system
		dao.create(user);

		// Return a representation of the user
		return createRepresentation(user);

	}

}
