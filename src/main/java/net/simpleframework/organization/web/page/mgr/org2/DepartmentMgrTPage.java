package net.simpleframework.organization.web.page.mgr.org2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.simpleframework.ado.query.DataQueryUtils;
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
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ext.userselect.UserSelectBean;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IAccountStatService;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.web.component.userselect.DefaultUserSelectHandler;

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
				.addColumn(new TablePagerColumn("text", $m("DepartmentMgrTPage.0")))
				.addColumn(new TablePagerColumn("name", $m("DepartmentMgrTPage.1"), 150))
				.addColumn(new TablePagerColumn("parentId", $m("DepartmentMgrTPage.3"), 210))
				.addColumn(
						new TablePagerColumn("users", $m("DepartmentMgrTPage.4"), 60)
								.setTextAlign(ETextAlign.center))
				.addColumn(TablePagerColumn.OPE().setWidth(150));

		// 添加部门
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "DepartmentMgrTPage_editPage",
				DepartmentEditPage.class);
		addWindowBean(pp, "DepartmentMgrTPage_editWin", ajaxRequest)
				.setTitle($m("DepartmentMgrTPage.2")).setHeight(320).setWidth(340);

		// 删除账号
		addDeleteAjaxRequest(pp, "DepartmentMgrTPage_delete");

		// 用户选取
		pp.addComponentBean("DepartmentMgrTPage_userSelect", UserSelectBean.class)
				.setShowGroupOpt(false)
				.setShowTreeOpt(false)
				.setMultiple(true)
				.setJsSelectCallback(
						"$Actions['DepartmentMgrTPage_userSelect_OK']('selectIds='); return true;")
				.setPopup(false).setModal(true).setDestroyOnClose(true)
				.setHandlerClass(_UserSelectHandler.class);
		ajaxRequest = addAjaxRequest(pp, "DepartmentMgrTPage_userSelect_OK").setHandlerMethod(
				"doUserSelect");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		orgContext.getDepartmentService().delete(ids);
		return new JavascriptForward("$Actions['DepartmentMgrTPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUserSelect(final ComponentParameter cp) {
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
			return new ListDataQuery<Department>(list(getOrg2(cp)));
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

		private static String[] L_COLORs = new String[] { "#333", "#666", "#999" };

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Department dept = (Department) dataObject;
			final KVMap data = new KVMap();
			final StringBuilder txt = new StringBuilder();
			final int margin = Convert.toInt(dept.getAttr("_margin"));
			for (int i = 0; i < margin; i++) {
				txt.append(i == 0 ? "| --- " : " --- ");
			}
			txt.append(dept.getText());
			data.add("text", new SpanElement(txt).setColor(L_COLORs[Math.min(margin - 1, 2)]));
			data.add("name", dept.getName());
			final Department parent = dService.getBean(dept.getParentId());
			if (parent != null && parent.getDepartmentType() == EDepartmentType.department) {
				data.add("parentId", SpanElement.grey777(parent.getText()));
			}
			final LinkElement le = new LinkElement(sService.getDeptAccountStat(dept).getNums())
					.setClassName("simple_btn2").setHref(
							uFactory.getUrl(cp, UserMgrTPage.class, "deptId=" + dept.getId()));
			data.add("users", le);
			data.add(TablePagerColumn.OPE, toOpeHTML(cp, dept));
			return data;
		}

		protected String toOpeHTML(final ComponentParameter cp, final Department dept) {
			final Object id = dept.getId();
			final StringBuilder sb = new StringBuilder();
			sb.append(new ButtonElement($m("DepartmentMgrTPage.5"))
					.setOnclick("$Actions['DepartmentMgrTPage_userSelect']('deptId=" + id + "');"));
			sb.append(SpanElement.SPACE);
			sb.append(ButtonElement.editBtn().setOnclick(
					"$Actions['DepartmentMgrTPage_editWin']('deptId=" + id + "');"));
			sb.append(SpanElement.SPACE).append(AbstractTablePagerSchema.IMG_DOWNMENU);
			return sb.toString();
		}
	}

	public static class _UserSelectHandler extends DefaultUserSelectHandler {
		@Override
		public Map<String, Object> getFormParameters(final ComponentParameter cp) {
			final KVMap kv = new KVMap();
			final Department dept = UserMgrTPage.getDept(cp);
			if (dept != null) {
				kv.add("deptId", dept.getId());
			}
			return kv;
		}

		@Override
		public IDataQuery<?> getUsers(final ComponentParameter cp) {
			final Department dept = UserMgrTPage.getDept(cp);
			if (dept == null) {
				return DataQueryUtils.nullQuery();
			}
			return orgContext.getUserService().queryUsers(
					orgContext.getDepartmentService().getOrg(dept), Account.TYPE_NO_DEPT);
		}
	}
}