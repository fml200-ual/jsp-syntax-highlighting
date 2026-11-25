<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.time.LocalDate, java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="aui" uri="http://liferay.com/tld/aui" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="liferay-portlet" uri="http://liferay.com/tld/portlet" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>

<%-- ==================== DECLARACIONES ==================== --%>
<%!
    private static final int MAX_NOMBRE = 30;
    private int contador = 0;

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }
%>

<%--
    Comentario JSP de varias líneas.
    Aquí dentro no se evalúa nada: "${noCuenta}" ni <%= tampoco %>.
--%>

<%-- ==================== LÓGICA ==================== --%>
<%
    contador++;
    String usuario = request.getParameter("usuario");
    if (usuario == null || usuario.isBlank()) {
        usuario = "invitado";
    }
    String cierre = "%>";            // un %> dentro de una cadena no cierra el scriptlet
    LocalDate hoy = LocalDate.now();
    // Comentario de línea
    /* Comentario de bloque */
    pageContext.setAttribute("hoy", hoy);
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${titulo} - Panel</title>
    <link rel="stylesheet" href="/o/panel-theme/css/main.css">
    <style>
        .par { background-color: #f5f5f5; }
        .aviso { color: #b00; font-weight: bold; }
    </style>
</head>
<body class="panel" data-usuario="${usuario}" data-vista=resumen>

<header class="cabecera">
    <h1>Hola, <%= usuario %></h1>
    <p>Visitas en esta sesión: <strong><%= contador %></strong> &middot; hoy es <fmt:formatDate value="${fechaHoy}" pattern="dd/MM/yyyy"/></p>
</header>

<%-- ==================== JSTL: condiciones ==================== --%>
<c:if test="${not empty usuario}">
    <p class="bienvenida">Sesión activa para <c:out value="${usuario}"/></p>
</c:if>

<c:choose>
    <c:when test="${fn:length(usuario) > 30}">
        <p class="aviso">El nombre es demasiado largo.</p>
    </c:when>
    <c:when test="${empty listaItems}">
        <p>No hay elementos que mostrar.</p>
    </c:when>
    <c:otherwise>
        <p>Hay ${fn:length(listaItems)} elementos.</p>
    </c:otherwise>
</c:choose>

<%-- ==================== JSTL: tabla y bucles ==================== --%>
<c:set var="total" value="0" scope="page" />
<c:url var="detalleURL" value="/detalle">
    <c:param name="id" value="${itemId}" />
</c:url>
<table class="tabla-datos">
    <thead>
    <tr>
        <th>#</th>
        <th>Nombre</th>
        <th>Precio</th>
        <th>Alta</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="item" items="${listaItems}" varStatus="estado">
        <tr class="${estado.index % 2 == 0 ? 'par' : 'impar'}">
            <td>${estado.count}</td>
            <td>
                <a href="${detalleURL}">${item.nombre}</a>
            </td>
            <td><fmt:formatNumber value="${item.precio}" type="currency" currencySymbol="€"/></td>
            <td><fmt:formatDate value="${item.alta}" pattern="dd/MM/yyyy HH:mm"/></td>
        </tr>
        <c:set var="total" value="${total + item.precio}" />
    </c:forEach>
    </tbody>
    <tfoot>
    <tr>
        <td colspan="2">Total</td>
        <td><fmt:formatNumber value="${total}" type="currency" currencySymbol="€"/></td>
        <td></td>
    </tr>
    </tfoot>
</table>

<%-- ==================== ACCIONES ESTÁNDAR JSP ==================== --%>
<jsp:useBean id="reloj" class="java.util.Date" scope="page" />
<jsp:include page="/WEB-INF/jsp/fragmentos/pie.jsp">
    <jsp:param name="usuario" value="<%= usuario %>" />
</jsp:include>

<%-- ==================== PORTLET / LIFERAY ==================== --%>
<portlet:renderURL var="volverURL">
    <portlet:param name="mvcPath" value="/view.jsp" />
    <portlet:param name="usuario" value="${usuario}" />
</portlet:renderURL>

<liferay-portlet:renderURL var="editarURL" portletName="panel_WAR_panelportlet">
    <liferay-portlet:param name="mvcPath" value="/editar.jsp" />
    <liferay-portlet:param name="id" value="${itemId}" />
</liferay-portlet:renderURL>

<%-- ==================== AUI (Alloy UI) ==================== --%>
<aui:form action="${editarURL}" method="post" name="fmEdicion">
    <aui:fieldset label="Datos del usuario" collapsed="false">
        <aui:input name="nombre" label="Nombre" value="${usuario.nombre}" required="true" />
        <aui:input name="email" type="email" value='${usuario.email}' />
        <aui:input name="edad" type="number" value="${usuario.edad}" />
        <aui:select name="perfil" label="Perfil">
            <aui:option value="admin" selected="${usuario.perfil == 'admin'}">Administrador</aui:option>
            <aui:option value="user" selected="${usuario.perfil eq 'user'}">Usuario</aui:option>
        </aui:select>
        <aui:input name="activo" type="checkbox" checked="${usuario.activo}" />
        <aui:input name="token" type="hidden" value="${token}" />
    </aui:fieldset>
    <aui:button-row>
        <aui:button type="submit" value="Guardar" />
        <aui:button type="cancel" onClick="history.back();" />
    </aui:button-row>
</aui:form>

<%-- ==================== HTML: formulario nativo ==================== --%>
<form action="${volverURL}" method="get" class="filtros" novalidate>
    <label for="q">Buscar</label>
    <input id="q" name="q" type="search" placeholder="Nombre&hellip;" value="${param.q}">
    <input name="pagina" type="hidden" value="1">
    <select name="orden">
        <option value="nombre" selected>Nombre</option>
        <option value="precio">Precio</option>
    </select>
    <button type="submit" disabled>Filtrar</button>
</form>

<liferay-ui:message key="panel.aviso.legal" />

<ul>
    <li>Entidades: &amp; &lt; &gt; &quot; &mdash; &hellip;</li>
    <li>Sin comillas: <span data-rol=admin data-id=${usuario.id}>ok</span></li>
    <li>Vacío: <br/> <hr> <img src="/img/logo.png" alt="logo"></li>
</ul>

<script>
    var hoy = '${hoy}';
    var total = ${total};
    if (total > 0 && total < 10) {
        console.log('pocos elementos: ' + total);
    }
</script>

</body>
</html>
