package hasar.com.hasarfiscallibraryexampletests;

public class GroupType {
    public static final int TESTS_FACTURA = 0;
    public static final int TESTS_HEADER_FOOTER = 1;
    public static final int TESTS_MEDIOS_DE_PAGO = 2;
    public static final int TESTS_PERCEPCIONES = 3;
    public static final int OTRAS_OPERACIONES = 4;

    class Subgroup_TESTS_FACTURA {
        public static final int FACTURA_A = 0;
        public static final int FACTURA_B = 1;
        public static final int FACTURA_C = 2;
        public static final int NOTA_DE_CREDITO_B = 3;
        public static final int TIQUE = 4;
        public static final int NO_CATEGORIZADO = 5;
        public static final int FACTURA_B_DOCUMENTO_ASOCIADO = 6;
    }

    class Subgroup_TESTS_HEADER_FOOTER {
        public static final int HEADER_FACTURA_A = 0;
        public static final int HEADER_FACTURA_B = 1;
        public static final int HEADER_NO_FISCAL = 2;
    }

    class Subgroup_TESTS_MEDIOS_DE_PAGO {
        public static final int MEDIOS_PAGO_4 = 0;
        public static final int MEDIOS_PAGO_5 = 1;
        public static final int MEDIOS_PAGO_6 = 2;
    }

    class Subgroup_TESTS_PERCEPCIONES {
        public static final int IIBB = 0;
        public static final int IVA = 1;
    }

    class Subgroup_OTRAS_OPERACIONES {
        public static final int CIERRE_Z = 0;
        public static final int CANCELAR = 1;
        public static final int SET_CONFIGURACION = 2;
        public static final int GET_CONFIGURACION = 3;
        public static final int DATOS_INICIALIZACION = 4;
        public static final int JSON_TEST = 5;
        public static final int DOWNLOAD_AFIP = 6;
    }

    class Subgroup_FACTURA_ELECTRONICA {
        public static final int FE_A = 0;
        public static final int FE_B = 1;
        public static final int FE_C = 2;
        public static final int FE_ACK = 3;
        public static final int PERCEPCION_FE_A = 4;
        public static final int PERCEPCION_FE_A_2 = 5;
        public static final int AFIP = 6;
        public static final int REGISTER_COMPANY = 7;
    }

}

