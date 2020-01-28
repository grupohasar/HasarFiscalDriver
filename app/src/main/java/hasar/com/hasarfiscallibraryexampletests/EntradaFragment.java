package hasar.com.hasarfiscallibraryexampletests;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hasar.fiscal.dataLayer.beans.Checkout;
import com.hasar.fiscal.dataLayer.beans.InscripcionIIBB;
import com.hasar.fiscal.dataLayer.beans.JurisdictionMapper;
import com.hasar.fiscal.dataLayer.beans.Perception;
import com.hasar.fiscal.dataLayer.beans.PointOfSales;
import com.hasar.fiscal.dataLayer.beans.Subsidiary;
import com.hasar.fiscal.dataLayer.beans.TaxException;
import com.hasar.fiscal.dataLayer.beans.Tributes;


import com.hasar.fiscal.dataLayer.beans.TributesModeMapper;
import com.hasar.fiscal.dataLayer.beans.download.DownloadAfipBean;
import com.hasar.fiscal.dataLayer.beans.get.GetDatesRangeByZBean;
import com.hasar.fiscal.dataLayer.beans.get.GetFirstElectronicReportBlockBean;
import com.hasar.fiscal.dataLayer.beans.operation.ElectronicInvoiceACKBean;
import com.hasar.fiscal.dataLayer.beans.operation.ElectronicInvoicerRegisterCompanyBean;
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceRegisterCompanyResponse;
import com.hasar.fiscal.dataLayer.beans.response.GetDatesRangeByZResponse;
import com.hasar.fiscal.dataLayer.beans.response.GetFirstElectronicReportBlockResponse;
import com.hasar.fiscal.dataLayer.beans.response.GetNextElectronicReportBlockResponse;
import com.hasar.fiscal.dataLayer.beans.response.LastDownloadedElectronicAfipReportResponse;
import com.hasar.fiscal.dataLayer.beans.response.RespuestaDatosInicializacion;
import com.hasar.fiscal.dataLayer.beans.response.PerceptionResponse;
import com.hasar.fiscal.dataLayer.enums.Jurisdictions;

import com.hasar.fiscal.dataLayer.beans.FiscalPayment;
import com.hasar.fiscal.dataLayer.beans.Text;
import com.hasar.fiscal.dataLayer.beans.configuration.ConfigureFiscalPrinterBean;
import com.hasar.fiscal.dataLayer.beans.operation.CloseInvoiceBean;
import com.hasar.fiscal.dataLayer.beans.operation.ElectronicInvoiceBean;
import com.hasar.fiscal.dataLayer.beans.operation.InvoiceBean;
import com.hasar.fiscal.dataLayer.beans.response.CloseFiscalDayZResponse;
import com.hasar.fiscal.dataLayer.beans.response.CloseInvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.InvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.StateQueryResponse;
import com.hasar.fiscal.dataLayer.enums.FiscalState;
import com.hasar.fiscal.dataLayer.enums.InvoiceTypes;
import com.hasar.fiscal.dataLayer.enums.PaymentTypes;
import com.hasar.fiscal.dataLayer.enums.ReportTypeAFIP;
import com.hasar.fiscal.dataLayer.enums.StationModes;
import com.hasar.fiscal.dataLayer.enums.TaxConditions;
import com.hasar.fiscal.dataLayer.enums.TributesModes;
import com.hasar.fiscal.dataLayer.factories.ClientFactory;
import com.hasar.fiscal.dataLayer.factories.DiscountsFactory;
import com.hasar.fiscal.dataLayer.factories.DocumentFactory;
import com.hasar.fiscal.dataLayer.factories.ElectronicInvoiceBeanImpl;
import com.hasar.fiscal.dataLayer.factories.ElectronicInvoiceFactory;
import com.hasar.fiscal.dataLayer.factories.FiscalItemFactory;
import com.hasar.fiscal.dataLayer.factories.FiscalPaymentFactory;
import com.hasar.fiscal.dataLayer.factories.IVARegistry;
import com.hasar.fiscal.dataLayer.factories.InscripcionIIBBFactory;
import com.hasar.fiscal.dataLayer.factories.InternalTaxesFactory;
import com.hasar.fiscal.dataLayer.factories.TaxExceptionFactory;
import com.hasar.fiscal.dataLayer.factories.TributeFactory;
import com.hasar.fiscal.dataLayer.factories.ZoneConfigurator;
import com.hasar.fiscal.exceptions.FiscalDriverException;
import com.hasar.fiscal.executioner.Executioner;
import com.hasar.fiscal.fiscalManager.FirstGenerationPrinterModel;
import com.hasar.fiscal.fiscalManager.FiscalManager;
import com.hasar.fiscal.fiscalManager.FiscalManagerConfigurationBuilder;
import com.hasar.fiscal.fiscalManager.SecondGenerationLocation;
import com.hasar.fiscal.services.base.ServiceCallback;
import com.hasar.fiscal.services.get.GetLastDownloadedElectronicAfipReportService;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static com.hasar.fiscal.dataLayer.enums.InvoiceTypes.TIQUE;
import static hasar.com.hasarfiscallibraryexampletests.SalidaFragment.names;

public class EntradaFragment extends Fragment {
    private ZoneConfigurator zoneConfigurator = ZoneConfigurator.getInstance();
    private IVARegistry ivaRegistry;
    private FiscalItemFactory fiscalItemFactory = new FiscalItemFactory();
    private InternalTaxesFactory internalTaxesFactory = new InternalTaxesFactory();
    private ClientFactory clientFactory = new ClientFactory();
    private TributeFactory tributeFactory = new TributeFactory();
    private DocumentFactory documentFactory = new DocumentFactory();
    private FiscalPaymentFactory fiscalPaymentFactory = new FiscalPaymentFactory();
    private DiscountsFactory discountsFactory = new DiscountsFactory("Discount: ");
    private String lastTransactionNumber = "0";
    private InscripcionIIBBFactory inscripcionIIBBFactory = new InscripcionIIBBFactory();
    private Tributes tributes;
    private TaxExceptionFactory taxExceptionFactory = new TaxExceptionFactory();
    private ConfigureFiscalPrinterBean configuracionImpresor = new ConfigureFiscalPrinterBean();
    private String versionLibrary = Executioner.getVersion();
    private ElectronicInvoiceFactory electronicInvoiceFactory;

    private EditText txtIp;
    private EditText txtJson;
    private Button btnSend;
    private Button btnFactura;
    private Button btnReintentar;
    private RadioButton rbFirst;
    private RadioButton rbSecond;
    private RadioButton rbElectronic;

    private InvoiceBean facturaDesconectar;
    private CloseInvoiceBean closeInvoiceDesconectar;
    private String estadoDeImpresion = "LIBRE";
    private ElectronicInvoiceBeanImpl electronicInvoiceBeanImpl;
    private Object Type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrada, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtIp = getView().findViewById(R.id.txtIp);
        txtJson = getView().findViewById(R.id.txtJson);
        rbFirst = getView().findViewById(R.id.rbFirstGen);
        rbSecond = getView().findViewById(R.id.rbSecondGen);
        rbElectronic = getView().findViewById(R.id.rbElectronic);
        btnFactura = getView().findViewById(R.id.factura_desconectar);
        btnReintentar = getView().findViewById(R.id.reintentar);
        btnSend = getView().findViewById(R.id.buttonSend);
        RadioGroup rbGroup = getView().findViewById(R.id.llPrinterGen);
        names = new ArrayList<>();

        generateIVAs();
        final TextView txtVersion = getView().findViewById(R.id.textView2);
        txtVersion.setText("VERSION: " + versionLibrary);
        final Spinner dropdown_group = getView().findViewById(R.id.spinner1);
        final Spinner dropdown_subgroup = getView().findViewById(R.id.spinner2);

        /*LLENO LISTAS SPINNER*/
        llenarSpinnerGroup(dropdown_group); //Lleno lista de test si es FE o FP
        llenarSpinnerSubGroup(dropdown_group, dropdown_subgroup); //Lleno lista de test de subgrupos

        /*LLENO SPINNER SEGUN RADIO BUTTON*/
        rbGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                txtIp.setEnabled(rbSecond.isChecked());

                /*LLENO LISTAS SPINNER*/
                llenarSpinnerGroup(dropdown_group); //Lleno lista de test si es FE o FP
                llenarSpinnerSubGroup(dropdown_group, dropdown_subgroup); //Lleno lista de test de subgrupos
            }
        });

        /*LLENO SUBGRUPO SEGUN SELECCION DE GRUPO*/
        dropdown_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                llenarSpinnerSubGroup(dropdown_group, dropdown_subgroup);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnReintentar.setOnClickListener((v) -> {
            Log.d("Estado", "Antes: " + estadoDeImpresion);
            this.reintentar();
            Log.d("Estado", "Despues: " + estadoDeImpresion);
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            ArrayAdapter<CharSequence> adapter_subgroup;

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                initFiscalManager();

                if (rbElectronic.isChecked()) { //TESTS FE
                    switch (dropdown_group.getSelectedItemPosition()) {
                        case 0:
                            FE_Factura_A();
                            break;
                        case 1:
                            FE_Factura_B();
                            break;
                        case 2:
                            FE_Factura_C();
                            break;
                        case 3:
                            FE_ACK();
                            break;
                        case 4:
                            Percepcion_Factura_A();
                            break;
                        case 5:
                            Percepcion_Factura_A_02();
                            break;
                        case 6:
                            FE_Afip_Is_Alive();
                            break;
                        case 7:
                            FE_Register_Company();
                            break;
                    }
                } else { //TESTS FP
                    switch (dropdown_group.getSelectedItemPosition()) {
                        case 0: //FACTURA
                            switch (dropdown_subgroup.getSelectedItemPosition()) {
                                case 0:
                                    FP_Factura_A();
                                    break;
                                case 1:
                                    FP_Factura_B();
                                    break;
                                case 2:
                                    FP_Factura_C();
                                    break;
                                case 3:
                                    FP_NDC_B();
                                    break;
                                case 4:
                                    FP_Tique();
                                    break;
                                case 5:
                                    FP_Cliente_No_Categorizado();
                                    break;
                            }
                            break;
                        case 1: //HEADER
                            switch (dropdown_subgroup.getSelectedItemPosition()) {
                                case 0:
                                    Header_Factura_A();
                                    break;
                                case 1:
                                    Header_Factura_B();
                                    break;
                                case 2:
                                    Header_No_Fiscal();
                                    break;
                            }
                            break;
                        case 2: //Pagos
                            switch (dropdown_subgroup.getSelectedItemPosition()) {
                                case 0:
                                    Medios_De_Pago(4);
                                    break;
                                case 1:
                                    Medios_De_Pago(5);
                                    break;
                                case 2:
                                    Medios_De_Pago(6);
                                    break;
                            }
                            break;
                        case 3: //Percepciones
                            switch (dropdown_subgroup.getSelectedItemPosition()) {
                                case 0:
                                    FP_Percepcion_IIBB();
                                    break;
                                case 1:
                                    FP_Percepcion_IVA();
                                    break;
                            }
                            break;
                        case 4: //Otras Operaciones
                            switch (dropdown_subgroup.getSelectedItemPosition()) {
                                case 0:
                                    Cierre_Z();
                                    break;
                                case 1:
                                    Cancelar();
                                    break;
                                case 2:
                                    set_Configuracion('A', 8000, 8000);
                                    break;
                                case 3:
                                    get_Configuracion();
                                    break;
                                case 4:
                                    Datos_Inicializacion();
                                    break;
                                case 5:
                                    FP_Json();
                                    break;
                                case 6:
                                    try {
                                        DownloadAfip();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            break;
                    }
                }
            }
        });
    }


    private void llenarSpinnerGroup(Spinner dropdown_group) {
        ArrayAdapter<CharSequence> adapter_group;
        if (rbElectronic.isChecked())
            adapter_group = ArrayAdapter.createFromResource(getContext(), R.array.group_fe, android.R.layout.simple_spinner_dropdown_item);
        else
            adapter_group = ArrayAdapter.createFromResource(getContext(), R.array.group_fp, android.R.layout.simple_spinner_dropdown_item);

        dropdown_group.setAdapter(adapter_group);
    }

    private void llenarSpinnerSubGroup(Spinner dropdown_group, Spinner dropdown_subgroup) {
        if (rbElectronic.isChecked())
            dropdown_subgroup.setAdapter(null);
        else {
            switch (dropdown_group.getSelectedItemPosition()) {
                case 0: //Factura
                    dropdown_subgroup.setAdapter(ArrayAdapter.createFromResource(getContext(), R.array.subgroup_Factura, android.R.layout.simple_spinner_dropdown_item));
                    break;
                case 1: //Factura
                    dropdown_subgroup.setAdapter(ArrayAdapter.createFromResource(getContext(), R.array.subgroup_Header, android.R.layout.simple_spinner_dropdown_item));
                    break;
                case 2: //Factura
                    dropdown_subgroup.setAdapter(ArrayAdapter.createFromResource(getContext(), R.array.subgroup_Pagos, android.R.layout.simple_spinner_dropdown_item));
                    break;
                case 3: //Factura
                    dropdown_subgroup.setAdapter(ArrayAdapter.createFromResource(getContext(), R.array.subgroup_Percepciones, android.R.layout.simple_spinner_dropdown_item));
                    break;
                case 4: //Factura
                    dropdown_subgroup.setAdapter(ArrayAdapter.createFromResource(getContext(), R.array.subgroup_OtrasOperaciones, android.R.layout.simple_spinner_dropdown_item));
                    break;
            }
        }
    }


    private void generateIVAs() {
        ivaRegistry = IVARegistry.getInstance();
        try {
            ivaRegistry.register("Gravado21", 21, TaxConditions.GRAVADO);
            ivaRegistry.register("Gravado10.5", 10.5, TaxConditions.GRAVADO);
            ivaRegistry.register("Gravado0", 0, TaxConditions.GRAVADO);
            ivaRegistry.register("Exento", 0, TaxConditions.EXENTO);
            ivaRegistry.register("NoGravado", 0, TaxConditions.NO_GRAVADO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFiscalManager() {
        URL endpoint;
        try {
            String sdkAppId = getResources().getString(R.string.sdkAppId);    //si da error al compilar dejar variable sdkAppId= ""
            FiscalManager result = FiscalManager.getInstance();
            if (rbFirst.isChecked()) {
                result.setup(FiscalManagerConfigurationBuilder.configure(getContext()).firstGen(FirstGenerationPrinterModel.P441_201, sdkAppId).build());
            } else if (rbSecond.isChecked()) {
                String ip = txtIp.getText().toString();
                SecondGenerationLocation loc = null;
                if (ip.isEmpty()) {
                    loc = SecondGenerationLocation.USB;
                } else {
                    endpoint = new URL(ip);
                    loc = new SecondGenerationLocation(endpoint);
                }
                result.setup(FiscalManagerConfigurationBuilder.configure(getContext()).secondGen(loc, sdkAppId).build());
            } else if (rbElectronic.isChecked()) {
                result.setup(FiscalManagerConfigurationBuilder.configure(getContext()).electronicInvoice("EmpresaPrueba", sdkAppId).build());
            }

        } catch (MalformedURLException e) {
            txtIp.setError("Invalid URL");
            Toast.makeText(getContext(), "Invalid URL", Toast.LENGTH_LONG).show();
            set_Historial("initFiscalManager", "Invalid URL");
        }

    }

    //To fix a known issue in 1rst gens
    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
    }


    private void reintentar() {
        if (estadoDeImpresion == "LIBRE") {
            Toast.makeText(getContext(), "No hay nada que reintentar", Toast.LENGTH_LONG).show();
            return;
        }

        FiscalManager.getInstance().stateQuery(new ToastOnExceptionServiceCallback<StateQueryResponse>(getContext()) {
            @Override
            public void onResult(StateQueryResponse stateQueryResponse) {
                if (stateQueryResponse.getFiscalStates().contains(FiscalState.DOCUMENT_OPENED)) {

                    if (estadoDeImpresion.equals("PAGANDO")) {
                        FiscalManager.getInstance().closeInvoice(closeInvoiceDesconectar, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
                            @Override
                            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {

                                estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);
                            }

                            @Override
                            public void onError(FiscalDriverException e) {
                                super.onError(e);
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    } else {
                        FiscalManager.getInstance().cancel(new ToastOnExceptionServiceCallback<Void>(getContext()) {
                            @Override
                            public void onResult(Void aVoid) {
                                //estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);
                            }

                            @Override
                            public void onError(FiscalDriverException e) {
                                super.onError(e);
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } else {
                    if (estadoDeImpresion.equals("PAGANDO")) {
                        Toast.makeText(getContext(), "El comprobante ya se imprimio", Toast.LENGTH_LONG).show();
                        estadoDeImpresion = "LIBRE";
                        Log.d("Estado", estadoDeImpresion);

                        return;
                    }
                }


                estadoDeImpresion = "FACTURANDO";
                Log.d("Estado", estadoDeImpresion);

                FiscalManager.getInstance().invoice(facturaDesconectar, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse invoiceResponse) {
                        estadoDeImpresion = "PAGANDO";
                        Log.d("Estado", estadoDeImpresion);

                        FiscalManager.getInstance().closeInvoice(closeInvoiceDesconectar, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
                            @Override
                            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                                estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);
                            }

                            @Override
                            public void onError(FiscalDriverException e) {
                                super.onError(e);
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void executePayment(double amount) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Master 1234")
                .installments(4);
        FiscalPayment payment2 = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Visa 1234")
                .installments(5);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(payment);
        closeInvoiceBean.getFiscalPayments().add(payment2);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void execute_Payment_4(double amount) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Master 1234")
                .installments(4);
        FiscalPayment payment2 = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Visa 1234")
                .installments(5);
        FiscalPayment payment3 = fiscalPaymentFactory.newCashPayment(amount / 2, "Cash")
                .installments(6);
        FiscalPayment payment4 = fiscalPaymentFactory.newDebitCardPayment(amount / 2, "Debito Visa 1234")
                .installments(7);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(payment);
        closeInvoiceBean.getFiscalPayments().add(payment2);
        closeInvoiceBean.getFiscalPayments().add(payment3);
        closeInvoiceBean.getFiscalPayments().add(payment4);

        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getContext(), "Pago realizado", Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago 4", "Pago realizado");
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago 4", e.getMessage());

            }
        });
    }

    private void execute_Payment_5(double amount) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Master 1234")
                .installments(1);
        FiscalPayment payment2 = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Visa 1234")
                .installments(2);
        FiscalPayment payment3 = fiscalPaymentFactory.newCashPayment(amount / 2, "Cash")
                .installments(3);
        FiscalPayment payment4 = fiscalPaymentFactory.newDebitCardPayment(amount / 2, "Debito Visa 1234")
                .installments(4);
        FiscalPayment payment5 = fiscalPaymentFactory.newPayment(amount / 2, "Deposito", PaymentTypes.DEPOSITO)
                .installments(5);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(payment);
        closeInvoiceBean.getFiscalPayments().add(payment2);
        closeInvoiceBean.getFiscalPayments().add(payment3);
        closeInvoiceBean.getFiscalPayments().add(payment4);
        closeInvoiceBean.getFiscalPayments().add(payment5);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getContext(), "Pago realizado", Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago 5", "Pago realizado");
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago 5", e.getMessage());
            }
        });
    }

    private void execute_Payment_6(double amount) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Master 1234")
                .installments(1);
        FiscalPayment payment2 = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Visa 1234")
                .installments(2);
        FiscalPayment payment3 = fiscalPaymentFactory.newCashPayment(amount / 2, "Cash")
                .installments(3);
        FiscalPayment payment4 = fiscalPaymentFactory.newDebitCardPayment(amount / 2, "Debito Visa 1234")
                .installments(4);
        FiscalPayment payment5 = fiscalPaymentFactory.newPayment(amount / 2, "Deposito", PaymentTypes.DEPOSITO)
                .installments(5);
        FiscalPayment payment6 = fiscalPaymentFactory.newCreditCardPayment(amount / 2, "Credito Visa 1234")
                .installments(1);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(payment);
        closeInvoiceBean.getFiscalPayments().add(payment2);
        closeInvoiceBean.getFiscalPayments().add(payment3);
        closeInvoiceBean.getFiscalPayments().add(payment4);
        closeInvoiceBean.getFiscalPayments().add(payment5);
        closeInvoiceBean.getFiscalPayments().add(payment6);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getContext(), "Pago realizado", Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago 6", "Pago realizado");
            }

            @Override
            public void onError(FiscalDriverException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago 6", e.getMessage());
            }
        });
    }

    private void executePaymentTest7(double amountCash, double amountcheque) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment paymentCash = fiscalPaymentFactory.newCashPayment(amountCash, "Efectivo");
        FiscalPayment cheque = fiscalPaymentFactory.newPayment(amountcheque, "Cheque", PaymentTypes.CHEQUE);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(paymentCash);
        closeInvoiceBean.getFiscalPayments().add(cheque);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getContext(), "Pago realizado", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void executeCreditPaymentWithInstallments(double amount, int installment) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCreditCardPayment(amount, "Amex").installments(installment);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(payment);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void set_Historial(String nombreTest, String respuesta) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);

        String opcion;
        if (rbElectronic.isChecked())
            opcion = "Electronic Invoice";
        else if (rbFirst.isChecked())
            opcion = "1st Generation";
        else
            opcion = "2nd Generation";

        String salidaList = fecha + " | " + opcion + "\n" + nombreTest + " | " + respuesta;
        names.add(salidaList);
        Collections.reverse(names);
    }

    private void Cierre_Z() {
        FiscalManager.getInstance().closeFiscalDayZ(new ToastOnExceptionServiceCallback<CloseFiscalDayZResponse>(getContext()) {
            @Override
            public void onResult(CloseFiscalDayZResponse closeFiscalDayZResponse) {
                Toast.makeText(getContext(), "Cierre Z finalizado", Toast.LENGTH_LONG).show();
                set_Historial("Cierre Z", "Cierre Z finalizado");
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Cierre Z", e.getMessage());
            }

        });
    }

    private void FE_Factura_A() {
        electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                "123ABC", //88814,
                3,
                documentFactory.newCUIT("30522211563"));

        InvoiceBean bean = new InvoiceBean();
        bean.setInvoiceType(InvoiceTypes.FACTURA_A);
        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR         ", "100", 105.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 53.80));
        ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);

        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("STATUS: " + response.getStatus());
                        builder.append('\n');
                        builder.append("Detalle Error: " + response.getErrorDetail());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
                        builder.append('\n');
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("Total: " + response.getTotal());
                        builder.append('\n');
                        builder.append("Total IVA: " + response.getIVA());
                        builder.append('\n');
                        Toast.makeText(getContext(),
                                builder.toString(),
                                Toast.LENGTH_LONG)
                                .show();
                        set_Historial("Factura Electronica A", builder.toString());
                        lastTransactionNumber = response.getTransactionNumber();

                        //SIEMPRE MANDAR UN ACK LUEGO DE UNA FE!
                        ElectronicInvoiceACKBean beanACK = electronicInvoiceFactory.newElectronicInvoiceACK("123ABC", 99, lastTransactionNumber, "30522211563");
                        FiscalManager.getInstance().electronicInvoiceACK(beanACK, new ServiceCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean response) {
                                // Toast.makeText(getApplicationContext(), response.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(FiscalDriverException ex) {
                                // Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        set_Historial("Factura Electronica A", e.getMessage());
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

    }


    private void FE_Factura_B() {
        electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                "123ABC", //88814,
                3,
                documentFactory.newCUIT("30522211563"));

        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "AvSiempreViva 666",
                        documentFactory.newDNI("34987654")));

        //bean.setEmptyClient();

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
        //Finally, send the invoice to the fiscal printer.
        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("STATUS: " + response.getStatus());
                        builder.append('\n');
                        builder.append("Detalle Error: " + response.getErrorDetail());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
                        builder.append('\n');
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("Total: " + response.getTotal());
                        builder.append('\n');
                        builder.append("Total IVA: " + response.getIVA());
                        builder.append('\n');
                        Toast.makeText(getContext(),
                                builder.toString(),
                                Toast.LENGTH_LONG)
                                .show();
                        set_Historial("Factura Electronica B", builder.toString());
                        lastTransactionNumber = response.getTransactionNumber();

                        //SIEMPRE MANDAR UN ACK LUEGO DE UNA FE!
                        ElectronicInvoiceACKBean beanACK = electronicInvoiceFactory.newElectronicInvoiceACK("123ABC", 99, lastTransactionNumber, "30522211563");
                        FiscalManager.getInstance().electronicInvoiceACK(beanACK, new ServiceCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean response) {
                                //Toast.makeText(getApplicationContext(), response.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(FiscalDriverException ex) {
                                //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        set_Historial("Factura Electronica B", e.getMessage());
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    private void FE_Factura_C() {
        electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                "123ABC", //88814,
                3,
                documentFactory.newCUIT("30522211563"));


        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_C);

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 00.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 00.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));
        ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);

        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("STATUS: " + response.getStatus());
                        builder.append('\n');
                        builder.append("Detalle Error: " + response.getErrorDetail());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
                        builder.append('\n');
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("Total: " + response.getTotal());
                        builder.append('\n');
                        builder.append("Total IVA: " + response.getIVA());
                        builder.append('\n');
                        Toast.makeText(getContext(),
                                builder.toString(),
                                Toast.LENGTH_LONG)
                                .show();
                        set_Historial("Factura Electronica C", builder.toString());
                        lastTransactionNumber = response.getTransactionNumber();

                        //SIEMPRE MANDAR UN ACK LUEGO DE UNA FE!
                        ElectronicInvoiceACKBean beanACK = electronicInvoiceFactory.newElectronicInvoiceACK("123ABC", 99, lastTransactionNumber, "30522211563");
                        FiscalManager.getInstance().electronicInvoiceACK(beanACK, new ServiceCallback<Boolean>() {
                            @Override
                            public void onResult(Boolean response) {
                                //Toast.makeText(getApplicationContext(), response.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(FiscalDriverException ex) {
                                //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        set_Historial("Factura Electronica C", e.getMessage());
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void FE_Register_Company() {
        electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                "123ABC", //88814,
                3,
                documentFactory.newCUIT("30522211563"));

        PointOfSales pos = new PointOfSales(true, 3, "CAE");

        ElectronicInvoicerRegisterCompanyBean company = electronicInvoiceFactory.newCompany("30522211563", "AND",
                new Subsidiary("sucursal_prueba", "99"),
                pos,
                new Checkout("123ABC", pos, 99, "0"),
                false);

        FiscalManager.getInstance().electronicInvoiceRegisterCompany(company, new ToastOnExceptionServiceCallback<ElectronicInvoiceRegisterCompanyResponse>(getContext()) {
            @Override
            public void onResult(ElectronicInvoiceRegisterCompanyResponse resp) {
                StringBuilder builder = new StringBuilder();
                builder.append("STATUS: " + resp.getStatus());
                builder.append('\n');
                builder.append("ERROR: " + resp.getError());
                builder.append('\n');
                builder.append("DATO: " + resp.getExistingId());
                builder.append('\n');
                builder.append("ERROR TYPE: " + resp.getRegisterCompanyErrorType());
                builder.append('\n');
                Toast.makeText(getContext(), builder.toString(), Toast.LENGTH_LONG).show();
                set_Historial("Register Company", builder.toString());
            }

            @Override
            public void onError(FiscalDriverException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Register Company", e.getMessage());
            }
        });

    }

    private void FE_ACK() { //CREO UNA FE PARA QUE ME DEVUELVA EL ULTIMO NUMERO DE TRANSACCION Y PASARSELO AL ACK
        electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                "123ABC", //88814,
                3,
                documentFactory.newCUIT("30522211563"));

        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newNinguno(null)));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
        //Finally, send the invoice to the fiscal printer.
        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getContext()) {
            @Override
            public void onResult(ElectronicInvoiceResponse response) {
                lastTransactionNumber = response.getTransactionNumber();

                ElectronicInvoiceACKBean beanACK = electronicInvoiceFactory.newElectronicInvoiceACK("123ABC", 99, lastTransactionNumber, "30522211563");
                FiscalManager.getInstance().electronicInvoiceACK(beanACK, new ServiceCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean response) {
                        Toast.makeText(getContext(), response.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                        set_Historial("ACK", response.toString().toUpperCase());
                    }

                    @Override
                    public void onError(FiscalDriverException ex) {
                        Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("ACK", ex.getMessage());
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("ACK", e.getMessage());
            }
        });
    }

    private void FP_Factura_A() {
        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681")));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR         ", "100", 105.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 53.80));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "102", 53.80));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getContext(), "**FACTURA A OK**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                        set_Historial("Factura A ", "FACTURA A OK");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Factura A ", e.getMessage());
                    }
                }
        );
    }

    private void FP_Factura_B() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newDNI("34849766")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 20000).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("COca lata", "105", 37.57).quantity(1).iva(ivaRegistry.get("Gravado21")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(10000);
                        response.setRoundAdjustment(5);
                        double redondeo = response.getRoundAdjustment();

                        StringBuilder builder = new StringBuilder();
                        builder.append("Factura B OK");
                        Toast.makeText(getContext(), builder, Toast.LENGTH_LONG).show();
                        set_Historial("Factura B", builder.toString());
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Factura B", e.getMessage());
                    }
                }
        );
    }

    private void FP_NDC_B() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_NOTA_CREDITO_B);

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 20000.72).quantity(1).iva(ivaRegistry.get("Gravado21")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(10000);
                        response.setRoundAdjustment(5);
                        double redondeo = response.getRoundAdjustment();

                        StringBuilder builder = new StringBuilder();
                        builder.append("Nota de Credito B OK");
                        Toast.makeText(getContext(), builder, Toast.LENGTH_LONG).show();
                        set_Historial("Nota de Credito B", builder.toString());
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Nota de Credito B", e.getMessage());
                    }
                }
        );
    }

    private static String cleanTextContent(String text) {
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");

        return text.trim();
    }

    private void Datos_Inicializacion() {
        FiscalManager.getInstance().initializationDataQuery(new ToastOnExceptionServiceCallback<RespuestaDatosInicializacion>(getContext()) {
                                                                @Override
                                                                public void onResult(RespuestaDatosInicializacion response) {
                                                                    StringBuilder builder = new StringBuilder();
                                                                    builder.append('\n');
                                                                    builder.append("CUIT:  " + response.getCUIT());
                                                                    builder.append('\n');
                                                                    builder.append("Razon social:  " + response.getRazonSocial());
                                                                    builder.append('\n');
                                                                    builder.append("Registro:  " + response.getRegistro());
                                                                    builder.append('\n');
                                                                    builder.append("InicioAct:  " + response.getFechaInicioActividades());
                                                                    builder.append('\n');
                                                                    builder.append("InscripcionIIBB:  " + response.getIngBrutos());
                                                                    builder.append('\n');
                                                                    builder.append("POS:  " + response.getNumeroPos());
                                                                    builder.append('\n');
                                                                    builder.append("ResponsabilidadIVA:  " + response.getResponsabilidadIVA());
                                                                    Toast.makeText(getContext(), builder, Toast.LENGTH_LONG).show();
                                                                    set_Historial("Datos Inicializacion", builder.toString());
                                                                }

                                                                @Override
                                                                public void onError(FiscalDriverException e) {
                                                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                                    set_Historial("Datos Inicializacion", e.getMessage());
                                                                }
                                                            }
        );
    }


    private void FP_Json() {
        String json = txtJson.getText().toString();
        String cleanJson = cleanTextContent(json);
        Gson gson = new Gson();
        InvoiceBean bean = gson.fromJson(cleanJson, InvoiceBean.class);

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getContext(), "*PRUEBA_OK*", Toast.LENGTH_LONG).show();
                        set_Historial("Json test FP", "PRUEBA OK");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Json test FP", e.getMessage());
                    }
                }
        );
    }

    private void FP_Factura_C() {
        //Este test solo funciona con le configuracion de impresora y clover: MONOTRIBUTISTA
        InvoiceBean bean = new InvoiceBean();
        bean.setInvoiceType(InvoiceTypes.FACTURA_C);
        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681")));
        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR         ", "100", 105.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 53.80));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "102", 53.80));
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getContext(), "**FACTURA C OK**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                        set_Historial("Factura C", "FACTURA C OK");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Factura C", e.getMessage());
                    }
                }
        );
    }

    private void Percepcion_Factura_A() {
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        List<InscripcionIIBB> InscripcionIIBBList = new ArrayList<InscripcionIIBB>();
        JurisdictionMapper jMapper = new JurisdictionMapper();
        TributesModeMapper tMapper = new TributesModeMapper();

        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, jMapper.JurisdictionCodeMapper(Jurisdictions.BUENOSAIRES)));

        List<TaxException> TaxExceptionList = new ArrayList<TaxException>();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "Unilever",
                        "CalleSiempreVivas 666",
                        documentFactory.newCUIT("30710591071"), TaxExceptionList, InscripcionIIBBList, true, false, false));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 10000).quantity(5).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Te green hills", "104", 20000).quantity(12).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("mamamamaTaragui", "119", 144.8975).quantity(1).iva(ivaRegistry.get("Gravado21")));


        FiscalManager.getInstance().perceptions(bean, new ToastOnExceptionServiceCallback<PerceptionResponse>(getContext()) {
            @Override
            public void onResult(PerceptionResponse perceptionResponse) {
                List<Perception> perceptionList;
                perceptionList = perceptionResponse.getPerceptionList();

                ArrayList<Tributes> tributesToPrint = new ArrayList<>();
                if (perceptionList != null) {
                    for (Perception perception : perceptionList) {

                        tributesToPrint.add(tributeFactory.newTribute(tMapper.Map(perception.getDescripcionAbreviaturaField()), perception.getDescripcionAbreviaturaField(), perception.getMontoBaseCalculoField(), perception.getMontoPercepcionField(), perception.getAlicuotaField()));
                    }
                    bean.setTributes(tributesToPrint);
                }

                electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                        "123ABC", //88814,
                        3,
                        documentFactory.newCUIT("30522211563"));
                ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("STATUS: " + response.getStatus());
                        builder.append('\n');
                        builder.append("Detalle Error: " + response.getErrorDetail());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
                        builder.append('\n');
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("Total: " + response.getTotal());
                        builder.append('\n');
                        builder.append("Total IVA: " + response.getIVA());
                        builder.append('\n');
                        Toast.makeText(getContext(),
                                builder.toString(),
                                Toast.LENGTH_SHORT)
                                .show();
                        set_Historial("Percepcion Factura A", builder.toString());
                        lastTransactionNumber = response.getTransactionNumber();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Percepcion Factura A", e.getMessage());
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Percepcion Factura A", e.getMessage());
            }
        });
    }


    private void Percepcion_Factura_A_02() {          //testElectronicInvoice06
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        List<InscripcionIIBB> InscripcionIIBBList = new ArrayList<InscripcionIIBB>();
        JurisdictionMapper jMapper = new JurisdictionMapper();
        TributesModeMapper tMapper = new TributesModeMapper();

        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, jMapper.JurisdictionCodeMapper(Jurisdictions.SANTACRUZ)));

        List<TaxException> TaxExceptionList = new ArrayList<TaxException>();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "Unilever",
                        "CalleSiempreVivas 666",
                        documentFactory.newCUIT("30710591071"), TaxExceptionList, InscripcionIIBBList, true, false, true));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Mate Taragui", "103", 119.752066).quantity(30).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Cebolla", "104", 30.679).quantity(5).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Durazno", "105", 54.208).quantity(12).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "119", 117.647).quantity(30).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Te Green Hills", "106", 104.96).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Kesitas", "107", 29.942149).quantity(6).iva(ivaRegistry.get("Gravado21")));


        FiscalManager.getInstance().perceptions(bean, new ToastOnExceptionServiceCallback<PerceptionResponse>(getContext()) {
            @Override
            public void onResult(PerceptionResponse perceptionResponse) {
                List<Perception> perceptionList;
                perceptionList = perceptionResponse.getPerceptionList();

                ArrayList<Tributes> tributesToPrint = new ArrayList<>();
                if (perceptionList != null) {
                    for (Perception perception : perceptionList) {

                        tributesToPrint.add(tributeFactory.newTribute(tMapper.Map(perception.getDescripcionAbreviaturaField()), perception.getDescripcionAbreviaturaField(), perception.getMontoBaseCalculoField(), perception.getMontoPercepcionField(), perception.getAlicuotaField()));
                    }
                    bean.setTributes(tributesToPrint);
                }
                electronicInvoiceFactory = new ElectronicInvoiceFactory(99,
                        "123ABC", //88814,
                        3,
                        documentFactory.newCUIT("30522211563"));
                ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
                //Finally, send the invoice to the fiscal printer.
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("STATUS: " + response.getStatus());
                        builder.append('\n');
                        builder.append("Detalle Error: " + response.getErrorDetail());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
                        builder.append('\n');
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("Total: " + response.getTotal());
                        builder.append('\n');
                        builder.append("Total IVA: " + response.getIVA());
                        builder.append('\n');
                        Toast.makeText(getContext(),
                                builder.toString(),
                                Toast.LENGTH_SHORT)
                                .show();
                        lastTransactionNumber = response.getTransactionNumber();
                        set_Historial("Percepcion Factura A", builder.toString());
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Percepcion Factura A 02", e.getMessage());
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Percepcion Factura 02", e.getMessage());
            }
        });
    }

    private void FE_Afip_Is_Alive() {      //    testAfipIsAlive
        // Check if afip is alive
        FiscalManager.getInstance().electronicInvoiceAfipAlive(new ToastOnExceptionServiceCallback<Boolean>(getContext()) {
            @Override
            public void onResult(Boolean response) {
                Toast.makeText(getContext(), response ? "Alive" : "Dead", Toast.LENGTH_SHORT).show();
                set_Historial("AFIP ", response ? "Alive" : "Dead");
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("AFIP ", e.getMessage());
            }
        });
    }

    private void Header_No_Fiscal() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(TIQUE);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA",
                        "CalleSiempreViva 666",
                        documentFactory.newDNI("34859766")));

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, new Text("HEADER ONE 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(2, new Text("HEADER ONE 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(3, new Text("HEADER ONE 3"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(1, new Text("HEADER TWO 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(2, new Text("HEADER TWO 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(3, new Text("HEADER TWO 3"), StationModes.ESTACION_TICKET);

        zoneConfigurator.configureTailOneZone(1, new Text("TAIL ONE 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(2, new Text("TAIL ONE 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(3, new Text("TAIL ONE 3") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(4, new Text("TAIL ONE 4") , StationModes.ESTACION_TICKET);

        zoneConfigurator.configureTailTwoZone(1, new Text("TAIL TWO 1") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(2, new Text("TAIL TWO 2") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(3, new Text("TAIL TWO 3") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(4, new Text("TAIL TWO 4") , StationModes.ESTACION_TICKET);

        bean.setZones(zoneConfigurator.getZones());

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getContext(), "**PRUEBA HEADER OK**", Toast.LENGTH_LONG).show();
                        set_Historial("Header No Fiscal", "**PRUEBA HEADER OK**");

                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Header No Fiscal", e.getMessage());
                    }
                }
        );
    }

    private void Header_Factura_B() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA",
                        "CalleSiempreViva 666",
                        documentFactory.newDNI("34859766")));

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, new Text("HEADER ONE 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(2, new Text("HEADER ONE 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(3, new Text("HEADER ONE 3"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(1, new Text("HEADER TWO 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(2, new Text("HEADER TWO 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(3, new Text("HEADER TWO 3"), StationModes.ESTACION_TICKET);

        zoneConfigurator.configureTailOneZone(1, new Text("TAIL ONE 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(2, new Text("TAIL ONE 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(3, new Text("TAIL ONE 3") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(4, new Text("TAIL ONE 4") , StationModes.ESTACION_TICKET);

        zoneConfigurator.configureTailTwoZone(1, new Text("TAIL TWO 1") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(2, new Text("TAIL TWO 2") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(3, new Text("TAIL TWO 3") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(4, new Text("TAIL TWO 4") , StationModes.ESTACION_TICKET);

        bean.setZones(zoneConfigurator.getZones());

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getContext(), "**PRUEBA HEADER OK**", Toast.LENGTH_LONG).show();
                        set_Historial("Header Factura B", "**PRUEBA HEADER OK**");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Header Factura B", e.getMessage());
                    }
                }
        );
    }

    private void Header_Factura_A() {    //  testLinesZone_Ticket_A
        InvoiceBean bean = new InvoiceBean();
        List<InscripcionIIBB> InscripcionIIBBList = new ArrayList<InscripcionIIBB>();
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "Unilever",
                        "CalleSiempreVivas 666",
                        documentFactory.newCUIT("30710591071")));


        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, new Text("HEADER ONE 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(2, new Text("HEADER ONE 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(3, new Text("HEADER ONE 3"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(1, new Text("HEADER TWO 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(2, new Text("HEADER TWO 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(3, new Text("HEADER TWO 3"), StationModes.ESTACION_TICKET);

        zoneConfigurator.configureTailOneZone(1, new Text("TAIL ONE 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(2, new Text("TAIL ONE 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(3, new Text("TAIL ONE 3") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailOneZone(4, new Text("TAIL ONE 4") , StationModes.ESTACION_TICKET);

        zoneConfigurator.configureTailTwoZone(1, new Text("TAIL TWO 1") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(2, new Text("TAIL TWO 2") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(3, new Text("TAIL TWO 3") , StationModes.ESTACION_TICKET);
        zoneConfigurator.configureTailTwoZone(4, new Text("TAIL TWO 4") , StationModes.ESTACION_TICKET);

        bean.setZones(zoneConfigurator.getZones());

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getContext(), "**PRUEBA HEADER OK**", Toast.LENGTH_LONG).show();
                        set_Historial("Header Factura A", "**PRUEBA HEADER OK**");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Header Factura A", e.getMessage());
                    }
                }
        );
    }

    private void Cancelar() {   //  testCancelar
        FiscalManager.getInstance().cancel(new ToastOnExceptionServiceCallback<Void>(getContext()) {
            @Override
            public void onResult(Void aVoid) {
                //estadoDeImpresion = "LIBRE";
                Log.d("Estado", estadoDeImpresion);
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Cancelar", e.getMessage());
            }
        });
    }

    private void set_Configuracion(char tipoHabiltiacion, int nominatedLimit, int notNomiatedLimit) {
        configuracionImpresor.setTipoHabilitacion(tipoHabiltiacion);
        configuracionImpresor.setNominatedLimit(nominatedLimit);
        configuracionImpresor.setNotNominatedLimit(notNomiatedLimit);
        FiscalManager.getInstance().configureFiscalPrinter(configuracionImpresor, new ToastOnExceptionServiceCallback<Void>(getContext()) {
            @Override
            public void onResult(Void aVoid) {
                /*Promise getTipoHabilitacion*/

                Toast.makeText(getContext(), "Set OK", Toast.LENGTH_LONG).show();
                set_Historial("Setear configuracion", "Set OK");
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Setear configuracion", e.getMessage());
            }
        });
    }


    private void get_Configuracion() {
        FiscalManager.getInstance().fiscalPrinterConfigurationQuery(new ToastOnExceptionServiceCallback<ConfigureFiscalPrinterBean>(getContext()) {
            @Override
            public void onResult(ConfigureFiscalPrinterBean configureFiscalPrinterBean) {
                StringBuilder builder = new StringBuilder();
                builder.append("Nomitated limit: " + configureFiscalPrinterBean.getNominatedLimit());
                builder.append('\n');
                builder.append("No nomitated limit: " + configureFiscalPrinterBean.getNotNominatedLimit());
                builder.append('\n');
                builder.append("Tipo habilitacion: " + configureFiscalPrinterBean.getTipoHabilitacion());
                builder.append('\n');

                Toast.makeText(getContext(), builder.toString(), Toast.LENGTH_LONG).show();
                set_Historial("Obtener configuracion", builder.toString());
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Obtener configuracion", e.getMessage());
            }
        });
    }


    private void FP_Cliente_No_Categorizado() {
        InvoiceBean bean = new InvoiceBean();
        List<InscripcionIIBB> InscripcionIIBBList = new ArrayList<InscripcionIIBB>();
        JurisdictionMapper jMapper = new JurisdictionMapper();
        TributesModeMapper tMapper = new TributesModeMapper();

        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, jMapper.JurisdictionCodeMapper(Jurisdictions.SANTACRUZ)));

        List<TaxException> TaxExceptionList = new ArrayList<TaxException>();
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newNoCategorizado(
                        "Unilever",
                        "CalleSiempreVivas 666", TaxExceptionList, InscripcionIIBBList, true, false, true));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 4550).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getContext(), "**PRUEBA OK**", Toast.LENGTH_LONG).show();
                        set_Historial("No Categorizado", "PRUEBA OK");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("No Categorizado", e.getMessage());
                    }
                }
        );
    }

    private void Medios_De_Pago(int cantMediosPago) {
        configuracionImpresor.getMaxPaymentsCount();
        FiscalManager.getInstance().fiscalPrinterConfigurationQuery(new ToastOnExceptionServiceCallback<ConfigureFiscalPrinterBean>(getContext()) {
            @Override
            public void onResult(ConfigureFiscalPrinterBean configureFiscalPrinterBean) {
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Medios de Pago", e.getMessage());
            }
        });


        InvoiceBean bean = new InvoiceBean();
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA",
                        "CalleSiempreViva 666",
                        documentFactory.newNinguno(null)));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        switch (cantMediosPago) {
                            case 4:
                                execute_Payment_4(1000);
                                break;
                            case 5:
                                execute_Payment_5(1000);
                                break;
                            case 6:
                                execute_Payment_6(1000);
                                break;
                        }
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Medios de Pago", e.getMessage());
                    }
                }


        );
    }

    private void FP_Tique() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newDNI("34849766")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 35.72).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("COca lata", "105", 37.57).quantity(1).iva(ivaRegistry.get("Gravado21")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000);
                        StringBuilder builder = new StringBuilder();
                        builder.append("TIQUE  OK");
                        Toast.makeText(getContext(), builder, Toast.LENGTH_LONG).show();
                        set_Historial("FP Tique", "TIQUE  OK");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("FP Tique", e.getMessage());
                    }
                }
        );
    }

    private void FP_Percepcion_IVA() {
        InvoiceBean bean = new InvoiceBean();
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);
        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR", "100", 5000.00).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 5000.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        ArrayList<Tributes> tributeList = new ArrayList<>();
        tributeList.add(tributeFactory.newTribute(TributesModes.PERCEPCION_IVA, "Percepcion 21", 5000, 150, 21.00));
        tributeList.add(tributeFactory.newTribute(TributesModes.PERCEPCION_IVA, "Percepcion 10.5", 5000, 75, 10.50));
        bean.setTributes(tributeList);

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getContext(), "**FACTURA A OK**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                        set_Historial("Percepciones FP IVA", "**FACTURA A OK**");

                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Percepciones FP IVA", e.getMessage());
                    }
                }
        );
    }

    private void FP_Percepcion_IIBB() {
        InvoiceBean bean = new InvoiceBean();
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);
        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR         ", "100", 1000).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 5000).quantity(1).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "102", 500).quantity(1).iva(ivaRegistry.get("Gravado0")));
        ArrayList<Tributes> tributeList = new ArrayList<>();
        tributeList.add(tributeFactory.newTribute(TributesModes.PERCEPCION_IIBB, "Percepcion 21", 1000, 100.00, 21.00));
        tributeList.add(tributeFactory.newTribute(TributesModes.PERCEPCION_IIBB, "Percepcion 10.5", 5000, 100.00, 10.50));
        tributeList.add(tributeFactory.newTribute(TributesModes.PERCEPCION_IIBB, "Percepcion 0", 500, 100.00, 0.00));
        bean.setTributes(tributeList);
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getContext(), "**FACTURA A OK**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                        set_Historial("Percepciones FP IIBB", "**FACTURA A OK**");
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        set_Historial("Percepciones FP IIBB", e.getMessage());
                    }
                }
        );
    }

    private void DownloadAfip() throws ParseException {
        DownloadAfipBean afipBean = new DownloadAfipBean();
        int requestCode = 0;
        String path;

        Date initDate = new SimpleDateFormat("yyyyMMdd").parse("20190101");
        Date endDate = new SimpleDateFormat("yyyyMMdd").parse("20191231");

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        }
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/afip.zip";

        afipBean.setFrom(initDate);
        afipBean.setTo(endDate);
        afipBean.setPath(path);

        FiscalManager.getInstance().downloadAfip(afipBean, new ToastOnExceptionServiceCallback<String>(getContext()) {
            @Override
            public void onResult(String respuesta) {
                Toast.makeText(getContext(), "Descarga en: " + respuesta, Toast.LENGTH_LONG).show();
                set_Historial("Download Af + ip", "Descarga en: " + respuesta);
            }

            @Override
            public void onError(FiscalDriverException e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                set_Historial("Download Afip", e.getMessage());
            }
        });

    }

}


