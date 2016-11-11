<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<jsp:include page="/WEB-INF/back/commons/head.jsp"/>
<script type="text/javascript">
$(function(){
	$("#parentId").treeSelect({
		url:"/admin/area/tree.do",
		values:["${query.search['parentId']}"]
	});	 
});
</script>
<title>地区</title>
</head>
<body>
<jsp:include page="/WEB-INF/back/commons/show_message.jsp"/>
<div class="areaTitle">
    <div class="key">地区管理列表</div>
</div>
<s:form action="list.do" method="post" modelAttribute="query">
<div class="areaContent">
	<div class="searchArea">
    	<table border="0" cellspacing="5" cellpadding="0">
         <thead>
          <tr>
          	<%-- <th>地区名称：</th>
            <td>
            	<div class="inputDiv">
            	<s:input path="search['areaName']" />
            	</div>
            </td> --%>
          	<th>地区名称：</th>
            <td>
            <s:select id="parentId" path="search['parentId']"/>
            </td>
          	<th>地区级别：</th>
            <td>
            	<div class="inputDiv">
            	<s:select path="search['areaLevel']">
            	<option value="">---所有---</option>
				<s:option value="0">国家</s:option>
				<s:option value="1">省</s:option>
				<s:option value="2">市</s:option>
				<s:option value="3">区县</s:option>
				<s:option value="4">街道/镇</s:option>
				<s:option value="5">社区/村</s:option>
				</s:select>
            	</div>
            </td>
            <td>
            	<input type="submit" value=" 搜  索 " class="searchAreaSubBtn" />
            </td>
          </tr>
          </thead>
        </table>
  </div>
  <div class="editButtonsArea">
  	<shiro:hasPermission name="admin:area:add">
  	<div class="add"><a href="add.do">新增</a></div>
  	<div class="cope"><a href="javascript:$.href('add.do?id=');">复制</a></div>
  	</shiro:hasPermission>
  	<shiro:hasPermission name="admin:area:view">
  	<div class="look"><a href="javascript:$.href('view.do?id=');">查看</a></div>
  	</shiro:hasPermission>
  	<shiro:hasPermission name="admin:area:alter">
    <div class="edit"><a href="javascript:$.href('alter.do?id=');">修改</a></div>
  	</shiro:hasPermission>
  	<shiro:hasPermission name="admin:area:remove">
    <div class="delete"><a href="javascript:$.remove();">删除</a></div>
  	</shiro:hasPermission>
  </div>
  <div class="listNews">
  	<table width="100%" border="0" cellspacing="0" cellpadding="0" class="listNewsTable">
      <thead>
      <tr>
        <th width="3%" align="center"><input type="checkbox" id="allBox"/></th>
	    <th><span class="ls-sort" pagesort="parentId">上级地区编码</span></th>
	    <th><span class="ls-sort" pagesort="areaName">地区名称</span></th>
	    <th><span class="ls-sort" pagesort="areaCode">地区编码</span></th>
	    <th><span class="ls-sort" pagesort="areaLevel">地区级别</span></th>
        <th width="15%">编辑</th>
      </tr>
      </thead>
      <tbody>
	  <c:forEach var="bean" varStatus="s" items="${pageNav.list}">
	  <tr class="${s.index%2==0?'normal1':'normal2' }">
	    <td align="center"><input type="checkbox" name="id" value="${bean.id}"/></td>
	    <td align="center">
	    	${bean.parentId}
	    </td>
	    <td align="center">
	    	${bean.areaName}
	    </td>
	    <td align="center">
	    	${bean.areaCode}
	    </td>
	    <td align="center">
	    	<c:choose>
        		<c:when test="${bean.areaLevel==0}">国家</c:when>
        		<c:when test="${bean.areaLevel==1}">省</c:when>
        		<c:when test="${bean.areaLevel==2}">市</c:when>
        		<c:when test="${bean.areaLevel==3}">区县</c:when>
        		<c:when test="${bean.areaLevel==4}">街道/镇</c:when>
        		<c:when test="${bean.areaLevel==5}">社区/村</c:when>
        	</c:choose>
	    </td>
	  <td align="center" class="listEditTd">
	  		<shiro:hasPermission name="admin:area:view">
	  		<a class="ls-opt" href="view.do?id=${bean.id}">查看</a>
	  		</shiro:hasPermission>
	  		<shiro:hasPermission name="admin:area:alter">
	    	<a class="ls-opt" href="alter.do?id=${bean.id}">修改</a>
	  		</shiro:hasPermission>
	  		<shiro:hasPermission name="admin:area:remove">
	    	<a class="ls-opt" href="javascript:$.remove('${bean.id}');">删除</a>
	  		</shiro:hasPermission>
	  </td>
	  </tr>
	  </c:forEach>
	  <%-- <c:if test="${fn:length(pageNav.list) le 0}"> 
		<div class="ls-norecord margin-top5">没有相关的数据!!</div>
	  </c:if> --%>
      </tbody>
      <tfoot>
      <tr>
        <td colspan="6">${pageNav.view}</td>
      </tr>
      </tfoot>
    </table>
  </div>
</div>
<div class="pagesort">
<c:forEach var="bean" varStatus="s" items="${query.collates}">
<span>
	<input type="hidden" name="collates[${s.index}].sortField" value="${bean.sortField}"/>
	<input type="hidden" name="collates[${s.index}].sortType" value="${bean.sortType}"/>
</span>
</c:forEach>
</div>
</s:form>
</body>
</html>
