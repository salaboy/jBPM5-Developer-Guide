package com.salaboy.jbpm5.dev.guide.ldap;

import java.util.List;
import org.jbpm.task.identity.UserGroupCallback;


/**
 * Implementation of {@link UserGroupCallback}, which interacts with ldap to get
 * information about users, groups and its relationship.
 * 
 * @author calcacuervo
 * 
 */
public class LdapUserGroupCallback implements UserGroupCallback {

	private LdapQueryHelper query;

	public void setQuery(LdapQueryHelper query) {
		this.query = query;
	}

	@Override
	public boolean existsGroup(String group) {
		return query.existsGroup(group);
	}

	@Override
	public boolean existsUser(String user) {
		return query.existsUser(user);
	}

	
	public List<String> getGroupsForUser(String user) {
		return query.groupsForUser(user);

	}

	
	public List<String> getGroupsForUser(String arg0, List<String> arg1) {
		return query.groupsForUser(arg0);
	}

	@Override
	public List<String> getGroupsForUser(String arg0, List<String> arg1,
			List<String> arg2) {
		return query.groupsForUser(arg0);
	}
}
