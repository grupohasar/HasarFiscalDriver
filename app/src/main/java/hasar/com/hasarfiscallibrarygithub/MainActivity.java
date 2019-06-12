package hasar.com.hasarfiscallibrarygithub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hasar.fiscal.dataLayer.beans.AssociatedDocumentBean;
import com.hasar.fiscal.dataLayer.beans.Discounts;
import com.hasar.fiscal.dataLayer.beans.FiscalPayment;
import com.hasar.fiscal.dataLayer.beans.FiscalText;
import com.hasar.fiscal.dataLayer.beans.Text;
import com.hasar.fiscal.dataLayer.beans.configuration.ConfigureFiscalPrinterBean;
import com.hasar.fiscal.dataLayer.beans.operation.CloseInvoiceBean;
import com.hasar.fiscal.dataLayer.beans.operation.GenericText;
import com.hasar.fiscal.dataLayer.beans.operation.GenericTextBean;
import com.hasar.fiscal.dataLayer.beans.operation.InvoiceBean;
import com.hasar.fiscal.dataLayer.beans.response.CloseFiscalDayZResponse;
import com.hasar.fiscal.dataLayer.beans.response.CloseInvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.GenericTextResponse;
import com.hasar.fiscal.dataLayer.beans.response.InvoiceResponse;
import com.hasar.fiscal.dataLayer.beans.response.StateQueryResponse;
import com.hasar.fiscal.dataLayer.enums.FiscalState;
import com.hasar.fiscal.dataLayer.enums.InvoiceTypes;
import com.hasar.fiscal.dataLayer.enums.Jurisdictions;
import com.hasar.fiscal.dataLayer.enums.PaymentTypes;
import com.hasar.fiscal.dataLayer.enums.PrinterState;
import com.hasar.fiscal.dataLayer.enums.StationModes;
import com.hasar.fiscal.dataLayer.enums.TaxConditions;
import com.hasar.fiscal.dataLayer.factories.ClientFactory;
import com.hasar.fiscal.dataLayer.factories.DiscountsFactory;
import com.hasar.fiscal.dataLayer.factories.DocumentFactory;
import com.hasar.fiscal.dataLayer.factories.FiscalItemFactory;
import com.hasar.fiscal.dataLayer.factories.FiscalPaymentFactory;
import com.hasar.fiscal.dataLayer.factories.IVARegistry;
import com.hasar.fiscal.dataLayer.factories.InternalTaxesFactory;
import com.hasar.fiscal.dataLayer.factories.ZoneConfigurator;
import com.hasar.fiscal.fiscalManager.FirstGenerationPrinterModel;
import com.hasar.fiscal.fiscalManager.FiscalManager;
import com.hasar.fiscal.fiscalManager.FiscalManagerConfigurationBuilder;
import com.hasar.fiscal.fiscalManager.SecondGenerationLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SelectableViewHolder.OnItemSelectedListener {
    private RecyclerView recyclerView;
    private SelectableAdapter adapter;
    private ZoneConfigurator zoneConfigurator = ZoneConfigurator.getInstance();
    private IVARegistry ivaRegistry;
    private FiscalItemFactory fiscalItemFactory = new FiscalItemFactory();
    private InternalTaxesFactory internalTaxesFactory = new InternalTaxesFactory();
    private ClientFactory clientFactory = new ClientFactory();
    private DocumentFactory documentFactory = new DocumentFactory();
    private FiscalPaymentFactory fiscalPaymentFactory = new FiscalPaymentFactory();
    private DiscountsFactory discountsFactory = new DiscountsFactory("Discount: ");

    private EditText txtIp;
    private Button btnSend;
    private RadioButton rbFirst;
    private RadioButton rbSecond;
    private RadioButton rbElectronic;

    private InvoiceBean facturaDesconectar;
    private CloseInvoiceBean closeInvoiceDesconectar;
    private String estadoDeImpresion = "LIBRE";
    private boolean initted = true;
    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView tv = findViewById(R.id.txtEmail);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "hsventas@hasar.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Solicitud de SDK");
                    intent.putExtra(Intent.EXTRA_BCC, new String[]{"investigacionydesarrollo@hasar.com"});
                    intent.putExtra(Intent.EXTRA_TEXT, "Buen día, pertenezco a la empresa XXXX, quisiera solicitar información sobre SDK Hasar Fiscal Printer.");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Sorry...You don't have any mail app", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        requestPermission();
        generateIVAs();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView = this.findViewById(R.id.selection_list);
        recyclerView.setLayoutManager(layoutManager);
        List<Item> selectableItems = generateItems();
        adapter = new SelectableAdapter(this, selectableItems, false);
        recyclerView.setAdapter(adapter);


        txtIp = findViewById(R.id.txtIp);
        rbFirst = findViewById(R.id.rbFirstGen);
        rbSecond = findViewById(R.id.rbSecondGen);
        rbElectronic = findViewById(R.id.rbElectronic);
        RadioGroup rbGroup = findViewById(R.id.llPrinterGen);
        rbGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                txtIp.setEnabled(rbSecond.isChecked());
                btnSend.setEnabled(!rbElectronic.isChecked());
                recyclerView.setEnabled(!rbElectronic.isChecked());
            }
        });

        btnSend = findViewById(R.id.buttonSend);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
                initFiscalManager();
                if (initted) {
                    switch (adapter.getPositionItem()) {
                        case 0:
                            Test0();
                            break;
                        case 1:
                            Test1();
                            break;
                        case 2:
                            Test2();
                            break;
                        case 3:
                            Test3();
                            break;
                        case 4:
                            Test4();
                            break;
                        case 5:
                            Test5();
                            break;
                        case 6:
                            Test6();
                            break;
                        case 7:
                            Test7();
                            break;
                        case 8:
                            Test8();
                            break;
                        case 9:
                            Test9();
                            break;
                        case 10:
                            Test10();
                            break;
                        case 11:
                            executeCancel();
                            break;
                        case 12:
                            executeZClose();
                            break;
                        case 13:
                            testElectronicInvoice01();
                            break;
                        case 14:
                            testElectronicInvoiceQuery();
                            break;
                        case 15:
                            testAfipIsAlive();
                            break;
                        case 16:
                            zByDate();
                            break;
                        case 17:
                            zByNumber();
                            break;
                        case 18:
                            reprint();
                            break;
                        case 19:
                            presupuesto();
                            break;
                        case 20:
                            limites();
                            break;
                        case 21:
                            consultaEstado();
                            break;

                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_contact:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "hsventas@hasar.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Solicitud de SDK");
                intent.putExtra(Intent.EXTRA_BCC, new String[]{"investigacionydesarrollo@hasar.com"});
                intent.putExtra(Intent.EXTRA_TEXT, "Buen día, pertenezco a la empresa XXXX, quisiera solicitar información sobre SDK Hasar Fiscal Printer.");
                startActivity(intent);
                return true;
            case R.id.action_github:
                Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/grupohasar/HasarFiscalDriver"));
                startActivity(browse);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public List<Item> generateItems() {

        String[] lista = getResources().getStringArray(R.array.command_array);
        List<Item> selectableItems = new ArrayList<>();
        for (String s : lista) {
            //Do your stuff here
            selectableItems.add(new Item(s));
        }

        return selectableItems;
    }

    private void generateIVAs() {
        ivaRegistry = IVARegistry.getInstance();
        try {
            ivaRegistry.register("Gravado21", 21, TaxConditions.GRAVADO);
            ivaRegistry.register("Gravado10.5", 10.5, TaxConditions.GRAVADO);
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
            api = new URL("http:///");
            FiscalManager result = FiscalManager.getInstance();
            if (rbFirst.isChecked()) {
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).jurisdiction(Jurisdictions.BUENOSAIRES).companyName("DEMO")
                        .firstGen(FirstGenerationPrinterModel.P441_201).build());
            } else if (rbSecond.isChecked()) {
                String ip = txtIp.getText().toString();
                SecondGenerationLocation loc = null;
                if (ip.isEmpty()) {
                    loc = SecondGenerationLocation.USB;
                } else {
                    if (ip.equals("http://")) {
                        throw new MalformedURLException();
                    }
                    endpoint = new URL(ip);
                    loc = new SecondGenerationLocation(endpoint);
                }
                result.setup(FiscalManagerConfigurationBuilder.configure(getApplicationContext()).jurisdiction(Jurisdictions.BUENOSAIRES).companyName("DEMO")
                        .secondGen(loc).build());
            } else if (rbElectronic.isChecked()) {


            }

        } catch (MalformedURLException e) {
            txtIp.setError("Invalid URL");
            Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_LONG).show();
            initted = false;
        }

    }

    private void enviarFacturaConPagoParaDesconectar() {
        initFiscalManager();
        facturaDesconectar = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        facturaDesconectar.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);

        facturaDesconectar.setClient(
                clientFactory.newConsumidorFinal(
                        "DIAZ, MARIA ROSA",
                        "av. Jose Jesus Oyola 223 - 5300 - LA RIOJA",
                        documentFactory.newDNI("31712509")));

        //Define a item to print in the invoice.,
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Coca", 100).quantity(2));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Empanadas carne", 20).quantity(12));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Coca", 100).quantity(2));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Empanadas carne", 20).quantity(12));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Coca", 100).quantity(2));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Empanadas carne", 20).quantity(12));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Coca", 100).quantity(2));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Empanadas carne", 20).quantity(12));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Coca", 100).quantity(2));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Empanadas carne", 20).quantity(12));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Coca", 100).quantity(2));
        facturaDesconectar.getFiscalItems().add(fiscalItemFactory.newFiscalItem("Empanadas carne", 20).quantity(12));

        closeInvoiceDesconectar = new CloseInvoiceBean();
        //Define a payment method to the invoice.
        FiscalPayment payment = fiscalPaymentFactory.newCashPayment(5000, "Efectivo");

        //Add the payment to the invoice.
        closeInvoiceDesconectar.getFiscalPayments().add(payment);

        estadoDeImpresion = "FACTURANDO";
        FiscalManager.getInstance().invoice(facturaDesconectar, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(InvoiceResponse invoiceResponse) {
                estadoDeImpresion = "PAGANDO";
                FiscalManager.getInstance().closeInvoice(closeInvoiceDesconectar, new ToastOnExceptionServiceCallback<CloseInvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(CloseInvoiceResponse closeInvoiceResponse) {
                        estadoDeImpresion = "LIBRE";
                    }
                });
            }
        });


    }

    private void reintentar() {


        if (estadoDeImpresion == "LIBRE") {
            Toast.makeText(getApplicationContext(), "No hay nada que reintentar", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
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
                        });
                        return;
                    } else {
                        FiscalManager.getInstance().cancel(new ToastOnExceptionServiceCallback<Void>(getApplicationContext()) {
                            @Override
                            public void onResult(Void aVoid) {
                                //estadoDeImpresion = "LIBRE";
                                Log.d("Estado", estadoDeImpresion);

                            }
                        });
                    }

                } else {
                    if (estadoDeImpresion.equals("PAGANDO")) {
                        Toast.makeText(getApplicationContext(), "El comprobante ya se imprimio", Toast.LENGTH_LONG).show();
                        estadoDeImpresion = "LIBRE";
                        Log.d("Estado", estadoDeImpresion);
                        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
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
                        });
                    }
                });
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
                Toast.makeText(getApplicationContext(), "Pago Finalizado.", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Pago finalizado.", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Pago Finalizado", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Pago Finalizado.", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void executeCancel() {
        FiscalManager.getInstance().cancel(null);
    }

    private void Test0() {
        Text text = new Text("X01951393");

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureTailOneZone(1, text, StationModes.ESTACION_POR_DEFECTO);

        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);

        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "DIAZ, MARIA ROSA",
                        "av. Jose Jesus Oyola 223 - 5300 - LA RIOJA",
                        documentFactory.newDNI("31712509")));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1118062-2 CORPO", 134.25).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1138517-5 SANDY", 149.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1010680-/ ESSENZA", "125", 149.25));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1152375-4 PAULO", "126", 337.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1141530-/ RELAX", "127", 37.46));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1160790-2 VENICE", "128", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1156790-6 MAYLEN", "129", 179.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("10810899-3 GAETANA", "130", 205.87));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159860-3 JERO", "131", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159470-2 TERESA", "132", 637.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1160240-2 JENIFER", "133", 261.75));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1158620-37 ALIX", "134", 179.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1161290-2 JONHATAN", "135", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1157550-1 BALI", "136", 289.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1116683-/ TREO", "137", 111.75).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1081081-3 GAETANA", "138", 205.87));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1160730-3 LOURDES", "139", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1155380-4 CARLOS", "140", 329.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159521-2 STELA", "141", 229.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1155380-2 CARLOS", "142", 329.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1141531-/ RELAX", "143", 37.46));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159170-4 AMALIA", "144", 291.75));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1158792-/ MAYRA", "145", 74.92).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1155340-4 JORGE", "146", 350.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159930-2 EVANGELINA", "147", 261.75));

        bean.setZones(zoneConfigurator.getZones());

        //Finally, send the invoice to the fiscal printer.
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(InvoiceResponse response) {
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
                sleep();
                executePayment(10000);
            }
        });
    }


    private void Test1() {
        Text text = new Text("X01951393");

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureTailOneZone(1, text, StationModes.ESTACION_POR_DEFECTO);

        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);

        bean.setClient(
                clientFactory.newConsumidorFinal(
                        "PALACIOS, MARIA ELENA",
                        "RAMIREZ DE VELAZCO Y CORUÑA    - 5300 - LA RIOJA",
                        documentFactory.newDNI("10028884")));
        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(
                fiscalItemFactory.newFiscalItem("1118062-2 CORPO", "123", 134.25)
                        .quantity(2)
                        .iva(ivaRegistry.get("Gravado10.5"))
                        .internalCode("123123123")
                        .discount(discountsFactory.newDiscount(10))
                        .internalTax(internalTaxesFactory.newPercentualTax(10))
        );

        FiscalText text1 = new FiscalText("Fiscal text in item").bold();

        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1138517-5 SANDY", "124", 149.00)
                .addFiscalText(text1));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1010680-/ ESSENZA", 0));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1152375-4 PAULO", "126", 337.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1141530-/ RELAX", "127", 37.46));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1160790-2 VENICE", "128", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1156790-6 MAYLEN", "129", 179.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("10810899-3 GAETANA", "130", 205.87));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159860-3 JERO", "131", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159470-2 TERESA", "132", 637.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1160240-2 JENIFER", "133", 261.75));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1158620-37 ALIX", "134", 179.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1161290-2 JONHATAN", "135", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1157550-1 BALI", "136", 289.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1116683-/ TREO", "137", 111.75).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1081081-3 GAETANA", "138", 205.87));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1160730-3 LOURDES", "139", 187.12));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1155380-4 CARLOS", "140", 329.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159521-2 STELA", "141", 229.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1155380-2 CARLOS", "142", 329.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1141531-/ RELAX", "143", 37.46));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159170-4 AMALIA", "144", 291.75));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1158792-/ MAYRA", "145", 74.92).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1155340-4 JORGE", "146", 350.00)
                .internalTax(internalTaxesFactory.newFixedTax(20)));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159930-2 EVANGELINA", "147", 261.75));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1159930-2 EVANGELINA", "147", 261.75).substract());

        Discounts d = discountsFactory.newDiscount(467.97);

        bean.getDiscounts().add(d);
        bean.setZones(zoneConfigurator.getZones());
        //Finally, send the invoice to the fiscal printer.
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(InvoiceResponse response) {
                executePaymentCash(1000000);
                FiscalManager.getInstance().closeInvoice(new CloseInvoiceBean(), null);
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Test2() {
        Text text = new Text("X01951505");

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureTailOneZone(1, text, StationModes.ESTACION_POR_DEFECTO);

        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_NOTA_CREDITO_B);

        AssociatedDocumentBean associatedDocumentBean = new AssociatedDocumentBean();
        associatedDocumentBean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_B);
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
    }

    private void Test3() {
        Text text = new Text("La empresa de fantasia");

        zoneConfigurator.cleanAll();
        zoneConfigurator.configureHeaderOneZone(1, text, StationModes.ESTACION_POR_DEFECTO);

        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "ENRIQUEZ, MARIA ALEJANDRA",
                        "av. Jose Jesus Oyola 223   - 5300 - LA RIOJA",
                        documentFactory.newCUIT("99999999995"), null, null, false, false, false));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("11413213-/ MARCIA", "123", 48.37));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1074281-/ ADELA", "121", 48.37));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("10742815-/ ADELA", "122", 48.37));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1158282-/ MAIA", "124", 55.87).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("10742824-/ ADELA", "125", 48.37));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1158283-/ MAIA", "126", 55.87).quantity(2));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1141322-/ MARCIA", "127", 48.37));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("10742824-/ ADELA", "128", 48.37));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1145240-/ CORPO", "129", 119.25));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("1145244-/ CORPO", "100", 119.25));

        Discounts discounts = discountsFactory.newDiscount(60.18, "Discount for good customer");
        bean.getDiscounts().add(discounts);

        bean.setZones(zoneConfigurator.getZones());
        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(InvoiceResponse response) {
                executePayment(1000000);
                FiscalManager.getInstance().closeInvoice(new CloseInvoiceBean(), null);
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Test4() {
        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_NOTA_DEBITO_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "ENRIQUEZ, MARIA ALEJANDRA",
                        "av. Jose Jesus Oyola 223   - 5300 - LA RIOJA",
                        documentFactory.newCUIT("99999999995"), null, null, false, false, false));


        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779394044800 - LECHE ENT. SACHET", "100", 19.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779394044800 - LECHE ENT. SACHET", "101", 19.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("125 - ATUN L.BAYAS .x170        ", "102", 37.29).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("44 - NUEZ C/CASCARA             ", "103", 16.07).quantity(2).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("103 - PIONONO PRIMAVERA         ", "104", 25.57).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("227 - LECHUGA MORADA        ", "105", 20.98).quantity(0.500).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("125 - ATUN L.BAYAS .x170        ", "106", 35.87).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779815870774 - GUANTES BEST     ", "107", 24.80));

        Discounts discounts = discountsFactory.newDiscount(5, "Mini discount");

        bean.getDiscounts().add(discounts);

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        executePayment(1000000);
                        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();


                    }
                }
        );
    }

    private void Test5() {
        GenericTextBean bean = new GenericTextBean();
        GenericText genericText = new GenericText("Mastercard").centered().doubleWidth().bold();
        GenericText genericText2 = new GenericText("Comercio: 234432 Terminal: 441");
        GenericText genericText3 = new GenericText("Tarjeta: XXXXXXXXXXXX3745");
        GenericText genericText4 = new GenericText("Cuotas 1");
        GenericText genericText5 = new GenericText("Monto: $ 188.87").bold();

        bean.getTexts().add(genericText);
        bean.getTexts().add(genericText2);
        bean.getTexts().add(genericText3);
        bean.getTexts().add(genericText4);
        bean.getTexts().add(genericText5);
        bean.setNumberOfCopies(2);

        FiscalManager.getInstance().printGenericText(bean, new ToastOnExceptionServiceCallback<GenericTextResponse>(getApplicationContext()) {
            @Override
            public void onResult(GenericTextResponse genericTextResponse) {
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void executeZClose() {
        FiscalManager.getInstance().closeFiscalDayZ(new ToastOnExceptionServiceCallback<CloseFiscalDayZResponse>(getApplicationContext()) {
            @Override
            public void onResult(CloseFiscalDayZResponse closeFiscalDayZResponse) {
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void Test6() {
        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "NAYA, LUIS ALBERTO",
                        "Mendoza 845 Chacabuco",
                        documentFactory.newCUIT("20138980643"), null, null, false, false, false));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439842 - COCKTAIL BEST X800", "100", 33.90));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779815870456 - CHAMPI.BEST X425GR", "101", 34.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779074214060 - DDL. COLONIALx400G", "102", 43.20));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779439600458 - CREMA DE LECHE POT", "103", 38.60));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("203603900000 - RABO              ", "104", 49.31).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("203603900000 - RABO              ", "105", 33.11).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779815870791 - ATUN EN LOMITOS LA", "106", 28.90));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779067005065 - PATY CLASICO X4   ", "107", 85.20));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779133712020 - CASANCREM CLAx300G", "109", 47.90));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("206459000000 - LENGUA            ", "108", 115.46).iva(ivaRegistry.get("Gravado10.5")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779815870214 - CHOCLO DESG.LAx340", "112", 23.20));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779815870792 - ATUN EN LOMITOS LA", "113", 28.90));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("762230086304 - CLIGHT MANZ DEL   ", "114", 5).quantity(18).internalTax(internalTaxesFactory.newFixedTax(2)));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("103 - BANANA ECUADOR             ", "115", 46.36).quantity(0.500).iva(ivaRegistry.get("Gravado10.5")));

        Discounts discounts = discountsFactory.newDiscount(21.58, "Mini discount");

        bean.getDiscounts().add(discounts);

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
                        sleep();
                        executePayment(617.78);
                    }
                }
        );
    }


    private void Test7() {
        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "CAPPELLO, PABLO FERNANDO",
                        "Sarmiento 6 CHACABUCO",
                        documentFactory.newCUIT("20214983681"), null, null, false, false, false));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("780681022100 - RALLADOR         ", "100", 105.00));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "101", 53.80));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439062 - PAN DE MESA GRANDE", "102", 53.80));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779071000010 - YERBA CBSE X500GR ", "103", 41.60));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439518 - SAL LA ANONIMA X1K", "104", 21.20));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779802439518 - SAL LA ANONIMA X1K", "105", 21.20));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779325300040 - LAVAND MAX.PUR.X1L", "106", 18.10));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779025005677 - ROLLO COC.SUSSEXx3", "107", 23.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779025005677 - ROLLO COC.SUSSEXx3", "1020", 23.50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779025005677 - ROLLO COC.SUSSEXx3", "1013", 23.50));


        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
                        //TODO: Known issue in 1st Generation printer
                        sleep();
                        executePaymentTest7(201.12, 200);

                    }
                }
        );
    }


    private void Test8() {
        //Instantiate a InvoiceBean, to define an Invoice
        InvoiceBean bean = new InvoiceBean();
        //Set the invoice type with the InvoiceTypes enumeration.
        bean.setInvoiceType(InvoiceTypes.TIQUE_FACTURA_A);

        bean.setClient(
                clientFactory.newResponsableInscripto(
                        "NAYA, LUIS ALBERTO",
                        "Mendoza 845 Chacabuco",
                        documentFactory.newCUIT("20138980643"), null, null, false, false, false));

        //Define a item to print in the invoice.,
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779074214060 - DDL. COLONIALx400G", "100", 50)
                .discount(discountsFactory.newDiscount(10, "descuento (resta)")));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779439600458 - CREMA DE LECHE POT", "101", 50));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779133712020 - CASANCREM CLAx300G", "102", 47.90));
        bean.getFiscalItems().add(fiscalItemFactory.newFiscalItem("779394044800 - LECHE ENT. SACHET ", "103", 19.00).quantity(2));

        Discounts recharge = discountsFactory.newDiscount(-16.77, "Recargos");

        bean.getDiscounts().add(recharge);

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
                    @Override
                    public void onResult(InvoiceResponse response) {
                        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
                        sleep();
                        executeCreditPaymentWithInstallments(184.47, 12);
                    }
                }
        );
    }


    private void Test9() {
        GenericTextBean bean = new GenericTextBean();
        GenericText genericText = new GenericText("AMERICAN EXPR").centered().doubleWidth().bold();
        GenericText genericText2 = new GenericText("Comercio: SS432 Terminal: 241");
        GenericText genericText3 = new GenericText("Tarjeta: XXXXXXXXXXXX5574");
        GenericText genericText4 = new GenericText("Cuotas 12");
        GenericText genericText5 = new GenericText("Monto: $ 184.47").bold();

        bean.getTexts().add(genericText);
        bean.getTexts().add(genericText2);
        bean.getTexts().add(genericText3);
        bean.getTexts().add(genericText4);
        bean.getTexts().add(genericText5);

        FiscalManager.getInstance().printGenericText(bean, new ToastOnExceptionServiceCallback<GenericTextResponse>(getApplicationContext()) {
            @Override
            public void onResult(GenericTextResponse genericTextResponse) {
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Test10() {
        GenericTextBean bean = new GenericTextBean();
        GenericText genericText = new GenericText("COMPROBANTE DE RETIRO").centered().bold();
        GenericText genericText2 = new GenericText("Cajero: 3465 - Juan Rodriguez");
        GenericText genericText3 = new GenericText("Efectivo   $  900");
        GenericText genericText4 = new GenericText("Visa       $ 1200");
        GenericText genericText5 = new GenericText("MasterCard $  300").bold();
        GenericText genericText6 = new GenericText("Total      $ 2400").bold();

        bean.getTexts().add(genericText);
        bean.getTexts().add(genericText2);
        bean.getTexts().add(genericText3);
        bean.getTexts().add(genericText4);
        bean.getTexts().add(genericText5);
        bean.getTexts().add(genericText6);

        FiscalManager.getInstance().printGenericText(bean, new ToastOnExceptionServiceCallback<GenericTextResponse>(getApplicationContext()) {
            @Override
            public void onResult(GenericTextResponse genericTextResponse) {
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void testElectronicInvoiceQuery() {
        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
    }

    private void testAfipIsAlive() {
        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
    }

    private void zByDate() {
        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
    }

    private void zByNumber() {
        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
    }

    private void reprint() {

        FiscalManager.getInstance().reprint(new ToastOnExceptionServiceCallback<Void>(getApplicationContext()) {
            @Override
            public void onResult(Void voidParam) {

                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void presupuesto() {
        InvoiceBean bean = new InvoiceBean();

        bean.setInvoiceType(InvoiceTypes.PRESUPUESTO_X);

        bean.getFiscalItems().add(
                fiscalItemFactory.newFiscalItem("Cocacola x12", "123", 250)
                        .iva(ivaRegistry.get("Gravado21")));

        FiscalManager.getInstance().invoice(bean, new ToastOnExceptionServiceCallback<InvoiceResponse>(getApplicationContext()) {
            @Override
            public void onResult(InvoiceResponse invoiceResponse) {
                FiscalManager.getInstance().closeInvoice(new CloseInvoiceBean(), null);
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void limites() {
        FiscalManager.getInstance().fiscalPrinterConfigurationQuery(new ToastOnExceptionServiceCallback<ConfigureFiscalPrinterBean>(getApplicationContext()) {
            @Override
            public void onResult(ConfigureFiscalPrinterBean result) {
                StringBuilder sb = new StringBuilder();
                sb.append("Los limites son: \n");
                sb.append("Si nominar: ").append(result.getNotNominatedLimit()).append("\n");
                sb.append("Nominado: ").append(result.getNominatedLimit()).append("\n");

                Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();

                result.setNotNominatedLimit(500);

                FiscalManager.getInstance().configureFiscalPrinter(result, new ToastOnExceptionServiceCallback<Void>(getApplicationContext()) {
                    @Override
                    public void onResult(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


    }

    private void consultaEstado() {
        FiscalManager.getInstance().stateQuery(new ToastOnExceptionServiceCallback<StateQueryResponse>(getApplicationContext()) {
            @Override
            public void onResult(StateQueryResponse stateQueryResponse) {
                StringBuilder sb = new StringBuilder();
                if (stateQueryResponse.getPrinterStates().contains(PrinterState.DOOR_OPENED))
                    sb.append("Door Opened");

                Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "**********DEMO***********", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void testElectronicInvoice01() {
        Toast.makeText(getApplicationContext(),
                "CONTACTESE CON NOSOTROS PARA OBTENER TODA LA FUNCIONALIDAD",
                Toast.LENGTH_LONG)
                .show();
    }


    public void install_apk() {

        if (isPackageExisted("com.hasar.fiscaldriver"))
            return;

        File directory = Environment.getExternalStoragePublicDirectory("HasarFiscalTest");

        File toInstall = new File(directory, "com.hasar.fiscaldriver-final" + ".apk");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Uri apkUri = FileProvider.getUriForFile(this, "com.hasar.fiscaldriver" + ".provider", toInstall);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(intent);
        } else {
            Uri apkUri = Uri.fromFile(toInstall);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }


    }


    public void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_WRITE_STORAGE);
            } else {
                install_apk();
            }
        }
    }

    //To fix a known issue in 1rst gen
    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {

        }
    }


    public String copyDirorfileFromAssetManager(String arg_assetDir, String arg_destinationDir) throws IOException {
        File sd_path = Environment.getExternalStorageDirectory();
        String dest_dir_path = sd_path + addLeadingSlash(arg_destinationDir);
        File dest_dir = new File(dest_dir_path);

        createDir(dest_dir);

        AssetManager asset_manager = getApplicationContext().getAssets();
        String[] files = asset_manager.list(arg_assetDir);

        for (int i = 0; i < files.length; i++) {


            if (files[i].contains("com.hasar.fiscaldriver-final.apk")) {

                // It is a file
                String dest_file_path = addTrailingSlash(dest_dir_path) + files[i];
                copyAssetFile(files[i], dest_file_path);
            }
        }

        return dest_dir_path;
    }


    public void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = getApplicationContext().getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    public String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        return path;
    }

    public String addLeadingSlash(String path) {
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    public void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory");
            }
        }
    }

    public boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        AssetManager assetManager = getAssets();
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        String[] filelist = assetManager.list("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        copyDirorfileFromAssetManager("", "HasarFiscalTest");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    install_apk();


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onItemSelected(SelectableItem selectableItem) {

    }
}
