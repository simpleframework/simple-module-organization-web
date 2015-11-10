package net.simpleframework.organization.web;

import net.simpleframework.organization.login.IAccountSession;
import net.simpleframework.organization.login.LoginObject;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IHttpAccountListener {

	void login(IAccountSession accountSession, LoginObject login);

	void logout(IAccountSession accountSession);
}
