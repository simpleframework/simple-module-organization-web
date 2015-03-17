package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ado.query.ListDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.organization.AccountStat;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IAccountStatService;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContext;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DepartmentMgrTPage extends AbstractOrgMgrTPage {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"DepartmentMgrTPage_tbl").setShowFilterBar(false).setSort(false)
				.setPagerBarLayout(EPagerBarLayout.none).setContainerId("idDepartmentMgrTPage_tbl")
				.setHandlerClass(DepartmentTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("text", $m("DepartmentMgrTPage.0"))
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("name", $m("DepartmentMgrTPage.1"), 150)
								.setTextAlign(ETextAlign.left))
				.addColumn(
						new TablePagerColumn("parentId", $m("DepartmentMgrTPage.3"), 210)
								.setTextAlign(ETextAlign.left))
				.addColumn(TablePagerColumn.OPE().setWidth(80));

		// 添加部门
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "DepartmentMgrTPage_editPage",
				DepartmentEditPage.class);
		addWindowBean(pp, "DepartmentMgrTPage_editWin", ajaxRequest)
				.setTitle($m("DepartmentMgrTPage.2")).setHeight(320).setWidth(340);

		// 删除账号
		addDeleteAjaxRequest(pp, "DepartmentMgrTPage_delete");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getDepartmentService().delete(ids);
		return new JavascriptForward("$Actions['DepartmentMgrTPage_tbl']();");
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='tbar'>");
		sb.append(ElementList.of(LinkButton.addBtn().setOnclick(
				"$Actions['DepartmentMgrTPage_editWin']();")));
		sb.append("</div>");
		sb.append("<div id='idDepartmentMgrTPage_tbl'>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class DepartmentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return new ListDataQuery<Department>(list(getOrg(cp)));
		}

		private List<Department> list(final Department parent) {
			final List<Department> l = new ArrayList<Department>();
			if (parent != null) {
				final IDataQuery<Department> dq = orgContext.getDepartmentService().queryDepartments(
						parent, EDepartmentType.department);
				Department dept;
				while ((dept = dq.next()) != null) {
					l.add(dept);
					dept.setAttr("_margin", Convert.toInt(parent.getAttr("_margin")) + 1);
					l.addAll(list(dept));
				}
			}
			return l;
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem != null) {
				return null;
			}
			final MenuItems items = MenuItems.of();
			items.add(MenuItem.itemDelete().setOnclick_act("DepartmentMgrTPage_delete", "id"));
			return items;
		}

		private final IAccountStatService sService = orgContext.getAccountStatService();
		private final IDepartmentService dService = orgContext.getDepartmentService();

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Department dept = (Department) dataObject;
			final KVMap data = new KVMap();
			final StringBuilder txt = new StringBuilder();
			for (int i = 0; i < Convert.toInt(dept.getAttr("_margin")); i++) {
				txt.append(i == 0 ? "| --- " : " --- ");
			}
			txt.append(dept.getText());
			final AccountStat stat = sService.getDeptAccountStat(dept);
			final int nums = stat.getNums();
			if (nums > 0) {
				txt.append(" (").append(nums).append(")");
			}
			data.add("text", txt.toString());
			data.add("name", dept.getName());
			final Department parent = dService.getBean(dept.getParentId());
			if (parent != null && parent.getDepartmentType() == EDepartmentType.department) {
				data.add("parentId", SpanElement.grey999(parent.getText()));
			}
			data.add(TablePagerColumn.OPE, toOpeHTML(cp, dept));
			return data;
		}

		protected String toOpeHTML(final ComponentParameter cp, final Department dept) {
			final Object id = dept.getId();
			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.editBtn().setOnclick(
					"$Actions['DepartmentMgrTPage_editWin']('deptId=" + id + "');"));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}
}