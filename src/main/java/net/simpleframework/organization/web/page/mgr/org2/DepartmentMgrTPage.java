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
import net.simpleframework.organization.AccountStat;
import net.simpleframework.organization.Department;
import net.simpleframework.organization.Department.EDepartmentType;
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

		pp.addImportJavascript(DepartmentMgrTPage.class, "/js/dept_tbl.js");

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
				"DepartmentMgrTPage_tbl", DepartmentTbl.class).setSort(false)
				.setPagerBarLayout(EPagerBarLayout.none)
				.setJsLoadedCallback("DepartmentMgrTPage.jsLoaded();")
				.setContainerId("idDepartmentMgrTPage_tbl");
		tablePager
				.addColumn(new TablePagerColumn("text", $m("DepartmentMgrTPage.0")))
				.addColumn(new TablePagerColumn("name", $m("DepartmentMgrTPage.1"), 150))
				.addColumn(
						new TablePagerColumn("parentId", $m("DepartmentMgrTPage.3"), 210)
								.setFilter(false))
				.addColumn(
						new TablePagerColumn("users", $m("DepartmentMgrTPage.4"), 60).setTextAlign(
								ETextAlign.center).setFilter(false))
				.addColumn(TablePagerColumn.OPE(120).setTextAlign(ETextAlign.left));
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
		_deptService.exchange(TablePagerUtils.getExchangeBeans(cp, _deptService));
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
		sb.append("<div class='tbar clearfix '>");
		sb.append(" <div class='left'>");
		sb.append(LinkButton.addBtn().corner()
				.setOnclick("$Actions['DepartmentMgrTPage_editWin']();"));
		sb.append(" </div>");
		sb.append(" <div class='right'>");
		sb.append(new SpanElement().setClassName("DepartmentMgrTPage_plus")
				.setTitle($m("DepartmentMgrTPage.6")).setOnclick("DepartmentMgrTPage.toggleAll()"));
		sb.append(new SpanElement().setClassName("DepartmentMgrTPage_minus")
				.setTitle($m("DepartmentMgrTPage.7")).setOnclick("DepartmentMgrTPage.toggleAll(true)"));
		sb.append(" </div>");
		sb.append("</div>");
		sb.append("<div id='idDepartmentMgrTPage_tbl'>");
		sb.append("</div>");
		return sb.toString();
	}

	static String[] L_COLORs = new String[] { "#765", "#876", "#987" };

	public static class DepartmentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final Department org = getOrg2(cp);
			final List<Department> list = list(org);
			list.add(0, org);
			return new ListDataQuery<Department>(list);
		}

		private List<Department> list(final Department parent) {
			final List<Department> l = new ArrayList<Department>();
			if (parent != null) {
				final IDataQuery<Department> dq = _deptService.queryDepartments(parent,
						EDepartmentType.department);
				Department dept;
				while ((dept = dq.next()) != null) {
					l.add(dept);
					dept.setAttr("_lev", Convert.toInt(parent.getAttr("_lev")) + 1);
					final List<Department> list2 = list(dept);
					dept.setAttr("_leaf", list2.size() == 0);
					l.addAll(list2);
				}
			}
			return l;
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final Department dept = (Department) dataObject;
			final KVMap data = new KVMap();
			final StringBuilder txt = new StringBuilder();
			if (dept.getDepartmentType() == EDepartmentType.organization) {
				txt.append(new SpanElement(dept.getText()).setStrong(true));
			} else {
				final boolean leaf = Convert.toBool(dept.getAttr("_leaf"));
				if (!leaf) {
					txt.append("<img class='toggle' style='' src=\"").append(
							cp.getCssResourceHomePath(DepartmentMgrTPage.class));
					txt.append("/images/p_toggle.png\" />");
				} else {
					txt.append("<span class='imgToggle'></span>");
				}
				txt.append(dept.getText());
			}
			final int lev = Convert.toInt(dept.getAttr("_lev"));
			final SpanElement tEle = new SpanElement(txt).setStyle("margin-left: "
					+ (lev == 1 ? 10 : lev * 15) + "px");
			if (lev > 1) {
				tEle.setColor(L_COLORs[lev - 2]);
			}

			data.add("text", tEle).add("name", dept.getName());
			final Department parent = _deptService.getBean(dept.getParentId());
			if (parent != null && parent.getDepartmentType() == EDepartmentType.department) {
				data.add("parentId", SpanElement.color777(parent.getText()));
			}

			final AccountStat stat = _accountStatService.getDeptAccountStat(dept);
			final LinkElement le = LinkElement.style2(stat.getNums() - stat.getState_delete())
					.setHref(getUrl(cp, UserMgrTPage.class, "deptId=" + dept.getId()));
			data.add("users", le);
			data.add(TablePagerColumn.OPE, toOpeHTML(cp, dept));
			return data;
		}

		@Override
		protected Map<String, Object> getRowAttributes(final ComponentParameter cp,
				final Object dataObject) {
			final Department dept = (Department) dataObject;
			return new KVMap().add("parentid", dept.getParentId());
		}

		protected String toOpeHTML(final ComponentParameter cp, final Department dept) {
			final Object id = dept.getId();
			final StringBuilder sb = new StringBuilder();
			sb.append(ButtonElement.addBtn().setOnclick(
					"$Actions['DepartmentMgrTPage_editWin']('parentId=" + id + "');"));
			sb.append(SpanElement.SPACE);
			sb.append(ButtonElement.editBtn().setOnclick(
					"$Actions['DepartmentMgrTPage_editWin']('deptId=" + id + "');"));
			if (dept.getDepartmentType() == EDepartmentType.department) {
				sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
			}
			return sb.toString();
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			if (menuItem != null) {
				return null;
			}
			final MenuItems items = MenuItems.of();
			items.append(MenuItem.of($m("DepartmentMgrTPage.5")).setOnclick_act(
					"DepartmentMgrTPage_userSelect", "deptId"));
			items.append(MenuItem.sep());
			items.append(MenuItem.itemEdit().setOnclick_act("DepartmentMgrTPage_editWin", "deptId"));
			items.append(MenuItem.itemDelete().setOnclick_act("DepartmentMgrTPage_delete", "id"));
			items.append(MenuItem.sep());
			// 移动菜单
			items.append(MenuItem.TBL_MOVE_UP("DepartmentMgrTPage_Move"));
			items.append(MenuItem.TBL_MOVE_UP2("DepartmentMgrTPage_Move"));
			items.append(MenuItem.TBL_MOVE_DOWN("DepartmentMgrTPage_Move"));
			items.append(MenuItem.TBL_MOVE_DOWN2("DepartmentMgrTPage_Move"));
			return items;
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