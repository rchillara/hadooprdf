<%-- 
    Document   : add_dataset
    Created on : May 21, 2010, 1:59:24 AM
    Author     : hadoop
--%>


<%@include file="/WEB-INF/jsp/common/taglibs.jsp" %>
<s:layout-render name="/WEB-INF/jsp/common/layout_main.jsp" title="Add Datasets">
    <s:layout-component name="body">
       <%-- <table>
            <tr>
                <th>DataSet Name</th>
           </tr>
             <c:forEach var="datasetinfo" items="${actionBean.datasets}">
             <tr>
                <td>${datasetinfo.name}</td>
             </tr>
             </c:forEach>
        </table>--%>
        <%--  <d:table name="${actionBean.datasets}" id="contact" requestURI=""
        defaultsort="1" >
            <d:column title="Dataset name" property="name" sortable="true" />
        </d:table>--%>
    </s:layout-component>
</s:layout-render>
