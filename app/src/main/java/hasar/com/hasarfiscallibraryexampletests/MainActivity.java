package hasar.com.hasarfiscallibraryexampletests;

import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hasar.fiscal.dataLayer.beans.Checkout;
import com.hasar.fiscal.dataLayer.beans.InscripcionIIBB;
import com.hasar.fiscal.dataLayer.beans.JurisdictionMapper;
import com.hasar.fiscal.dataLayer.beans.Perception;
import com.hasar.fiscal.dataLayer.beans.PointOfSales;
import com.hasar.fiscal.dataLayer.beans.Subsidiary;
import com.hasar.fiscal.dataLayer.beans.TaxException;
import com.hasar.fiscal.dataLayer.beans.Tributes;


import com.hasar.fiscal.dataLayer.beans.TributesModeMapper;
import com.hasar.fiscal.dataLayer.beans.operation.ElectronicInvoiceACKBean;
import com.hasar.fiscal.dataLayer.beans.operation.ElectronicInvoicerRegisterCompanyBean;
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceRegisterCompanyResponse;
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
import com.hasar.fiscal.services.query.InitializationDataQueryService;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.hasar.fiscal.dataLayer.enums.InvoiceTypes.TIQUE;

public class MainActivity extends AppCompatActivity {
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


    //Data for testing

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        generateIVAs();
        final TextView txtVersion = findViewById(R.id.textView2);
        txtVersion.setText("VERSION: " + versionLibrary);

        final Spinner dropdown = findViewById(R.id.spinner1);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.command_array, android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        txtIp = findViewById(R.id.txtIp);
        txtJson = findViewById(R.id.txtJson);
        rbFirst = findViewById(R.id.rbFirstGen);
        rbSecond = findViewById(R.id.rbSecondGen);
        rbElectronic = findViewById(R.id.rbElectronic);


        RadioGroup rbGroup = findViewById(R.id.llPrinterGen);
        rbGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                txtIp.setEnabled(rbSecond.isChecked());
            }
        });

        btnFactura = findViewById(R.id.factura_desconectar);
        btnReintentar = findViewById(R.id.reintentar);

        //btnFactura.setOnClickListener((v) -> this.enviarFacturaConPagoParaDesconectar());
        btnReintentar.setOnClickListener((v) -> {
            Log.d("Estado", "Antes: " + estadoDeImpresion);
            this.reintentar();
            Log.d("Estado", "Despues: " + estadoDeImpresion);

        });

        btnSend = findViewById(R.id.buttonSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                initFiscalManager();


                switch (dropdown.getSelectedItemPosition()) {
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
                    case 8:
                        FP_Factura_A();
                        break;
                    case 9:
                        FP_Factura_B();
                        break;
                    case 10:
                        FP_NDC_B();
                        break;
                    case 11:
                        FP_Factura_C();
                        break;
                    case 12:
                        FP_Tique();
                        break;
                    case 13:
                        Header_Factura_A();
                        break;
                    case 14:
                        Header_Factura_B();
                        break;
                    case 15:
                        Header_No_Fiscal();
                        break;
                    case 16:
                        Tipo_Habilitacion('A');
                        break;
                    case 17:
                        Tipo_Habilitacion('L');
                        break;
                    case 18:
                        Tipo_Habilitacion('M');
                        break;
                    case 19:
                        FP_Cliente_No_Categorizado();
                        break;
                    case 20:
                        Medios_De_Pago(4);
                        break;
                    case 21:
                        Medios_De_Pago(5);
                        break;
                    case 22:
                        Medios_De_Pago(6);
                        break;
                    case 23:
                        Cierre_Z();
                        break;
                    case 24:
                        Cancelar();
                        break;
                    case 25:
                        FP_Json();
                        break;
                    case 26:
                        Datos_Inicializacion();
                        break;
                    case 27:
                        FP_Percepcion();
                        break;
                }
            }
        });
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
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).firstGen(FirstGenerationPrinterModel.P441_201, sdkAppId).build());
            } else if (rbSecond.isChecked()) {
                String ip = txtIp.getText().toString();
                SecondGenerationLocation loc = null;
                if (ip.isEmpty()) {
                    loc = SecondGenerationLocation.USB;
                } else {
                    endpoint = new URL(ip);
                    loc = new SecondGenerationLocation(endpoint);
                }
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).secondGen(loc, sdkAppId).build());
            } else if (rbElectronic.isChecked()) {
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).electronicInvoice("EmpresaPrueba", sdkAppId).build());
            }

        } catch (MalformedURLException e) {
            txtIp.setError("Invalid URL");
            Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), "No hay nada que reintentar", Toast.LENGTH_LONG).show();
            return;
        }

        FiscalManager.getInstance().stateQuery(new ToastOnExceptionServiceCallback<StateQueryResponse>(getApplicationContext()) {
            @Override
            public void onResult(StateQueryResponse stateQueryResponse) {
                if (stateQueryResponse.getFiscalStates().contains(FiscalState.DOCUMENT_OPENED)) {

                    if (estadoDeImpresion.equals("PAGANDO")) {
                        FiscalManager.getInstance().closeInvoice(closeInvoiceDesconectar, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
                            @Override
                            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {

                                estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);
                            }

                            @Override
                            public void onError(FiscalDriverException e) {
                                super.onError(e);
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    } else {
                        FiscalManager.getInstance().cancel(new ToastOnExceptionServiceCallback<Void>(getApplicationContext()) {
                            @Override
                            public void onResult(Void aVoid) {
                                //estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);
                            }

                            @Override
                            public void onError(FiscalDriverException e) {
                                super.onError(e);
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } else {
                    if (estadoDeImpresion.equals("PAGANDO")) {
                        Toast.makeText(getApplicationContext(), "El comprobante ya se imprimio", Toast.LENGTH_LONG).show();
                        estadoDeImpresion = "LIBRE";
                        Log.d("Estado", estadoDeImpresion);

                        return;
                    }
                }


                estadoDeImpresion = "FACTURANDO";
                Log.d("Estado", estadoDeImpresion);

                FiscalManager.getInstance().invoice(facturaDesconectar, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse invoiceResponse) {
                        estadoDeImpresion = "PAGANDO";
                        Log.d("Estado", estadoDeImpresion);

                        FiscalManager.getInstance().closeInvoice(closeInvoiceDesconectar, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
                            @Override
                            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                                estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);
                            }

                            @Override
                            public void onError(FiscalDriverException e) {
                                super.onError(e);
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Pago realizado correctamente", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

               /* FiscalManager.getInstance().cancel(new ToastOnExceptionServiceCallback<Void>(getApplicationContext()) {
                    @Override
                    public void onResult(Void aVoid) {
                        //estadoDeImpresion = "LIBRE";
                        Log.d("Estado", estadoDeImpresion);

                    }
                });*/
            }

        });
    }

    private void executePaymentCash(double amountCash) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment paymentCash = fiscalPaymentFactory.newCashPayment(amountCash, "Efectivo");


        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(paymentCash);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void executeCreditPaymentWithInstallments(double amount, int installment) {
        CloseInvoiceBean closeInvoiceBean = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCreditCardPayment(amount, "Amex").installments(installment);

        //Add the payment to the invoice.
        closeInvoiceBean.getFiscalPayments().add(payment);
        FiscalManager.getInstance().closeInvoice(closeInvoiceBean, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                Toast.makeText(getApplicationContext(), "Payment finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Cierre_Z() {   //  executeZClose
        FiscalManager.getInstance().closeFiscalDayZ(new ToastOnExceptionServiceCallback<CloseFiscalDayZResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseFiscalDayZResponse closeFiscalDayZResponse) {
                Toast.makeText(MainActivity.this, "Close Fiscal Day Z finished.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
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
                        Toast.makeText(getApplicationContext(),
                                builder.toString(),
                                Toast.LENGTH_LONG)
                                .show();
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
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
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
                        Toast.makeText(getApplicationContext(),
                                builder.toString(),
                                Toast.LENGTH_LONG)
                                .show();
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
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
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
                        Toast.makeText(getApplicationContext(),
                                builder.toString(),
                                Toast.LENGTH_LONG)
                                .show();
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
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().electronicInvoiceRegisterCompany(company, new ToastOnExceptionServiceCallback<ElectronicInvoiceRegisterCompanyResponse>(getApplicationContext()) {
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
                Toast.makeText(getApplicationContext(), builder.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(ElectronicInvoiceResponse response) {
                lastTransactionNumber = response.getTransactionNumber();

                ElectronicInvoiceACKBean beanACK = electronicInvoiceFactory.newElectronicInvoiceACK("123ABC", 99, lastTransactionNumber, "30522211563");
                FiscalManager.getInstance().electronicInvoiceACK(beanACK, new ServiceCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean response) {
                        Toast.makeText(getApplicationContext(), response.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException ex) {
                        btnSend.setBackgroundColor(Color.RED);
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void FP_Factura_B() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newDNI("34849766")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 35.72).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("COca lata", "105", 37.57).quantity(1).iva(ivaRegistry.get("Gravado21")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(10000);
                        response.setRoundAdjustment(5);
                        double redondeo = response.getRoundAdjustment();

                        StringBuilder builder = new StringBuilder();
                        builder.append("Factura B OK");
                        builder.append("Get redondeo: " + ((int) redondeo));
                        Toast.makeText(getApplicationContext(), builder, Toast.LENGTH_LONG).show();
                    }


                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void FP_NDC_B() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_NOTA_CREDITO_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newDNI("34849766")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 35.72).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("COca lata", "105", 37.57).quantity(1).iva(ivaRegistry.get("Gravado21")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(10000);
                        response.setRoundAdjustment(5);
                        double redondeo = response.getRoundAdjustment();

                        StringBuilder builder = new StringBuilder();
                        builder.append("Factura B OK");
                        builder.append("Get redondeo: " + ((int) redondeo));
                        Toast.makeText(getApplicationContext(), builder, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().initializationDataQuery(new ToastOnExceptionServiceCallback<RespuestaDatosInicializacion>(getApplicationContext()) {
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
                    Toast.makeText(getApplicationContext(), builder, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FiscalDriverException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        );
    }


    private void FP_Json() {
        String json = txtJson.getText().toString();
        String cleanJson = cleanTextContent(json);
        Gson gson = new Gson();
        InvoiceBean bean = gson.fromJson(cleanJson, InvoiceBean.class);

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "*PRUEBA_OK*", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getApplicationContext(), "**DEMO**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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


        FiscalManager.getInstance().perceptions(bean, new ToastOnExceptionServiceCallback<PerceptionResponse>(getApplicationContext()) {
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
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
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
                        Toast.makeText(getApplicationContext(),
                                builder.toString(),
                                Toast.LENGTH_SHORT)
                                .show();
                        lastTransactionNumber = response.getTransactionNumber();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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


        FiscalManager.getInstance().perceptions(bean, new ToastOnExceptionServiceCallback<PerceptionResponse>(getApplicationContext()) {
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
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
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
                        Toast.makeText(getApplicationContext(),
                                builder.toString(),
                                Toast.LENGTH_SHORT)
                                .show();
                        lastTransactionNumber = response.getTransactionNumber();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getApplicationContext(), "**DEMO**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void FE_Afip_Is_Alive() {      //    testAfipIsAlive
        // Check if afip is alive
        FiscalManager.getInstance().electronicInvoiceAfipAlive(new ToastOnExceptionServiceCallback<Boolean>(getApplicationContext()) {
            @Override
            public void onResult(Boolean response) {
                Toast.makeText(getApplicationContext(), response ? "Alive" : "Dead", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Header_No_Fiscal() {   //  testLinesZone_Ticket_No_Fiscal
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(TIQUE);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA",
                        "CalleSiempreViva 666",
                        documentFactory.newDNI("34859766")));

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, new Text("linea 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(2, new Text("linea 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(3, new Text("linea 3"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(1, new Text("linea 4"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(2, new Text("linea 5"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderTwoZone(3, new Text("linea 6"), StationModes.ESTACION_TICKET);

        bean.setZones(zoneConfigurator.getZones());

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "***PRUEBA_AND***", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void Header_Factura_B() {      //  testLinesZone_Ticket_B
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA",
                        "CalleSiempreViva 666",
                        documentFactory.newNinguno(null)));

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, new Text("linea 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(2, new Text("linea 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(3, new Text("linea 3"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(4, new Text("linea 4"), StationModes.ESTACION_TICKET);

        bean.setZones(zoneConfigurator.getZones());

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "***PRUEBA_AND***", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    /*private void FE_Factura_B_Json() {
        try {
            String json = txtJson.getText().toString();
            Gson gson = new Gson();

            ElectronicInvoiceBeanImpl toRet = gson.fromJson(json, ElectronicInvoiceBeanImpl.class);

            FiscalManager.getInstance().electronicInvoice(toRet, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
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
                            Toast.makeText(getApplicationContext(),
                                    builder.toString(),
                                    Toast.LENGTH_LONG)
                                    .show();
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
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } catch (JsonSyntaxException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }*/

    private void Header_Factura_A() {    //  testLinesZone_Ticket_A
        InvoiceBean bean = new InvoiceBean();

        List<InscripcionIIBB> InscripcionIIBBList = new ArrayList<InscripcionIIBB>();

        //JurisdictionMapper jMapper = new JurisdictionMapper();
        //TributesModeMapper tMapper = new TributesModeMapper();
        //InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, jMapper.JurisdictionCodeMapper(Jurisdictions.SANTACRUZ)));
        //List<TaxException> TaxExceptionList = new ArrayList<TaxException>();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "Unilever",
                        "CalleSiempreVivas 666",
                        documentFactory.newCUIT("30710591071")));


        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, new Text("linea 1"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(2, new Text("linea 2"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(3, new Text("linea 3"), StationModes.ESTACION_TICKET);
        zoneConfigurator.configureHeaderOneZone(4, new Text("linea 4"), StationModes.ESTACION_TICKET);

        bean.setZones(zoneConfigurator.getZones());

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "***PRUEBA_AND***", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void Cancelar() {   //  testCancelar
        FiscalManager.getInstance().cancel(new ToastOnExceptionServiceCallback<Void>(getApplicationContext()) {
            @Override
            public void onResult(Void aVoid) {
                //estadoDeImpresion = "LIBRE";
                Log.d("Estado", estadoDeImpresion);
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Tipo_Habilitacion(char valor) {   //  testTipoHabilitacion
        /* Promise setTipoHabilitacion */
        configuracionImpresor.setTipoHabilitacion(valor);
        configuracionImpresor.setNominatedLimit(5000);
        configuracionImpresor.setNotNominatedLimit(5000);
        FiscalManager.getInstance().configureFiscalPrinter(configuracionImpresor, new ToastOnExceptionServiceCallback<Void>(getApplication()) {
            @Override
            public void onResult(Void aVoid) {
                /*Promise getTipoHabilitacion*/
                configuracionImpresor.getTipoHabilitacion();
                FiscalManager.getInstance().fiscalPrinterConfigurationQuery(new ToastOnExceptionServiceCallback<ConfigureFiscalPrinterBean>(getApplication()) {
                    @Override
                    public void onResult(ConfigureFiscalPrinterBean configureFiscalPrinterBean) {
                        if (configureFiscalPrinterBean.getTipoHabilitacion() == valor)
                            Toast.makeText(getApplicationContext(), "Se establecio correctamente", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Error de tipo habilitacion", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
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

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "***PRUEBA_AND***", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void Medios_De_Pago(int cantMediosPago) {
        configuracionImpresor.getMaxPaymentsCount();
        FiscalManager.getInstance().fiscalPrinterConfigurationQuery(new ToastOnExceptionServiceCallback<ConfigureFiscalPrinterBean>(getApplication()) {
            @Override
            public void onResult(ConfigureFiscalPrinterBean configureFiscalPrinterBean) {
            }

            @Override
            public void onError(FiscalDriverException e) {
                super.onError(e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
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
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000);
                        StringBuilder builder = new StringBuilder();
                        builder.append("Factura B OK");
                        Toast.makeText(getApplicationContext(), builder, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void FP_Percepcion() {
        InvoiceBean bean = new InvoiceBean();
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);
        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR         ", "100", 1000.00).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 50.00).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "102", 500.00).quantity(1).iva(ivaRegistry.get("Gravado21")));
        ArrayList<Tributes> tributeList= new ArrayList<>();
        tributeList.add(tributeFactory.newTribute(TributesModes.PERCEPCION_IIBB, "Tributo", 100.00, 100.0, 100.0));
        bean.setTributes(tributeList);
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getApplicationContext(), "**DEMO**", Toast.LENGTH_LONG).show();
                        sleep();
                        executePaymentTest7(201.12, 200);
                    }
                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

}
