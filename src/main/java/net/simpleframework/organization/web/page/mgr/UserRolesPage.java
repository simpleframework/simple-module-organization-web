package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.IteratorDataQuery;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.IRoleService;
import net.simpleframework.organization.Role;
import net.simpleframework.organization.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class UserRolesPage extends OneTableTemplatePage implements IOrganizationContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp, "VoteLogPage_table",
				UserRolesTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("roletext", $m("RoleMgrTPage.0"), 180).setTextAlign(
								ETextAlign.left).setSort(false))
				.addColumn(
						new TablePagerColumn("rolename", $m("RoleMgrTPage.1"), 120).setTextAlign(
								ETextAlign.left).setSort(false))
				.addColumn(
						new TablePagerColumn("roletype", $m("RoleMgrTPage.2"), 90).setTextAlign(
								ETextAlign.left).setFilterSort(false))
				.addColumn(TablePagerColumn.DESCRIPTION())
				.addColumn(TablePagerColumn.OPE().setWidth(80));
	}

	protected Department getOrg(final PageParameter pp) {
		return null;
	}

	public static class UserRolesTbl extends AbstractDbTablePagerHandler {

		private final IRoleService rService = orgContext.getRoleService();

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final User user = orgContext.getUserService().getBean(cp.getParameter("accountId"));
			if (user != null) {
				cp.addFormParameter("accountId", user.getId());
				return new IteratorDataQuery<Role>(rService.roles(user));
			}
			return null;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final KVMap kv = new KVMap();
			final Role r = (Role) dataObject;
			kv.add("rolename", rService.toUniqueName(r));
			kv.add("roletext", r.getText());
			kv.add("roletype", r.getRoleType());
			return kv;
		}
	}
}
