package net.simpleframework.organization.web;

import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.mvc.IMVCContextVar;
import net.simpleframework.organization.IOrganizationContext;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IOrganizationWebContext extends IOrganizationContext, IMVCContextVar {

	/**
	 * 得到日志的引用
	 * 
	 * @return
	 */
	IModuleRef getLogRef();

	OrganizationUrlsFactory getUrlsFactory();
}
