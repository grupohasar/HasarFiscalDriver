Modificacion de diseño de aplicacion. Se debe seleccionar el grupo del test, y luego el test.
Se agrega nuevo test para descarga de reporte de AFIP DownloadAfip()

VERSION 0.10.2 de SDK
Se agregan test de percepciones, uno para IVA y otro para IIBB.
Se modifica SDK para que tome el valor del parametro Alicuota del metodo newTribute().
Ahora permite cargar distintas percepciones por ticket (MAXIMO 5). Debe coincidir el valor de alicuota segun el valor de iva del item.


VERSION 0.10.1 de SDK
Se agrega nuevo test Datos_Inicializacion para obtener los datos de inicializacion del impresor
Se agrega test Percepciones, se realiza la carga de tributos mediante newTribute()

VERSION 0.9.0
Clase FiscalItemImpl, ya no implementa interfaz IFiscalItem.
Se corrigió mapeo de tipo de cbte a libreria de 1ra generacion, de tipo de cbte TIQUE a TICKET_C.
Se agrega test para impresion de cbte tipo TIQUE.
Se agrega test para impresion de cbte tipo TIQUE_NOTA_CREDITO_B.
Se modifico SandBox para poder realizar un test de InvoiceBean() por medio de un JSon. Se agrega espacio para entrada de JSon y test.

VERSION 0.8.0
Se corrigio error que no permitia realizar facturas en 2da generacion por red

VERSION 0.7.0
Se agrega metodo getErrorDetail() a ElectronicInvoiceResponse.
Se agrega test para factura C

VERSION 0.6.0
Se elimina test PUT PosNumber. Se reemplazo boolean en metodo newCompany();
Se modifico electronicInvoiceFactory.newCompany("30522211563", "PRUEBA",
                new Subsidiary("sucursal_prueba", "32"),
                new PointOfSales(true, 12, "CAE"),
                new Checkout("123ABC", new PointOfSales(true, 12, "CAE"), 32, null),
                false);
Se agrega nuevo parametro boolean a newCompany(), este controla el impacto al servidor para create y put.
Se modifico electronicInvoice(api, "EmpresaPrueba", sdkAppId)
    Ya no se le pasa usuario y clave de la api.
Se agrega Test FP_Factura_C(). Este funciona solo con configuracion de impresora Monotributista.
Se revierte Toast de FiscalDriverException por pedido del cliente
Se elimina parametro, electronicInvoice("Empresa", sdkAppId)
Se agrega ACK a test FE_FACTURA_B.
Se controla punto de venta sólo por cuit y tipo de punto de venta, debido a una limitacion de la API FE.

VERSION 0.5.0
Se agrega numero de version en la aplicacion.
Modificacion de Checkout("ABC123", new PointOfSales(true, 11, "CAE"), 2, null))
    Ahora admite valor alfanumerico en checkoutNumber.
    Devuelve mensaje detallado en caso de error en electronicInvoiceRegisterCompanyResponse.getStatus()
Se modifico ElectronicInvoiceACKBean
    electronicInvoiceFactory.newElectronicInvoiceACK("1", 1, lastTransactionNumber, "30618829150")
    Retorna Boolean. Previamente realizar factura electronica para obtener lastTransactionNumber.
Se agrega metodo boolean putPosNumber(int number)
    Por ahora dummy retorna true.

VERSION 0.4.25
Modificaciones en el SDK para que no permita crear empresas repetidas. Lo mismo para sucursales, puntos de venta y cajas.
Se modificaron los test del Sandbox para que todos muestren un mensaje de error en el caso que existan.
Se creo nuevo test de Register Company con datos válidos para la prueba.

VERSION 0.4.24
Se agregó un control que valida que ambas versiones(SDK y .aar) tengan la misma versión.
En caso de no coincidir arrojará un mensaje para indicar actualizar las mismas.
Ya no se tendrá que agregar manualmente una jurisdicción por defecto desde su aplicación, ya lo hace el SDK. Ver ejemplo en Sandbox, en el metido initFiscalManager().
Nuevos constructores. Se agregaron constructores con menos parámetros para que puedan usar esos en vez de los originales y ponerle parámetros null o false sin utilidad. Se agregó nuevo Test en Sandbox, FP_FACTURA_A, en el cual se utiliza el nuevo constructor.
"Invalida parameter exception" al querer imprimir un cierreZ en impresora 2gen. 
Se agregó validacion en metodo TipoHabilitacion() para valores incorrectos ya que detectamos (debugeando el SDK mientras corríamos la aplicación de Clover) que se llamaba a ese método luego de realizar un Cierre Z. Se probó desde la aplicación de Clover Fiscal Printer y funcionó correctamente. Igualmente esto no soluciona el problema de raíz ya que sospechamos que se debe estar llamando de forma incorrecta a la función debido que en nuestro Sandbox no tenemos ese error.
Por ese motivo pedimos el fragmento del código de la aplicación de Clover para que podamos revisarlo.