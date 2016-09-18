package net.simpleframework.organization.web.page.mgr;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.mvc.IPageHandler.PageSelector;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.LinkAddCategoryNode;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ui.propeditor.InputComp;
import net.simpleframework.mvc.component.ui.propeditor.PropEditorBean;
import net.simpleframework.mvc.component.ui.propeditor.PropField;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;
import net.simpleframework.organization.IDepartmentService;
import net.simpleframework.organization.IOrganizationContextAware;
import net.simpleframework.organization.bean.Account;
import net.simpleframework.organization.bean.Account.EAccountStatus;
import net.simpleframework.organization.bean.AccountStat;
import net.simpleframework.organization.bean.Department;
import net.simpleframework.organization.bean.Department.EDepartmentType;
import net.simpleframework.organization.web.page.mgr.t1.AccountMgrPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DepartmentCategory extends CategoryBeanAwareHandler<Department>
		implements IOrganizationContextAware {

	@Override
	protected IDepartmentService getBeanService() {
		return _deptService;
	}

	@Override
	public TreeNodes getCategoryTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		final TreeNodes treeNodes = TreeNodes.of();
		if (treeNode == null) {
			final String[] images = new String[] { "/users.png", "/user_online.png",
					"/users_nodept.png", "/dept_root.png" };
			int i = 0;
			for (final int id : new int[] { Account.TYPE_ALL, Account.TYPE_ONLINE,
					Account.TYPE_NO_DEPT, Account.TYPE_DEPT }) {
				String text = $m("AccountMgrPage." + id);
				if (id == Account.TYPE_DEPT) {
					text += "<br />" + new LinkAddCategoryNode();
				}
				final TreeNode treeNode2 = new TreeNode(treeBean, treeNode, text);
				treeNode2.setId(String.valueOf(id));
				treeNode2.setJsClickCallback(
						CategoryTableLCTemplatePage.createTableRefresh("deptId=&type=" + id).toString());
				treeNode2.setImage(images[i++]);
				treeNode2.setPostfixText(getPostfixText(id));
				treeNode2.setContextMenu("none");
				treeNode2.setSelect(id == Account.TYPE_ALL);
				treeNode2.setOpened(id == Account.TYPE_ALL || id == Account.TYPE_DEPT);
				treeNodes.add(treeNode2);
			}
		} else {
			if (treeNode.getId().equals(String.valueOf(Account.TYPE_ALL))) {
				final String[] images = new String[] { "/users_normal.png", "/users_regist.png",
						"/users_locked.png", "/users_delete.png" };
				int i = 0;
				for (final int id : new int[] { Account.TYPE_STATE_NORMAL,
						Account.TYPE_STATE_REGISTRATION, Account.TYPE_STATE_LOCKED,
						Account.TYPE_STATE_DELETE }) {
					final TreeNode treeNode2 = new TreeNode(treeBean, treeNode,
							EAccountStatus.values()[i]);
					treeNode2.setId(String.valueOf(id));
					treeNode2.setImage(images[i++]);
					treeNode2.setJsClickCallback(CategoryTableLCTemplatePage
							.createTableRefresh("deptId=&type=" + id).toString());
					treeNode2.setPostfixText(getPostfixText(id));
					treeNode2.setContextMenu("none");
					treeNodes.add(treeNode2);
				}
			} else if (treeNode.getId().equals(String.valueOf(Account.TYPE_DEPT))) {
				treeNode.setAcceptdrop(true);
				final TreeNodes nodes = super.getCategoryTreenodes(cp, treeBean, null);
				if (nodes != null) {
					for (final TreeNode tn : nodes) {
						tn.setParent(treeNode);
					}
				}
				return nodes;
			} else {
				final Object dataObject = treeNode.getDataObject();
				if (dataObject instanceof Department) {
					final Department dept = (Department) dataObject;
					treeNode.setImage(dept.getDepartmentType() == EDepartmentType.organization
							? "/org.gif" : "/dept.png");
					treeNode.setPostfixText(getPostfixText(dept));
					treeNode.setJsClickCallback(CategoryTableLCTemplatePage
							.createTableRefresh("deptId=" + dept.getId()).toString());
					final TreeNodes nodes = super.getCategoryTreenodes(cp, treeBean, treeNode);
					if (nodes != null) {
						for (final TreeNode tn : nodes) {
							tn.setDynamicLoading(true);
						}
					}
					return nodes;
				}
			}
		}
		return treeNodes.size() > 0 ? treeNodes : null;
	}

	private String getPostfixText(final Object type) {
		int c = 0;
		if (type instanceof Integer) {
			final AccountStat stat = _accountStatService.getAllAccountStat();
			final int iType = (Integer) type;
			if (iType == Account.TYPE_ALL) {
				c = stat.getRnums();
			} else if (iType == Account.TYPE_ONLINE) {
				c = stat.getOnline_nums();
			} else if (iType == Account.TYPE_STATE_NORMAL) {
				c = stat.getState_normal();
			} else if (iType == Account.TYPE_STATE_REGISTRATION) {
				c = stat.getState_registration();
			} else if (iType == Account.TYPE_STATE_LOCKED) {
				c = stat.getState_locked();
			} else if (iType == Account.TYPE_STATE_DELETE) {
				c = stat.getState_delete();
			}
		} else {
			final Department dept = _deptService.getBean(type);
			if (dept != null) {
				AccountStat stat;
				if (dept.getDepartmentType() == EDepartmentType.department) {
					stat = _accountStatService.getDeptAccountStat(type);
				} else {
					stat = _accountStatService.getOrgAccountStat(type);
				}
				c = stat.getRnums();
			}
		}
		return c > 0 ? "(" + c + ")" : null;
	}

	@Override
	public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeBean treeBean,
			final TreeNode treeNode) {
		final Object dept;
		if (treeNode != null && (dept = treeNode.getDataObject()) instanceof Department) {
			treeNode.setImage(((Department) dept).getDepartmentType() == EDepartmentType.organization
					? "/org.gif" : "/dept.png");
		}
		final TreeNodes nodes = super.getCategoryTreenodes(cp, treeBean, treeNode);
		if (nodes != null) {
			for (final TreeNode tn : nodes) {
				tn.setDynamicLoading(true);
			}
		}
		return nodes;
	}

	@Override
	protected void onLoaded_dataBinding(final ComponentParameter cp,
			final Map<String, Object> dataBinding, final PageSelector selector,
			final Department dept) {
		if (dept != null) {
			dataBinding.put("department_type", dept.getDepartmentType());
		}
	}

	@Override
	protected void onSave_setProperties(final ComponentParameter cp, final Department dept,
			final boolean insert) {
		if (insert) {
			dept.setDepartmentType(
					Convert.toEnum(EDepartmentType.class, cp.getParameter("department_type")));
		}
	}

	@Override
	public KVMap categoryEdit_attri(final ComponentParameter cp) {
		return ((KVMap) super.categoryEdit_attri(cp)).add(window_title, $m("AccountMgrPage.9"))
				.add(window_height, 360).add(window_width, 340);
	}

	@Override
	protected AbstractComponentBean categoryEdit_createPropEditor(final ComponentParameter cp) {
		final PropEditorBean editor = (PropEditorBean) super.categoryEdit_createPropEditor(cp);
		final Department t = getBeanService().getBean(cp.getParameter(PARAM_CATEGORY_ID));
		if (t == null) {
			editor.getFormFields().add(1, new PropField($m("DepartmentCategory.0"))
					.addComponents(InputComp.select("department_type", EDepartmentType.class)));
		}
		return editor;
	}

	@Override
	public void setTreeBean(final ComponentParameter cp, final TreeBean treeBean) {
		super.setTreeBean(cp, treeBean);
		treeBean.setImgHome(cp.getCssResourceHomePath(AccountMgrPage.class) + "/images")
				.setDragScroll(".left_c>div");
	}
}
