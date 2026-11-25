<%
    int visitas = 0;
    String saludo = "Hola ${no-es-el}";
    // comentario de linea
    /* comentario de bloque */
    visitas += 1;
    if (visitas > 10) { saludo = "muchas"; }
%>
<p><%= visitas * 2 %></p>
<%!
    private static final long TOTAL = 1000L;
%>
