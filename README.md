# SpendLog: Aplicación de Registro de Gastos para Pequeños Negocios

**Luis Miguel Echeverri Vélez** · COM 437 Desarrollo de Aplicaciones Móviles · Saint Leo University

---

## 1. Descripción del Proyecto

SpendLog es una aplicación móvil nativa para Android pensada para pequeños negocios, trabajadores independientes y emprendedores que necesitan registrar sus gastos operativos sin depender de internet, suscripciones de pago ni conocimientos contables. Todo funciona de manera local en el dispositivo mediante SQLite, lo que garantiza privacidad, velocidad y disponibilidad permanente. La propuesta no busca competir con plataformas empresariales complejas, sino resolver un problema concreto y cotidiano, la falta de visibilidad financiera en negocios de pequeña escala.

El mercado global de software de gestión de gastos está proyectado para crecer de 7.64 mil millones de dólares en 2024 a 16.48 mil millones en 2032 (Revill, 2026). Paradójicamente, ese crecimiento está concentrado en soluciones para medianas y grandes empresas. SpendLog apunta al segmento que estas plataformas dejan sin atender.

---

## 2. Exposición del Problema

En mi país, Colombia, y en gran parte de América Latina, la mayoría de los pequeños negocios no llevan registro sistemático de sus gastos. Esa ausencia impide tomar decisiones informadas sobre precios, rentabilidad y flujo de caja. Según Kumar (2026), los negocios que hacen seguimiento de sus gastos tienen un 40% más de probabilidad de ahorrar dinero que aquellos que no lo hacen. La diferencia no está en el conocimiento financiero sino en tener la herramienta correcta a la mano.

Las aplicaciones existentes requieren conectividad constante, registro de tarjetas bancarias o flujos de aprobación corporativa que no corresponden a la realidad de un negocio unipersonal. SpendLog resuelve esto siendo completamente local, sin configuraciones complejas y con un flujo de registro que no toma más de diez segundos.

---

## 3. Plataforma

La aplicación fue desarrollada exclusivamente para Android, usando Android Studio como entorno de desarrollo integrado y Java como lenguaje de programación principal, de acuerdo con los objetivos del curso y el libro de texto de Montero Miguel (2014). La base de datos es SQLite gestionada con SQLiteOpenHelper nativo de Android. La compatibilidad objetivo es Android 8.0 (API 26) en adelante, cubriendo la mayoría de dispositivos activos en el mercado. No se usaron APIs externas de pago, servicios en la nube ni librerías de terceros que requieran conexión a internet para las funciones principales.

---

## 4. Interfaz de Usuario e Interfaz de Administrador

La aplicación está orientada a un usuario único por dispositivo, así que no hay distinción entre roles. La interfaz se organiza en cuatro pantallas principales: tres de ellas accesibles desde un BottomNavigationView de tres pestañas, y la de registro desde el botón flotante del dashboard. La pantalla principal muestra el total del mes desglosado por categoría con indicadores de progreso frente a los límites definidos. La pantalla de registro permite ingresar un gasto con monto, categoría, descripción y fecha. El historial lista los gastos del mes visible con filtros por categoría y flechas para navegar entre meses. La configuración permite crear categorías personalizadas y asignarles límites de gasto. El diseño sigue los lineamientos de Material Design de Android, priorizando la menor cantidad posible de pasos entre pantallas.

---

## 5. Funcionalidad

SpendLog incluye cinco funcionalidades centrales. El registro de gastos permite ingresar monto, categoría, descripción y fecha, almacenando todo localmente en SQLite. La categorización personalizada permite que el usuario defina sus propias categorías según su tipo de negocio, como transporte, insumos o arriendo. El sistema de alertas avisa visualmente en el dashboard cuando el gasto acumulado en una categoría llega al 80% del límite definido, y en rojo cuando lo supera, sin notificaciones push ni servicios externos. El resumen exportable permite compartir un reporte del período directamente por SMS o correo electrónico usando intents nativos de Android. El historial filtrable permite revisar los gastos de un mes o de un rango de fechas elegido a mano, con totales calculados automáticamente, y editar o borrar cualquier gasto ya registrado.

---

## 7. Diseño (Wireframes / Esquemas de Página)

El dashboard muestra un encabezado verde con el total del mes y, debajo, dos tarjetas de resumen en paralelo que muestran el presupuesto total y el disponible. Una tercera tarjeta contiene la lista de categorías activas, con una barra de progreso y el monto gastado por cada una. Un botón flotante (FAB) en la esquina inferior derecha da acceso directo al registro de nuevo gasto. La pantalla de registro tiene campos para monto, descripción, un spinner para la categoría y un DatePicker para la fecha. El historial usa un RecyclerView en orden cronológico descendente con chips de filtro por categoría y flechas para navegar entre meses. La pantalla de configuración muestra una lista editable de categorías con su límite mensual y un interruptor para activar o desactivar las alertas. Los wireframes están publicados en la wiki del repositorio: https://github.com/luismev09/Desarrollo-de-Apps-Moviles/wiki

---

## 8. Registro de Cambios del Proyecto

Esta sección documenta la evolución de SpendLog desde el borrador inicial hasta el estado actual de la implementación. El código está publicado en https://github.com/luismev09/Desarrollo-de-Apps-Moviles

### 8.1 Cambios pasados

**Módulo 1. Definición del alcance.** Se entregó el borrador que constituye las secciones anteriores de este documento. Quedaron fijados el problema que la aplicación resuelve, el usuario objetivo, la plataforma y las cuatro pantallas que tendría. La decisión más importante de esta etapa fue que la aplicación funcionaría completamente sin conexión a internet, porque el usuario al que apunta opera con frecuencia en zonas de conectividad intermitente o costosa. Esa decisión condicionó todo el diseño posterior.

**Módulo 2. Ciclo de vida e intents.** Se definió el manejo del ciclo de vida de las Activities, en particular el comportamiento al girar la pantalla. Se estableció el uso de intents con banderas de control de pila para la navegación entre pantallas, de modo que la barra inferior no acumule instancias duplicadas al navegar entre pestañas.

**Módulo 3. Diseño de la interfaz.** Se elaboró y publicó en la wiki del repositorio el wireframe de las cuatro pantallas en formato de dispositivo Android. Se definió la paleta de colores alrededor del verde oscuro, se resolvió la navegación principal con un BottomNavigationView de tres pestañas y se ubicó el botón flotante de nuevo gasto directamente en el dashboard. Esa decisión no fue estética: el uso más frecuente de la aplicación es registrar un gasto en el momento en que ocurre, de modo que el camino hacia el formulario tenía que ser de un solo toque.

**Módulo 4. Material Design.** Se aplicaron los componentes de Material Design al diseño de las cuatro pantallas: CardView para las tarjetas de resumen y la tarjeta de categorías, FloatingActionButton para el acceso rápido al registro, Chip para los filtros del historial, SwitchMaterial para el interruptor de alertas en la configuración, y ConstraintLayout en el dashboard para distribuir las tarjetas de presupuesto sin necesidad de pesos en LinearLayout anidados.

**Módulo 5. Base de datos y preferencias.** Se definió el esquema con dos tablas, una para gastos y otra para categorías, y se publicó en la wiki una guía de instrucciones sobre el uso de SQLite en Android. Se fijó la arquitectura de la aplicación en Activity más SQLiteOpenHelper más Adapter. Se estableció también el uso de SharedPreferences para persistir la preferencia del interruptor de alertas, que el dashboard consulta antes de mostrar el banner de límite superado.

### 8.2 Cambios actuales

**Módulo 6. SMS y correo.** En este módulo el diseño pasó a ser código funcional. La aplicación compila sin errores, funciona de punta a punta y fue probada en un emulador con Android 16 (API 36).

**Lo que quedó implementado.** La clase DatabaseHelper gestiona las dos tablas del esquema con sus cinco categorías iniciales y diez métodos de consulta y escritura. Las cuatro pantallas están construidas con sus respectivos layouts en XML, incluyendo un layout alternativo para el historial en orientación horizontal, donde el encabezado y el pie se compactan para que el RecyclerView tenga espacio visible. El historial usa un RecyclerView con su adaptador, la clase ExpenseAdapter. La exportación del resumen funciona mediante un Intent nativo de Android que abre el selector del sistema y permite enviar por SMS, correo o mensajería sin declarar ningún permiso adicional. Las preferencias del usuario se guardan con SharedPreferences.

**Decisiones técnicas tomadas durante la construcción.** La exportación del resumen usa Intent.ACTION_SEND en lugar de la clase SmsManager. Enviar mensajes directamente en segundo plano exigiría el permiso SEND_SMS, que Google Play restringe. Con el intent nativo el usuario elige la aplicación de destino y conserva el control de lo que se comparte. Hasta el cierre de este módulo la aplicación no declaraba ningún permiso; el único que declara hoy se agregó en el módulo 7 y se explica más abajo.

La fecha se guarda como texto en formato año, mes y día separados por guiones. Ese formato tiene la propiedad de ordenar igual como cadena de texto que como fecha, así que las consultas pueden filtrar por mes y ordenar cronológicamente sin conversiones intermedias.

Para el nivel de API objetivo se eligió la versión 36, porque desde el 31 de agosto de 2026 Google Play no acepta envíos nuevos por debajo de ese nivel, y la aplicación se publica en el módulo 8.

**Ajuste respecto al borrador.** El borrador original decía que el historial listaría todos los gastos con filtros por categoría y rango de fechas. Al implementarlo apareció una contradicción con el wireframe aprobado, donde el encabezado muestra el mes en curso y la tarjeta del pie dice total de este mes. Se resolvió a favor del wireframe: el historial muestra un mes a la vez, con flechas para navegar entre meses, y los filtros por categoría operan dentro del mes visible. Los gastos de meses anteriores siguen guardados y accesibles navegando hacia atrás.

**Problemas encontrados y corregidos durante las pruebas.** Después de verificar el flujo normal se hizo una segunda ronda buscando activamente errores. Tocar el botón de guardar varias veces seguidas registraba el mismo gasto más de una vez; se corrigió desactivando el botón inmediatamente después de que la validación pasa. Al girar el teléfono, la fecha seleccionada volvía a la de hoy mientras el monto y la descripción sí se conservaban; hubo que guardarla y restaurarla explícitamente al recrearse la pantalla. Los chips de filtro inactivos del historial resultaron ser invisibles por un problema de contraste con Material Design; solo se detectó al revisar una captura de imagen, no leyendo el código. Con el nivel de API 36, el reloj del sistema quedaba encima del título en las cuatro pantallas; hubo que aplicar los márgenes del sistema directamente al encabezado y no a la raíz del layout.


**Módulo 7. Ubicación, gráfico, íconos y sonido.** Este módulo completó la lista de componentes que exige el curso. La aplicación ya usaba botones y arreglos; faltaban sensores, canvas, imágenes y sonido. Los cuatro se agregaron como funciones de la aplicación y no como adornos: cada uno resuelve algo que hacía falta.

**Geoetiquetado de gastos (sensor).** La pantalla de registro tiene ahora un control para adjuntar la ubicación al gasto que se está registrando, que responde a la pregunta de dónde se hizo cada gasto. Es una función opcional: el permiso se pide en el momento en que el usuario toca el botón y no al abrir la pantalla, de modo que el sistema pregunta cuando se entiende para qué. Si el usuario lo niega, el formulario sigue funcionando igual y el gasto se guarda sin coordenadas. La lectura usa LocationManager, la clase de ubicación que ya trae Android, y no FusedLocationProviderClient, porque esa última pertenece a los servicios de Google Play y habría metido una dependencia externa en una aplicación que desde el primer módulo se definió como autónoma. En el historial, los gastos que quedaron geoetiquetados muestran un pin junto a la fecha.

La posición que el sistema tiene guardada como última conocida puede ser de hace horas y de otro sitio, así que no se acepta sin más: se descarta cualquier lectura de más de cinco minutos y se espera una nueva. Cinco minutos es corto para que la posición siga siendo del mismo lugar, porque un gasto se registra donde ocurre, y largo para que un teléfono que acaba de ubicarse no tenga que esperar otra lectura. Si en quince segundos no llega ninguna válida, la pantalla lo dice y el gasto se guarda sin ubicación, igual que cuando se niega el permiso.

**Cambio de esquema con ALTER TABLE.** Guardar la ubicación obligó a agregar dos columnas a la tabla de gastos, latitude y longitude, y a subir la versión de la base de datos de 1 a 2. La versión anterior de onUpgrade borraba las tablas para volver a crearlas, que es lo que hace el ejemplo más repetido de la documentación, y eso habría borrado los gastos que el usuario ya tuviera registrados en el momento de actualizar la aplicación. Se reemplazó por dos sentencias ALTER TABLE ADD COLUMN, que agregan las columnas sin tocar las filas que ya existen. Las dos admiten nulo, porque ni los gastos anteriores al cambio ni los que se guarden sin adjuntar ubicación tienen coordenadas. Se probó actualizando encima de una instalación de la versión anterior que tenía gastos cargados: los tres gastos sobrevivieron, la base quedó en versión 2 y las dos columnas nuevas quedaron en nulo.

**Gráfico de gastos por categoría (canvas).** El dashboard muestra, encima de la lista de categorías, un gráfico de barras horizontales con lo gastado en cada categoría del mes. Es una vista propia, GraficoCategoriasView, que hereda de View y dibuja en su método onDraw con drawRect y drawText; no se usó ninguna librería de gráficos. La barra más larga es la de la categoría que más gastó y las demás se dibujan en proporción a ella, que es la comparación que sirve cuando lo que se quiere ver de un vistazo es en qué se está yendo el dinero. Si el mes no tiene gastos, la vista dibuja un mensaje centrado en vez de quedarse en blanco. Los nombres de categoría que no caben se recortan con puntos suspensivos para que nunca se monten sobre el monto.

**Íconos vectoriales de categoría (imágenes).** Los íconos de categoría eran emojis dentro de un TextView. Un emoji no se dibuja igual en todos los teléfonos porque depende de la fuente que traiga instalada el fabricante. Se reemplazaron por cinco drawables vectoriales, uno por categoría semilla, dibujados con paths y mostrados en un ImageView. Un vector se ve idéntico en cualquier dispositivo y se puede teñir con el color de la aplicación. El cambio tocó las tres pantallas donde aparecen los íconos: el historial, la lista de categorías del dashboard y la de configuración. Las categorías que crea el usuario siguen usando el ícono genérico.

**Sonido de confirmación (sonido).** Guardar un gasto reproduce un tono corto de dos notas, generado como archivo de audio y guardado en res/raw. Se reproduce con MediaPlayer y solo cuando el gasto quedó realmente guardado: los errores de validación, como el monto vacío o en cero, no suenan, porque esas ramas salen del método antes de llegar al sonido. El reproductor se libera en el callback de onCompletion para que cada guardado no deje un MediaPlayer ocupando memoria.

**Alertas de límite con umbral intermedio.** La pantalla de Configuración siempre anunció "Notificar al superar el 80%", pero el dashboard solo avisaba cuando el gasto pasaba el límite completo. Era la única promesa de la interfaz que el código no cumplía y estaba anotada como limitación conocida. Ahora el banner tiene dos estados: entre el 80% y el 100% del límite aparece en amarillo diciendo que la categoría se está acercando, y por encima del 100% aparece en rojo diciendo que lo superó. Por debajo del 80% no aparece nada. Una categoría sin límite definido nunca dispara el banner, porque no hay proporción contra la cual calcular. El interruptor de alertas apaga el aviso entero, no solo el banner: con el interruptor abajo también las filas de la lista y las barras del gráfico vuelven al verde neutro, porque un usuario que apagó las alertas no puede seguir viendo la pantalla pintada de amarillo y rojo.

El banner es uno solo, así que cuando varias categorías disparan alerta a la vez se muestra la más grave, porque haber superado el límite pesa más que estar acercándose; entre dos categorías del mismo estado gana la primera de la consulta, que es la más antigua, y así el banner no salta de una a otra al recargar la pantalla. La fila de la categoría en la lista de abajo se pinta del mismo color que le corresponde al banner, para que no quede una categoría en verde mientras el banner la está nombrando en amarillo.

El porcentaje se calcula dividiendo el gasto entre el límite y comparando el resultado contra 0,8, y no multiplicando el límite por 0,8. Multiplicar mueve el resultado unos decimales y un gasto de exactamente el 80% se quedaba fuera del aviso por ese redondeo; los dos bordes exactos, el del 80% y el del 100%, se probaron uno por uno.

**El resumen exportado dice si está filtrado.** Con un chip de categoría activo, el resumen que se envía por SMS o correo llevaba solo los gastos de esa categoría pero se leía como si fuera el del mes completo. Ahora, cuando hay filtro, el encabezado incluye una línea que dice de qué categoría es; sin filtro no aparece nada nuevo.

**El resto de los íconos.** Con el umbral implementado se convirtieron a vector los dos emojis que quedaban usados como ícono: la campana de la fila de alertas en Configuración y el triángulo de advertencia del banner del dashboard, en el mismo estilo que los cinco íconos de categoría. El del banner cambia de color junto con el estado, amarillo o rojo. Ya no queda ningún emoji dentro de un TextView haciendo de ícono en toda la aplicación.

**Cómo se probó.** La verificación se hizo a mano sobre el emulador con Android 16 (API 36), mirando capturas de pantalla y no solo el volcado de la jerarquía de vistas, porque en el módulo anterior los chips del historial estuvieron dos sesiones invisibles mientras las herramientas seguían reportando su texto. El cambio de esquema se probó de la única forma que sirve: instalando primero la versión anterior, registrando gastos con ella y actualizando encima sin desinstalar. Se comprobó además que una instalación limpia produce exactamente el mismo esquema que una actualizada, para que las dos rutas no queden distintas. Se guardó un gasto negando el permiso y otro concediéndolo, se apagó la ubicación del dispositivo a mitad del formulario, se revisó el gráfico con cero, una y cinco categorías, en vertical y en horizontal, y con un nombre de categoría de setenta caracteres, y se giró la pantalla en cada punto del recorrido. Los tres rangos del banner de alertas se probaron con los bordes exactos, con el gasto en el 79,999%, en el 80% justo, en el 99,999%, en el 100% justo y en el 100,001% del límite, además del caso de dos categorías en estados distintos a la vez, el de una categoría sin límite y el del interruptor apagado. La matriz completa del banner, los tres rangos por las dos posiciones del interruptor, se revisó capturando las seis pantallas.

El descarte de la ubicación vieja se probó aprovechando que el emulador guardaba una lectura de hacía más de ocho horas: al tocar Adjuntar la pantalla se quedó buscando en vez de adjuntarla, y adjuntó en cuanto llegó una lectura nueva. El aviso de que no hay ninguna reciente se comprobó dejando envejecer una lectura por encima de los cinco minutos y esperando a que venciera el plazo: el gasto se guardó sin coordenadas.

**Problemas encontrados y corregidos durante las pruebas.** Tocar dos veces el botón de adjuntar registraba un segundo escucha de ubicación sin dar de baja el primero, que quedaba recibiendo posiciones hasta que muriera el proceso; ahora la búsqueda anterior se corta antes de empezar la nueva. El plazo de espera de quince segundos preguntaba si ya había coordenadas en vez de preguntar si la búsqueda seguía abierta, así que con una ubicación adjuntada antes no cortaba nada y el mensaje se quedaba en "buscando"; ahora mira el escucha. Si una búsqueda fallaba después de haber adjuntado una ubicación, la pantalla decía que no había ubicación pero el gasto se guardaba con las coordenadas viejas; ahora las coordenadas se sueltan al empezar cada búsqueda, de modo que lo que dice la etiqueta y lo que se guarda siempre coinciden. El sonido de confirmación y el aviso de guardado se disparaban sin mirar el resultado del insert, que devuelve menos uno si la fila no se pudo escribir; ahora se comprueba. El pin de ubicación del historial se salía de la fila cuando el nombre de la categoría era largo, porque el texto ocupaba todo el ancho disponible; se resolvió dándole peso y una sola línea al detalle. Y el escucha de ubicación no implementaba onStatusChanged: ese método solo tiene implementación por defecto desde Android 11, así que el compilador no lo exigía, pero en Android 8 y 9, que esta aplicación todavía soporta, el sistema lo llama y la pantalla se habría caído con el formulario lleno.

**Segunda ronda de revisión.** Antes de cerrar el proyecto se hizo una pasada completa buscando fallas en lo que ya existía, sin agregar funciones. Lo que salió y se corrigió:

Los tres campos de dinero eran de tipo decimal, y en Colombia el punto es el separador de miles: escribir 50.000 pensando en cincuenta mil se guardaba como cincuenta pesos. Además, la aplicación muestra los montos sin decimales, así que un gasto de 1.500,60 se veía como 1.501 mientras el total se calculaba con el valor sin redondear y las líneas del resumen exportado no sumaban su propio total. Los tres campos pasaron a aceptar solo dígitos, que es exactamente lo que la aplicación sabe mostrar, y se les puso un tope de nueve dígitos: sin tope, un número absurdamente largo se convertía en infinito y envenenaba todas las sumas.

El diálogo de categoría nueva se cerraba aunque la validación fallara, porque AlertDialog cierra la ventana después de ejecutar el listener del botón pase lo que pase; el aviso de "ingresa un nombre" salía junto con el diálogo desapareciendo y el usuario perdía el límite que ya había escrito. Ahora el botón se conecta después de mostrar el diálogo y solo se cierra cuando el guardado ocurre. El diálogo de límite precargaba el valor truncado en vez de redondeado, así que reabrirlo y confirmar sin tocar nada bajaba el límite guardado.

La tarjeta de Disponible restaba del presupuesto todo el gasto del mes, incluido el de las categorías sin límite, que no forman parte de ese presupuesto: bastaba un gasto grande en una categoría sin límite para que el disponible apareciera en rojo sin que ningún límite se hubiera tocado. Ahora solo se descuenta el gasto de las categorías que sí tienen límite.

El pie del historial decía "Total este mes" incluso navegando a meses anteriores o con un filtro de categoría activo, donde era falso; ahora dice "Total mostrado". Exportar con la lista vacía enviaba un resumen con el encabezado y un total en cero, y ahora avisa en vez de abrir el selector. El nombre de categoría se recortaba en el historial pero se partía en varias líneas en el dashboard y en configuración; ahora se recorta igual en las tres. Tocar dos veces seguidas la fecha, una fila del historial o una categoría abría dos diálogos y solo se cerraba el último al destruirse la pantalla. Y `SystemBars` solo aplicaba el hueco de arriba y el de abajo: en horizontal la barra de navegación se va a un lado y tapaba el borde de la pantalla.

**Prueba en un dispositivo físico.** La aplicación se instaló con `./gradlew installDebug` en un Samsung Galaxy Note 9 (SM-N9600) con Android 10, nivel de API 29, y se recorrió entera. Importaba porque hasta ese momento todo se había probado en un emulador con API 36, cinco versiones de Android por encima, y varias decisiones del proyecto dependen de versiones viejas. Lo que el emulador no podía mostrar: el diálogo de permisos de Android 10 es distinto y ofrece tres opciones en vez de dos cuando ya se negó antes; y sobre todo, con ACCESS_COARSE_LOCATION la lectura funcionó sin lanzar SecurityException, que era el riesgo real de haber elegido el permiso aproximado, porque el proveedor de red responde con permiso aproximado en todas las versiones. La ubicación que devolvió fue la del sitio donde está el teléfono, y quedó guardada en la base junto al gasto. No hubo ni un cierre inesperado en todo el recorrido.

**Editar y borrar, y elegir el período.** Al instalar la aplicación en un teléfono se hizo evidente un problema que las pruebas no habían detectado, porque quien las hacía ya sabía dónde tocar: las dos acciones de mantenimiento existían pero estaban escondidas. Borrar un gasto solo se podía haciendo una pulsación larga sobre la fila, sin que nada lo indicara, y que la fila de una categoría se tocara para cambiar su límite tampoco lo decía ningún texto. Una función que el usuario no encuentra es, en la práctica, una función que no está.

Ahora cada gasto del historial lleva su botón de borrar a la vista, y la lista de categorías de Configuración lleva encima una línea que dice que se tocan para cambiar el límite. La pulsación larga se conservó, porque ya funcionaba así y no estorba.

Además se agregaron dos cosas que de verdad faltaban. La primera es editar un gasto ya registrado: tocar una fila del historial abre la misma pantalla de registro, con los campos cargados, y al guardar actualiza la fila en lugar de crear otra. Se reutilizó la pantalla que ya existía en vez de escribir una nueva, así que el formulario, las validaciones, la fecha y la ubicación se comportan igual en los dos casos.

La segunda es elegir el período que se está viendo. El historial consultaba por mes con un LIKE sobre el texto de la fecha; ahora consulta por rango con un BETWEEN, y un mes es simplemente el rango que va de su día 1 a su último día, de modo que no hay dos caminos distintos en el código. Tocando el período del encabezado se puede saltar a cualquier mes y año, o fijar un rango de fechas a mano; las flechas de mes siguen estando y se ocultan cuando hay un rango puesto, porque ahí no hay un "mes siguiente" al que ir. Como los dos selectores de fecha del rango son idénticos, cada uno se anuncia antes con un aviso que dice si toca la fecha inicial o la final.

La revisión de este cambio encontró ocho fallas que se corrigieron antes de cerrarlo. La más grave la introdujo el propio cambio: al reutilizar la pantalla de registro para editar, la regla de soltar las coordenadas al empezar una búsqueda de ubicación (que era correcta dando de alta un gasto, donde no hay nada que perder) pasaba a borrar las coordenadas que el gasto ya tenía guardadas si la búsqueda fallaba, y el guardado escribía nulo encima. Ahora una búsqueda fallida no toca lo que ya había: se avisa de que no se pudo actualizar y la etiqueta vuelve a mostrar la ubicación que se conserva, de modo que lo que dice la pantalla y lo que se guarda siguen coincidiendo en los dos casos. La segunda: elegir la fecha inicial de un rango y cancelar el segundo selector dejaba la fecha ya escrita en el estado de la pantalla, así que el periodo cambiaba solo la próxima vez que la lista se recargaba, sin que el usuario lo hubiera pedido; ahora las dos fechas se guardan aparte y solo se aplican cuando las dos están confirmadas. Además, un doble toque sobre una fila abría dos veces el editor del mismo gasto, el menú de período no tenía la guarda contra dobles toques que sí tenían los otros diálogos, el aviso del primer selector se quedaba encima del segundo, y el botón de borrar era más pequeño que el mínimo recomendado y estaba pegado al monto.

**El permiso que la aplicación declara.** La aplicación declara un único permiso, ACCESS_COARSE_LOCATION. El criterio fue pedir el permiso mínimo que la función necesita: para saber en qué zona se hizo un gasto no hace falta la ubicación precisa, así que no se declara ACCESS_FINE_LOCATION. El permiso se pide en tiempo de ejecución cuando el usuario toca el botón de adjuntar, y negarlo no bloquea nada: la aplicación sigue funcionando completa y el gasto se guarda sin ubicación. Se declaró además la característica android.hardware.location con required="false", para que la aplicación se siga pudiendo instalar en dispositivos que no tengan hardware de ubicación.

### 8.3 Cambios futuros

**Módulo 8.** Corresponden los servicios en segundo plano y la publicación en Google Play. La prueba en un dispositivo físico ya se hizo y quedó documentada arriba. Queda pendiente preparar las capturas de pantalla para la ficha de la tienda y definir el ícono definitivo de la aplicación.

**Limitaciones conocidas.** Girar la pantalla con un diálogo abierto lo cierra. La aplicación no permite eliminar categorías, y antes de agregar esa función habría que definir qué ocurre con los gastos que ya apuntan a la categoría eliminada. El esquema de la base de datos no declara claves foráneas explícitas; la integridad referencial entre gastos y categorías se mantiene por convención en el código. La ubicación se guarda como par de coordenadas y la aplicación no la muestra en un mapa ni la traduce a una dirección; el historial solo marca con un pin qué gastos la tienen. Una vez adjuntada la ubicación a un gasto, el formulario no ofrece quitarla sin salir de la pantalla. Toda la verificación se hizo de forma manual sobre el emulador, sin pruebas automatizadas.

---

## Referencias

Kumar, M. (2026, 8 de junio). *15 must-have features of expense tracking apps that will make it successful*. RipenApps. https://ripenapps.com/blog/expense-tracking-app-features/

Montero Miguel, R. (2014). *Desarrollo de aplicaciones para Android*. RA-MA Editorial.

Revill, L. (2026). *Best expense tracking apps for small businesses in 2026*. Expensify. https://use.expensify.com/resource-center/guides/best-business-expense-tracking-app

---

# Documentación técnica

## Las cuatro pantallas

| Pantalla | Clase | Cómo se llega |
|---|---|---|
| Dashboard | `DashboardActivity` | pantalla de arranque |
| Registro de gasto | `RegistroActivity` | botón flotante del Dashboard |
| Historial | `HistorialActivity` | barra de navegación inferior |
| Configuración | `ConfiguracionActivity` | barra de navegación inferior |

## Configuración del proyecto

| | |
|---|---|
| Lenguaje | Java 17 |
| `minSdk` / `targetSdk` / `compileSdk` | 26 / 36 / 36 |
| Android Gradle Plugin | 8.13.0 |
| Gradle | 8.13 |
| Base de datos | SQLite con `SQLiteOpenHelper`, versión 2 del esquema |
| Permisos | `ACCESS_COARSE_LOCATION`, el único que declara la aplicación |
| Dependencias | AppCompat, Material Components, ConstraintLayout, RecyclerView y CardView, declaradas en `app/build.gradle` |

La arquitectura es Activity + `SQLiteOpenHelper` + Adapter, con `findViewById`, sin capas intermedias.

## Estructura

```
app/src/main/
├── AndroidManifest.xml          declara ACCESS_COARSE_LOCATION
├── java/com/spendlog/app/
│   ├── DashboardActivity.java
│   ├── RegistroActivity.java
│   ├── HistorialActivity.java
│   ├── ConfiguracionActivity.java
│   ├── adapters/ExpenseAdapter.java
│   ├── database/DatabaseHelper.java
│   ├── models/Category.java
│   ├── models/Expense.java
│   ├── utils/  CategoryIcon, CurrencyFormatter, DateFormatter, SystemBars
│   └── views/GraficoCategoriasView.java   vista propia que dibuja en onDraw
└── res/
    ├── layout/       activity_* , item_* , dialog_*
    ├── layout-land/  activity_historial.xml
    ├── values/       colors, strings, themes, dimens
    ├── drawable/     fondos redondeados e iconos vectoriales
    ├── raw/          sonido_guardado.wav
    └── menu/         menu_navegacion.xml
```

## Base de datos

`SpendLog.db`, versión 2, dos tablas.

**`categories`** — `_id`, `name`, `monthly_limit` (0 significa sin límite)
**`expenses`** — `_id`, `amount`, `category_id`, `description`, `date`, `latitude`, `longitude`

La fecha se guarda como texto en formato `yyyy-MM-dd` porque así ordena y filtra igual como cadena que como fecha: el total de un mes sale con un `LIKE '2026-08%'` y el orden cronológico con un `ORDER BY` de texto.

`latitude` y `longitude` son `REAL` y admiten nulo: adjuntar la ubicación es opcional, así que un gasto sin ella guarda las dos columnas vacías.

Al crear la base se insertan cinco categorías: Transporte, Insumos, Arriendo, Servicios y Otros, todas sin límite.

### Migración de la versión 1 a la 2

`onUpgrade` agrega las dos columnas con `ALTER TABLE ADD COLUMN` y no borra nada:

```java
if (oldVersion < 2) {
    db.execSQL("ALTER TABLE expenses ADD COLUMN latitude REAL");
    db.execSQL("ALTER TABLE expenses ADD COLUMN longitude REAL");
}
```

Actualizar la aplicación sobre una instalación de la versión 1 conserva los gastos y las categorías que el usuario ya tenía; los gastos anteriores quedan con las dos columnas en nulo, que es exactamente lo que significa un gasto sin ubicación.

## Compilar y ejecutar

```bash
./gradlew assembleDebug     # compilar
./gradlew installDebug      # instalar en el dispositivo o emulador conectado
adb devices                 # ver qué hay conectado
```

`local.properties` no está en el repositorio porque es específico de cada máquina. Si falta, hay que crearlo con la ruta del SDK:

```
sdk.dir=/ruta/a/tu/Android/sdk
```
