<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="net.simpleframework.organization.web.component.roleselect.RoleSelectUtils"%>
<%@ page import="net.simpleframework.mvc.component.ComponentParameter"%>
<%@ page import="net.simpleframework.mvc.component.ui.dictionary.DictionaryRender"%>
<%
	final ComponentParameter nCP = RoleSelectUtils.get(request,
			response);
	final String hashId = nCP.hashId();
	final String name = (String) nCP.getComponentName();
%>
<div class="role_select">
  <form>
    <input type="hidden" name="<%=RoleSelectUtils.BEAN_ID%>" value="<%=hashId%>" />
  </form>
  <div class="tb">
    <span class="icon"></span> <a class="chart"
      onclick="var act=$Actions['<%=name%>_chart']; act.a = this; act();"><%=RoleSelectUtils.getRoleChart(nCP)%></a>
  </div>
  <div class="ct">
    <div id="container_<%=hashId%>"></div>
  </div>
  <div class="bottom">
    <div style="float: right;">
      <input type="button" value="#(Button.Cancel)" onclick="$Actions['<%=name%>'].close();" />
    </div>
    <div style="float: left;"><%=DictionaryRender.getActions(nCP)%></div>
  </div>
</div>
<script type="text/javascript">
  function selected_<%=name%>(branch, ev) {
    var selects = $tree_getSelects($Actions['<%=name%>_tree'].tree, branch, ev);
    if (selects && selects.length > 0) {
      <%=DictionaryRender.genSelectCallback(nCP, "selects")%>
    }
  }
  
  $ready(function() {
    var tp = $("container_<%=hashId%>").up();
    var w = $Actions['<%=name%>'].window;
    w.content.setStyle("overflow:hidden;");
    var s = function() {
      var h = w.getSize(true).height - tp.previous().getHeight()
          - tp.next().getHeight() - 8; // padding
      tp.setStyle('height: ' + h + 'px;');
    };
    s();
    w.observe("resize:ended", s);
  });
</script>
