package hasar.com.hasarfiscallibraryexampletests;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hasar.fiscal.dataLayer.beans.AssociatedDocumentBean;
import com.hasar.fiscal.dataLayer.beans.Checkout;
import com.hasar.fiscal.dataLayer.beans.Discounts;
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
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceACKResponse;
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceRegisterCompanyResponse;
import com.hasar.fiscal.dataLayer.beans.response.PerceptionResponse;
import com.hasar.fiscal.dataLayer.enums.Jurisdictions;

import com.hasar.fiscal.dataLayer.beans.FiscalPayment;
import com.hasar.fiscal.dataLayer.beans.FiscalText;
import com.hasar.fiscal.dataLayer.beans.Text;
import com.hasar.fiscal.dataLayer.beans.configuration.ConfigureFiscalPrinterBean;
import com.hasar.fiscal.dataLayer.beans.download.DownloadAfipBean;
import com.hasar.fiscal.dataLayer.beans.operation.CloseInvoiceBean;
import com.hasar.fiscal.dataLayer.beans.operation.ElectronicInvoiceBean;
import com.hasar.fiscal.dataLayer.beans.operation.GenericText;
import com.hasar.fiscal.dataLayer.beans.operation.GenericTextBean;
import com.hasar.fiscal.dataLayer.beans.operation.InvoiceBean;
import com.hasar.fiscal.dataLayer.beans.operation.ReportZByDateBean;
import com.hasar.fiscal.dataLayer.beans.operation.ReportZByZNumberBean;
import com.hasar.fiscal.dataLayer.beans.query.ElectronicInvoiceQueryBean;
import com.hasar.fiscal.dataLayer.beans.response.CloseFiscalDayZResponse;
import com.hasar.fiscal.dataLayer.beans.response.CloseInvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceQueryResponse;
import com.hasar.fiscal.dataLayer.beans.response.ElectronicInvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.GenericTextResponse;
import com.hasar.fiscal.dataLayer.beans.response.InvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.StateQueryResponse;
import com.hasar.fiscal.dataLayer.beans.response.LastDownloadedElectronicAfipReportResponse;
import com.hasar.fiscal.dataLayer.enums.AuditReportModes;
import com.hasar.fiscal.dataLayer.enums.FiscalState;
import com.hasar.fiscal.dataLayer.enums.InvoiceTypes;
import com.hasar.fiscal.dataLayer.enums.PaymentTypes;
import com.hasar.fiscal.dataLayer.enums.PrinterState;
import com.hasar.fiscal.dataLayer.enums.StationModes;
import com.hasar.fiscal.dataLayer.enums.TaxConditions;
import com.hasar.fiscal.dataLayer.enums.TributesModes;
import com.hasar.fiscal.dataLayer.factories.ClientFactory;
import com.hasar.fiscal.dataLayer.factories.DiscountsFactory;
import com.hasar.fiscal.dataLayer.factories.DocumentFactory;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    private String lastTransactionNumber = "";
    private InscripcionIIBBFactory inscripcionIIBBFactory = new InscripcionIIBBFactory();
    private Tributes tributes;
    private TaxExceptionFactory taxExceptionFactory = new TaxExceptionFactory();
    private ConfigureFiscalPrinterBean configuracionImpresor = new ConfigureFiscalPrinterBean();
   // private String versionLibrary = Executioner.getInstance();

    //Data for testing

    private ElectronicInvoiceFactory electronicInvoiceFactory = new ElectronicInvoiceFactory(1,
            "1",
            12,
            documentFactory.newCUIT("30618829150"));

    private EditText txtIp;
    private Button btnSend;
    private Button btnFactura;
    private Button btnReintentar;
    private RadioButton rbFirst;
    private RadioButton rbSecond;
    private RadioButton rbElectronic;

    private InvoiceBean facturaDesconectar;
    private CloseInvoiceBean closeInvoiceDesconectar;
    private String estadoDeImpresion = "LIBRE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        generateIVAs();
        //final TextView txtVersion = findViewById(R.id.textView2);
        //txtVersion.setText("VERSION: "+ versionLibrary);

        final Spinner dropdown = findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.command_array, android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        txtIp = findViewById(R.id.txtIp);
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
                        FE_Factura_B();
                        break;
                    case 1:
                        Percepcion_Factura_A();
                        break;
                    case 2:
                        Percepcion_Factura_A_02();
                        break;
                    case 3:
                        FE_Afip_Is_Alive();
                        break;
                    case 4:
                        FP_FACTURA_A();
                        break;
                    case 5:
                        FP_Factura_B();
                        break;
                    case 6:
                        FP_Factura_C();
                        break;
                    case 7:
                        Header_Factura_A();
                        break;
                    case 8:
                        Header_Factura_B();
                        break;
                    case 9:
                        Header_No_Fiscal();
                        break;
                    case 10:
                        Tipo_Habilitacion('A');
                        break;
                    case 11:
                        Tipo_Habilitacion('L');
                        break;
                    case 12:
                        Tipo_Habilitacion('M');
                        break;
                    case 13:
                        FP_Cliente_No_Categorizado();
                        break;
                    case 14:
                        Medios_De_Pago(4);
                        break;
                    case 15:
                        Medios_De_Pago(5);
                        break;
                    case 16:
                        Medios_De_Pago(6);
                        break;
                    case 17:
                        Cierre_Z();
                        break;
                    case 18:
                        Cancelar();
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
        URL api;
        //URL webService;
        try {
            String sdkAppId = getResources().getString(R.string.sdkAppId);    //si da error al compilar dejar variable sdkAppId= ""
            api = new URL(" http://34.212.218.149:8080/api/");
            FiscalManager result = FiscalManager.getInstance();
            if (rbFirst.isChecked()) {
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).firstGen(FirstGenerationPrinterModel.P441_201).build());
            } else if (rbSecond.isChecked()) {
                String ip = txtIp.getText().toString();
                SecondGenerationLocation loc = null;
                if (ip.isEmpty()) {
                    loc = SecondGenerationLocation.USB;
                } else {
                    endpoint = new URL(ip);
                    loc = new SecondGenerationLocation(endpoint);
                }
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).secondGen(loc).build());
            } else if (rbElectronic.isChecked()) {
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).electronicInvoice(api, "admin","admin", "Prueba").build());
            }

        } catch (MalformedURLException e) {
            txtIp.setError("Invalid URL");
            Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_LONG).show();
        }

    }

    //To fix a known issue in 1rst gen
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


    private void FE_Factura_B() {      //    testElectronicInvoice01
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newNinguno(null)));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 35.70).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite 2250", "106", 69.80).quantity(1).iva(ivaRegistry.get("Gravado21")).internalTax(internalTaxesFactory.newFixedTax(2.59)));

        ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
        //Finally, send the invoice to the fiscal printer.
        FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
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
                    }

                    @Override
                    public void onError(FiscalDriverException e) {
                        super.onError(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );

    }


    /*private void FE_ACK() { //CREO UNA FE PARA QUE ME DEVUELVA EL ULTIMO NUMERO DE TRANSACCION Y PASARSELO AL ACK
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

                ElectronicInvoiceACKBean beanACK = electronicInvoiceFactory.newElectronicInvoiceACK("1", 1, lastTransactionNumber, "30618829150");
                FiscalManager.getInstance().electronicInvoiceACK(beanACK, new ServiceCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean response) {
                        Toast.makeText(getApplicationContext(), response.toString().toUpperCase(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FiscalDriverException ex) {
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
    }*/


    private void FP_Factura_B() {      //  testElectronicInvoice02
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA_AND",
                        "CalleSiempreVivas 666",
                        documentFactory.newDNI("34849766")));

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 45.50).quantity(1).iva(ivaRegistry.get("Gravado0")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "104", 130.00).quantity(1).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite lata", "105", 35.70).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Sprite 2250", "106", 69.80).quantity(1).iva(ivaRegistry.get("Gravado21")).internalTax(internalTaxesFactory.newFixedTax(2.59)));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "**********PRUEBA_AND***********", Toast.LENGTH_LONG).show();
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
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_C);
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


    private void Percepcion_Factura_A() {       //testElectronicInvoice05
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

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Leche", "103", 32.60000029).quantity(5).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Te green hills", "104", 127.00000038).quantity(12).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Hilerete", "105", 78.9041).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("mamamamaTaragui", "119", 144.8975).quantity(1).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Cebolla", "106", 33.900295).quantity(1.590).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Durazno", "107", 59.89984).quantity(2.360).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Frutilla", "108", 129.999935).quantity(1.378).iva(ivaRegistry.get("Gravado10.5")));


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
                ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);


                //Finally, send the invoice to the fiscal printer.
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
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
                ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
                //Finally, send the invoice to the fiscal printer.
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
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


   /* private void Percepcion_Factura_A_03() {     //testElectronicInvoice07
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
                ElectronicInvoiceBean electronicInvoiceBean = electronicInvoiceFactory.newElectronicInvoice(bean);
                //Finally, send the invoice to the fiscal printer.
                FiscalManager.getInstance().electronicInvoice(electronicInvoiceBean, new ToastOnExceptionServiceCallback<ElectronicInvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(ElectronicInvoiceResponse response) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("CAE: " + response.getCae());
                        builder.append('\n');
                        builder.append("T.Number: " + response.getTransactionNumber());
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
                });
            }


        });
    }*/


    private void FP_FACTURA_A() {
        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.FACTURA_A);

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


    /*private void FP_Nota_Credito_M() {      //   Test2
        Text text = new Text("X01951505");
        zoneConfigurator.cleanAll();
        zoneConfigurator.configureTailOneZone(1, text, StationModes.ESTACION_POR_DEFECTO);

        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_NOTA_CREDITO_M);

        AssociatedDocumentBean associatedDocumentBean = new AssociatedDocumentBean();
        associatedDocumentBean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_M);
        associatedDocumentBean.setPosNumber("123");
        associatedDocumentBean.setReceiptNumber("1234567");

        //bean.setAssociatedDocument(associatedDocumentBean);

        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PALACIOS, MARIA ELENA",
                        "RAMIREZ DE VELAZCO Y CORUÑA    - 5300 - LA RIOJA",
                        documentFactory.newDNI("10028884")));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1161390-2 TANINA", 374.25).quantity(2));

        bean.setZones(zoneConfigurator.getZones());
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(InvoiceResponse response) {
                FiscalManager.getInstance().closeInvoice(new CloseInvoiceBean(), null);
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }*/

    /*private void Percepcion_Factura_A_04() {     //    Test7PrintPerception
        TributesModeMapper tMapper = new TributesModeMapper();
        List<InscripcionIIBB> InscripcionIIBBList = new ArrayList<InscripcionIIBB>();

        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "902"));
        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "905"));
        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "911"));
        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "913"));
        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "915"));
        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "920"));
        InscripcionIIBBList.add(inscripcionIIBBFactory.newInscripcionIIBB(0, "921"));

        List<TaxException> TaxExceptionList = new ArrayList<TaxException>();

        TaxExceptionList.add(taxExceptionFactory.newTaxException("16", 80, Calendar.getInstance().getTime(), 0));
        TaxExceptionList.add(taxExceptionFactory.newTaxException("555", 60, Calendar.getInstance().getTime(), 0));

        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "ENRIQUEZ, MARIA ALEJANDRA",
                        "av. Jose Jesus Oyola 223   - 5300 - LA RIOJA",
                        documentFactory.newCUIT("99999999995"), TaxExceptionList, InscripcionIIBBList, true, false, true));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779394044800 - LECHE ENT. SACHET", "100", 19.00).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779394044800 - LECHE ENT. SACHET", "101", 19.00).iva(ivaRegistry.get("Gravado21")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("125 - ATUN L.BAYAS .x170        ", "102", 37.29).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("44 - NUEZ C/CASCARA             ", "103", 16.07).quantity(2).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("103 - PIONONO PRIMAVERA         ", "104", 25.57).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("227 - LECHUGA MORADA        ", "105", 20.98).quantity(0.500).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("125 - ATUN L.BAYAS .x170        ", "106", 35.87).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779815870774 - GUANTES BEST     ", "107", 24.80));

        Discounts discounts = discountsFactory.newDiscount(5, "Mini discount");

        bean.getDiscounts().add(discounts);

        FiscalManager.getInstance().perceptions(bean, new ToastOnExceptionServiceCallback<PerceptionResponse>(getApplicationContext()) {
            @Override
            public void onResult(PerceptionResponse perceptionResponse) {
                List<Perception> perceptionList;
                perceptionList = perceptionResponse.getPerceptionList();
                sleep();
                ArrayList<Tributes> tributesToPrint = new ArrayList<>();
                for (Perception perception : perceptionList) {

                    tributesToPrint.add(tributeFactory.newTribute(tMapper.Map(perception.getDescripcionAbreviaturaField()), perception.getDescripcionAbreviaturaField(), perception.getMontoBaseCalculoField(), perception.getMontoPercepcionField(), perception.getAlicuotaField()));
                } // TODO VER LOS TIPOS DE PERCEPTIONES PARA PODER IMPRIMIRLAS

                bean.setTributes(tributesToPrint);

                FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                            @Override
                            public void onResult(InvoiceResponse response) {


                                Toast.makeText(getApplicationContext(), "Test 4 finished.", Toast.LENGTH_LONG).show();
                                sleep();
                                executePayment(300);
                            }
                        }
                );

            }

        });

    }*/


    /*private void FE_Query() {     //    testElectronicInvoiceQuery
        if (lastTransactionNumber.isEmpty()) {
            Toast.makeText(getApplicationContext(), "You must create a electronic invoice", Toast.LENGTH_SHORT).show();
            return;
        }
        ElectronicInvoiceQueryBean query = electronicInvoiceFactory.newQuery(lastTransactionNumber);
        //Or
        // ElectronicInvoiceQueryBean query = electronicInvoiceFactory.newQuery(aElectronicInvoiceResponse);
        //Or
        // ElectronicInvoiceQueryBean query = electronicInvoiceFactory.newQuery(aElectronicInvoiceBean);
        FiscalManager.getInstance().electronicInvoiceQuery(query, new ToastOnExceptionServiceCallback<ElectronicInvoiceQueryResponse>(getApplicationContext()) {
            @Override
            public void onResult(ElectronicInvoiceQueryResponse response) {
                Toast.makeText(getApplicationContext(), "Last transaction list size: " + response.getElectronicInvoices().size(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

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

    /*private void FE_Register_Company() {
        ElectronicInvoicerRegisterCompanyBean company = electronicInvoiceFactory.newCompany("30522211563", "AND",
                new Subsidiary("sucursal_prueba", "32"),
                new PointOfSales(true, 12, "CAE"),
                new Checkout("123ABC", new PointOfSales(true, 12, "CAE"), 32, null),
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

    }*/

    private void Header_No_Fiscal() {   //  testLinesZone_Ticket_No_Fiscal
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(TIQUE);
        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PRUEBA",
                        "CalleSiempreViva 666",
                        documentFactory.newNinguno("34859766")));

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


}