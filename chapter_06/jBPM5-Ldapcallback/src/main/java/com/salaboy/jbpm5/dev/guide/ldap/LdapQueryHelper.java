package com.salaboy.jbpm5.dev.guide.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Helper class which uses an ldap tempalte to query information in an ldap
 * server.
 * 
 * @author calcacuervo
 * 
 */
public class LdapQueryHelper {

	private LdapTemplate template;

	public LdapQueryHelper(LdapTemplate template) {
		this.template = template;
	}

	/**
	 * Returns whether an user exists in the ldap server.
	 * 
	 * @param userId
	 * @return
	 */
	public boolean existsUser(String userId) {
		List users = template.search("", "(uid=" + userId + ")",
				new ExistsEntryMapper());
		return users != null && users.size() > 0;
	}

	/**
	 * Returns whether a group exists in the ldap server.
	 * 
	 * @param groupId
	 * @return
	 */
	public boolean existsGroup(String groupId) {
		List users = template.search("", "(cn=" + groupId + ")",
				new ExistsEntryMapper());
		return users != null && users.size() > 0;
	}

	/**
	 * Gets the group list of a user.
	 * 
	 * @param userId
	 * @return
	 */
	public List<String> groupsForUser(String userId) {
		List users = template.search("",
				"(&(objectClass=group)(uniqueMember=cn=" + userId
						+ ",ou=users,o=mojo))", new GroupAttributesMapper());
		return users;
	}

	/**
	 * Gets the user emails for a group.
	 * 
	 * @param group
	 * @return
	 */
	public List<String> userEmailsForGroup(String group) {
		List<String> usersList = template
				.search("", "(&(objectClass=group)(cn=" + group + "))",
						new UserAttibutesMapper());
		String[] users = usersList.get(0).split(",");
		List<String> mails = new ArrayList<String>();
		for (String string : users) {
			mails.add(template
					.search("", string.split(",")[0], new UserMailMapper())
					.get(0).toString().split(":")[1].replace(" ", ""));

		}
		return mails;
	}

	private static class ExistsEntryMapper implements AttributesMapper {

		public ExistsEntryMapper() {
		}

		@Override
		public Object mapFromAttributes(Attributes attributes)
				throws NamingException {
			return attributes.getAll();

		}
	}

	// *************************************************************
	// HELPER CLASSES INNER CLASSES
	private static class UserMailMapper implements AttributesMapper {

		public UserMailMapper() {
		}

		@Override
		public Object mapFromAttributes(Attributes attributes)
				throws NamingException {
			return attributes.get("mail");

		}
	}

	private static class GroupAttributesMapper implements AttributesMapper {

		public GroupAttributesMapper() {
		}

		@Override
		public Object mapFromAttributes(Attributes attributes)
				throws NamingException {
			String group = attributes.get("cn").get().toString();
			return group;

		}
	}

	private static class UserAttibutesMapper implements AttributesMapper {

		public UserAttibutesMapper() {
		}

		@Override
		public Object mapFromAttributes(Attributes attributes)
				throws NamingException {
			NamingEnumeration ne = attributes.get("uniquemember").getAll();
			String group = "";
			while (ne.hasMore()) {
				group = group + "," + ne.next().toString().split(",")[0];
			}

			return group.substring(1);

		}
	}

}
