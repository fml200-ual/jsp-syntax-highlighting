<h1>${usuario.nombre}</h1>
<p>#{sessionScope['carrito'].total}</p>
<a href="/detalle?id=${usuario.id}&modo=${empty modo ? 'simple' : 'full'}">detalle</a>
<p>Suma: ${a + b * 2.5}</p>
