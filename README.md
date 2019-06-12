# HasarFiscalDriver
Aplicación de testing, que hace uso del SDK y una librería .aar

<p align="center">
 <b>Quick Code</b>
 <a href='https://play.google.com/store/apps/details?id=hasar.com.hasarfiscallibraryexampletests'>
  <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='200'/>
 </a>
</p
 
**Acerca de Hasar Driver Fiscal**

Hasar Driver Fiscal, es un componente que integra las funcionalidades de impresoras fiscales Hasar, tanto como de Primera Generación como de Segunda. 
Este componente es un .apk el cuál se integra en dispositivos Android con versión 4.4 KitKat y posteriores.
 
 Hay dos tipos de impresores fiscales:

- 1ra generación
  - Comunicación por puerto serie
- 2da generación
  - Comunicación por USB
  - Comunicación por HTTP

Por otro lado, existe la posibilidad de facturar mediante **Facturación Electronica** y **Percepciones** .

La idea del Driver Fiscal es proveer una interfaz de serivicios comunes, y que el usuario se desentienda de las particularidades del medio de facturación.


**Modelos de Impresoras Compatibles**


Hasta el momento se ha testeado el driver fiscal, con versiones de Android 4.4 y posteriores. La misma se encuentra funcionando correctamente en todos los equipos de Segunda Generación.
En el caso de los equipos de primera generación, hasta el momento, el driver funciona con versión Android 4.4.2 y posteriores.

En la siguiente tabla se detallan los modelos compatibles con el Driver Fiscal.

| Primera Gen   | Segunda Gen   |
| ------------- | ------------- |
| SMH/PT-262F   | SMH/PT-1000 F |
| SMH/PT-272F   | SMH/P 441 F   | 
| SMH/P-320F    | SMH/PT 250 F  |
| SMH/P-321F    |
| SMH/P-322F    |
| SMH/P-330F    |
| SMH/P-340F    |
| SMH/P-425F    |
| SMH/P-435F    |
| SMH/P-441F    |
| SMH/P-614F    |
| SMH/P-615F    |
| SMH/P-715F    |
| SMH/P-950F    |
| SMH/P-1120F   |




### Tips que se deben conocer:

- El objeto que tiene todas las funciones se llama Fiscal Manager
- Por cada función que se quiere llamar del Driver, el Fiscal Manager tiene su función implementada, que toma un bean y un callback. Ejemplo

```Java
        FiscalManager.getInstance().printGenericText(genericTextBean, new ServiceCallback<GenericTextResponse>() {
            @Override
            public void onResult(GenericTextResponse genericTextResponse) {

            }

            @Override
            public void onError(FiscalDriverException e) {

            }
        });
```
- (Casi) todos los beans se crean mediante [Factories](https://en.wikipedia.org/wiki/Factory_method_pattern). Ejemplo:

```Java
Client c = clientFactory.newResponsableInscripto(
                        "GUGLIELMANA, LEONOR MARIA",
                        "9 DE JULIO 125 CHACABUCO",
                        documentFactory.newCUIT("27047916491"))
```
- Los IVAs se manejan con un [Registry](https://martinfowler.com/eaaCatalog/registry.html). Ejemplo

```Java
ivaRegistry.register("Gravado21", 21, TaxConditions.GRAVADO);
ivaRegistry.register("Gravado10.5", 10.5, TaxConditions.GRAVADO);

Iva i = ivaRegistry.get("Gravado21");
```
- La mayoria de los factories son Singletons
- El Fiscal Manager se configura mediante el FiscalManagerConfigurationBuilder. Esta configuracion puede ser:
  - Con primera o segunda generación de impresoras (o ninguna)
    - Si es primera generacion, se puede configurar el modelo o la autodetección
    - Si es segunda generación, se puede conectar por HTTP o USB
  - Con Facturación electronica (o no)


#### Facturación electronica

En caso de que se desee adquirir FE, favor comunicarse con hsventas@hasar.com, para consultas técnicas investigacionydesarrollo@hasar.com

#### Percepciones

En caso de que se desee adquirir Percepciones, favor comunicarse con hsventas@hasar.com, para consultas técnicas investigacionydesarrollo@hasar.com


  
