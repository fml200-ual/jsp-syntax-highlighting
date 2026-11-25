<script type="text/javascript">
    // Comentario de línea
    /* Comentario de bloque */
    var total = 0;
    let nombre = '${usuario.nombre}';
    const url = "/api/items?page=" + pagina;
    if (total > 0 && total < 10) {
        console.log(`Hay ${total} elementos`);
    }
    function sumar(a, b) { return a + b; }
</script>
<p>despues del script</p>
<script src="/js/otro.js"></script>
