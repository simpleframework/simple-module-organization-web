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
import net.simpleframework.mvc.component.ui.pager.TablePagerUtils;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.organization.Account;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.EDepartmentType;
import net.simpleframework.organization.IAccountStatService;
import net.simpleframework.organization.IOrganizationContext;
import net.simpleframework.organization.User;
import net.simpleframework.organization.web.component.userselect.DefaultUserSelectHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DepartmentMgrTPage extends AbstractOrgMgrTPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addTablePagerBean(pp);

		// 添加部门
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "DepartmentMgrTPage_editPage",
				DepartmentEditPage.class);
		addWindowBean(pp, "DepartmentMgrTPage_editWin", ajaxRequest)
				.setTitle($m("DepartmentMgrTPage.2")).setHeight(320).setWidth(340);

		// 删除账号
		addDeleteAjaxRequest(pp, "DepartmentMgrTPage_delete");
		// 移动
		addAjaxRequest(pp, "DepartmentMgrTPage_Move").setHandlerMethod("doMove");

		// 用户选取
		pp.addComponentBean("DepartmentMgrTPage_userSelect", UserSelectBean.class)
				.setShowGroupOpt(false)
				.setShowTreeOpt(false)
				.setMultiple(true)
				.setJsSelectCallback(
						"$Actions['DepartmentMgrTPage_userSelect_OK']('deptId=' + $F('.user_select #deptId') + '&selectIds=' + selects.pluck('id').join(';')); return true;")
				.setPopup(false).setModal(true).setDestroyOnClose(true)
				.setHandlerClass(_UserSelectHandler.class);
		ajaxRequest = addAjaxRequest(pp, "DepartmentMgrTPage_userSelect_OK").setHandlerMethod(
				"doUserSelect");
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"DepartmentMgrTPage_tbl").setSort(false).setPagerBarLayout(EPagerBarLayout.none)
				.setContainerId("idDepartmentMgrTPage_tbl").setHandlerClass(DepartmentTbl.class);
		tablePager
				.addColumn(new TablePagerColumn("text", $m("DepartmentMgrTPage.0")))
				.addColumn(new TablePagerColumn("name", $m("DepartmentMgrTPage.1"), 150))
				.addColumn(
						new TablePagerColumn("parentId", $m("DepartmentMgrTPage.3"), 210)
								.setFilter(false))
				.addColumn(
						new TablePagerColumn("users", $m("DepartmentMgrTPage.4"), 60).setTextAlign(
								ETextAlign.center).setFilter(false))
				.addColumn(TablePagerColumn.OPE().setWidth(150));
		return tablePager;
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		_deptService.delete(ids);
		return new JavascriptForward("$Actions['DepartmentMgrTPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doMove(final ComponentParameter cp) {
		final Department item = _deptService.getBean(cp
				.getParameter(TablePagerUtils.PARAM_MOVE_ROWID));
		final Department item2 = _deptService.getBean(cp
				.getParameter(TablePagerUtils.PARAM_MOVE_ROWID2));
		if (item != null && item2 != null) {
			_deptService.exchange(item, item2,
					Convert.toBool(cp.getParameter(TablePagerUtils.PARAM_MOVE_UP)));
		}
		return new JavascriptForward("$Actions['DepartmentMgrTPage_tbl']();");
	}

	@Transaction(context = IOrganizationContext.class)
	public IForward doUserSelect(final ComponentParameter cp) {
		final Department dept = UserMgrTPage.getDept(cp);
		for (final String id : StringUtils.split(cp.getParameter("selectIds"))) {
			final User user = _userService.getBean(id);
			if (user != null) {
				user.setDepartmentId(dept.getId());
				_userService.update(new String[] { "departmentId" }, user);
			}
		}
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
				final IDataQuery<Department> dq = _deptService.queryDepartments(parent,
						EDepartmentType.department);
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
			items.add(MenuItem.sep());
			items.append(MenuItem
					.of($m("Menu.move"))
					.addChild(
							MenuItem.of($m("Menu.up"), MenuItem.ICON_UP,
									"$pager_action(item).move(true, 'DepartmentMgrTPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.up2"), MenuItem.ICON_UP2,
									"$pager_action(item).move2(true, 'DepartmentMgrTPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.down"), MenuItem.ICON_DOWN,
									"$pager_action(item).move(false, 'DepartmentMgrTPage_Move');"))
					.addChild(
							MenuItem.of($m("Menu.down2"), MenuItem.ICON_DOWN2,
									"$pager_action(item).move2(false, 'DepartmentMgrTPage_Move');")));
			return items;
		}

		private final IAccountStatService sService = orgContext.getAccountStatService();

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
			final Department parent = _deptService.getBean(dept.getParentId());
			if (parent != null && parent.getDepartmentType() == EDepartmentType.department) {
				data.add("parentId", SpanElement.grey777(parent.getText()));
			}
			// String params = ;
			// final String orgid = cp.getParameter("orgId");
			// if (StringUtils.hasText(orgid)) {
			// params += "&orgId=" + orgid;
			// }
			final LinkElement le = new LinkElement(sService.getDeptAccountStat(dept).getNums())
					.setClassName("simple_btn2").setHref(
							getUrl(cp, UserMgrTPage.class, "deptId=" + dept.getId()));
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
			return _userService.queryUsers(_deptService.getOrg(dept), Account.TYPE_NO_DEPT);
		}
	}
}