package net.simpleframework.organization.web.component.deptselect;

import static net.simpleframework.common.I18n.$m;

import java.util.Collection;

import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.common.Convert;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.dictionary.AbstractDictionaryHandler;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean.DictionaryTreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.organization.IDepartment;
import net.simpleframework.organization.IOrganizationContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class DefaultDeptSelectHandler extends AbstractDictionaryHandler implements
		IDeptSelectHandle, IOrganizationContextAware {

	@Override
	public Collection<? extends IDepartment> getDepartments(final ComponentParameter cp,
			final TreeBean treeBean, final IDepartment parent) {
		return DataQueryUtils.toList(context.getDepartmentService().queryChildren(parent));
	}

	@Override
	protected ElementList getRightElements(final ComponentParameter cp) {
		if (Convert.toBool(cp.getBeanProperty("multiple"))) {
			final String componentName = ((DictionaryTreeBean) ((DeptSelectBean) cp.componentBean)
					.getDictionaryTypeBean()).getRef();
			return ElementList.of(new Checkbox("idDefaultDeptSelectHandler_cb",
					$m("DefaultDeptSelectHandler.0")).setOnclick("$Actions['" + componentName
					+ "'].checkAll(this.checked);"));
		}
		return null;
	}
}
